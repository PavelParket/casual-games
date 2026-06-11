import { useCallback, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useSelector } from "react-redux";
import type { RootState } from "../../store/store";
import type { TicTacToeGameMessage } from "../../models/WsMessage";
import { validateToastMessage, validateAmountInput } from "../../utils/SecurityUtils";
import { clearError } from "../../store/slices/TicTacToeRoomSlice";
import { useGameToast } from "../../hooks/useGameToast";
import { useSystemToastContext } from "../../providers/SystemToastContext";
import { useGameSocket } from "../../hooks/useGameSocket";
import { useTicTacToeMessages } from "../../hooks/useTicTacToeMessages";
import { useSliceErrorToast } from "../../hooks/useSliceErrorToast";
import { MiniProfile } from "../profile/components/MiniProfile";
import { Avatar, Box, Button, Card, Container, Icon, FormField, Stack, ToastContainer, Typography, useThemedIcon } from "../../ui";

export default function TicTacToeRoom() {
    const { getInverseIcon } = useThemedIcon();

    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const balance = useSelector((state: RootState) => state.user.user?.balance);
    const { room, players, readyPlayersCount, totalPlayersCount, playerBetMap } = useSelector((state: RootState) => state.ticTacToeRoom);

    const navigate = useNavigate();

    const roomId: string | undefined = useParams<{ roomId?: string }>().roomId;

    const [ready, setReady] = useState<boolean>(false);
    const [gameAborted, setGameAborted] = useState(false);

    const [isGame, setIsGame] = useState(false);
    const [board, setBoard] = useState<string[]>(Array(9).fill(null));
    const [mySymbol, setMySymbol] = useState<string>();
    const [currentPlayerSymbol, setCurrentPlayerSymbol] = useState<string>();
    const [playersSymbols, setPlayersSymbols] = useState<Record<string, string>>();
    const [playersWithSymbols, setPlayersWithSymbols] = useState<Record<string, string>>({});
    const [winner, setWinner] = useState<string>();

    const [betInput, setBetInput] = useState<string>("");
    const [betPlaced, setBetPlaced] = useState<boolean>(false);

    const { toasts, showGameToast, dismiss } = useGameToast();
    const { showSystemToast } = useSystemToastContext();

    useSliceErrorToast((state: RootState) => state.ticTacToeRoom.errors, clearError);

    const handleDisplaced = useCallback(() => {
        showSystemToast("Your session was opened in another window", "system-error");
        navigate("/rooms");
    }, [navigate, showSystemToast]);

    const handleDisconnect = useCallback(() => {
        showSystemToast("Connection lost. Redirecting to rooms...", "system-error");
        setTimeout(() => navigate("/rooms"), 5000);
    }, [navigate, showSystemToast]);

    const processStart = useCallback((message: TicTacToeGameMessage) => {
        setBoard(message.board!);
        setCurrentPlayerSymbol(message.currentPlayerSymbol);
        setPlayersSymbols(message.playersSymbols);

        const playersMap = message.players || {};
        const symbolsMap = message.playersSymbols || {};
        const combinedMap: Record<string, string> = {};

        Object.keys(playersMap).forEach(guid => {
            const username = playersMap[guid];
            const symbol = symbolsMap[guid];
            if (username && symbol) {
                combinedMap[username] = symbol;
            }
        });

        setPlayersWithSymbols(combinedMap);

        if (guid) {
            setMySymbol(symbolsMap[guid]);
        }

        setIsGame(true);
    }, [guid]);

    const processMove = useCallback((message: TicTacToeGameMessage) => {
        if (!message.board) {
            return;
        }

        setBoard(message.board);
        setCurrentPlayerSymbol(message.nextPlayerSymbol);
    }, []);

    const processWin = useCallback((message: TicTacToeGameMessage) => {
        if (!message.board || !message.winner || !message.players) {
            return;
        }

        setBoard(message.board);
        setWinner(message.players[message.winner]);

        if (message.winner === guid) {
            showGameToast("You are the winner!", "game-info");
        } else {
            showGameToast("Your opponent won!", "game-info");
        }

        setIsGame(false);
    }, [guid, showGameToast]);

    const processDraw = useCallback((message: TicTacToeGameMessage) => {
        if (!message.board || !message.message) {
            return;
        }

        setBoard(message.board);
        setWinner(message.winner);
        showGameToast(validateToastMessage(message.message), "game-info");
        setIsGame(false);
    }, [showGameToast]);

    const processAbort = useCallback(() => {
        console.warn("[TicTacToeRoom] game aborted — opponent left");
        setGameAborted(true);
        showGameToast("Opponent left the game. Redirecting to rooms...", "game-info");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showGameToast]);

    const handleMessage = useTicTacToeMessages({
        roomId,
        isGame,
        gameAborted,
        processStart,
        processMove,
        processWin,
        processDraw,
        processAbort,
        setBetPlaced,
        setReady,
        showGameToast,
    });

    const { isConnected, send } = useGameSocket<TicTacToeGameMessage>({
        roomId,
        roomType: room?.type,
        showGameToast,
        onGameMessage: handleMessage,
        onDisplaced: handleDisplaced,
        onConnectionLost: handleDisconnect,
    });

    const handleMove = (index: number) => {
        if (!guid || !room || !isConnected || board[index] || winner || currentPlayerSymbol !== mySymbol || gameAborted) {
            return;
        }

        send({
            type: "USER_MESSAGE",
            event: "MOVE",
            fromUserId: guid,
            roomId: room.id,
            board: board,
            cell: index,
            currentPlayerSymbol: mySymbol,
            playersSymbols,
        });
    };

    const handleReady = () => {
        if (!room || !isConnected || ready || gameAborted) {
            return;
        }

        if (!betPlaced) {
            showGameToast("You must place a bet before becoming ready!", "game-error");
            return;
        }

        send({
            type: "USER_MESSAGE",
            event: "READY",
            roomId: room.id,
        });

        setReady(true);
    };

    const handlePlaceBet = () => {
        if (!room || !isConnected || !guid || gameAborted) {
            return;
        }

        const betAmount = parseFloat(betInput);

        if (isNaN(betAmount) || betAmount <= 0) {
            showGameToast("Please enter a valid bet amount greater than 0", "game-error");
            return;
        }

        if (balance && betAmount > balance) {
            showGameToast("Insufficient balance", "game-error");
            return;
        }

        send({
            type: "USER_MESSAGE",
            event: "BET",
            fromUserId: guid,
            roomId: room.id,
            bet: betAmount,
        });
    };

    const handleBetChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const validated = validateAmountInput(e.target.value);
        if (validated !== null) {
            setBetInput(validated);
        }
    };

    const handleLeave = () => {
        navigate("/rooms");
    };

    return (
        <Box className="page-wrapper">
            <Container>
                <Box style={{ padding: "2rem 0" }}>
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        Tic-Tac-Toe: {room?.name}
                    </Typography>
                </Box>

                <Card style={{
                    padding: "0",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center"
                }}>
                    <Typography variant="h3" style={{ margin: "2rem 0" }}>
                        {winner
                            ? winner === "Draw"
                                ? "Draw!"
                                : `Winner: ${winner}`
                            : isGame
                                ? `Turn: ${currentPlayerSymbol}`
                                : `Ready players: ${readyPlayersCount} / ${totalPlayersCount}`
                        }
                    </Typography>

                    <Box style={{
                        width: "100%",
                        display: "grid",
                        gridTemplateColumns: "repeat(3, 1fr)",
                        alignItems: "center",
                        justifyContent: "center",
                    }}>

                        {/* ===== PLAYERS ===== */}
                        <Box style={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            justifyContent: "center",
                            rowGap: "1.5rem",
                        }}>
                            {Object.entries(players ?? {}).map(([playerGuid, player]) => {
                                const symbol = isGame && playersWithSymbols ? playersWithSymbols[player.username] : null;

                                return (
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
                                                minWidth: "200px",
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
                                                {player.username}{symbol ? `: ${symbol}` : ""}
                                            </Typography>
                                        </Stack>
                                    </MiniProfile>
                                );
                            })}
                        </Box>

                        {/* ===== BOARD ===== */}
                        <Box style={{
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                        }}>
                            <Box style={{
                                display: "grid",
                                gridTemplateColumns: "repeat(3, 80px)",
                                gridTemplateRows: "repeat(3, 80px)",
                                justifyContent: "center",
                                borderRadius: "var(--radius-lg)",
                                overflow: "hidden",
                                boxShadow: "var(--shadow-lg)",
                            }}>
                                {board.map((cell, index) => {
                                    const style: React.CSSProperties = {
                                        width: "80px",
                                        height: "80px",
                                        fontSize: "32px",
                                        fontWeight: "bold",
                                        borderRadius: "0",
                                        borderRight: "none",
                                        borderBottom: "none",
                                        boxShadow: "0 0 0 var(--color-bg)",
                                    };

                                    if (index % 3 !== 2)
                                        style.borderRight = "2px solid var(--color-text)";
                                    if (index < 6)
                                        style.borderBottom = "2px solid var(--color-text)";

                                    return (
                                        <Button
                                            key={index}
                                            variant="ghost"
                                            style={style}
                                            onClick={() => handleMove(index)}
                                            disabled={!!cell || !!winner || !isGame || gameAborted}
                                        >
                                            {cell}
                                        </Button>
                                    );
                                })}
                            </Box>
                        </Box>

                        {/* ===== BETS ===== */}
                        <Box style={{
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "flex-start",
                            gap: "1rem",
                            marginRight: "1.5rem",
                            padding: "1rem",
                            background: "var(--color-bg-secondary)",
                            borderRadius: "var(--radius-md)",
                            boxShadow: "var(--shadow-md)",
                        }}>
                            {playerBetMap && Object.keys(playerBetMap).length > 0 && (
                                <>
                                    <Typography variant="h3">Current Bets</Typography>
                                    <Box style={{
                                        width: "100%",
                                        display: "flex",
                                        flexDirection: "column",
                                        gap: "0.5rem",
                                        padding: "0.75rem",
                                        background: "var(--color-bg)",
                                        borderRadius: "var(--radius-sm)",
                                        border: "1px solid var(--color-border)",
                                    }}>
                                        {Object.entries(playerBetMap).map(([username, bet]) => (
                                            <Box key={username} style={{
                                                display: "flex",
                                                justifyContent: "space-between",
                                                alignItems: "center"
                                            }}>
                                                <Typography variant="body" style={{ fontWeight: 500 }}>
                                                    {username}
                                                </Typography>
                                                <Typography variant="body" style={{ color: "var(--color-success)" }}>
                                                    ${bet}
                                                </Typography>
                                            </Box>
                                        ))}
                                    </Box>

                                    <Box style={{
                                        width: "100%",
                                        height: "1px",
                                        background: "var(--color-border)",
                                        margin: "0.5rem 0"
                                    }} />
                                </>
                            )}

                            <Typography variant="h3">Place Your Bet</Typography>

                            {balance !== undefined && (
                                <Typography variant="body" style={{ color: "var(--color-text-secondary)" }}>
                                    Balance: ${balance.toFixed(2)}
                                </Typography>
                            )}

                            <Box style={{ width: "100%" }}>
                                <FormField
                                    type="text"
                                    inputMode="decimal"
                                    value={betInput}
                                    onChange={handleBetChange}
                                    placeholder="Enter bet amount"
                                    disabled={betPlaced || isGame}
                                    style={{
                                        width: "100%",
                                        background: betPlaced || isGame ? "var(--color-bg-disabled)" : "var(--color-bg)",
                                        color: "var(--color-text)",
                                        fontSize: "1rem",
                                        opacity: betPlaced || isGame ? 0.6 : 1,
                                    }}
                                />
                            </Box>

                            <Button
                                onClick={handlePlaceBet}
                                disabled={betPlaced || isGame}
                                style={{
                                    width: "100%",
                                    opacity: (betPlaced || isGame) ? 0.5 : 1,
                                }}
                            >
                                Place Bet
                            </Button>

                            <Typography
                                variant="caption"
                                style={{
                                    color: "var(--color-text-secondary)",
                                    fontSize: "0.875rem",
                                    lineHeight: "1.4"
                                }}
                            >
                                {betPlaced
                                    ? "Your bet has been accepted. You can now get ready!"
                                    : "You must place a bet before becoming ready"
                                }
                            </Typography>
                        </Box>
                    </Box>

                    <Box style={{
                        width: "100%",
                        display: "grid",
                        gridTemplateColumns: "repeat(3, 1fr)",
                        alignItems: "center",
                        justifyItems: "center",
                    }}>
                        <Button variant="outline" onClick={handleLeave}>Leave</Button>

                        <Button
                            onClick={handleReady}
                            disabled={ready || !betPlaced || gameAborted}
                            style={{
                                margin: "2rem",
                                display: "flex",
                                alignItems: "center",
                                gap: "8px",
                                opacity: (!betPlaced || ready) ? 0.5 : 1,
                            }}
                        >
                            {ready ? (
                                <>
                                    <Typography variant="body" inverse style={{ fontSize: "20px", fontWeight: 500 }}>Ready</Typography>
                                    <Icon src={getInverseIcon("check")} alt="check" size={20} />
                                </>
                            ) : (
                                <Typography variant="body" inverse style={{ fontSize: "20px", fontWeight: 500 }}>Get Ready</Typography>
                            )}
                        </Button>
                    </Box>
                </Card>
            </Container>

            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </Box>
    );
}
