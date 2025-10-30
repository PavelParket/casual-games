import { useSelector } from "react-redux";
import { useNavigate, useParams } from "react-router-dom";
import type { RootState } from "../../store/store";
import { useEffect, useState } from "react";
import { useWebSocket } from "../../hooks/useWebSocket";
import type { GameMessage } from "../../services/WebSocketService";
import { Box, Button, Card, Container, Typography } from "../../ui";

type Cell = string | null;

export default function TicTacToeRoom() {
   const { roomName } = useParams<{ roomName: string }>();
   const navigate = useNavigate();
   const { user } = useSelector((state: RootState) => state.auth);

   const [board, setBoard] = useState<Cell[]>(Array(9).fill(null));
   const [players, setPlayers] = useState<string[]>([]);
   const [readyPlayers, setReadyPlayers] = useState<string[]>([]);
   const [mySymbol, setMySymbol] = useState<string | null>(null);
   const [currentPlayer, setCurrentPlayer] = useState<string>("X");
   const [winner, setWinner] = useState<string | null>(null);
   const [gameMessage, setGameMessage] = useState<string>("");
   const [isReady, setIsReady] = useState(false);

   const { sendMessage, subscribe } = useWebSocket({
      roomName: roomName || "default",
      isGameRoom: true,
      onConnect: () => console.log("Connected to game room"),
   });

   useEffect(() => {
      const unsubscribe = subscribe("player_list", (msg) => {
         try {
            const data = JSON.parse((msg as GameMessage).message || "{}");
            setPlayers(data.players || []);
            setReadyPlayers(data.readyPlayers || []);
         } catch (e) {
            console.error("Failed to parse player list:", e);
         }
      });

      return unsubscribe;
   }, [subscribe]);

   useEffect(() => {
      const unsubscribe = subscribe("START", (msg) => {
         const gameMsg = msg as GameMessage;

         // Convert 2D board to 1D
         const flatBoard: Cell[] = [];
         if (gameMsg.board) {
            for (let i = 0; i < 3; i++) {
               for (let j = 0; j < 3; j++) {
                  flatBoard.push(gameMsg.board[i]?.[j] || null);
               }
            }
         }
         setBoard(flatBoard);

         if (gameMsg.playersSymbols && user) {
            setMySymbol(gameMsg.playersSymbols[user.id.toString()]);
         }

         setCurrentPlayer(gameMsg.nextPlayer || "X");
         setGameMessage(gameMsg.message || "Game started!");
      });

      return unsubscribe;
   }, [subscribe, user]);

   useEffect(() => {
      const unsubscribe = subscribe("MOVE", (msg) => {
         const gameMsg = msg as GameMessage;

         const flatBoard: Cell[] = [];
         if (gameMsg.board) {
            for (let i = 0; i < 3; i++) {
               for (let j = 0; j < 3; j++) {
                  flatBoard.push(gameMsg.board[i]?.[j] || null);
               }
            }
         }
         setBoard(flatBoard);
         setCurrentPlayer(gameMsg.nextPlayer || "X");
         setGameMessage(gameMsg.message || "");
      });

      return unsubscribe;
   }, [subscribe]);

   useEffect(() => {
      const unsubscribeX = subscribe("WINNER_X", (msg) => {
         const gameMsg = msg as GameMessage;
         setWinner("X");
         setGameMessage(gameMsg.message || "X wins!");

         const flatBoard: Cell[] = [];
         if (gameMsg.board) {
            for (let i = 0; i < 3; i++) {
               for (let j = 0; j < 3; j++) {
                  flatBoard.push(gameMsg.board[i]?.[j] || null);
               }
            }
         }
         setBoard(flatBoard);
      });

      const unsubscribeO = subscribe("WINNER_O", (msg) => {
         const gameMsg = msg as GameMessage;
         setWinner("O");
         setGameMessage(gameMsg.message || "O wins!");

         const flatBoard: Cell[] = [];
         if (gameMsg.board) {
            for (let i = 0; i < 3; i++) {
               for (let j = 0; j < 3; j++) {
                  flatBoard.push(gameMsg.board[i]?.[j] || null);
               }
            }
         }
         setBoard(flatBoard);
      });

      const unsubscribeDraw = subscribe("DRAW", (msg) => {
         const gameMsg = msg as GameMessage;
         setWinner("Draw");
         setGameMessage(gameMsg.message || "It's a draw!");

         const flatBoard: Cell[] = [];
         if (gameMsg.board) {
            for (let i = 0; i < 3; i++) {
               for (let j = 0; j < 3; j++) {
                  flatBoard.push(gameMsg.board[i]?.[j] || null);
               }
            }
         }
         setBoard(flatBoard);
      });

      return () => {
         unsubscribeX();
         unsubscribeO();
         unsubscribeDraw();
      };
   }, [subscribe]);

   const handleReady = () => {
      sendMessage({
         type: "ready",
         roomName: roomName || "default",
      });
      setIsReady(true);
   };

   const handleMove = (index: number) => {
      if (board[index] || winner || !mySymbol || currentPlayer !== mySymbol) {
         return;
      }

      // Convert 1D to 2D board
      const board2D: (string | null)[][] = [];
      for (let i = 0; i < 3; i++) {
         board2D[i] = [];
         for (let j = 0; j < 3; j++) {
            board2D[i][j] = board[i * 3 + j];
         }
      }

      sendMessage({
         type: "move",
         roomName: roomName || "default",
         cell: index,
         player: mySymbol,
         board: board2D,
      });
   };

   const isMyTurn = mySymbol === currentPlayer && !winner;

   return (
      <Box style={{
         minHeight: "calc(100vh - 60px - 50px)",
         margin: "0 10rem",
         padding: "2rem 1rem",
         background: "var(--color-bg-glass)",
         backdropFilter: "blur(2px)",
         borderRadius: "var(--radius-md)",
         boxShadow: "var(--shadow-lg)"
      }}>
         <Container>
            <Box style={{ textAlign: "center", marginBottom: "2rem" }}>
               <Typography variant="h2">Tic-Tac-Toe</Typography>
               <Typography variant="caption" style={{ opacity: 0.7, marginTop: "0.5rem", display: "block" }}>
                  Room: {roomName}
               </Typography>
            </Box>

            <Card style={{
               padding: "2rem",
               display: "flex",
               flexDirection: "column",
               alignItems: "center",
               gap: "2rem"
            }}>
               <Typography variant="h3">
                  {winner ? gameMessage : isMyTurn ? `Your turn (${mySymbol})` : `Waiting... (${currentPlayer})`}
               </Typography>

               {gameMessage && !winner && (
                  <Typography variant="body" style={{ opacity: 0.7 }}>
                     {gameMessage}
                  </Typography>
               )}

               <Box style={{ display: "flex", gap: "3rem", alignItems: "center" }}>
                  {/* Players */}
                  <Box style={{ textAlign: "center" }}>
                     <Typography variant="h3" style={{ marginBottom: "1rem" }}>Players</Typography>
                     {players.map((p, i) => (
                        <Box key={i} style={{ marginBottom: "0.5rem" }}>
                           <Typography variant="body">
                              {p} {readyPlayers.includes(p) ? "✓" : ""}
                           </Typography>
                        </Box>
                     ))}
                  </Box>

                  {/* Board */}
                  <Box style={{
                     display: "grid",
                     gridTemplateColumns: "repeat(3, 80px)",
                     gridTemplateRows: "repeat(3, 80px)",
                     gap: "4px"
                  }}>
                     {board.map((cell, index) => (
                        <Button
                           key={index}
                           variant="ghost"
                           onClick={() => handleMove(index)}
                           disabled={!!cell || !!winner || !isMyTurn}
                           style={{
                              width: "80px",
                              height: "80px",
                              fontSize: "32px",
                              fontWeight: "bold"
                           }}
                        >
                           {cell}
                        </Button>
                     ))}
                  </Box>
               </Box>

               {/* Controls */}
               <Box style={{ display: "flex", gap: "1rem" }}>
                  <Button variant="outline" onClick={() => navigate("/rooms")}>
                     Leave
                  </Button>

                  {!mySymbol && (
                     <Button
                        variant="solid"
                        onClick={handleReady}
                        disabled={isReady || players.length < 2}
                     >
                        {isReady ? "Ready ✓" : "Get Ready"}
                     </Button>
                  )}
               </Box>
            </Card>
         </Container>
      </Box>
   );
}