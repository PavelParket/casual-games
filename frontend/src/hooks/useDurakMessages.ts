import { useCallback, type RefObject } from "react";
import type { DurakPhase, DurakTablePair } from "../models/Durak";
import type { DurakGameMessage, ErrorWSMessage } from "../models/WsMessage";
import type { AppDispatch, RootState } from "../store/store";
import type { ToastVariant } from "../ui";
import { errorCodeMessages, systemErrorCodes } from "../models/constants/ErrorCodeMessages";
import { validateToastMessage } from "../utils/SecurityUtils";
import { getPlayersBets, syncReadiness, syncRoomState } from "../store/slices/DurakRoomSlice";
import { useDispatch, useSelector } from "react-redux";
import { useSystemToastContext } from "../providers/SystemToastContext";

interface UseDurakMessagesProps {
    roomId?: string;
    isGame: boolean;
    processGameState: (message: DurakGameMessage) => void;
    processGameOver: (winnerId?: string) => void;
    processReset: () => void;
    setBetPlaced: (value: boolean) => void;
    setReady: (value: boolean) => void;
    setRemainingSeconds: (value: number | null) => void;
    setAwaitingResponse: (value: boolean) => void;
    setDiscardCount: React.Dispatch<React.SetStateAction<number>>;
    prevTableRef: RefObject<DurakTablePair[]>;
    prevPhaseRef: RefObject<DurakPhase | null>;
    showGameToast: (message: string, variant: ToastVariant) => void;
}

export function useDurakMessages({
    roomId,
    isGame,
    processGameState,
    processGameOver,
    processReset,
    setBetPlaced,
    setReady,
    setRemainingSeconds,
    setAwaitingResponse,
    setDiscardCount,
    prevTableRef,
    prevPhaseRef,
    showGameToast,
}: UseDurakMessagesProps): (message: DurakGameMessage) => void {
    const dispatch = useDispatch<AppDispatch>();
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const room = useSelector((state: RootState) => state.durakRoom.room);
    const { showSystemToast } = useSystemToastContext();

    return useCallback((message: DurakGameMessage) => {
        if (!guid || !roomId || !room) {
            console.debug("[DurakMsg] skipped — no guid/roomId/room");
            return;
        }

        console.debug(`[DurakMsg] received: event=${message.event}`);

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

            case "ERROR": {
                setAwaitingResponse(false);
                const errorMsg = message as ErrorWSMessage;
                const code = errorMsg.errorCode ?? "";
                let text = "";

                if (code) {
                    text = errorMsg.message ?? errorCodeMessages[code];
                } else {
                    text = errorCodeMessages.DEFAULT;
                }

                console.warn(`[DurakMsg] ERROR`, { code, message: errorMsg.message });

                if (systemErrorCodes.has(code)) {
                    showSystemToast(text, "system-error");
                } else {
                    showGameToast(text, "game-error");
                }
                break;
            }

            default:
                console.debug(`[DurakMsg] unhandled event: ${message.event}`);
                break;
        }
    }, [
        dispatch, guid, isGame, prevPhaseRef, prevTableRef,
        processGameOver, processGameState, processReset, room, roomId,
        setAwaitingResponse, setBetPlaced, setDiscardCount, setReady, setRemainingSeconds,
        showGameToast, showSystemToast,
    ]);
}
