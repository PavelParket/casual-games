import { useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { MahjongGameMessage } from "../models/WsMessage";
import type { AppDispatch, RootState } from "../store/store";
import type { ToastVariant } from "../ui";
import { errorCodeMessages } from "../models/constants/ErrorCodeMessages";
import { validateToastMessage } from "../utils/SecurityUtils";
import { getPlayersBets, syncReadiness, syncRoomState } from "../store/slices/MahjongRoomSlice";

interface UseMahjongMessagesProps {
    roomId?: string;
    isGame: boolean;
    gameAborted: boolean;
    processGameState: (message: MahjongGameMessage) => void;
    processDeadlockWait: (seconds: number) => void;
    processGameOver: (winnerId?: string) => void;
    processAbort: () => void;
    setBetPlaced: (value: boolean) => void;
    setReady: (value: boolean) => void;
    showGameToast: (message: string, variant: ToastVariant) => void;
}

export function useMahjongMessages({
    roomId,
    isGame,
    gameAborted,
    processGameState,
    processDeadlockWait,
    processGameOver,
    processAbort,
    setBetPlaced,
    setReady,
    showGameToast,
}: UseMahjongMessagesProps): (message: MahjongGameMessage) => void {
    const dispatch = useDispatch<AppDispatch>();
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const room = useSelector((state: RootState) => state.mahjongRoom.room);

    return useCallback((message: MahjongGameMessage) => {
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

            case "READY":
                showGameToast(validateToastMessage(message.message ?? "Player is ready"), "game-info");
                dispatch(syncReadiness({ roomId, roomType: room.type }));
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
                    validateToastMessage(message.message ?? "You must place a bet before becoming ready."),
                    "game-error"
                );
                break;

            case "START_FAILED":
                setReady(false);
                showGameToast(errorCodeMessages.START_FAILED ?? "Failed to start the game.", "game-error");
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "START":
            case "GAME_STATE":
                processGameState(message);
                break;

            case "DEADLOCK_WAIT":
                if (message.seconds !== undefined) {
                    processDeadlockWait(message.seconds);
                }
                break;

            case "GAME_OVER":
                processGameOver(message.winner);
                break;

            default:
                break;
        }
    }, [
        dispatch, gameAborted, guid, isGame, processAbort, processDeadlockWait, 
        processGameOver, processGameState, room, roomId, setBetPlaced, setReady, showGameToast
    ]);
}
