import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { getPlayers, } from "../../store/slices/DeCoderRoomSlice";
import { useGameToast } from "../../hooks/useGameToast";
import { useSystemToastContext } from "../../providers/SystemToastContext";
import { Box, Card, Container, Typography, ToastContainer, Button } from "../../ui";
import { validateRoomName } from "../../utils/SecurityUtils";
import type { DeCoderMessage } from "../../models/WsMessage";
import type { DeCoderGameHistory } from "../../models/DeCoderGameHistory";
import { useDeCoderMessages } from "../../hooks/useDeCoderMessages";
import { useGameSocket } from "../../hooks/useGameSocket";
import { PlayersPanel } from "./decoder/components/PlayersPanel";
import { DeCoderHistory } from "./decoder/components/DeCoderHistory";
import { DeCoderBoard } from "./decoder/components/DeCoderBoard";
import { EndGameOverlay } from "./decoder/components/EndGameOverlay";
import "./decoder/styles/DeCoderRoom.css";

export default function DeCoderRoom() {
    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();

    const { players, room } = useSelector((state: RootState) => state.deCoderRoom);

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

    const [endGameState, setEndGameState] = useState<{
        isOpen: boolean;
        isWin: boolean;
        winnerName?: string;
    } | null>(null);

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
                            <Box />
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
                                <Box className="decoder-players-panel">
                                    <PlayersPanel players={players} />
                                </Box>

                                <Box className="decoder-board-panel">
                                    <DeCoderBoard gameActive={gameActive} onSendMove={handleSendMove} />
                                </Box>

                                <Box className="decoder-history-panel">
                                    <DeCoderHistory history={history} onRequestSync={requestSync} />
                                </Box>
                            </Box>
                        </Box>
                    )}
                </Card>
            </Container>

            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </Box>
    );
}
