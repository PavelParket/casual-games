import { useEffect, useRef } from "react";
import type { Room } from "../models/Room";
import type { ErrorWSMessage, TicTacToeGameMessage } from "../models/WsMessage";
import type { AppDispatch } from "../store/store";
import type { ToastVariant } from "../ui";
import { validateToastMessage } from "../utils/SecurityUtils";
import { getPlayersBets, syncReadiness, syncRoomState } from "../store/slices/TicTacToeRoomSlice";
import { errorCodeMessages } from "../models/constants/ErrorCodeMessages";

interface UseTicTacToeMessagesProps {
    message: TicTacToeGameMessage | undefined;
    isConnected: boolean;
    guid: string | undefined;
    roomId: string | undefined;
    room: Room | undefined;
    isGame: boolean;
    processStart: (message: TicTacToeGameMessage) => void;
    processMove: (message: TicTacToeGameMessage) => void;
    processWin: (message: TicTacToeGameMessage) => void;
    processDraw: (message: TicTacToeGameMessage) => void;
    processReset: () => void;
    setBetPlaced: (value: boolean) => void;
    setReady: (value: boolean) => void;
    showGameToast: (message: string, variant: ToastVariant) => void;
    showSystemToast: (message: string, variant: ToastVariant) => void;
    dispatch: AppDispatch;
}

export function useTicTacToeMessages({
    message,
    isConnected,
    guid,
    roomId,
    room,
    isGame,
    processStart,
    processMove,
    processWin,
    processDraw,
    processReset,
    setBetPlaced,
    setReady,
    showGameToast,
    showSystemToast,
    dispatch,
}: UseTicTacToeMessagesProps) {
    const processedMessageRef = useRef<TicTacToeGameMessage | null>(null);

    useEffect(() => {
        if (!isConnected || !message || !guid || !roomId || !room) {
            return;
        }

        if (message === processedMessageRef.current) {
            return;
        }

        processedMessageRef.current = message;

        switch (message.event) {
            case "JOIN":
                showGameToast(validateToastMessage(message.message ?? "Player joined the room"), "game-info");
                dispatch(syncRoomState({ roomId, roomType: room.type }));
                break;

            case "LEAVE":
                if (isGame) {
                    processReset();
                } else {
                    showGameToast(validateToastMessage(message.message ?? "Player left the room"), "game-info");
                }
                dispatch(syncRoomState({ roomId, roomType: room.type }));
                break;

            case "START":
                processStart(message);
                showGameToast(validateToastMessage(message.message ?? "Game started"), "game-info");
                break;

            case "READY":
                showGameToast(validateToastMessage(message.message ?? "Player is ready"), "game-info");
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "MOVE":
                processMove(message);
                break;

            case "WINNER_X":
            case "WINNER_O":
                processWin(message);
                break;

            case "DRAW":
                processDraw(message);
                break;

            case "BET":
                if (message.fromUserId === guid) {
                    setBetPlaced(true);
                    showGameToast(validateToastMessage(message.message ?? "Your bet has been accepted!"), "game-info");
                } else {
                    showGameToast(validateToastMessage(message.message ?? "Opponent placed a bet"), "game-info");
                }
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "BET_REJECT":
                setBetPlaced(false);
                showGameToast(
                    validateToastMessage(message.message ?? "") || errorCodeMessages.BET_REJECT,
                    "game-error"
                );
                dispatch(getPlayersBets({ roomId }));
                break;

            case "BET_OUTBID":
                setBetPlaced(false);
                setReady(false);
                showGameToast(
                    validateToastMessage(message.message ?? "You have been outbid! Please place a new bet."),
                    "game-error"
                );
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "BET_REQUIRED":
                showGameToast(
                    validateToastMessage(message.message ?? "You must place a bet before becoming ready"),
                    "game-error"
                );
                break;

            case "START_FAILED":
                setReady(false);
                showGameToast(errorCodeMessages.START_FAILED, "game-error");
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "ERROR": {
                const errorMsg = message as ErrorWSMessage;
                const code = errorMsg.errorCode ?? "";
                const text = errorCodeMessages[code] ?? errorCodeMessages.DEFAULT;

                if (errorMsg.errorCategory === "SYSTEM") {
                    showSystemToast(text, "system-error");
                } else {
                    showGameToast(text, "game-error");
                }
                break;
            }

            default:
                break;
        }
    }, [dispatch, guid, isConnected, isGame, message, processDraw, processMove, processReset, processStart, processWin, room, roomId, setBetPlaced, setReady, showGameToast, showSystemToast]);
}
