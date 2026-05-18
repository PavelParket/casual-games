import { useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { DeCoderGameHistory } from "../models/DeCoderGameHistory";
import type { DeCoderMessage, ErrorWSMessage } from "../models/WsMessage";
import { errorCodeMessages } from "../models/constants/ErrorCodeMessages";
import type { AppDispatch, RootState } from "../store/store";
import { getUsernamesInRoom } from "../store/slices/DeCoderRoomSlice";
import { getBalance } from "../store/slices/UserSlice";
import { useSystemToastContext } from "../providers/SystemToastContext";
import type { ToastVariant } from "../ui";

interface UseDeCoderMessagesProps {
    roomId?: string;
    setGameActive: (value: boolean) => void;
    setHistory: React.Dispatch<React.SetStateAction<DeCoderGameHistory[]>>;
    setJackpot: (value: number) => void;
    setGameOverModal: (modal: { isOpen: boolean; isWin: boolean; winnerName?: string }) => void;
    requestSync: () => void;
    showGameToast: (message: string, variant: ToastVariant) => void;
}

export function useDeCoderMessages({
    roomId,
    setGameActive,
    setHistory,
    setJackpot,
    setGameOverModal,
    requestSync,
    showGameToast,
}: UseDeCoderMessagesProps): (message: DeCoderMessage) => void {
    const dispatch = useDispatch<AppDispatch>();
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const players = useSelector((state: RootState) => state.deCoderRoom.players);
    const room = useSelector((state: RootState) => state.deCoderRoom.room);
    const { showSystemToast } = useSystemToastContext();

    return useCallback((message: DeCoderMessage) => {
        if (!guid || !roomId || !room) {
            console.debug("[DeCoderMsg] skipped — no guid/roomId/room");
            return;
        }

        console.debug(`[DeCoderMsg] received: event=${message.event}`);

        switch (message.event) {
            case "STATE":
                setHistory(message.gameState || []);
                setJackpot(message.jackpot || 0);
                if (message.isGameStarted !== undefined) {
                    setGameActive(message.isGameStarted);
                }
                break;

            case "MOVE":
                if (message.gameState && message.gameState.length > 0) {
                    setHistory((prev) => [...prev, ...message.gameState!]);
                }
                if (message.jackpot !== undefined) {
                    setJackpot(message.jackpot);
                }

                if (message.player !== guid) {
                    const playerName = (players ?? {})[message.player!] || "Someone";
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

                const winnerName = (players ?? {})[message.winner!] || "Unknown Player";
                const isMe = message.winner === guid;

                setGameOverModal({
                    isOpen: true,
                    isWin: isMe,
                    winnerName: isMe ? "You" : winnerName,
                });

                if (guid) {
                    dispatch(getBalance(guid));
                }
                break;
            }

            case "ERROR": {
                const errorMsg = message as ErrorWSMessage;
                const code = errorMsg.errorCode ?? "";
                let text = errorCodeMessages[code];

                console.warn(`[DeCoderMsg] ERROR`, { code, message: errorMsg.message });

                if (!text) {
                    const msg = errorMsg.message || "Error occurred";
                    if (msg.includes("not started") || msg.includes("Game not found")) {
                        setGameActive(false);
                        text = "Game session expired or not started.";
                    } else if (msg.includes("already in progress")) {
                        setGameActive(true);
                        requestSync();
                        return;
                    } else if (msg.includes("Insufficient funds")) {
                        text = "Transaction failed: Insufficient funds!";
                    } else {
                        text = msg;
                    }
                }

                showGameToast(text, "game-error");
                break;
            }

            case "JOIN":
            case "LEAVE":
                dispatch(getUsernamesInRoom({ roomId, roomType: room.type }));
                break;

            default:
                console.debug(`[DeCoderMsg] unhandled event: ${message.event}`);
                break;
        }
    }, [dispatch, guid, players, requestSync, room, roomId, setGameActive, setGameOverModal, setHistory, setJackpot, showGameToast]);
}
