import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import type { HorseRaceHorseKeyframes } from "../../models/HorseRace";
import { validateToastMessage } from "../../utils/SecurityUtils";
import { getPreset, getRoomById, syncReadiness, syncRoomState } from "../../store/slices/HorseRaceRoomSlice";
import { findByGuid } from "../../store/slices/UserSlice";
import { useWebSocket } from "../../hooks/useWebSocket";
import type { HorseRaceGameMessage } from "../../models/WsMessage";
import { Box, Button, Card, Container, Input, Toast, Typography } from "../../ui";
import HorseSprite from "../../assets/sprites/HorseSprite";

const RACE_DURATION_MS = 12_000;

const HORSE_SPRITE_SIZE = 88;
const HORSE_COLORS = [
    "#e74c3c",
    "#e67e22",
    "#2ecc71",
    "#3498db",
    "#9b59b6",
    "#f1c40f",
    "#e91e63",
    "#1abc9c",
];

type RacePhase = "LOBBY" | "WAITING" | "RACING" | "FINISHED";

interface PlacedBetInfo {
    horseIndex: number;
    amount: number;
}

function formatCountdown(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;

    return `${m}:${s.toString().padStart(2, "0")}`;
}

export default function HorseRaceRoom() {
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const balance = useSelector((state: RootState) => state.user.user?.balance);
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();

    const roomId = useParams<{ roomId?: string }>().roomId;

    const { room, readyPlayersCount, totalPlayersCount, preset } = useSelector(
        (state: RootState) => state.horseRaceRoom
    );

    const [toast, setToast] = useState<{ text: string } | null>(null);
    const showToast = useCallback((text: string) => {
        setToast({ text: validateToastMessage(text) });
    }, []);

    const [phase, setPhase] = useState<RacePhase>("LOBBY");
    const [ready, setReady] = useState(false);
    const [winnerIndex, setWinnerIndex] = useState<number | undefined>();

    const [selectedHorse, setSelectedHorse] = useState<number | null>(null);
    const [betInput, setBetInput] = useState<string>("");
    const [betPlaced, setBetPlaced] = useState<boolean>(false);
    const [placedBetInfo, setPlacedBetInfo] = useState<PlacedBetInfo | null>(null);
    const [hoveredHorse, setHoveredHorse] = useState<number | null>(null);

    const [secondsLeft, setSecondsLeft] = useState<number | null>(null);
    const countdownIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

    const horseElemsRef = useRef<(HTMLDivElement | null)[]>([]);
    const animationsRef = useRef<Animation[]>([]);
    const winnerRef = useRef<number>(0);

    useEffect(() => {
        if (!roomId || !guid) {
            navigate("/rooms");
            return;
        }

        dispatch(getRoomById({ roomId }));
        dispatch(findByGuid(guid));
    }, [dispatch, guid, navigate, roomId]);

    const { isConnected, message, send } = useWebSocket<HorseRaceGameMessage>(
        roomId,
        room?.type
    );

    useEffect(() => {
        if (!isConnected || !roomId || !room) {
            return;
        }

        dispatch(syncRoomState({ roomId, roomType: room.type }));
        dispatch(getPreset({ roomId }));
    }, [dispatch, isConnected, room, roomId]);

    const clearCountdown = useCallback(() => {
        if (countdownIntervalRef.current !== null) {
            clearInterval(countdownIntervalRef.current);
            countdownIntervalRef.current = null;
        }
    }, []);

    const startCountdown = useCallback((initialSeconds: number) => {
        clearCountdown();
        setSecondsLeft(initialSeconds);
        countdownIntervalRef.current = setInterval(() => {
            setSecondsLeft((prev) => {
                if (prev === null || prev <= 1) {
                    clearCountdown();
                    return 0;
                }
                return prev - 1;
            });
        }, 1000);
    }, [clearCountdown]);

    useEffect(() => {
        return () => clearCountdown();
    }, [clearCountdown]);

    const stopAnimation = useCallback(() => {
        animationsRef.current.forEach((anim) => {
            try {
                anim.cancel();
            } catch {
                /* already finished */
            }
        });
        animationsRef.current = [];
    }, []);

    const startAnimation = useCallback((
        horseKeyframes: HorseRaceHorseKeyframes[],
        winner: number
    ) => {
        stopAnimation();

        winnerRef.current = winner;

        horseElemsRef.current.forEach((el) => {
            if (el) el.style.transform = "translateY(-50%)";
        });

        requestAnimationFrame(() => {
            const animations: Animation[] = [];

            for (const horse of horseKeyframes) {
                const el = horseElemsRef.current[horse.horseIndex];
                if (!el) continue;

                const trackWidth = el.parentElement?.clientWidth ?? 0;
                const maxOffset = trackWidth - HORSE_SPRITE_SIZE;

                const wapiKeyframes: Keyframe[] = horse.keyframes.map((kf) => ({
                    transform: `translateX(${(kf.position / 100) * maxOffset}px) translateY(-50%)`,
                    offset: kf.offset,
                }));

                const animation = el.animate(wapiKeyframes, {
                    duration: RACE_DURATION_MS,
                    easing: "linear",
                    fill: "forwards",
                });

                animations.push(animation);
            }

            animationsRef.current = animations;

            if (animations.length > 0) {
                animations[0].finished.then(() => {
                    setWinnerIndex(winnerRef.current);
                    setPhase("FINISHED");
                    showToast(`🏆 Horse #${winnerRef.current + 1} wins!`);
                }).catch(() => {

                });
            }
        });
    }, [showToast, stopAnimation]);

    useEffect(() => {
        return () => stopAnimation();
    }, [stopAnimation]);

    useEffect(() => {
        if (!isConnected || !message || !roomId || !room) {
            return;
        }

        switch (message.event) {
            case "JOIN":
                showToast(message.message ?? "Player joined the room");
                dispatch(syncRoomState({ roomId, roomType: room.type }));
                break;

            case "LEAVE":
                showToast(message.message ?? "Player left the room");
                dispatch(syncRoomState({ roomId, roomType: room.type }));
                break;

            case "READY":
                showToast(message.message ?? "Player is ready");
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "START": {
                const { horseKeyframes, winnerHorseIndex } = message;

                if (!horseKeyframes || winnerHorseIndex === undefined) {
                    break;
                }

                clearCountdown();
                setPhase("RACING");
                startAnimation(horseKeyframes, winnerHorseIndex);
                break;
            }

            case "BET": {
                if (message.fromUserId !== guid) {
                    break;
                }

                const amount = message.bet;
                const horseIdx = message.horseIndex;

                if (amount !== undefined && horseIdx !== undefined) {
                    setPlacedBetInfo({ horseIndex: horseIdx, amount });
                }

                setBetPlaced(true);
                showToast(message.message ?? "Your bet has been accepted!");
                break;
            }

            case "BET_REJECT":
                setBetPlaced(false);
                setPlacedBetInfo(null);
                showToast(message.message ?? "Your bet was rejected. Please try again.");
                break;

            case "BET_REQUIRED":
                showToast(message.message ?? "You must place a bet before becoming ready.");
                break;

            case "COUNTDOWN": {
                const remaining = message.remainingSeconds;
                if (remaining !== undefined && remaining > 0) {
                    startCountdown(remaining);
                }
                break;
            }

            case "CANCELED": {
                clearCountdown();
                setSecondsLeft(null);
                showToast(message.message ?? "Race was canceled — no players in the room.");
                break;
            }

            default:
                break;
        }
    }, [dispatch, guid, isConnected, message, room, roomId, showToast, startAnimation, clearCountdown, startCountdown]);

    const handlePlaceBet = () => {
        if (!room || !isConnected || betPlaced || phase !== "LOBBY") {
            return;
        }

        if (selectedHorse === null) {
            showToast("Please select a horse first.");
            return;
        }

        const amount = parseFloat(betInput);

        if (isNaN(amount) || amount <= 0) {
            showToast("Please enter a valid bet amount.");
            return;
        }

        if (balance !== undefined && amount > balance) {
            showToast("Bet amount exceeds your balance.");
            return;
        }

        send({
            type: "USER_MESSAGE",
            event: "BET",
            roomId: room.id,
            horseIndex: selectedHorse,
            bet: amount,
        });
    };

    const handleReady = () => {
        if (!room || !isConnected || ready || phase !== "LOBBY") {
            return;
        }

        send({
            type: "USER_MESSAGE",
            event: "READY",
            roomId: room.id,
        });

        setReady(true);
        setPhase("WAITING");
    };

    const handleLeave = () => {
        stopAnimation();
        clearCountdown();
        navigate("/rooms");
    };

    const horseCount = preset?.horseCount ?? 0;
    const odds = preset?.odds ?? [];

    const betAmountParsed = parseFloat(betInput);
    const potentialWin = selectedHorse !== null
        && !isNaN(betAmountParsed)
        && betAmountParsed > 0
        && odds[selectedHorse] !== undefined
        ? betAmountParsed * odds[selectedHorse]
        : null;

    const isBetButtonDisabled = betPlaced || selectedHorse === null || !betInput || parseFloat(betInput) <= 0;

    if (!roomId || !room) {
        return (
            <Container>
                <Card style={{ textAlign: "center", padding: "2rem" }}>
                    <Typography variant="h2">Invalid Room</Typography>
                    <Button onClick={() => navigate("/rooms")} style={{ marginTop: "1rem" }}>
                        Back to Rooms
                    </Button>
                </Card>
            </Container>
        );
    }

    return (
        <Box
            style={{
                minHeight: "calc(100vh - 60px - 50px)",
                margin: "0 10rem",
                padding: "0 1rem",
                background: "var(--color-bg-glass)",
                backdropFilter: "blur(2px)",
                borderRadius: "var(--radius-md)",
                boxShadow: "var(--shadow-lg)",
            }}
        >
            <Container>
                <Box style={{ padding: "2rem 0" }}>
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        Horse Race: {room.name}
                    </Typography>
                </Box>

                <Card style={{ padding: "1.5rem", display: "flex", flexDirection: "column", gap: "1.5rem" }}>

                    <Typography
                        variant="caption"
                        style={{ textAlign: "center", color: "var(--color-text-secondary)" }}
                    >
                        {readyPlayersCount ?? 0} / {totalPlayersCount ?? 0} players ready
                    </Typography>

                    <Box style={{ display: "flex", gap: "1.5rem", alignItems: "flex-start", minHeight: "280px" }}>

                        <Box style={{ flex: 1, display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                            {secondsLeft !== null && (
                                <Box
                                    style={{
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        padding: "0.5rem 1rem",
                                        borderRadius: "var(--radius-sm)",
                                        background: secondsLeft > 0 && secondsLeft <= 10
                                            ? "rgba(231,76,60,0.12)"
                                            : "rgba(255,255,255,0.04)",
                                        border: `1px solid ${secondsLeft > 0 && secondsLeft <= 10
                                            ? "rgba(231,76,60,0.4)"
                                            : "var(--color-border)"}`,
                                    }}
                                >
                                    <Typography
                                        variant="caption"
                                        style={{
                                            fontWeight: 700,
                                            fontSize: "0.9rem",
                                            color: secondsLeft <= 10
                                                ? "var(--color-error, #e74c3c)"
                                                : "var(--color-text-secondary)",
                                            letterSpacing: "0.04em",
                                        }}
                                    >
                                        🏁 Race starts in {formatCountdown(secondsLeft)}
                                    </Typography>
                                </Box>
                            )}

                            <Card
                                style={{
                                    flex: 1,
                                    padding: "1.25rem",
                                    background: "var(--color-bg-secondary)",
                                    minHeight: "260px",
                                }}
                            >
                                <div
                                    style={{
                                        display: "flex",
                                        flexDirection: "column",
                                        gap: "5rem",
                                        paddingLeft: "24px",
                                        position: "relative",
                                    }}
                                >
                                    {horseCount === 0 ? (
                                        <Typography variant="caption" style={{ color: "var(--color-text-secondary)" }}>
                                            Loading race...
                                        </Typography>
                                    ) : (
                                        Array.from({ length: horseCount }, (_, i) => {
                                            const color = HORSE_COLORS[i % HORSE_COLORS.length];
                                            const isWinner = phase === "FINISHED" && winnerIndex === i;

                                            return (
                                                <Box
                                                    key={i}
                                                    style={{
                                                        position: "relative",
                                                        height: `${HORSE_SPRITE_SIZE}`,
                                                        display: "flex",
                                                        alignItems: "center",
                                                        overflow: "visible",
                                                    }}
                                                >
                                                    <span style={{
                                                        position: "absolute",
                                                        left: -24,
                                                        width: 20,
                                                        textAlign: "right",
                                                        fontSize: "16px",
                                                        fontWeight: 700,
                                                        color: color,
                                                        opacity: 0.85,
                                                        userSelect: "none",
                                                        lineHeight: `${HORSE_SPRITE_SIZE}px`,
                                                    }}>
                                                        #{i + 1}
                                                    </span>
                                                    <Box
                                                        style={{
                                                            position: "absolute",
                                                            left: 0,
                                                            right: 0,
                                                            height: "2px",
                                                            background: "var(--color-border)",
                                                            borderRadius: "1px",
                                                        }}
                                                    />

                                                    <div
                                                        ref={(el) => { horseElemsRef.current[i] = el; }}
                                                        style={{
                                                            position: "absolute",
                                                            top: "50%",
                                                            left: 0,
                                                            transform: "translateY(-50%)",
                                                            zIndex: 1,
                                                            willChange: "transform",
                                                        }}
                                                    >
                                                        <HorseSprite
                                                            color={color}
                                                            size={HORSE_SPRITE_SIZE}
                                                            isRunning={phase === "RACING"}
                                                            isWinner={isWinner}
                                                        />
                                                    </div>
                                                </Box>
                                            );
                                        })
                                    )}
                                </div>
                            </Card>
                        </Box>

                        <Card
                            style={{
                                width: "220px",
                                flexShrink: 0,
                                padding: "1rem",
                                display: "flex",
                                flexDirection: "column",
                                gap: "0.5rem",
                                minHeight: "260px",
                            }}
                        >
                            <Typography variant="h3" style={{ fontSize: "1rem", fontWeight: 700 }}>
                                Place a Bet
                            </Typography>

                            {odds.length === 0 ? (
                                <Typography variant="caption" style={{ color: "var(--color-text-secondary)" }}>
                                    —
                                </Typography>
                            ) : (
                                odds.map((odd, i) => {
                                    const color = HORSE_COLORS[i % HORSE_COLORS.length];
                                    const isWinner = phase === "FINISHED" && winnerIndex === i;
                                    const isSelected = selectedHorse === i;
                                    const isMyBet = betPlaced && placedBetInfo?.horseIndex === i;
                                    const isActive = isSelected || isMyBet;
                                    const isClickable = !betPlaced && phase === "LOBBY";
                                    const isHovered = hoveredHorse === i && isClickable && !isActive;

                                    return (
                                        <Box
                                            key={i}
                                            onClick={() => { if (isClickable) setSelectedHorse(i); }}
                                            onMouseEnter={() => { if (isClickable) setHoveredHorse(i); }}
                                            onMouseLeave={() => setHoveredHorse(null)}
                                            style={{
                                                display: "flex",
                                                justifyContent: "space-between",
                                                alignItems: "center",
                                                gap: "0.75rem",
                                                padding: "0.4rem 0.5rem",
                                                borderRadius: "var(--radius-sm)",
                                                border: isActive
                                                    ? "1.5px solid var(--color-text)"
                                                    : "1.5px solid transparent",
                                                background: (isHovered || isActive)
                                                    ? "rgba(128, 128, 128, 0.12)"
                                                    : "transparent",
                                                cursor: isClickable ? "pointer" : "default",
                                                opacity: isWinner ? 1 : phase === "FINISHED" ? 0.45 : betPlaced && !isMyBet ? 0.45 : 1,
                                                transition: "background 0.12s, border-color 0.12s, opacity 0.2s",
                                            }}
                                        >

                                            <Box style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
                                                <Typography
                                                    variant="body"
                                                    style={{
                                                        fontWeight: 700,
                                                        fontSize: "16px",
                                                        lineHeight: 1,
                                                        color: `${HORSE_COLORS[i % HORSE_COLORS.length]}`
                                                    }}
                                                >
                                                    #{i + 1}
                                                </Typography>
                                                <HorseSprite
                                                    color={color}
                                                    size={48}
                                                    isRunning={false}
                                                    isWinner={isWinner}
                                                />
                                            </Box>

                                            <Typography
                                                variant="body"
                                                style={{
                                                    color: "var(--color-text-secondary)",
                                                    fontSize: "0.875rem",
                                                    fontWeight: 500,
                                                    fontVariantNumeric: "tabular-nums",
                                                }}
                                            >
                                                {odd.toFixed(1)}x
                                            </Typography>
                                        </Box>
                                    );
                                })
                            )}

                            <Box
                                style={{
                                    borderTop: "1px solid var(--color-border)",
                                    marginTop: "0.375rem",
                                    paddingTop: "0.75rem",
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: "0.5rem",
                                    minHeight: "140px",
                                }}
                            >
                                {balance !== undefined && (
                                    <Typography
                                        variant="caption"
                                        style={{ color: "var(--color-text-secondary)", fontSize: "0.75rem" }}
                                    >
                                        Balance: ${balance.toFixed(2)}
                                    </Typography>
                                )}

                                {!betPlaced && (
                                    <>
                                        <Typography
                                            variant="caption"
                                            style={{ color: "var(--color-text-secondary)", fontWeight: 600 }}
                                        >
                                            {selectedHorse !== null
                                                ? `Horse #${selectedHorse} · ${odds[selectedHorse]?.toFixed(1)}x`
                                                : "Select a horse above"}
                                        </Typography>

                                        <Input
                                            type="number"
                                            value={betInput}
                                            onChange={(e) => setBetInput(e.target.value)}
                                            placeholder="Amount"
                                            disabled={betPlaced}
                                            style={{
                                                width: "100%",
                                                padding: "0.5rem 0.6rem",
                                                borderRadius: "var(--radius-sm)",
                                                border: "1px solid var(--color-border)",
                                                background: "var(--color-bg)",
                                                color: "var(--color-text)",
                                                fontSize: "0.875rem",
                                            }}
                                        />

                                        {potentialWin !== null && (
                                            <Typography
                                                variant="caption"
                                                style={{ fontSize: "0.75rem", color: "var(--color-text-secondary)" }}
                                            >
                                                Win:{" "}
                                                <span style={{ color: "var(--color-success, #2ecc71)", fontWeight: 600 }}>
                                                    ${potentialWin.toFixed(2)}
                                                </span>
                                            </Typography>
                                        )}

                                        <Button
                                            onClick={handlePlaceBet}
                                            disabled={isBetButtonDisabled}
                                            style={{
                                                width: "100%",
                                                opacity: isBetButtonDisabled ? 0.5 : 1,
                                            }}
                                        >
                                            Place Bet
                                        </Button>

                                        <Typography
                                            variant="caption"
                                            style={{
                                                fontSize: "0.75rem",
                                                textAlign: "center",
                                                color: "var(--color-text-secondary)",
                                            }}
                                        >
                                            Place a bet to get ready
                                        </Typography>
                                    </>
                                )}

                                {betPlaced && (() => {
                                    const betHorse = placedBetInfo?.horseIndex ?? selectedHorse ?? 0;
                                    const betAmount = placedBetInfo?.amount ?? (betAmountParsed || 0);
                                    const betOdd = odds[betHorse] ?? 1;
                                    return (
                                        <>
                                            <Box
                                                style={{
                                                    display: "flex",
                                                    flexDirection: "column",
                                                    gap: "0.35rem",
                                                    padding: "0.5rem 0.6rem",
                                                    borderRadius: "var(--radius-sm)",
                                                    background: "rgba(128, 128, 128, 0.08)",
                                                }}
                                            >
                                                <Typography
                                                    variant="caption"
                                                    style={{ fontWeight: 600, color: "var(--color-text)" }}
                                                >
                                                    Your bet: Horse #{betHorse}
                                                </Typography>
                                                <Typography
                                                    variant="caption"
                                                    style={{ fontSize: "0.8rem", color: "var(--color-text-secondary)" }}
                                                >
                                                    Amount: ${betAmount.toFixed(2)}
                                                </Typography>
                                            </Box>

                                            <Box style={{ display: "flex", justifyContent: "space-between" }}>
                                                <Typography variant="caption" style={{ color: "var(--color-text-secondary)" }}>
                                                    Potential win
                                                </Typography>
                                                <Typography
                                                    variant="caption"
                                                    style={{ fontWeight: 700, color: "var(--color-success, #2ecc71)" }}
                                                >
                                                    ${(betAmount * betOdd).toFixed(2)}
                                                </Typography>
                                            </Box>

                                            <Button
                                                disabled
                                                style={{
                                                    width: "100%",
                                                    opacity: 0.5,
                                                }}
                                            >
                                                Place Bet
                                            </Button>

                                            <Typography
                                                variant="caption"
                                                style={{
                                                    fontSize: "0.75rem",
                                                    textAlign: "center",
                                                    color: "var(--color-success, #2ecc71)",
                                                }}
                                            >
                                                ✓ Bet placed — get ready!
                                            </Typography>
                                        </>
                                    );
                                })()}
                            </Box>
                        </Card>
                    </Box>

                    {phase === "FINISHED" && winnerIndex !== undefined && (() => {
                        const betHorse = placedBetInfo?.horseIndex ?? selectedHorse;
                        const betAmount = placedBetInfo?.amount ?? (betAmountParsed || 0);
                        if (betHorse === null || betHorse === undefined) return null;
                        const won = betHorse === winnerIndex;
                        const winAmount = betAmount * (odds[betHorse] ?? 1);
                        return (
                            <Box
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    alignItems: "center",
                                    gap: "0.25rem",
                                    padding: "1rem 1.5rem",
                                    borderRadius: "var(--radius-md)",
                                    background: won
                                        ? "rgba(46, 204, 113, 0.12)"
                                        : "rgba(231, 76, 60, 0.10)",
                                    border: won
                                        ? "1px solid rgba(46, 204, 113, 0.3)"
                                        : "1px solid rgba(231, 76, 60, 0.25)",
                                }}
                            >
                                <Typography
                                    variant="h3"
                                    style={{
                                        fontSize: "1.1rem",
                                        fontWeight: 700,
                                        color: won ? "#2ecc71" : "#e74c3c",
                                    }}
                                >
                                    {won ? "You won!" : "You lost"}
                                </Typography>
                                <Typography
                                    variant="caption"
                                    style={{
                                        fontSize: "0.85rem",
                                        color: won ? "#27ae60" : "#c0392b",
                                        fontWeight: 600,
                                    }}
                                >
                                    {won
                                        ? `+$${winAmount.toFixed(2)}`
                                        : `-$${betAmount.toFixed(2)}`}
                                </Typography>
                            </Box>
                        );
                    })()}

                    <Box
                        style={{
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                            minHeight: "44px",
                            paddingTop: "0.5rem",
                        }}
                    >
                        {(phase === "LOBBY" || phase === "WAITING") && (
                            <Box style={{ display: "flex", gap: "2rem", alignItems: "center" }}>
                                <Button variant="outline" onClick={handleLeave}>
                                    Leave
                                </Button>

                                <Button
                                    onClick={handleReady}
                                    disabled={!betPlaced || ready || phase === "WAITING"}
                                    style={{ opacity: (!betPlaced || ready) ? 0.5 : 1 }}
                                >
                                    {ready ? "Waiting..." : "Ready"}
                                </Button>
                            </Box>
                        )}

                        {phase === "RACING" && (
                            <Typography
                                variant="body"
                                style={{ textAlign: "center", color: "var(--color-text-secondary)" }}
                            >
                                🏇 Race in progress...
                            </Typography>
                        )}

                        {phase === "FINISHED" && (
                            <Button variant="outline" onClick={handleLeave}>
                                Leave
                            </Button>
                        )}
                    </Box>

                </Card>
            </Container>

            {toast && <Toast message={toast.text} onClose={() => setToast(null)} />}
        </Box>
    );
}
