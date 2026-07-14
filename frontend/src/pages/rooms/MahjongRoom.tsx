import "./mahjong/styles/MahjongRoom.css";
import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import { motion, AnimatePresence } from "framer-motion";

import type { RootState } from "../../store/store";
import type { MahjongGameMessage } from "../../models/WsMessage";
import { clearError } from "../../store/slices/MahjongRoomSlice";

import { useGameToast } from "../../hooks/useGameToast";
import { useSystemToastContext } from "../../hooks/useSystemToastContext";
import { useSliceErrorToast } from "../../hooks/useSliceErrorToast";
import { useGameSocket } from "../../hooks/useGameSocket";
import { useMahjongMessages } from "../../hooks/useMahjongMessages";

import { Box, Button, Card, Container, Icon, ToastContainer, Typography, useThemedIcon } from "../../ui";
import { validateAmountInput } from "../../utils/SecurityUtils";

import type { MahjongTileData } from "./mahjong/utils/MahjongTypes";
import { MAHJONG_LAYOUT } from "./mahjong/utils/MahjongLayoutData";
import { facesMatch } from "./mahjong/utils/MahjongGameUtils";

import { MahjongBoard } from "./mahjong/components/MahjongBoard";
import { BettingPanel } from "./mahjong/components/BettingPanel";
import { MahjongPlayersPanel } from "./mahjong/components/MahjongPlayersPanel";
import { EndGameOverlay } from "./mahjong/components/EndGameOverlay";
import { DeadlockOverlay } from "./mahjong/components/DeadlockOverlay";

