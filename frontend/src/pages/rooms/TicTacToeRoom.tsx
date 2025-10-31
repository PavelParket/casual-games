import { useNavigate, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { useWebSocket } from "../../hooks/useWebSocket";
import { Box, Button, Card, Container, Icon, Typography, useThemedIcon } from "../../ui";
import type { GameMessage } from "../../types/ws";

export default function TicTacToeRoom() {
   const roomName = useParams<{ roomId: string }>();
   const navigate = useNavigate();
   const { getInverseIcon } = useThemedIcon();

   const { connected, send, subscribe } = useWebSocket<GameMessage>(roomName.roomId);

   const [board, setBoard] = useState<(string | null)[]>(Array(9).fill(null));
   const [currentPlayer, setCurrentPlayer] = useState<string | null>(null);
   const [mySymbol, setMySymbol] = useState<string | null>(null);
   const [winner, setWinner] = useState<string | null>(null);
   const [players, setPlayers] = useState<{ name: string; symbol: string }[]>([]);
   const [ready, setReady] = useState(false);

   useEffect(() => {
      const unsubscribe = subscribe((message: GameMessage) => {
         switch (message.type) {
            case "start":
               if (message.board)
                  setBoard(message.board.flat() as (string | null)[]);
               if (message.nextPlayer)
                  setCurrentPlayer(message.nextPlayer);
               if (message.player)
                  setMySymbol(message.player);
               if (message.playersSymbols) {
                  const playersList = Object.entries(message.playersSymbols).map(([name, symbol]) => ({
                     name,
                     symbol,
                  }));
                  setPlayers(playersList);
               }
               break;

            case "move":
               if (message.board)
                  setBoard(message.board.flat() as (string | null)[]);
               if (message.nextPlayer)
                  setCurrentPlayer(message.nextPlayer);
               if (message.winner !== undefined && message.winner !== null)
                  setWinner(message.winner);
               break;

            case "system":
               if (message.message)
                  alert(message.message);
               break;

            case "ready":
               console.log("Player ready:", message.fromUserId);
               break;

            case "leave":
               if (message.playersSymbols) {
                  const updatedPlayers = Object.entries(message.playersSymbols).map(([name, symbol]) => ({
                     name,
                     symbol,
                  }));
                  setPlayers(updatedPlayers);
               }
               break;

            default:
               break;
         }
      });

      return () => {
         unsubscribe?.();
      };
   }, [subscribe]);

   const handleClick = (index: number) => {
      if (!connected || board[index] || winner || currentPlayer !== mySymbol) {
         return;
      }

      send({
         type: "move",
         player: mySymbol!,
         cell: index,
         roomName: roomName as string,
      });
   };

   const handleReady = () => {
      if (!connected || ready) {
         return;
      }

      send({ type: "ready", roomName: roomName as string });
      setReady(true);
   };

   const handleLeave = () => {
      if (connected) {
         send({ type: "leave", roomName: roomName as string });
      }

      navigate("/rooms");
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
      </Box>
   );
}