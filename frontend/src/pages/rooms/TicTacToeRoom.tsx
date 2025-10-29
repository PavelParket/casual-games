import { useNavigate, useParams } from "react-router-dom";
import type { MessageType, WSMessage } from "../../services/WebSocketService";
import { useSelector } from "react-redux";
import type { RootState } from "../../store/store";
import { useThemedIcon } from "../../ui";
import { useState } from "react";
import { useWebSocket } from "../../hooks/useWebSocket";

type Player = "X" | "O";
type Cell = Player | null;

interface GameMessage extends WSMessage {
   board?: (string | null)[][];
   cell?: number;
   player?: string;
   nextPlayer?: string;
   playersSymbols?: Map<string, string>;
   players?: Set<string>;
   winner?: string;
   message?: string;
}

export default function TicTacToeRoom() {
   const { roomId } = useParams<{ roomId: string }>();
   const navigate = useNavigate();
   const { user } = useSelector((state: RootState) => state.auth);
   const { getInverseIcon } = useThemedIcon();

   const [board, setBoard] = useState<Cell[]>(Array(9).fill(null));
   const [ready, setReady] = useState(false);
   const [currentPlayer, setCurrentPlayer] = useState<Player>("X");
   const [players, setPlayers] = useState<Set<string>>(new Set<string>());
   const [mySymbol, setMySymbol] = useState<string | null>(null);
   const [winner, setWinner] = useState<string | null>(null);
   const [gameStarted, setGameStarted] = useState(false);
   const [connectionStatus, setConnectionStatus] = useState<string>("Connecting...");
   const [gameMessage, setGameMessage] = useState<string>("");

   const { isConnected, sendMessage, subscribe } = useWebSocket({
      roomName: roomId || "default",
      onConnect: () => {
         setConnectionStatus("Connected");
         console.log("Connected to game room");
      },
      onDisconnect: () => {
         setConnectionStatus("Disconnected");
         console.log("Disconnected from game room");
      },
      onError: (error) => {
         console.error("WebSocket error:", error);
         setConnectionStatus("Connection error");
      },
   });

   useEffect(() => {
      const unsubscribePlayerList = subscribe(
         "system",
         (message: WSMessage) => {
            try {
               const data:
            }
         }
      );
   });
}