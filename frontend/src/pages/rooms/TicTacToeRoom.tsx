import { useNavigate, useParams } from "react-router-dom";
import { useCallback, useEffect, useState } from "react";
import { useWebSocket } from "../../hooks/useWebSocket";
import { Box, Button, Card, Container, Icon, Input, Toast, Typography, useThemedIcon } from "../../ui";
import type { GameMessage } from "../../models/WsMessage";
import { RoomAPI } from "../../api/WsHubApi";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { validateToastMessage } from "../../utils/SecurityUtils";
import { getRoomById } from "../../store/slices/RoomSlice";

export default function TicTacToeRoom() {
   const { getInverseIcon } = useThemedIcon();

   const guid = useSelector((state: RootState) => state.auth.user?.guid);
   const navigate = useNavigate();
   const dispatch = useDispatch<AppDispatch>();

   const roomId: string | undefined = useParams<{ roomId?: string }>().roomId;
   const room = useSelector((state: RootState) => state.rooms.room);

   const [toast, setToast] = useState<{ text: string } | null>(null);

   const [ready, setReady] = useState<boolean>(false);
   const [readyCount, setReadyCount] = useState<number>(0);
   const [totalPlayers, setTotalPlayers] = useState<number>(0);

   const [isGame, setIsGame] = useState(false);
   const [board, setBoard] = useState<string[]>(Array(9).fill(null));
   const [mySymbol, setMySymbol] = useState<string>();
   const [currentPlayerSymbol, setCurrentPlayerSymbol] = useState<string>();
   const [playersSymbols, setPlayersSymbols] = useState<Record<string, string>>();
   const [players, setPlayers] = useState<Record<string, string>>();
   const [playersWithSymbols, setPlayersWithSymbols] = useState<Record<string, string>>({});
   const [winner, setWinner] = useState<string>();

   const [betInput, setBetInput] = useState<string>("");
   const [betPlaced, setBetPlaced] = useState<boolean>(false);
   const [playerBets, setPlayerBets] = useState<Record<string, number>>({});

   const balance: number = 1000;

   useEffect(() => {
      if (!roomId) {
         navigate("/rooms");
         return;
      }

      dispatch(getRoomById({ roomId }));
   }, [dispatch, navigate, roomId]);

   const { isConnected, message, send } = useWebSocket<GameMessage>(
      roomId,
      room?.type,
   );

   const fetchPlayers = useCallback(async () => {
      if (!roomId || !room?.type) {
         return;
      }

      try {
         const response = await RoomAPI.getUsernamesInRoom(roomId, room.type);

         setPlayers(response.data);
         setTotalPlayers(Object.keys(response.data).length);
      } catch (error) {
         console.info("Failed to fetch players:", error);
      }
   }, [room?.type, roomId]);

   const fetchReadyPlayers = useCallback(async () => {
      if (!roomId || !room?.type) {
         return;
      }

      try {
         const response = await RoomAPI.getReadyPlayers(roomId, room.type);

         setReadyCount(response.data);
      } catch (error) {
         console.info("Failed to fetch ready players:", error);
      }
   }, [room?.type, roomId]);

   const fetchPlayerBets = useCallback(async () => {
      if (!roomId || !room?.type) {
         return;
      }

      try {
         console.log();
      } catch (error) {
         console.error("Failed to fetch player bets:", error);
      }
   }, [room?.type, roomId]);

   const processReset = useCallback(() => {
      showToast("Your opponent left the room. Waiting for a new player...");
      setBoard(Array(9).fill(null));
      setCurrentPlayerSymbol(undefined);
      setMySymbol(undefined);
      setPlayersWithSymbols({});
      setWinner(undefined);
      setReady(false);
      setIsGame(false);
      setBetPlaced(false);
      setBetInput("");
   }, []);

   const processStart = useCallback((message: GameMessage) => {
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

   const processMove = useCallback((message: GameMessage) => {
      if (!message.board) {
         return;
      }

      setBoard(message.board);
      setCurrentPlayerSymbol(message.nextPlayerSymbol);
   }, []);

   const processWin = useCallback((message: GameMessage) => {
      if (!message.board || !message.winner || !message.players) {
         return;
      }

      setBoard(message.board);
      setWinner(message.players[message.winner]);

      if (message.winner === mySymbol) {
         showToast("You are the winner!");
      } else {
         showToast(`Your opponent won!`);
      }

      setIsGame(false);
   }, [mySymbol]);

   const processDraw = useCallback((message: GameMessage) => {
      if (!message.board || !message.message) {
         return;
      }

      setBoard(message.board);
      setWinner(message.winner);
      showToast(message.message);
      setIsGame(false);
   }, []);

   useEffect(() => {
      if (!isConnected || !message || !guid) {
         return;
      }

      switch (message.event) {
         case "JOIN":
            showToast(message.message ?? "Player join the room");
            fetchPlayers();
            fetchReadyPlayers();
            fetchPlayerBets();
            break;

         case "LEAVE":
            if (isGame) {
               processReset();
            } else {
               showToast(message.message ?? "Player leave the room");
            }

            fetchPlayers();
            fetchReadyPlayers();
            fetchPlayerBets();
            break;

         case "START":
            processStart(message);
            break;

         case "READY":
            fetchReadyPlayers();
            showToast(message.message ?? "Player is ready");
            break;

         case "MOVE":
            processMove(message);
            break;

         case "WINNER_X":
         case "WINNER_O": {
            processWin(message);
            break;
         }

         case "DRAW":
            processDraw(message);
            break;

         case "BET":
            if (message.toUserId === guid) {
               setBetPlaced(true);
               showToast(message.message || "Your bet has been accepted!");
            } else {
               showToast(message.message || "Opponent placed a bet");
            }

            fetchPlayerBets();
            fetchReadyPlayers();
            break;

         case "BET_REJECT":
            setBetPlaced(false);
            showToast(message.message || "Your bet was rejected. Please increase your bet.");
            fetchPlayerBets();
            break;

         case "BET_OUTBID":
            setBetPlaced(false);
            setReady(false);
            showToast(message.message || "You have been outbid! Please place a new bet.");
            fetchPlayerBets();
            fetchReadyPlayers();
            break;

         case "BET_REQUIRED":
            showToast(message.message || "You must place a bet before becoming ready");
            break;

         default:
            break;
      }
   }, [isConnected, message, guid, isGame, fetchPlayers, fetchReadyPlayers, fetchPlayerBets, processStart, processReset, processMove, processDraw, processWin]);

   const handleClick = (index: number) => {
      if (!guid || !room || !isConnected || board[index] || winner || currentPlayerSymbol !== mySymbol) {
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
      if (!room || !isConnected || ready) {
         return;
      }

      if (!betPlaced) {
         showToast("You must place a bet before becoming ready!");
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
      if (!room || !isConnected || !guid) {
         return;
      }

      const betAmount = parseFloat(betInput);

      if (isNaN(betAmount) || betAmount <= 0) {
         showToast("Please enter a valid bet amount greater than 0");
         return;
      }

      if (balance && betAmount > balance) {
         showToast("Insufficient balance");
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

   const handleLeave = () => {
      navigate("/rooms");
   };

   const showToast = (text: string): void => {
      setToast({ text: validateToastMessage(text) })
   };

   // todo: Сделать нормальный компонент-страницу с сообщение о несуществующей комнате
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
      <Box style={{
         minHeight: "calc(100vh - 60px - 50px)",
         margin: "0 10rem",
         padding: "0 1rem",
         background: "var(--color-bg-glass)",
         backdropFilter: "blur(2px)",
         borderRadius: "var(--radius-md)",
         boxShadow: "var(--shadow-lg)"
      }}>
         <Container>
            <Box style={{ padding: "2rem 0" }}>
               <Typography variant="h2" style={{ textAlign: "center" }}>
                  Tic-Tac-Toe: {room.name}
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
                        : `Ready players: ${readyCount} / ${totalPlayers}`
                  }
               </Typography>

               <Box style={{
                  width: "100%",
                  display: "grid",
                  gridTemplateColumns: "repeat(3, 1fr)",
                  alignItems: "center",
                  justifyContent: "center",
               }}>
                  <Box style={{
                     display: "flex",
                     flexDirection: "column",
                     alignItems: "center",
                     justifyContent: "center",
                     rowGap: "1.5rem",
                  }}>
                     {isGame ? (
                        Object.entries(playersWithSymbols).map(([username, symbol]) => (
                           <Typography key={username} variant="h2">
                              {username}: {symbol}
                           </Typography>
                        ))
                     ) : (
                        Object.values(players || {}).map((username) => (
                           <Typography key={username} variant="h2">
                              {username}
                           </Typography>
                        ))
                     )}
                  </Box>

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
                                 onClick={() => handleClick(index)}
                                 disabled={!!cell || !!winner || !isGame}
                              >
                                 {cell}
                              </Button>
                           );
                        })}
                     </Box>
                  </Box>

                  <Box style={{
                     display: "flex",
                     flexDirection: "column",
                     alignItems: "flex-start",
                     gap: "1rem",
                     padding: "1rem",
                     background: "var(--color-bg-secondary)",
                     borderRadius: "var(--radius-md)",
                     boxShadow: "var(--shadow-md)",
                  }}>
                     {Object.keys(playerBets).length > 0 && (
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
                              {Object.entries(playerBets).map(([username, bet]) => (
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

                     {betPlaced && (
                        <Box style={{
                           display: "flex",
                           alignItems: "center",
                           gap: "0.5rem",
                           padding: "0.5rem",
                           background: "var(--color-success-bg)",
                           borderRadius: "var(--radius-sm)",
                           width: "100%"
                        }}>
                           <Icon src={getInverseIcon("check")} alt="check" size={16} />
                           <Typography variant="caption" style={{ color: "var(--color-success)" }}>
                              Bet placed: ${betInput}
                           </Typography>
                        </Box>
                     )}

                     <Box style={{ width: "100%" }}>
                        <Input
                           type="number"
                           value={betInput}
                           onChange={(e) => setBetInput(e.target.value)}
                           placeholder="Enter bet amount"
                           disabled={betPlaced || isGame}
                           style={{
                              width: "100%",
                              padding: "0.75rem",
                              borderRadius: "var(--radius-sm)",
                              border: "1px solid var(--color-border)",
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
                     disabled={ready || !betPlaced}
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

         {toast && (
            <Toast message={toast.text} onClose={() => setToast(null)} />
         )}
      </Box>
   );
}
