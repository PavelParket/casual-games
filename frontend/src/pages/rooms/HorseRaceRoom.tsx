import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import type { HorseRaceGameTick } from "../../models/HorseRace";
import { validateToastMessage } from "../../utils/SecurityUtils";
import { getPreset, getRoomById, syncReadiness, syncRoomState } from "../../store/slices/HorseRaceRoomSlice";
import { findByGuid } from "../../store/slices/UserSlice";
import { useWebSocket } from "../../hooks/useWebSocket";
import type { HorseRaceGameMessage } from "../../models/WsMessage";
import { Box, Button, Card, Container, Toast, Typography } from "../../ui";

const TICK_DURATION_MS = 600;

const HORSE_SIZE = 36;

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

export default function HorseRaceRoom() {
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();

    const roomId = useParams<{ roomId?: string }>().roomId;

    const { room, players, readyPlayersCount, totalPlayersCount, preset } = useSelector(
        (state: RootState) => state.horseRaceRoom
    );

    const [toast, setToast] = useState<{ text: string } | null>(null);
    const showToast = useCallback((text: string) => {
        setToast({ text: validateToastMessage(text) });
    }, []);

    const [phase, setPhase] = useState<RacePhase>("LOBBY");
    const [ready, setReady] = useState(false);
    const [horsePositions, setHorsePositions] = useState<number[]>([]);
    const [winnerIndex, setWinnerIndex] = useState<number | undefined>();

    const ticksRef = useRef<HorseRaceGameTick[]>([]);
    const rafRef = useRef<number | null>(null);
    const startTimeRef = useRef<number>(0);
    const winnerRef = useRef<number>(0);

    const trackRef = useRef<HTMLDivElement>(null);
    const trackWidthRef = useRef<number>(0);

    useEffect(() => {
        if (!roomId || !guid) {
            navigate("/rooms");
            return;
        }

        dispatch(getRoomById({ roomId }));
        dispatch(findByGuid(guid));
    }, [dispatch, guid, navigate, roomId]);

    useEffect(() => {
        const el = trackRef.current;
        if (!el) return;

        trackWidthRef.current = el.getBoundingClientRect().width;

        const observer = new ResizeObserver((entries) => {
            for (const entry of entries) {
                trackWidthRef.current = entry.contentRect.width;
            }
        });

        observer.observe(el);
        return () => observer.disconnect();
    }, []);

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

    const lerp = (a: number, b: number, t: number) => a + (b - a) * t;

    const stopAnimation = useCallback(() => {
        if (rafRef.current !== null) {
            cancelAnimationFrame(rafRef.current);
            rafRef.current = null;
        }
    }, []);

    const startAnimation = useCallback((ticks: HorseRaceGameTick[], winner: number) => {
        const zeroTick: HorseRaceGameTick = { tickIndex: 0, positions: new Array(ticks[0].positions.length).fill(0) };
        const allTicks = [zeroTick, ...ticks];
        ticksRef.current = allTicks;
        winnerRef.current = winner;
        startTimeRef.current = performance.now();

        const totalTicks = ticks.length;

        const frame = (now: number) => {
            const raceDuration = allTicks.length * TICK_DURATION_MS;
            const elapsed = now - startTimeRef.current;
            const progress = Math.min(elapsed / raceDuration, 1);

            const rawIndex = progress * (totalTicks - 1);
            const tickIndex = Math.floor(rawIndex);
            const localT = rawIndex - tickIndex;

            const fromTick = allTicks[Math.min(tickIndex, totalTicks - 1)];
            const toTick = allTicks[Math.min(tickIndex + 1, totalTicks - 1)];

            const interpolated = fromTick.positions.map((fromPos, i) =>
                lerp(fromPos, toTick.positions[i], localT)
            );

            setHorsePositions(interpolated);

            if (progress < 1) {
                rafRef.current = requestAnimationFrame(frame);
            } else {
                rafRef.current = null;
                setWinnerIndex(winnerRef.current);
                setPhase("FINISHED");
                showToast(`🏆 Horse #${winnerRef.current} wins!`);
            }
        };

        rafRef.current = requestAnimationFrame(frame);
    }, [showToast]);

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
                const { ticks, winnerHorseIndex } = message;

                if (!ticks || winnerHorseIndex === undefined) {
                    break;
                }

                setPhase("RACING");
                startAnimation(ticks, winnerHorseIndex);
                break;
            }

            default:
                break;
        }
    }, [dispatch, isConnected, message, room, roomId, showToast, startAnimation]);

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
        navigate("/rooms");
    };

    const positionToPx = (position: number): number => {
        const usableWidth = trackWidthRef.current - HORSE_SIZE;
        return (position / 100) * usableWidth;
    };

    const horseCount = preset?.horseCount ?? 0;
    const odds = preset?.odds ?? [];

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
                    <Typography
                        variant="caption"
                        style={{ textAlign: "center", display: "block", marginTop: "0.25rem", color: "var(--color-text-secondary)" }}
                    >
                        {readyPlayersCount ?? 0} / {totalPlayersCount ?? 0} players ready
                    </Typography>
                </Box>

                <Card style={{ padding: "1.5rem", display: "flex", flexDirection: "column", gap: "1.5rem" }}>
                    <Box style={{ display: "flex", gap: "1.5rem", alignItems: "flex-start" }}>
                        <Card
                            style={{
                                flex: 1,
                                padding: "1.25rem",
                                background: "var(--color-bg-secondary)",
                            }}
                        >
                            <div
                                ref={trackRef}
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: "0.875rem",
                                }}
                            >
                                {horseCount === 0 ? (
                                    <Typography
                                        variant="body"
                                        style={{ color: "var(--color-text-secondary)", textAlign: "center" }}
                                    >
                                        Loading race...
                                    </Typography>
                                ) : (
                                    Array.from({ length: horseCount }, (_, i) => {
                                        const position = horsePositions[i] ?? 0;
                                        const color = HORSE_COLORS[i % HORSE_COLORS.length];
                                        const isWinner = phase === "FINISHED" && winnerIndex === i;
                                        const leftPx = positionToPx(position);

                                        return (
                                            <Box
                                                key={i}
                                                style={{
                                                    position: "relative",
                                                    height: `${HORSE_SIZE}px`,
                                                    display: "flex",
                                                    alignItems: "center",
                                                }}
                                            >
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

                                                <Box
                                                    style={{
                                                        position: "absolute",
                                                        left: `${leftPx}px`,
                                                        width: `${HORSE_SIZE}px`,
                                                        height: `${HORSE_SIZE}px`,
                                                        background: color,
                                                        borderRadius: "var(--radius-sm)",
                                                        display: "flex",
                                                        alignItems: "center",
                                                        justifyContent: "center",
                                                        boxShadow: isWinner
                                                            ? `0 0 12px 4px ${color}`
                                                            : "var(--shadow-sm)",
                                                        outline: isWinner ? `2px solid ${color}` : "none",
                                                        zIndex: 1,
                                                    }}
                                                >
                                                    <Typography
                                                        variant="caption"
                                                        inverse
                                                        style={{ fontWeight: 700, fontSize: "14px", lineHeight: 1 }}
                                                    >
                                                        {i}
                                                    </Typography>
                                                </Box>
                                            </Box>
                                        );
                                    })
                                )}
                            </div>
                        </Card>

                        <Card
                            style={{
                                minWidth: "140px",
                                padding: "1rem 1.25rem",
                                background: "var(--color-bg-secondary)",
                                display: "flex",
                                flexDirection: "column",
                                gap: "0.625rem",
                            }}
                        >
                            <Typography variant="h3" style={{ marginBottom: "0.25rem" }}>
                                Odds
                            </Typography>

                            {odds.length === 0 ? (
                                <Typography variant="caption" style={{ color: "var(--color-text-secondary)" }}>
                                    —
                                </Typography>
                            ) : (
                                odds.map((odd, i) => {
                                    const color = HORSE_COLORS[i % HORSE_COLORS.length];
                                    const isWinner = phase === "FINISHED" && winnerIndex === i;

                                    return (
                                        <Box
                                            key={i}
                                            style={{
                                                display: "flex",
                                                justifyContent: "space-between",
                                                alignItems: "center",
                                                gap: "0.75rem",
                                                opacity: isWinner ? 1 : phase === "FINISHED" ? 0.45 : 1,
                                            }}
                                        >
                                            <Box style={{ display: "flex", alignItems: "center", gap: "0.4rem" }}>
                                                <Box
                                                    style={{
                                                        width: "10px",
                                                        height: "10px",
                                                        borderRadius: "2px",
                                                        background: color,
                                                        flexShrink: 0,
                                                    }}
                                                />
                                                <Typography variant="body" style={{ fontWeight: 500 }}>
                                                    #{i}
                                                </Typography>
                                            </Box>
                                            <Typography
                                                variant="body"
                                                style={{
                                                    color: "var(--color-text-secondary)",
                                                    fontVariantNumeric: "tabular-nums",
                                                }}
                                            >
                                                {odd.toFixed(1)}x
                                            </Typography>
                                        </Box>
                                    );
                                })
                            )}
                        </Card>
                    </Box>

                    {(phase === "LOBBY" || phase === "WAITING") && (
                        <Box
                            style={{
                                display: "flex",
                                justifyContent: "center",
                                alignItems: "center",
                                gap: "2rem",
                                paddingTop: "0.5rem",
                            }}
                        >
                            <Button variant="outline" onClick={handleLeave}>
                                Leave
                            </Button>

                            <Button
                                onClick={handleReady}
                                disabled={ready || phase === "WAITING"}
                                style={{ opacity: ready ? 0.5 : 1 }}
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
                        <Box style={{ display: "flex", justifyContent: "center", paddingTop: "0.5rem" }}>
                            <Button variant="outline" onClick={handleLeave}>
                                Leave
                            </Button>
                        </Box>
                    )}

                    {phase === "LOBBY" || phase === "WAITING" ? (
                        <Box
                            style={{
                                display: "flex",
                                justifyContent: "center",
                                gap: "0.75rem",
                                flexWrap: "wrap",
                            }}
                        >
                            {players && Object.values(players).map((username) => (
                                <Typography
                                    key={username}
                                    variant="caption"
                                    style={{
                                        padding: "0.25rem 0.75rem",
                                        borderRadius: "var(--radius-sm)",
                                        background: "var(--color-bg)",
                                        border: "1px solid var(--color-border)",
                                    }}
                                >
                                    {username}
                                </Typography>
                            ))}
                        </Box>
                    ) : null}
                </Card>
            </Container>

            {toast && <Toast message={toast.text} onClose={() => setToast(null)} />}
        </Box>
    );
}
