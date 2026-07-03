import "./decoder/styles/DeCoderRoom.css";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { motion, AnimatePresence } from "framer-motion";
import type { AppDispatch, RootState } from "../../store/store";
import { getPlayers, } from "../../store/slices/DeCoderRoomSlice";
import { useGameToast } from "../../hooks/useGameToast";
import { Box, Card, Container, Typography, ToastContainer, Button, Icon, useThemedIcon } from "../../ui";
import { validateRoomName } from "../../utils/SecurityUtils";
import type { DeCoderMessage } from "../../models/WsMessage";
import type { DeCoderGameHistory } from "../../models/DeCoderGameHistory";
import { useDeCoderMessages } from "../../hooks/useDeCoderMessages";
import { useGameSocket } from "../../hooks/useGameSocket";
import { PlayersPanel } from "./decoder/components/PlayersPanel";
import { DeCoderHistory } from "./decoder/components/DeCoderHistory";
import { DeCoderBoard } from "./decoder/components/DeCoderBoard";
import { EndGameOverlay } from "./decoder/components/EndGameOverlay";
import { useSystemToastContext } from "../../hooks/useSystemToastContext";

export default function DeCoderRoom() {
    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();

    const { getIcon } = useThemedIcon();

    const { players, room } = useSelector((state: RootState) => state.deCoderRoom);
    const balance = useSelector((state: RootState) => state.user.user?.balance);

    const { roomName: rawRoomName, roomId } = useParams<{
        roomName?: string;
        roomId?: string;
    }>();

    const roomName = validateRoomName(rawRoomName ?? "");

    const { toasts, showGameToast, dismiss } = useGameToast();
    const { showSystemToast } = useSystemToastContext();

    const [gameActive, setGameActive] = useState<boolean>(false);
    const [history, setHistory] = useState<DeCoderGameHistory[]>([]);
    const [jackpot, setJackpot] = useState<number>(0);
    const [spent, setSpent] = useState<number>(0);

    const [endGameState, setEndGameState] = useState<{
        isOpen: boolean;
        isWin: boolean;
        winnerName?: string;
    } | null>(null);

    const [windowWidth, setWindowWidth] = useState(window.innerWidth);

    useEffect(() => {
        const handleResize = () => setWindowWidth(window.innerWidth);
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    const isMobile = windowWidth <= 600;
    const [isMobilePlayersOpen, setIsMobilePlayersOpen] = useState(false);

    const requestSyncRef = useRef<() => void>(() => { });

    const handleDisplaced = useCallback(() => {
        showSystemToast("Your session was opened in another window", "system-error");
        navigate("/rooms");
    }, [navigate, showSystemToast]);

    const handleDisconnect = useCallback(() => {
        showSystemToast("Connection lost. Redirecting to rooms...", "system-error");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showSystemToast]);

    const requestSync = useCallback(() => {
        requestSyncRef.current();
    }, []);

    const handleMessage = useDeCoderMessages({
        roomId,
        setGameActive,
        setHistory,
        setJackpot,
        setSpent,
        setEndGameOverlay: setEndGameState,
        showGameToast,
    });

    const { isConnected, send } = useGameSocket<DeCoderMessage>({
        roomId,
        roomType: room?.type,
        showGameToast,
        onGameMessage: handleMessage,
        onDisplaced: handleDisplaced,
        onConnectionLost: handleDisconnect,
    });

    useEffect(() => {
        requestSyncRef.current = () => {
            if (isConnected && roomId) {
                send({ type: "SYSTEM", event: "STATE", roomId });
            }
        };
    }, [isConnected, roomId, send]);

    const handleSendMove = useCallback((codeStr: string) => {
        if (!roomId || !isConnected) return;
        send({ type: "SYSTEM", event: "MOVE", roomId, code: codeStr });
    }, [roomId, isConnected, send]);

    useEffect(() => {
        if (roomId && room?.type) {
            dispatch(getPlayers({ roomId, roomType: room.type }));
        }
    }, [dispatch, roomId, room?.type]);

    return (
        <Box className="page-wrapper">
            <Container>
                <Box style={{ padding: "2rem 0 1rem" }}>
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        De-Coder: {roomName}
                    </Typography>
                </Box>

                <Card
                    style={{
                        padding: 0,
                        display: "flex",
                        flexDirection: "column",
                        flex: 1,
                        minHeight: 0,
                    }}
                >
                    {!endGameState?.isOpen && (
                        <Box style={{
                            padding: "0.75rem 1.5rem",
                            borderBottom: "1px solid var(--color-border)",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                        }}>
                            <Box>
                                {isMobile && (
                                    <Button variant="outline" onClick={() => setIsMobilePlayersOpen(true)} style={{ padding: "0.25rem 0.75rem", display: "flex", alignItems: "center", gap: "8px" }}>
                                        <Icon src={getIcon("user")} size={18} alt="players" />
                                        <Typography variant="body" style={{ fontSize: "14px", fontWeight: 500 }}>
                                            Players ({players ? Object.keys(players).length : 0})
                                        </Typography>
                                    </Button>
                                )}
                            </Box>
                            <Button variant="outline" onClick={() => navigate("/rooms")} style={{ padding: "0.25rem 0.75rem" }}>
                                Leave
                            </Button>
                        </Box>
                    )}

                    {endGameState?.isOpen ? (
                        <EndGameOverlay
                            isWin={endGameState.isWin}
                            winnerName={endGameState.winnerName}
                            jackpot={jackpot}
                            onLeave={() => navigate("/rooms")}
                        />
                    ) : (
                        <Box className="decoder-main-content">
                            <Box className="decoder-grid">
                                {!isMobile && (
                                    <Box className="decoder-players-panel">
                                        <PlayersPanel players={players} inDrawer={false} />
                                    </Box>
                                )}

                                <Box className="decoder-board-panel">
                                    <DeCoderBoard
                                        gameActive={gameActive}
                                        balanceBefore={balance}
                                        spent={spent}
                                        onSendMove={handleSendMove}
                                    />                                </Box>

                                <Box className="decoder-history-panel">
                                    <DeCoderHistory history={history} onRequestSync={requestSync} />
                                </Box>
                            </Box>
                        </Box>
                    )}
                </Card>
            </Container>

            <AnimatePresence>
                {isMobile && isMobilePlayersOpen && (
                    <>
                        <motion.div
                            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} transition={{ duration: 0.2 }}
                            style={{ position: "fixed", inset: 0, background: "rgba(0, 0, 0, 0.4)", zIndex: 1000, backdropFilter: "blur(4px)" }}
                            onClick={() => setIsMobilePlayersOpen(false)}
                        />
                        <motion.div
                            initial={{ x: "-100%" }} animate={{ x: 0 }} exit={{ x: "-100%" }} transition={{ type: "spring", bounce: 0, duration: 0.4 }}
                            style={{ position: "fixed", top: 0, left: 0, bottom: 0, width: "300px", maxWidth: "85vw", background: "var(--color-bg)", zIndex: 1001, boxShadow: "var(--shadow-lg)", display: "flex", flexDirection: "column" }}
                        >
                            <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1.5rem", borderBottom: "1px solid var(--color-border)" }}>
                                <Typography variant="h2">Players</Typography>
                                <Button variant="ghost" onClick={() => setIsMobilePlayersOpen(false)} style={{ padding: "0.25rem", boxShadow: "none" }}>
                                    <Icon src={getIcon("close")} size={20} alt="close" />
                                </Button>
                            </Box>
                            <Box className="custom-scrollbar" style={{ flex: 1, overflowY: "auto" }}>
                                <PlayersPanel players={players} inDrawer={true} />
                            </Box>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>


            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </Box>
    );
}
