import { useNavigate, useParams } from "react-router-dom";
import { useCallback, useEffect, useState } from "react";
import { useWebSocket } from "../../hooks/useWebSocket";
import { Box, Button, Card, Container, Icon, Toast, Typography, useThemedIcon } from "../../ui";
import type { GameMessage } from "../../models/WsMessage";
import { RoomAPI } from "../../api/WsHubApi";
import { useSelector } from "react-redux";
import type { RootState } from "../../store/store";
import type { Room } from "../../models/Room";
import { validateToastMessage, validateWSMessage } from "../../utils/SecurityUtils";

export default function TicTacToeRoom() {
   const { getInverseIcon } = useThemedIcon();

   const authentication = useSelector((state: RootState) => state.auth.user);
   const navigate = useNavigate();

   const roomId: string | undefined = useParams<{ roomId?: string }>().roomId;
   const [room, setRoom] = useState<Room>();

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

   const { isConnected, message, send } = useWebSocket<GameMessage>(room?.id, room?.type);

   const fetchRoom = async (roomId: string) => {
      try {
         const response = (await RoomAPI.getRoomById(roomId));
         setRoom(response.data);
      } catch {
         setRoom(undefined);
      }
   };

   const fetchPlayers = useCallback(async () => {
      if (!roomId || !room) {
         return;
      }

      try {
         const response = (await RoomAPI.getUsernamesInRoom(roomId, room.type));

         setPlayers(response.data);
         setTotalPlayers(Object.keys(response.data).length);
      } catch (error) {
         console.info("Failed to fetch players:", error);
      }
   }, [room, roomId]);

   const fetchReadyPlayers = useCallback(async () => {
      if (!roomId || !room) {
         return;
      }

      try {
         const response = await RoomAPI.getReadyPlayers(roomId, room.type);

         setReadyCount(response.data);
      } catch (error) {
         console.info("Failed to fetch ready players:", error);
      }
   }, [room, roomId]);

   useEffect(() => {
      if (!roomId) {
         navigate("/rooms");
         return;
      }

      fetchRoom(roomId);
   }, [navigate, roomId]);

   useEffect(() => {
      if (!room) {
         return;
      }

      fetchPlayers();
      fetchReadyPlayers();
   }, [room, fetchPlayers, fetchReadyPlayers]);

   const processReset = useCallback(() => {
      showToast("Your opponent left the room. Waiting for a new player...");
      setBoard(Array(9).fill(null));
      setCurrentPlayerSymbol(undefined);
      setMySymbol(undefined);
      setPlayersWithSymbols({});
      setWinner(undefined);
      setReady(false);
      setIsGame(false);
   }, []);

   const processStart = useCallback((message: GameMessage) => {
      setBoard(message.board!);
      setCurrentPlayerSymbol(message.nextPlayerSymbol);
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

      if (authentication?.guid) {
         setMySymbol(symbolsMap[authentication.guid]);
      }

      setIsGame(true);
   }, [authentication]);

   const processMove = (message: GameMessage) => {
      setBoard(message.board!);
      setCurrentPlayerSymbol(message.nextPlayerSymbol);
   };

   const processWin = useCallback((message: GameMessage) => {
      setBoard(message.board!);
      setWinner(message.winner);

      if (message.winner === mySymbol) {
         showToast("You are the winner!");
      } else {
         showToast(`Your opponent won!`);
      }
      setIsGame(false);
   }, [mySymbol]);

   const processDraw = useCallback((message: GameMessage) => {
      setBoard(message.board!);
      setWinner(message.winner);
      showToast(message.message!);
      setIsGame(false);
   }, []);

   useEffect(() => {
      if (!isConnected || !message) {
         return;
      }

      const validatedMessage = validateWSMessage(message, [
         "type",
         "event",
         "fromUserId",
         "toUserId",
         "roomId",
         "message",
         "board",
         "cell",
         "currentPlayerSymbol",
         "nextPlayerSymbol",
         "playersSymbols",
         "players",
         "winner",
         "bet",
      ]) as GameMessage;

      switch (validatedMessage.event) {
         case "JOIN":
            fetchPlayers();
            fetchReadyPlayers();
            showToast(validatedMessage.message!);
            break;

         case "LEAVE":
            if (isGame) {
               processReset();
            } else {
               showToast(validatedMessage.message!);
            }

            fetchPlayers();
            fetchReadyPlayers();

            break;

         case "START":
            processStart(validatedMessage);
            break;

         case "READY":
            fetchReadyPlayers();
            showToast(validatedMessage.message!);
            break;

         case "MOVE":
            processMove(validatedMessage);
            break;

         case "WINNER_X":
         case "WINNER_O": {
            processWin(validatedMessage);
            break;
         }

         case "DRAW":
            processDraw(validatedMessage);
            break;

         case "BET":
            break;

         case "BET_REJECT":
            break;

         case "BET_OUTBID":
            break;

         default:
            break;
      }
   }, [isConnected, message, fetchPlayers, fetchReadyPlayers, isGame, processStart, processDraw, processReset, processWin]);

   const handleClick = (index: number) => {
      if (!authentication || !room || !isConnected || board[index] || winner || currentPlayerSymbol !== mySymbol) {
         return;
      }

      send({
         type: "USER_MESSAGE",
         event: "MOVE",
         fromUserId: authentication.guid,
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

      send({
         type: "USER_MESSAGE",
         event: "READY",
         roomId: room.id,
      });
      setReady(true);
   };

   const handleLeave = () => {
      navigate("/rooms");
   };

   const showToast = (text: string): void => {
      setToast({ text: validateToastMessage(text) })
   };

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
                  display: "grid",
                  gridTemplateColumns: "repeat(3, 1fr)",
                  alignItems: "center",
                  justifyContent: "center",
               }}>
                  <Box style={{
                     marginRight: "5rem",
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
                     disabled={ready}
                     style={{
                        margin: "2rem",
                        display: "flex",
                        alignItems: "center",
                        gap: "8px"
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
