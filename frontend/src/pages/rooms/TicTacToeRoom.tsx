import { useNavigate, useParams } from "react-router-dom";
import { useCallback, useEffect, useRef, useState } from "react";
import { useWebSocket } from "../../hooks/useWebSocket";
import { Box, Button, Card, Container, Icon, Toast, Typography, useThemedIcon } from "../../ui";
import type { GameMessage } from "../../models/WsMessage";
import { RoomAPI } from "../../api/WsHubApi";
import { useSelector } from "react-redux";
import type { RootState } from "../../store/store";
import type { LastRoom } from "../../models/room";
import { getSecureLocalStorage, sanitizeRoomName, sanitizeToastMessage, sanitizeWSMessage } from "../../utils/SecurityUtils";

export default function TicTacToeRoom() {
   const email = useSelector((state: RootState) => state.auth.user?.email);
   const lastRoom: LastRoom | null = getSecureLocalStorage<LastRoom>("lastRoom");

   const navigate = useNavigate();

   const roomName: string = sanitizeRoomName(useParams<{ roomName?: string }>().roomName ?? "");

   const [roomType, setRoomType] = useState<string | null>(lastRoom?.type?.name ?? null);
   const [handlerUrl, setHandlerUrl] = useState<string | null>(lastRoom?.type?.handlerUrl ?? null);

   const { getInverseIcon } = useThemedIcon();

   const [toast, setToast] = useState<{ text: string }>();
   const [isGame, setIsGame] = useState(false);
   const [board, setBoard] = useState<(string)[]>(Array(9).fill(null));
   const [currentPlayer, setCurrentPlayer] = useState<string>();
   const [mySymbol, setMySymbol] = useState<string>();
   const [winner, setWinner] = useState<string>();
   const [players, setPlayers] = useState<{ name: string; symbol: string }[]>([]);
   const [ready, setReady] = useState(false);
   const [readyCount, setReadyCount] = useState(0);
   const [totalPlayers, setTotalPlayers] = useState(0);

   const emailRef = useRef(email);
   const winnerRef = useRef(winner);
   const mySymbolRef = useRef(mySymbol);

   const { isConnected, message, send } = useWebSocket<GameMessage>("ws://localhost:8081/ws", handlerUrl!, roomName!, roomType!);

   useEffect(() => {
      if (!lastRoom || !roomName) {
         navigate("/rooms");
         return;
      }

      if (lastRoom.type?.name && lastRoom.type?.name !== roomType) {
         setRoomType(lastRoom.type?.name);
      }
      if (lastRoom.type?.handlerUrl && lastRoom.type?.handlerUrl !== handlerUrl) {
         setHandlerUrl(lastRoom.type?.handlerUrl);
      }
   }, [lastRoom, roomName, roomType, handlerUrl, navigate]);

   useEffect(() => { emailRef.current = email }, [email]);
   useEffect(() => { winnerRef.current = winner }, [winner]);
   useEffect(() => { mySymbolRef.current = mySymbol }, [mySymbol]);

   const fetchPlayers = useCallback(async () => {
      if (!roomName || !roomType) {
         return;
      }

      try {
         const response = await RoomAPI.getPlayersInRoom(roomName, roomType);
         const data = response.data;

         setPlayers(data.map((player: string) => ({ name: player, symbol: "" })));
         setTotalPlayers(data.length);
      } catch (error) {
         console.info("Failed to fetch players:", error);
      }
   }, [roomName, roomType]);

   const fetchReadyPlayers = useCallback(async () => {
      if (!roomName || !roomType) {
         return;
      }

      try {
         const response = await RoomAPI.getReadyPlayers(roomName, roomType);
         const data = response.data;

         setReadyCount(data);
      } catch (error) {
         console.info("Failed to fetch ready players:", error);
      }
   }, [roomName, roomType]);

   const processReset = useCallback(() => {
      showToast("Your opponent left the room. Waiting for a new player...");
      setBoard(Array(9).fill(null));
      setCurrentPlayer();
      setMySymbol(null);
      setWinner(null);
      setReady(false);
      setIsGame(false);
   }, []);

   const processStart = useCallback((message: GameMessage) => {
      setBoard(message.board!);
      setCurrentPlayer(message.nextPlayer!);
      setMySymbol(message.playersSymbols![emailRef.current!]);
      setPlayers(prev =>
         prev.map(p => ({
            ...p,
            symbol: message.playersSymbols![p.name] ?? "",
         }))
      );
      setIsGame(true);
   }, []);

   const processMove = (message: GameMessage) => {
      setBoard(message.board!);
      setCurrentPlayer(message.nextPlayer!);
   };

   const processWin = useCallback((message: GameMessage) => {
      setBoard(message.board!);
      setWinner(message.player!);

      if (winnerRef.current === mySymbolRef.current) {
         showToast("You are the winner!");
      } else {
         showToast(`Your opponent won!`);
      }
      setIsGame(false);
   }, []);

   const processDraw = useCallback((message: GameMessage) => {
      setBoard(message.board!);
      setWinner(message.winner!);
      showToast(message.message!);
      setIsGame(false);
   }, []);

   useEffect(() => {
      if (!isConnected || !message) {
         return;
      }

      const sanitizedMessage = sanitizeWSMessage(message, [
         "type",
         "event",
         "message",
         "board",
         "nextPlayer",
         "player",
         "playersSymbols",
         "winner"
      ]) as GameMessage;

      switch (sanitizedMessage.event) {
         case "joined":
            fetchPlayers();
            fetchReadyPlayers();
            showToast(sanitizedMessage.message!);
            break;

         case "left":
            if (isGame) {
               processReset();
            } else {
               showToast(sanitizedMessage.message!);
            }

            fetchPlayers();
            fetchReadyPlayers();

            break;

         case "ready":
            fetchReadyPlayers();
            showToast(sanitizedMessage.message!);
            break;

         case "start":
            processStart(sanitizedMessage);
            break;

         case "move":
            processMove(sanitizedMessage);
            break;

         case "winner X":
         case "winner O": {
            processWin(sanitizedMessage);
            break;
         }

         case "draw":
            processDraw(sanitizedMessage);
            break;

         default:
            break;
      }
   }, [isConnected, message, fetchPlayers, fetchReadyPlayers, isGame, processStart, processDraw, processReset, processWin]);

   const handleClick = (index: number) => {
      if (!isConnected || board[index] || winner || currentPlayer !== mySymbol) {
         return;
      }

      send({
         type: "message",
         event: "move",
         fromUserId: email,
         roomName: roomName!,
         board: board,
         cell: index,
         player: mySymbol!,
      });
   };

   const handleReady = () => {
      if (!isConnected || ready) {
         return;
      }

      send({ type: "message", event: "ready", roomName: roomName! });
      setReady(true);
   };

   const handleLeave = () => {
      localStorage.removeItem("lastRoom");
      localStorage.removeItem("action");
      navigate("/rooms");
   };

   const showToast = (text: string): void => {
      setToast({ text: sanitizeToastMessage(text) })
   };

   if (!roomName) {
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
                  Tic-Tae-Toe: {roomName}
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
                        ? `Turn: ${currentPlayer}`
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
                     {players.map((User, index) => (
                        <Typography key={index} variant="h2">
                           {User.name}: {User.symbol}
                        </Typography>
                     ))}
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
