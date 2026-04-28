import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import type { AppDispatch, RootState } from "../../store/store";
import { useGameToast } from "../../hooks/useGameToast";
import { useSystemToastContext } from "../../providers/SystemToastContext";
import { useSliceErrorToast } from "../../hooks/useSliceErrorToast";
import { useCallback, useEffect, useRef, useState } from "react";
import { clearError, getRoomById } from "../../store/slices/DurakRoomSlice";
import type { CardSuit, DurakAction, DurakCard, DurakPhase, DurakTablePair } from "../../models/Durak";
import { findByGuid } from "../../store/slices/UserSlice";
import { MAX_RECONNECT_ATTEMPTS, useWebSocket } from "../../hooks/useWebSocket";
import type { DurakGameMessage } from "../../models/WsMessage";
import { useDurakMessages } from "../../hooks/useDurakMessages";
import LoadingPage from "../LoadingPage";
import InvalidRoomPage from "./InvalidRoomPage";
import type { ErrorResponse } from "../../helpers/ApiErrorHelper";
import { Box, Button, Card, Container, Stack, ToastContainer, Typography } from "../../ui";
import { DurakBoard } from "./durak/components/DurakBoard";
import { BettingPanel } from "./durak/components/BettingPanel";
import { GameOverOverlay } from "./durak/components/GameOverOverlay";

export type TableExitMode = "bita" | "pickup" | null;

