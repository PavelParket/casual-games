import { useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { DeCoderGameHistory } from "../models/DeCoderGameHistory";
import type { DeCoderMessage } from "../models/WsMessage";
import type { AppDispatch, RootState } from "../store/store";
import { validateToastMessage } from "../utils/SecurityUtils";
import { getPlayers } from "../store/slices/DeCoderRoomSlice";
import { getBalance } from "../store/slices/UserSlice";
import type { ToastVariant } from "../ui";

interface UseDeCoderMessagesProps {
    roomId?: string;
    setGameActive: (value: boolean) => void;
    setHistory: React.Dispatch<React.SetStateAction<DeCoderGameHistory[]>>;
    setJackpot: (value: number) => void;
    setSpent: (value: number) => void;
    setEndGameOverlay: (overlay: { isOpen: boolean; isWin: boolean; winnerName?: string }) => void;
    showGameToast: (message: string, variant: ToastVariant) => void;
}

export function useDeCoderMessages({
    roomId,
    setGameActive,
    setHistory,
    setJackpot,
    setSpent,
    setEndGameOverlay,
    showGameToast,
}: UseDeCoderMessagesProps): (message: DeCoderMessage) => void {
    const dispatch = useDispatch<AppDispatch>();
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const players = useSelector((state: RootState) => state.deCoderRoom.players);
    const room = useSelector((state: RootState) => state.deCoderRoom.room);

    return useCallback((message: DeCoderMessage) => {
        if (!guid || !roomId || !room) {
            return;
        }

        switch (message.event) {
            case "STATE":
                setHistory(message.gameState || []);
                setJackpot(message.jackpot || 0);
                if (message.spent !== undefined) setSpent(message.spent);
                if (message.isGameStarted !== undefined) {
                    setGameActive(message.isGameStarted);
                }
                showGameToast(validateToastMessage(message.message ?? "Game state synchronized"), "game-info");
                break;

            case "MOVE":
                if (message.gameState && message.gameState.length > 0) {
                    setHistory((prev) => [...prev, ...message.gameState!]);
                }
                if (message.jackpot !== undefined) {
                    setJackpot(message.jackpot);
                }
                if (message.spent !== undefined) {
                    setSpent(message.spent);
                }
                if (message.player !== guid) {
                    const playerName = (players ?? {})[message.player!]?.username || "Someone";
                    showGameToast(`${playerName} made a move`, "game-info");
                } else {
                    showGameToast("Move accepted", "game-info");
                }

                if (guid) {
                    dispatch(getBalance(guid));
                }
                break;

            case "WINNER": {
                setGameActive(false);
                if (message.gameState && message.gameState.length > 0) {
                    setHistory((prev) => [...prev, ...message.gameState!]);
                }
                if (message.jackpot !== undefined) {
                    setJackpot(message.jackpot);
                }
                if (message.spent !== undefined) {
                    setSpent(message.spent);
                }

                const winnerName = (players ?? {})[message.winner!]?.username || "Unknown Player";
                const isMe = message.winner === guid;

                setEndGameOverlay({
                    isOpen: true,
                    isWin: isMe,
                    winnerName: isMe ? "You" : winnerName,
                });

                if (guid) {
                    dispatch(getBalance(guid));
                }
                break;
            }

            case "JOIN":
                showGameToast(validateToastMessage(message.message ?? "Player joined the room"), "game-info");
                dispatch(getPlayers({ roomId, roomType: room.type }));
                break;

            case "LEAVE":
                showGameToast(validateToastMessage(message.message ?? "Player left the room"), "game-info");
                dispatch(getPlayers({ roomId, roomType: room.type }));
                break;

            default:
                break;
        }
    }, [dispatch, guid, players, room, roomId, setGameActive, setEndGameOverlay, setHistory, setJackpot, setSpent, showGameToast]);
}
