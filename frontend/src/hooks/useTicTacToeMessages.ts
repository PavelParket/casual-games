import { useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { getPlayersBets, syncReadiness, syncRoomState } from "../store/slices/TicTacToeRoomSlice";
import type { TicTacToeGameMessage } from "../models/WsMessage";
import { errorCodeMessages } from "../models/constants/ErrorCodeMessages";
import { validateToastMessage } from "../utils/SecurityUtils";
import type { ToastVariant } from "../ui";

interface UseTicTacToeMessagesProps {
    roomId?: string;
    isGame: boolean;
    gameAborted: boolean;
    processStart: (message: TicTacToeGameMessage) => void;
    processMove: (message: TicTacToeGameMessage) => void;
    processWin: (message: TicTacToeGameMessage) => void;
    processDraw: (message: TicTacToeGameMessage) => void;
    processAbort: () => void;
    setBetPlaced: (value: boolean) => void;
    setReady: (value: boolean) => void;
    showGameToast: (message: string, variant: ToastVariant) => void;
}

export function useTicTacToeMessages({
    roomId,
    isGame,
    gameAborted,
    processStart,
    processMove,
    processWin,
    processDraw,
    processAbort,
    setBetPlaced,
    setReady,
    showGameToast,
}: UseTicTacToeMessagesProps): (message: TicTacToeGameMessage) => void {

    const dispatch = useDispatch<AppDispatch>();
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const room = useSelector((state: RootState) => state.ticTacToeRoom.room);

    return useCallback((message: TicTacToeGameMessage) => {
        if (!guid || !roomId || !room) {
            return;
        }

        switch (message.event) {
            case "JOIN":
                if (gameAborted) {
                    break;
                }
                showGameToast(validateToastMessage(message.message ?? "Player joined the room"), "game-info");
                dispatch(syncRoomState({ roomId, roomType: room.type }));
                break;

            case "LEAVE":
                if (isGame) {
                    processAbort();
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

            default:
                break;
        }
    }, [dispatch, gameAborted, guid, isGame, processAbort, processDraw, processMove, processStart, processWin, room, roomId, setBetPlaced, setReady, showGameToast]);
}