export default function MahjongRoom() {
    const navigate = useNavigate();
    const { getIcon } = useThemedIcon();

    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const balance = useSelector((state: RootState) => state.user.user?.balance);
    const { room, players, readyPlayersCount, totalPlayersCount, playerBetMap } = useSelector((state: RootState) => state.mahjongRoom);

    const roomId = useParams<{ roomId?: string }>().roomId;

    const { toasts, showGameToast, dismiss } = useGameToast();
    const { showSystemToast } = useSystemToastContext();
    useSliceErrorToast((state: RootState) => state.mahjongRoom.errors, clearError);

    // Локальный стейт лобби
    const [ready, setReady] = useState(false);
    const [betInput, setBetInput] = useState("");
    const [betPlaced, setBetPlaced] = useState(false);
    const [gameAborted, setGameAborted] = useState(false);

    // Локальный стейт игры
    const [isGame, setIsGame] = useState(false);
    const [tiles, setTiles] = useState<MahjongTileData[]>([]);
    const [removedIds, setRemovedIds] = useState<Set<string>>(new Set());
    const [selectedId, setSelectedId] = useState<string | null>(null);
    const [winnerId, setWinnerId] = useState<string | null | undefined>(undefined);
    const [deadlockSecondsLeft, setDeadlockSecondsLeft] = useState<number | null>(null);
    const [awaitingResponse, setAwaitingResponse] = useState(false);

    // Статистика доски
    const [myTilesRemaining, setMyTilesRemaining] = useState<number>(0);
    const [availableMoves, setAvailableMoves] = useState<number>(0);

    const [windowWidth, setWindowWidth] = useState(window.innerWidth);
    useEffect(() => {
        const handleResize = () => setWindowWidth(window.innerWidth);
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);
    
    const isMobile = windowWidth <= 1060;
    const [isMobilePlayersOpen, setIsMobilePlayersOpen] = useState(false);

    // -------------------------------------------------------------
    // WebSocket Handlers
    // -------------------------------------------------------------
    const handleDisplaced = useCallback(() => {
        showSystemToast("Your session was opened in another window", "system-error");
        navigate("/rooms");
    }, [navigate, showSystemToast]);

    const handleDisconnect = useCallback(() => {
        showSystemToast("Connection lost. Redirecting to rooms...", "system-error");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showSystemToast]);

    const handleSocketError = useCallback(() => {
        setAwaitingResponse(false);
    }, []);

    const processGameState = useCallback((msg: MahjongGameMessage) => {
        setAwaitingResponse(false);

        // Инициализация при START
        if (msg.tiles && msg.tiles.length > 0) {
            const newTiles = msg.tiles.map(bt => {
                const slot = MAHJONG_LAYOUT.find(s => s.id === bt.slotId);
                return slot ? { slot, face: { suit: bt.suit, value: bt.value } } : null;
            }).filter(Boolean) as MahjongTileData[];
            
            setTiles(newTiles);
            setRemovedIds(new Set());
            setIsGame(true);
            setWinnerId(undefined);
            setDeadlockSecondsLeft(null);
            setSelectedId(null);
        }

        // Удаление тайлов при MOVE
        if (msg.removedSlotIds && msg.removedSlotIds.length > 0) {
            setRemovedIds(prev => {
                const next = new Set(prev);
                msg.removedSlotIds!.forEach(id => next.add(id));
                return next;
            });
        }

        // Обновление статистики
        if (msg.tilesRemaining !== undefined) {
            setMyTilesRemaining(msg.tilesRemaining);
        }
        if (msg.availableMoves !== undefined) {
            setAvailableMoves(msg.availableMoves);
        }
    }, []);

    const processDeadlockWait = useCallback((seconds: number) => {
        setDeadlockSecondsLeft(seconds);
    }, []);

    const processGameOver = useCallback((winnerGuid: string | undefined) => {
        setWinnerId(winnerGuid ?? null);
        setIsGame(false);
        setAwaitingResponse(false);
        setDeadlockSecondsLeft(null);
    }, []);

    const processAbort = useCallback(() => {
        setGameAborted(true);
        showGameToast("Opponent left the game. Redirecting to rooms...", "game-info");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showGameToast]);

    const handleMessage = useMahjongMessages({
        roomId,
        isGame,
        gameAborted,
        processGameState,
        processDeadlockWait,
        processGameOver,
        processAbort,
        setBetPlaced,
        setReady,
        showGameToast,
    });

    const { isConnected, send } = useGameSocket<MahjongGameMessage>({
        roomId,
        roomType: room?.type,
        showGameToast,
        onGameMessage: handleMessage,
        onError: handleSocketError,
        onDisplaced: handleDisplaced,
        onConnectionLost: handleDisconnect,
    });

    // -------------------------------------------------------------
    // Component Logic
    // -------------------------------------------------------------
    
    // Таймер дедлока
    useEffect(() => {
        if (deadlockSecondsLeft === null || deadlockSecondsLeft <= 0) return;
        
        const timer = setInterval(() => {
            setDeadlockSecondsLeft(prev => prev !== null && prev > 0 ? prev - 1 : 0);
        }, 1000);
        
        return () => clearInterval(timer);
    }, [deadlockSecondsLeft]);

    const handleTileClick = (id: string) => {
        if (!room || !isConnected || !guid || awaitingResponse || deadlockSecondsLeft !== null || gameAborted) {
            return;
        }

        if (selectedId === id) {
            setSelectedId(null);
            return;
        }

        if (!selectedId) {
            setSelectedId(id);
            return;
        }

        const tile1 = tiles.find(t => t.slot.id === selectedId);
        const tile2 = tiles.find(t => t.slot.id === id);

        if (tile1 && tile2 && facesMatch(tile1.face, tile2.face)) {
            // Отправляем ход на сервер (оптимистично не удаляем, ждем GAME_STATE)
            setAwaitingResponse(true);
            send({
                type: "USER_MESSAGE",
                event: "MOVE",
                roomId: room.id,
                fromUserId: guid,
                slot1: selectedId,
                slot2: id
            });
            setSelectedId(null);
        } else {
            setSelectedId(id);
        }
    };

    const handlePlaceBet = () => {
        if (!room || !isConnected || !guid || betPlaced || gameAborted) return;
        
        const amount = parseFloat(betInput);
        if (isNaN(amount) || amount <= 0) {
            showGameToast("Please enter a valid bet amount greater than 0", "game-error");
            return;
        }
        if (balance !== undefined && amount > balance) {
            showGameToast("Insufficient balance", "game-error");
            return;
        }

        send({ type: "USER_MESSAGE", event: "BET", fromUserId: guid, roomId: room.id, bet: amount });
    };

    const handleReady = () => {
        if (!room || !isConnected || ready || gameAborted) return;
        if (!betPlaced) {
            showGameToast("You must place a bet before becoming ready!", "game-error");
            return;
        }

        send({ type: "USER_MESSAGE", event: "READY", roomId: room.id });
        setReady(true);
    };

    const handleLeave = () => navigate("/rooms");

    const isGameOver = winnerId !== undefined;

    return (
        <Box className="page-wrapper">
            <Container>
                <Box style={{ padding: "2rem 0 1rem" }}>
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        Mahjong: {room?.name}
                    </Typography>
                </Box>

                <Card style={{ padding: 0 }}>
                    {!isGameOver && (
                        <Box style={{
                            padding: "0.75rem 1.5rem",
                            borderBottom: "1px solid var(--color-border)",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                        }}>
                            <Box>
                                {isMobile ? (
                                    <Button variant="outline" onClick={() => setIsMobilePlayersOpen(true)} style={{ padding: "0.25rem 0.75rem", display: "flex", alignItems: "center", gap: "8px" }}>
                                        <Icon src={getIcon("user")} size={18} alt="players" />
                                        <Typography variant="body" style={{ fontSize: "14px", fontWeight: 500 }}>
                                            Players ({players ? Object.keys(players).length : 0})
                                        </Typography>
                                    </Button>
                                ) : (
                                    <Typography variant="caption" style={{ opacity: 0.7 }}>
                                        {`Ready: ${readyPlayersCount ?? 0} / ${totalPlayersCount ?? 0}`}
                                    </Typography>
                                )}
                            </Box>
                            <Button variant="outline" onClick={handleLeave} style={{ padding: "0.25rem 0.75rem" }}>
                                Leave
                            </Button>
                        </Box>
                    )}

                    {isGame && (
                        <Box style={{ position: "relative", minHeight: "450px" }}>
                            
                            {deadlockSecondsLeft !== null && (
                                <DeadlockOverlay secondsLeft={deadlockSecondsLeft} />
                            )}

                            <Box style={{ padding: "1.5rem" }}>
                                <MahjongBoard
                                    tiles={tiles}
                                    removedIds={removedIds}
                                    selectedId={selectedId}
                                    disabled={awaitingResponse || gameAborted || deadlockSecondsLeft !== null}
                                    myTilesRemaining={myTilesRemaining}
                                    availableMoves={availableMoves}
                                    onTileClick={handleTileClick}
                                />
                            </Box>
                        </Box>
                    )}

                    {isGameOver && (
                        <EndGameOverlay
                            winnerId={winnerId}
                            myGuid={guid}
                            players={players}
                            onLeave={handleLeave}
                        />
                    )}

                    {!isGame && !isGameOver && (
                        <Box className="mahjong-main-content">
                            <Box className="mahjong-lobby-grid">
                                {!isMobile && (
                                    <MahjongPlayersPanel players={players} inDrawer={false} />
                                )}
                                <Box />
                                <BettingPanel
                                    players={players}
                                    balance={balance}
                                    betInput={betInput}
                                    betPlaced={betPlaced}
                                    ready={ready}
                                    isGame={isGame}
                                    playerBetMap={playerBetMap}
                                    isConnected={isConnected}
                                    gameAborted={gameAborted}
                                    onBetInputChange={(val) => {
                                        const validated = validateAmountInput(val);
                                        if (validated !== null) setBetInput(validated);
                                    }}
                                    onPlaceBet={handlePlaceBet}
                                    onReady={handleReady}
                                />
                            </Box>
                        </Box>
                    )}
                </Card>
            </Container>

            {/* Mobile Players Drawer */}
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
                                <MahjongPlayersPanel players={players} inDrawer={true} />
                            </Box>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>

            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </Box>
    );
}
