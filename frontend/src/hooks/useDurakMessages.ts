import { useCallback, type RefObject } from "react";
import type { DurakPhase, DurakTablePair } from "../models/Durak";
import type { DurakGameMessage } from "../models/WsMessage";
import type { AppDispatch, RootState } from "../store/store";
import type { ToastVariant } from "../ui";
import { errorCodeMessages } from "../models/constants/ErrorCodeMessages";
import { validateToastMessage } from "../utils/SecurityUtils";
import { getPlayersBets, syncReadiness, syncRoomState } from "../store/slices/DurakRoomSlice";
import { useDispatch, useSelector } from "react-redux";

interface UseDurakMessagesProps {
    roomId?: string;
    isGame: boolean;
    gameAborted: boolean;
    processGameState: (message: DurakGameMessage) => void;
    processGameOver: (winnerId?: string) => void;
    processAbort: () => void;
    setBetPlaced: (value: boolean) => void;
    setReady: (value: boolean) => void;
    setRemainingSeconds: (value: number | null) => void;
    setDiscardCount: React.Dispatch<React.SetStateAction<number>>;
    prevTableRef: RefObject<DurakTablePair[]>;
    prevPhaseRef: RefObject<DurakPhase | null>;
    showGameToast: (message: string, variant: ToastVariant) => void;
}

export function useDurakMessages({
    roomId,
    isGame,
    gameAborted,
    processGameState,
    processGameOver,
    processAbort,
    setBetPlaced,
    setReady,
    setRemainingSeconds,
    setDiscardCount,
    prevTableRef,
    prevPhaseRef,
    showGameToast,
}: UseDurakMessagesProps): (message: DurakGameMessage) => void {
    const dispatch = useDispatch<AppDispatch>();
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const room = useSelector((state: RootState) => state.durakRoom.room);

    return useCallback((message: DurakGameMessage) => {
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
                    validateToastMessage(message.message ?? errorCodeMessages.BET_OUTBID),
                    "game-error"
                );
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "BET_REQUIRED":
                showGameToast(
                    validateToastMessage(message.message ?? errorCodeMessages.BET_REQUIRED),
                    "game-error"
                );
                break;

            case "START_FAILED":
                setReady(false);
                showGameToast(errorCodeMessages.START_FAILED, "game-error");
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "GAME_STATE": {
                const prevTable = prevTableRef.current ?? [];
                const prevPhase = prevPhaseRef.current;
                const newTable = message.table ?? [];

                if (prevTable.length > 0 && newTable.length === 0 && prevPhase !== "PICKING_UP") {
                    const cardsOnTable = prevTable.reduce(
                        (sum, pair) => sum + (pair.defendCard ? 2 : 1),
                        0
                    );
                    setDiscardCount(prev => prev + cardsOnTable);
                }

                processGameState(message);
                break;
            }

            case "GAME_OVER":
                processGameOver(message.winnerId);
                break;

            case "TIMER_UPDATE":
                setRemainingSeconds(message.remainingSeconds ?? null);
                break;

            default:
                break;
        }
    }, [dispatch, gameAborted, guid, isGame, prevPhaseRef, prevTableRef, processAbort, processGameOver, processGameState, room, roomId, setBetPlaced, setDiscardCount, setReady, setRemainingSeconds, showGameToast]);
}
