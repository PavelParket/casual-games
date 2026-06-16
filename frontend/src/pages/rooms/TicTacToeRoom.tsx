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
import { Box, Button, Card, Container, ToastContainer, Typography } from "../../ui";
import { TicTacToePlayersPanel } from "./tictactoe/components/TicTacToePlayersPanel";
import { TicTacToeBoard } from "./tictactoe/components/TicTacToeBoard";
import { BettingPanel } from "./tictactoe/components/BettingPanel";
import { EndGameOverlay } from "./tictactoe/components/EndGameOverlay";
import "./tictactoe/styles/TicTacToeRoom.css";

export default function TicTacToeRoom() {
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
    const [winnerId, setWinnerId] = useState<string | null | undefined>(undefined);

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
        setWinnerId(message.winner);

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
        setWinnerId(message.winner);
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
        if (!guid || !room || !isConnected || board[index] || winnerId !== undefined || currentPlayerSymbol !== mySymbol || gameAborted) {
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

    const handleBetChange = (val: string) => {
        const validated = validateAmountInput(val);
        if (validated !== null) setBetInput(validated);
    };

    const handleLeave = () => {
        navigate("/rooms");
    };

    const isGameOver = winnerId !== undefined;

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
                }}>

                    {!isGameOver && (
                        <Box style={{
                            padding: "0.75rem 1.5rem",
                            borderBottom: "1px solid var(--color-border)",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "space-between",
                        }}>
                            <Typography variant="caption" style={{ opacity: 0.7 }}>
                                {`Ready: ${readyPlayersCount ?? 0} / ${totalPlayersCount ?? 0}`}
                            </Typography>
                            <Button variant="outline" onClick={handleLeave} style={{ padding: "0.25rem 0.75rem" }}>
                                Leave
                            </Button>
                        </Box>
                    )}

                    {isGameOver ? (
                        <EndGameOverlay
                            winnerId={winnerId}
                            myGuid={guid}
                            players={players}
                            onLeave={handleLeave}
                        />
                    ) : (
                        <Box className="ttt-main-content">
                            <Box className="ttt-grid">
                                <Box className="ttt-players-panel">
                                    <TicTacToePlayersPanel
                                        players={players}
                                        playersWithSymbols={playersWithSymbols}
                                        isGame={isGame}
                                    />
                                </Box>

                                <Box className="ttt-board-panel">
                                    <TicTacToeBoard
                                        board={board}
                                        isGame={isGame}
                                        gameAborted={gameAborted}
                                        winnerId={winnerId}
                                        mySymbol={mySymbol}
                                        currentPlayerSymbol={currentPlayerSymbol}
                                        onMove={handleMove}
                                    />
                                </Box>

                                <Box className="ttt-betting-panel">
                                    <BettingPanel
                                        balance={balance}
                                        betInput={betInput}
                                        betPlaced={betPlaced}
                                        ready={ready}
                                        isGame={isGame}
                                        playerBetMap={playerBetMap}
                                        isConnected={isConnected}
                                        gameAborted={gameAborted}
                                        onBetInputChange={handleBetChange}
                                        onPlaceBet={handlePlaceBet}
                                        onReady={handleReady}
                                    />
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
