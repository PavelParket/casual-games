import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { getPreset, syncRoomState } from "../../store/slices/HorseRaceRoomSlice";
import type { HorseRaceHorseKeyframes, PlacedBetInfo } from "../../models/HorseRace";
import type { HorseRaceGameMessage } from "../../models/WsMessage";
import { useGameToast } from "../../hooks/useGameToast";
import { useHorseRaceMessages } from "../../hooks/useHorseRaceMessages";
import { useGameSocket } from "../../hooks/useGameSocket";
import { Box, Button, Card, Container, Icon, ToastContainer, Typography, useThemedIcon } from "../../ui";
import { validateAmountInput } from "../../utils/SecurityUtils";
import { BettingPanel } from "./horserace/components/BettingPanel";
import { EndGameOverlay } from "./horserace/components/EndGameOverlay";
import { HorseRaceTrack } from "./horserace/components/HorseRaceTrack";
import { AnimatePresence, motion } from "framer-motion";
import { useSystemToastContext } from "../../hooks/useSystemToastContext";

type RacePhase = "LOBBY" | "WAITING" | "RACING" | "FINISHED";

function formatCountdown(seconds: number): string {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;

    return `${m}:${s.toString().padStart(2, "0")}`;
}

export default function HorseRaceRoom() {
    const balance = useSelector((state: RootState) => state.user.user?.balance);
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();

    const { getIcon } = useThemedIcon();

    const roomId = useParams<{ roomId?: string }>().roomId;

    const { room, readyPlayersCount, totalPlayersCount, preset } = useSelector(
        (state: RootState) => state.horseRaceRoom
    );

    const { toasts, showGameToast, dismiss } = useGameToast();
    const { showSystemToast } = useSystemToastContext();

    const [phase, setPhase] = useState<RacePhase>("LOBBY");
    const [ready, setReady] = useState(false);
    const [winnerIndex, setWinnerIndex] = useState<number | undefined>();
    const [raceKeyframes, setRaceKeyframes] = useState<HorseRaceHorseKeyframes[] | null>(null);

    const [selectedHorse, setSelectedHorse] = useState<number | null>(null);
    const [betInput, setBetInput] = useState<string>("");
    const [betPlaced, setBetPlaced] = useState<boolean>(false);
    const [placedBetInfo, setPlacedBetInfo] = useState<PlacedBetInfo | null>(null);
    const [hoveredHorse, setHoveredHorse] = useState<number | null>(null);

    const [secondsLeft, setSecondsLeft] = useState<number | null>(null);
    const countdownIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

    const [windowWidth, setWindowWidth] = useState(window.innerWidth);
    useEffect(() => {
        const handleResize = () => setWindowWidth(window.innerWidth);
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);
    const isMobile = windowWidth <= 1060;
    const [isMobileBettingOpen, setIsMobileBettingOpen] = useState(false);

    const horseSize = windowWidth <= 600 ? 56 : (windowWidth <= 1060 ? 72 : 88);

    useEffect(() => {
        if (phase === "RACING" || phase === "FINISHED") {
            setIsMobileBettingOpen(false);
        }
    }, [phase]);

    const handleDisplaced = useCallback(() => {
        showSystemToast("Your session was opened in another window", "system-error");
        navigate("/rooms");
    }, [navigate, showSystemToast]);

    const handleDisconnect = useCallback(() => {
        showSystemToast("Connection lost. Redirecting to rooms...", "system-error");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showSystemToast]);

    const clearCountdown = useCallback(() => {
        if (countdownIntervalRef.current !== null) {
            clearInterval(countdownIntervalRef.current);
            countdownIntervalRef.current = null;
        }
    }, []);

    const startCountdown = useCallback((initialSeconds: number) => {
        clearCountdown();
        setSecondsLeft(initialSeconds);

        const endTime = Date.now() + initialSeconds * 1000;

        countdownIntervalRef.current = setInterval(() => {
            const remaining = Math.ceil((endTime - Date.now()) / 1000);

            if (remaining <= 0) {
                clearCountdown();
                setSecondsLeft(0);
                return;
            }

            setSecondsLeft(remaining);
        }, 200);
    }, [clearCountdown]);

    useEffect(() => {
        return () => clearCountdown();
    }, [clearCountdown]);

    const handleRaceEnd = useCallback(() => {
        setPhase((prev) => {
            if (prev === "RACING") {
                showGameToast(`Horse #${(winnerIndex ?? 0) + 1} wins!`, "game-info");
                return "FINISHED";
            }
            return prev;
        });
    }, [winnerIndex, showGameToast]);

    const processStart = useCallback((message: HorseRaceGameMessage) => {
        const { horseKeyframes, winnerHorseIndex } = message;

        if (!horseKeyframes || winnerHorseIndex === undefined) {
            return;
        }

        clearCountdown();
        setSecondsLeft(null);
        setRaceKeyframes(horseKeyframes);
        setWinnerIndex(winnerHorseIndex);
        setPhase("RACING");
    }, [clearCountdown]);

    const handleMessage = useHorseRaceMessages({
        roomId,
        processStart,
        startCountdown,
        clearCountdown,
        setSecondsLeft,
        setBetPlaced,
        setPlacedBetInfo,
        setReady,
        showGameToast,
    });

    const { isConnected, send } = useGameSocket<HorseRaceGameMessage>({
        roomId,
        roomType: room?.type,
        showGameToast,
        onGameMessage: handleMessage,
        onDisplaced: handleDisplaced,
        onConnectionLost: handleDisconnect,
    });

    useEffect(() => {
        if (!isConnected || !roomId || !room) {
            return;
        }

        dispatch(syncRoomState({ roomId, roomType: room.type }));
        dispatch(getPreset({ roomId }));
    }, [dispatch, isConnected, room, roomId]);

    const handlePlaceBet = () => {
        if (!room || !isConnected || betPlaced || phase !== "LOBBY") {
            return;
        }

        if (selectedHorse === null) {
            showGameToast("Please select a horse first.", "game-error");
            return;
        }

        const amount = parseFloat(betInput);

        if (isNaN(amount) || amount <= 0) {
            showGameToast("Please enter a valid bet amount.", "game-error");
            return;
        }

        if (balance !== undefined && amount > balance) {
            showGameToast("Bet amount exceeds your balance.", "game-error");
            return;
        }

        console.debug("[HorseRaceRoom] send BET", { horseIndex: selectedHorse, amount });
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

    const handleBetChange = (val: string) => {
        const validated = validateAmountInput(val);
        if (validated !== null) setBetInput(validated);
    };

    const handleLeave = () => {
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

    const isGameOver = phase === "FINISHED" && winnerIndex !== undefined;

    const betHorse = placedBetInfo?.horseIndex ?? selectedHorse;
    const betAmount = placedBetInfo?.amount ?? (betAmountParsed || 0);
    const won = betHorse === winnerIndex;
    const winAmount = betAmount * (betHorse != null ? (odds[betHorse] ?? 1) : 1);

    const bettingPanelProps = {
        odds, phase, winnerIndex, selectedHorse, hoveredHorse,
        betPlaced, placedBetInfo, balance, betInput, potentialWin, ready,
        onSelectHorse: setSelectedHorse,
        onHoverHorse: setHoveredHorse,
        onBetChange: handleBetChange,
        onPlaceBet: handlePlaceBet,
        onReady: handleReady,
    };

    return (
        <Box className="page-wrapper">
            <Container>
                <Box style={{ padding: "2rem 0" }}>
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        Horse Race: {room?.name}
                    </Typography>
                </Box>

                <Card style={{ padding: "1rem", display: "flex", flexDirection: "column", gap: "1.5rem" }}>

                    {!isGameOver && (
                        <Box style={{ padding: "0.75rem 1.5rem", borderBottom: "1px solid var(--color-border)", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                            <Typography variant="caption" style={{ opacity: 0.7 }}>
                                {phase === "RACING"
                                    ? "Race in progress..."
                                    : `Ready: ${readyPlayersCount ?? 0} / ${totalPlayersCount ?? 0}`
                                }
                            </Typography>
                            <Button variant="outline" onClick={handleLeave} style={{ padding: "0.25rem 0.75rem" }}>
                                Leave
                            </Button>
                        </Box>
                    )}

                    {isGameOver ? (
                        <EndGameOverlay
                            won={won}
                            betAmount={betAmount}
                            winAmount={winAmount}
                            onLeave={handleLeave}
                        />
                    ) : (

                        <Box style={{ display: "flex", gap: "1.5rem", alignItems: "flex-start", minHeight: "280px" }}>

                            <Box style={{ flex: 1, width: "100%", display: "flex", flexDirection: "column", gap: "0.5rem", minWidth: 0 }}>

                                {(secondsLeft !== null || (isMobile && (phase === "LOBBY" || phase === "WAITING"))) && (
                                    <Box style={{ display: "flex", gap: "0.5rem", width: "100%", alignItems: "stretch" }}>

                                        {secondsLeft !== null && (
                                            <Box style={{
                                                flex: 1, display: "flex", alignItems: "center", justifyContent: "center", padding: "0.5rem 1rem", borderRadius: "var(--radius-sm)",
                                                background: secondsLeft <= 10 ? "rgba(231,76,60,0.12)" : "rgba(255,255,255,0.04)",
                                                border: `1px solid ${secondsLeft <= 10 ? "rgba(231,76,60,0.4)" : "var(--color-border)"}`
                                            }}>
                                                <Typography variant="caption" style={{ fontWeight: 700, fontSize: "0.9rem", color: secondsLeft <= 10 ? "#e74c3c" : "var(--color-text-secondary)", letterSpacing: "0.04em" }}>
                                                    Race starts in {formatCountdown(secondsLeft)}
                                                </Typography>
                                            </Box>
                                        )}

                                        {isMobile && (phase === "LOBBY" || phase === "WAITING") && (
                                            <Button
                                                variant="outline"
                                                onClick={() => setIsMobileBettingOpen(true)}
                                                style={{ width: "50px", flexShrink: 0, padding: "0.25rem 0.35rem" }}
                                            >
                                                Bets
                                            </Button>
                                        )}
                                    </Box>
                                )}

                                <Card style={{ flex: 1, padding: "1.25rem", background: "var(--color-bg-secondary)", minHeight: "260px" }}>
                                    <HorseRaceTrack
                                        phase={phase}
                                        horseCount={horseCount}
                                        winnerIndex={winnerIndex}
                                        raceKeyframes={raceKeyframes}
                                        onRaceEnd={handleRaceEnd}
                                        horseSize={horseSize}
                                    />
                                </Card>
                            </Box>
                            {!isMobile && (
                                <BettingPanel {...bettingPanelProps} inDrawer={false} />
                            )}
                        </Box>
                    )}
                </Card>
            </Container>

            <AnimatePresence>
                {isMobile && isMobileBettingOpen && (
                    <>
                        <motion.div
                            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} transition={{ duration: 0.2 }}
                            style={{ position: "fixed", inset: 0, background: "rgba(0, 0, 0, 0.4)", zIndex: 1000, backdropFilter: "blur(4px)" }}
                            onClick={() => setIsMobileBettingOpen(false)}
                        />
                        <motion.div
                            initial={{ x: "100%" }} animate={{ x: 0 }} exit={{ x: "100%" }} transition={{ type: "spring", bounce: 0, duration: 0.4 }}
                            style={{ position: "fixed", top: 0, right: 0, bottom: 0, width: "300px", maxWidth: "85vw", background: "var(--color-bg)", zIndex: 1001, boxShadow: "var(--shadow-lg)", display: "flex", flexDirection: "column" }}
                        >
                            <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1.5rem", borderBottom: "1px solid var(--color-border)" }}>
                                <Typography variant="h2">Bets & Ready</Typography>
                                <Button variant="ghost" onClick={() => setIsMobileBettingOpen(false)} style={{ padding: "0.25rem", boxShadow: "none" }}>
                                    <Icon src={getIcon("close")} size={20} alt="close" />
                                </Button>
                            </Box>
                            <Box className="custom-scrollbar" style={{ flex: 1, overflowY: "auto" }}>
                                <BettingPanel {...bettingPanelProps} inDrawer={true} />
                            </Box>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>

            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </Box>
    );
}
