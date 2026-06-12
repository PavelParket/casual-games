import { useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import type { RootState } from "../../store/store";
import { useGameToast } from "../../hooks/useGameToast";
import { useSystemToastContext } from "../../providers/SystemToastContext";
import { useSliceErrorToast } from "../../hooks/useSliceErrorToast";
import { useCallback, useRef, useState } from "react";
import { clearError } from "../../store/slices/DurakRoomSlice";
import type { CardSuit, DurakAction, DurakCard, DurakPhase, DurakTablePair } from "../../models/Durak";
import type { DurakGameMessage } from "../../models/WsMessage";
import { useDurakMessages } from "../../hooks/useDurakMessages";
import { Avatar, Box, Button, Card, Container, Stack, ToastContainer, Typography } from "../../ui";
import { DurakBoard } from "./durak/components/DurakBoard";
import { BettingPanel } from "./durak/components/BettingPanel";
import { GameOverOverlay } from "./durak/components/GameOverOverlay";
import { MiniProfile } from "../profile/components/MiniProfile";
import { useGameSocket } from "../../hooks/useGameSocket";

export type TableExitMode = "bita" | "pickup" | null;

export default function DurakRoom() {
    const navigate = useNavigate();

    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const balance = useSelector((state: RootState) => state.user.user?.balance);
    const { room, players, readyPlayersCount, totalPlayersCount, playerBetMap } = useSelector((state: RootState) => state.durakRoom);

    const roomId = useParams<{ roomId?: string }>().roomId;

    const { toasts, showGameToast, dismiss } = useGameToast();
    const { showSystemToast } = useSystemToastContext();
    useSliceErrorToast((state: RootState) => state.durakRoom.errors, clearError);

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
    const [gameAborted, setGameAborted] = useState(false);

    const myName = guid && players ? (players[guid]?.username ?? "You") : "You";
    const opponentName = guid && players
        ? (Object.values(players).find((p) => p.guid !== guid)?.username ?? "Opponent")
        : "Opponent";

    const isOpponentAttacker = !!attackerId && !!guid && attackerId !== guid;

    const handleDisplaced = useCallback(() => {
        console.warn("[DurakRoom] session displaced");
        showSystemToast("Your session was opened in another window", "system-error");
        navigate("/rooms");
    }, [navigate, showSystemToast]);

    const handleDisconnect = useCallback(() => {
        console.warn("[DurakRoom] connection lost");
        showSystemToast("Connection lost. Redirecting to rooms...", "system-error");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showSystemToast]);

    const handleSocketError = useCallback(() => {
        setAwaitingResponse(false);
    }, []);

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

    const processAbort = useCallback(() => {
        console.warn("[DurakRoom] game aborted — opponent left");
        setGameAborted(true);
        showGameToast("Opponent left the game. Redirecting to rooms...", "game-info");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showGameToast]);

    const handleMessage = useDurakMessages({
        roomId,
        isGame,
        gameAborted,
        processGameState,
        processGameOver,
        processAbort,
        setBetPlaced,
        setReady,
        setRemainingSeconds,
        setDiscardCount,
        prevTableRef,
        prevPhaseRef,
        showGameToast,
    });

    const { isConnected, send } = useGameSocket<DurakGameMessage>({
        roomId,
        roomType: room?.type,
        showGameToast,
        onGameMessage: handleMessage,
        onError: handleSocketError,
        onDisplaced: handleDisplaced,
        onConnectionLost: handleDisconnect,
    });

    const handleDealComplete = useCallback(() => {
        setIsDealAnimation(false);
    }, []);

    const handlePlayCard = useCallback((card: DurakCard) => {
        if (!room || !isConnected || !guid || awaitingResponse || gameAborted) {
            return;
        }

        console.debug("[DurakRoom] send MOVE PLAY_CARD", card);
        setAwaitingResponse(true);
        send({ type: "USER_MESSAGE", event: "MOVE", fromUserId: guid, roomId: room.id, action: "PLAY_CARD", card });
    }, [awaitingResponse, gameAborted, guid, isConnected, room, send]);

    const handlePass = useCallback(() => {
        if (!room || !isConnected || !guid || awaitingResponse || gameAborted) {
            return;
        }

        console.debug("[DurakRoom] send MOVE PASS");
        setAwaitingResponse(true);
        send({ type: "USER_MESSAGE", event: "MOVE", fromUserId: guid, roomId: room.id, action: "PASS" });
    }, [awaitingResponse, gameAborted, guid, isConnected, room, send]);

    const handleTakeCards = useCallback(() => {
        if (!room || !isConnected || !guid || awaitingResponse || gameAborted) {
            return;
        }

        console.debug("[DurakRoom] send MOVE TAKE_CARDS");
        setAwaitingResponse(true);
        send({ type: "USER_MESSAGE", event: "MOVE", fromUserId: guid, roomId: room.id, action: "TAKE_CARDS" });
    }, [awaitingResponse, gameAborted, guid, isConnected, room, send]);

    const handlePlaceBet = () => {
        if (!room || !isConnected || !guid || betPlaced || gameAborted) {
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

        console.debug("[DurakRoom] send BET");
        send({ type: "USER_MESSAGE", event: "BET", fromUserId: guid, roomId: room.id, bet: amount });
    };

    const handleReady = () => {
        if (!room || !isConnected || ready || gameAborted) {
            return;
        }

        if (!betPlaced) {
            showGameToast("You must place a bet before becoming ready!", "game-error");
            return;
        }

        console.debug("[DurakRoom] send READY");
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
                            disabled={awaitingResponse || isDealAnimation || gameAborted}
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
                                {Object.entries(players ?? {}).map(([playerGuid, player]) => (
                                    <MiniProfile
                                        key={playerGuid}
                                        guid={playerGuid}
                                        username={player.username}
                                        status={player.status}
                                        avatarUrl={player.linkProfilePictureMini}
                                        avatarUrlFull={player.linkProfilePicture}>
                                        <Stack
                                            direction="row"
                                            align="center"
                                            gap="0.75rem"
                                            style={{
                                                cursor: "pointer",
                                                width: "200px",
                                                padding: "0.35rem",
                                                paddingRight: "1rem",
                                                background: "var(--color-bg-glass)",
                                                border: "1px solid var(--color-border)",
                                                borderRadius: "var(--radius-md)",
                                                boxShadow: "var(--shadow-sm)",
                                                transition: "all 0.2s ease",
                                                userSelect: "none"
                                            }}
                                            onMouseEnter={(e) => {
                                                e.currentTarget.style.boxShadow = "var(--shadow-md)";
                                            }}
                                            onMouseLeave={(e) => {
                                                e.currentTarget.style.boxShadow = "var(--shadow-sm)";
                                            }}
                                        >
                                            <Avatar src={player.linkProfilePictureMini} fallback={player.username} size={40} />

                                            <Typography variant="body" style={{ fontWeight: "bold" }}>
                                                {player.username}
                                            </Typography>
                                        </Stack>
                                    </MiniProfile>
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
        </Box >
    );
}
