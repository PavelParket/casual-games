import { useNavigate, useParams } from "react-router-dom";
import { useCallback, useEffect, useState } from "react";
import { useWebSocket } from "../../hooks/useWebSocket";
import { Box, Button, Card, Container, Icon, Toast, Typography, useThemedIcon } from "../../ui";
import type { GameMessage } from "../../types/ws";
import { RoomAPI } from "../../api/WsHubApi";
import { useSelector } from "react-redux";
import type { RootState } from "../../store/store";

export default function TicTacToeRoom() {
   const user = useSelector((state: RootState) => state.auth.user);
   const { roomName } = useParams<string>();
   const navigate = useNavigate();
   const { getInverseIcon } = useThemedIcon();

   const { isConnected, message, send } = useWebSocket<GameMessage>("ws://localhost:8081/ws/game", roomName ?? "");

   const [board, setBoard] = useState<(string | null)[]>(Array(9).fill(null));
   const [currentPlayer, setCurrentPlayer] = useState<string | null>(null);
   const [mySymbol, setMySymbol] = useState<string | null>(null);
   const [winner, setWinner] = useState<string | null>(null);
   const [players, setPlayers] = useState<{ name: string; symbol: string }[]>([]);
   const [ready, setReady] = useState(false);
   const [toast, setToast] = useState<{ text: string } | null>(null);

   const fetchPlayers = useCallback(async () => {
      try {
         const response = await RoomAPI.getPlayersInRoom(roomName!);
         const data = response.data;

         setPlayers(data.map(player => ({ name: player, symbol: "" })));
      } catch (error) {
         console.error("Failed to fetch players:", error);
      }
   }, [roomName]);

   useEffect(() => {
      if (isConnected && message) {
         switch (message.event) {
            case "joined":
               fetchPlayers();
               showToast(message.content!);
               break;

            case "left":
               fetchPlayers();
               showToast(message.content!);
               break;

            case "ready":
               showToast(message.content!);
               break;

            case "start":
               if (message.board)
                  setBoard(message.board);

               if (message.nextPlayer)
                  setCurrentPlayer(message.nextPlayer);

               if (message.playersSymbols) {
                  if (user?.email && message.playersSymbols[user.email]) {
                     setMySymbol(message.playersSymbols[user.email]);
                  } else {
                     console.log("Email not found", user?.email, message.playersSymbols);
                  }

                  setPlayers(prev =>
                     prev.map(p => ({
                        ...p,
                        symbol: message.playersSymbols![p.name] ?? "",
                     }))
                  );
               }

               break;

            case "move":
               if (message.board)
                  setBoard(message.board);
               if (message.nextPlayer)
                  setCurrentPlayer(message.nextPlayer);
               if (message.winner !== undefined && message.winner !== null)
                  setWinner(message.winner);

               break;

            case "winner X":
            case "winner O": {
               setWinner(message.player!);

               if (winner === mySymbol) {
                  showToast("You are the winner!");
               } else {
                  showToast(`Your oponent has won!`);
               }

               break;
            }

            case "draw":
               setWinner("draw");
               showToast(message.content!);

               break;

            default:
               break;
         }
      }
   }, [fetchPlayers, isConnected, message, mySymbol, user?.email, winner]);

   const handleClick = (index: number) => {
      if (!isConnected || board[index] || winner || currentPlayer !== mySymbol) {
         return;
      }

      send({
         type: "message",
         event: "move",
         fromUserId: user?.email,
         roomName: roomName,
         board: board,
         cell: index,
         player: mySymbol!,
      });
   };

   const handleReady = () => {
      if (!isConnected || ready) {
         return;
      }

      send({ type: "message", event: "ready", roomName: roomName });
      setReady(true);
   };

   const handleLeave = () => {
      navigate("/rooms");
   };

   const showToast = (text: string) => {
      setToast({ text });
   };

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
                  Tic-Tae-Toe
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
                     : `Turn: ${currentPlayer}`
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
                     display: "grid",
                     gridTemplateColumns: "repeat(3, 80px)",
                     gridTemplateRows: "repeat(3, 80px)",
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
                              disabled={!!cell || !!winner}
                           >
                              {cell}
                           </Button>
                        );
                     })}
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