export default function DurakRoom() {
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();

    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const balance = useSelector((state: RootState) => state.user.user?.balance);
    const { room, players, readyPlayersCount, totalPlayersCount, playerBetMap } = useSelector((state: RootState) => state.durakRoom);

    const roomId = useParams<{ roomId?: string }>().roomId;

    const { toasts, showGameToast, dismiss } = useGameToast();
    const { showSystemToast } = useSystemToastContext();
    useSliceErrorToast((state: RootState) => state.durakRoom.errors, clearError);

    const [isLoading, setIsLoading] = useState(true);
    const [roomError, setRoomError] = useState<ErrorResponse | null>(null);

    const [ready, setReady] = useState(false);
    const [betInput, setBetInput] = useState("");
    const [betPlaced, setBetPlaced] = useState(false);

    const [isGame, setIsGame] = useState(false);
    const [phase, setPhase] = useState<DurakPhase | null>(null);
    const [myCards, setMyCards] = useState<DurakCard[]>([]);
    const [opponentCardCount, setOpponentCardCount] = useState(0);
    const [deckCardsLeft, setDeckCardsLeft] = useState(0);
    const [trumpCard, setTrumpCard] = useState<DurakCard | null>(null);
    const [trumpSuit, setTrumpSuit] = useState<CardSuit | null>(null);
    const [table, setTable] = useState<DurakTablePair[]>([]);
    const [isMyTurn, setIsMyTurn] = useState(false);
    const [availableActions, setAvailableActions] = useState<DurakAction[]>([]);
    const [remainingSeconds, setRemainingSeconds] = useState<number | null>(null);
    const [discardCount, setDiscardCount] = useState(0);
    const [winnerId, setWinnerId] = useState<string | null | undefined>(undefined);
    const [attackerId, setAttackerId] = useState<string | null>(null);
    const [tableExitMode, setTableExitMode] = useState<TableExitMode>(null);

    const [isDealAnimation, setIsDealAnimation] = useState(false);

    const wasGameRef = useRef(false);

    const prevTableRef = useRef<DurakTablePair[]>([]);
    const prevPhaseRef = useRef<DurakPhase | null>(null);

    const [awaitingResponse, setAwaitingResponse] = useState(false);

    const myName = guid && players ? (players[guid] ?? "You") : "You";
    const opponentName = guid && players
        ? (Object.entries(players).find(([g]) => g !== guid)?.[1] ?? "Opponent")
        : "Opponent";

    const isOpponentAttacker = !!attackerId && !!guid && attackerId !== guid;

    useEffect(() => {
        if (!roomId || !guid) {
            navigate("/rooms");
            return;
        }

        setIsLoading(true);
        setRoomError(null);

        Promise.all([
            dispatch(getRoomById({ roomId })),
            dispatch(findByGuid(guid)),
        ])
            .then(([roomResult]) => {
                if (getRoomById.rejected.match(roomResult)) {
                    setRoomError(roomResult.payload ?? { message: "Failed to fetch room" });
                }
            })
            .finally(() => setIsLoading(false));
    }, [dispatch, guid, navigate, roomId]);

    const handleDisplaced = useCallback(() => {
        showSystemToast("Your session was opened in another window", "system-error");
        navigate("/rooms");
    }, [navigate, showSystemToast]);

    const handleDisconnect = useCallback(() => {
        showSystemToast("Connection lost. Redirecting to rooms...", "system-error");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showSystemToast]);

    const { isConnected, message, send, reconnectAttempt } = useWebSocket<DurakGameMessage>(
        roomId,
        room?.type,
        handleDisconnect,
        handleDisplaced,
    );

    useEffect(() => {
        if (reconnectAttempt > 0) {
            showSystemToast(
                `Connection lost. Reconnecting... (${reconnectAttempt}/${MAX_RECONNECT_ATTEMPTS})`,
                "system-error"
            );
        }
    }, [reconnectAttempt, showSystemToast]);

    const processGameState = useCallback((msg: DurakGameMessage) => {
        const prevTable = prevTableRef.current;
        const prevPhase = prevPhaseRef.current;

        prevTableRef.current = msg.table ?? [];
        prevPhaseRef.current = msg.phase ?? null;

        const newTable = msg.table ?? [];

        if (prevTable.length > 0 && newTable.length === 0) {
            setTableExitMode(prevPhase === "PICKING_UP" ? "pickup" : "bita");
        } else {
            setTableExitMode(null);
        }

        if (!wasGameRef.current) {
            wasGameRef.current = true;
            setIsDealAnimation(true);
        }

        setRemainingSeconds(null);
        setIsGame(true);
        setPhase(msg.phase ?? null);
        setMyCards(msg.myCards ?? []);
        setOpponentCardCount(msg.opponentCardCount ?? 0);
        setDeckCardsLeft(msg.deckCardsLeft ?? 0);
        setTrumpCard(msg.trumpCard ?? null);
        setTrumpSuit(msg.trumpSuit ?? null);
        setTable(newTable);
        setIsMyTurn(msg.isMyTurn ?? false);
        setAvailableActions(msg.availableActions ?? []);
        setAttackerId(msg.attackerId ?? null);
        setAwaitingResponse(false);
    }, []);

    const processGameOver = useCallback((winnerGuid: string | undefined) => {
        setWinnerId(winnerGuid ?? null);
        setIsGame(false);
        setAvailableActions([]);
        setAwaitingResponse(false);
    }, []);

    const processReset = useCallback(() => {
        showGameToast("Your opponent left the room. Waiting for a new player...", "game-info");
        setIsGame(false);
        setPhase(null);
        setMyCards([]);
        setOpponentCardCount(0);
        setDeckCardsLeft(0);
        setTrumpCard(null);
        setTrumpSuit(null);
        setTable([]);
        setIsMyTurn(false);
        setAvailableActions([]);
        setRemainingSeconds(null);
        setDiscardCount(0);
        setWinnerId(undefined);
        setAttackerId(null);
        setTableExitMode(null);
        setIsDealAnimation(false);
        wasGameRef.current = false;
        setReady(false);
        setBetPlaced(false);
        setBetInput("");
        prevTableRef.current = [];
        prevPhaseRef.current = null;
    }, [showGameToast]);

    const handleDealComplete = useCallback(() => {
        setIsDealAnimation(false);
    }, []);

    useDurakMessages({
        message,
        isConnected,
        guid,
        roomId,
        room,
        isGame,
        processGameState,
        processGameOver,
        processReset,
        setBetPlaced,
        setReady,
        setRemainingSeconds,
        setAwaitingResponse,
        setDiscardCount,
        prevTableRef,
        prevPhaseRef,
        showGameToast,
        showSystemToast,
        dispatch,
    });

    const handlePlayCard = useCallback((card: DurakCard) => {
        if (!room || !isConnected || !guid || awaitingResponse) {
            return;
        }

        setAwaitingResponse(true);
        send({ type: "USER_MESSAGE", event: "MOVE", fromUserId: guid, roomId: room.id, action: "PLAY_CARD", card });
    }, [awaitingResponse, guid, isConnected, room, send]);

    const handlePass = useCallback(() => {
        if (!room || !isConnected || !guid || awaitingResponse) {
            return;
        }

        setAwaitingResponse(true);
        send({ type: "USER_MESSAGE", event: "MOVE", fromUserId: guid, roomId: room.id, action: "PASS" });
    }, [awaitingResponse, guid, isConnected, room, send]);

    const handleTakeCards = useCallback(() => {
        if (!room || !isConnected || !guid || awaitingResponse) {
            return;
        }

        setAwaitingResponse(true);
        send({ type: "USER_MESSAGE", event: "MOVE", fromUserId: guid, roomId: room.id, action: "TAKE_CARDS" });
    }, [awaitingResponse, guid, isConnected, room, send]);

    const handlePlaceBet = () => {
        if (!room || !isConnected || !guid || betPlaced) {
            return;
        }

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
        if (!room || !isConnected || ready) {
            return;
        }

        if (!betPlaced) {
            showGameToast("You must place a bet before becoming ready!", "game-error");
            return;
        }

        send({ type: "USER_MESSAGE", event: "READY", roomId: room.id });
        setReady(true);
    };

    const handleLeave = () => navigate("/rooms");

    if (isLoading) {
        return <LoadingPage />;
    }

    if (roomError) {
        return <InvalidRoomPage message={roomError.message} />;
    }

    const isGameOver = winnerId !== undefined;

    return (
        <Box style={{
            minHeight: "calc(100vh - 60px - 50px)",
            margin: "0 10rem",
            padding: "0 1rem",
            background: "var(--color-bg-glass)",
            backdropFilter: "blur(2px)",
            borderRadius: "var(--radius-md)",
            boxShadow: "var(--shadow-lg)",
        }}>
            <Container>
                <Box style={{ padding: "2rem 0 1rem" }}>
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        Durak: {room?.name}
                    </Typography>
                </Box>

                <Card style={{ padding: 0 }}>
                    <Box style={{
                        padding: "0.75rem 1.5rem",
                        borderBottom: "1px solid var(--color-border)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                    }}>
                        <Typography variant="caption" style={{ opacity: 0.7 }}>
                            {isGame
                                ? `Phase: ${phase ?? "..."}`
                                : `Ready: ${readyPlayersCount ?? 0} / ${totalPlayersCount ?? 0}`
                            }
                        </Typography>
                        <Button variant="outline" onClick={handleLeave} style={{ padding: "0.25rem 0.75rem" }}>
                            Leave
                        </Button>
                    </Box>

                    {isGame && (
                        <DurakBoard
                            myCards={myCards}
                            opponentCardCount={opponentCardCount}
                            deckCardsLeft={deckCardsLeft}
                            trumpCard={trumpCard}
                            trumpSuit={trumpSuit}
                            table={table}
                            phase={phase}
                            isMyTurn={isMyTurn}
                            availableActions={availableActions}
                            remainingSeconds={remainingSeconds}
                            discardCount={discardCount}
                            playerName={myName}
                            opponentName={opponentName}
                            isOpponentAttacker={isOpponentAttacker}
                            tableExitMode={tableExitMode}
                            isDealAnimation={isDealAnimation}
                            onDealComplete={handleDealComplete}
                            onPlayCard={handlePlayCard}
                            onPass={handlePass}
                            onTakeCards={handleTakeCards}
                            disabled={awaitingResponse || isDealAnimation}
                        />
                    )}

                    {isGameOver && (
                        <GameOverOverlay
                            winnerId={winnerId}
                            myGuid={guid}
                            players={players}
                            onLeave={handleLeave}
                        />
                    )}

                    {!isGame && !isGameOver && (
                        <Box style={{
                            display: "grid",
                            gridTemplateColumns: "1fr 1fr 1fr",
                            alignItems: "start",
                            padding: "1.5rem",
                            gap: "1.5rem",
                        }}>
                            <Stack gap="1rem" align="center" justify="center" style={{ paddingTop: "1rem" }}>
                                <Typography variant="h3">Players</Typography>
                                {Object.values(players ?? {}).map(username => (
                                    <Typography key={username} variant="body">{username}</Typography>
                                ))}
                            </Stack>
                            <Box />
                            <BettingPanel
                                players={players}
                                balance={balance}
                                betInput={betInput}
                                betPlaced={betPlaced}
                                ready={ready}
                                playerBetMap={playerBetMap}
                                isConnected={isConnected}
                                onBetInputChange={setBetInput}
                                onPlaceBet={handlePlaceBet}
                                onReady={handleReady}
                            />
                        </Box>
                    )}
                </Card>
            </Container>

            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </Box>
    );
}
