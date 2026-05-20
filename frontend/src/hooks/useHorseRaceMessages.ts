import { useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { PlacedBetInfo } from "../models/HorseRace";
import type { HorseRaceGameMessage } from "../models/WsMessage";
import type { AppDispatch, RootState } from "../store/store";
import type { ToastVariant } from "../ui";
import { validateToastMessage } from "../utils/SecurityUtils";
import { syncReadiness, syncRoomState } from "../store/slices/HorseRaceRoomSlice";

interface UseHorseRaceMessagesProps {
    roomId?: string;
    processStart: (message: HorseRaceGameMessage) => void;
    startCountdown: (seconds: number) => void;
    clearCountdown: () => void;
    setSecondsLeft: (value: number | null) => void;
    setBetPlaced: (value: boolean) => void;
    setPlacedBetInfo: (info: PlacedBetInfo | null) => void;
    setReady: (value: boolean) => void;
    showGameToast: (message: string, variant: ToastVariant) => void;
}

export function useHorseRaceMessages({
    roomId,
    processStart,
    startCountdown,
    clearCountdown,
    setSecondsLeft,
    setBetPlaced,
    setPlacedBetInfo,
    setReady,
    showGameToast,
}: UseHorseRaceMessagesProps): (message: HorseRaceGameMessage) => void {
    const dispatch = useDispatch<AppDispatch>();
    const guid = useSelector((state: RootState) => state.auth.user?.guid);
    const room = useSelector((state: RootState) => state.horseRaceRoom.room);

    return useCallback((message: HorseRaceGameMessage) => {
        if (!guid || !roomId || !room) {
            return;
        }

        switch (message.event) {
            case "JOIN":
                showGameToast(validateToastMessage(message.message ?? "Player joined the room"), "game-info");
                dispatch(syncRoomState({ roomId, roomType: room.type }));
                break;

            case "LEAVE":
                showGameToast(validateToastMessage(message.message ?? "Player left the room"), "game-info");
                dispatch(syncRoomState({ roomId, roomType: room.type }));
                break;

            case "READY":
                showGameToast(validateToastMessage(message.message ?? "Player is ready"), "game-info");
                dispatch(syncReadiness({ roomId, roomType: room.type }));
                break;

            case "START":
                processStart(message);
                break;

            case "BET": {
                if (message.fromUserId !== guid) {
                    break;
                }

                const amount = message.bet;
                const horseIdx = message.horseIndex;

                if (amount !== undefined && horseIdx !== undefined) {
                    setPlacedBetInfo({ horseIndex: horseIdx, amount });
                }

                setBetPlaced(true);
                showGameToast(
                    validateToastMessage(message.message ?? "Your bet has been accepted!"),
                    "game-info"
                );
                break;
            }

            case "BET_REJECT":
                setBetPlaced(false);
                setPlacedBetInfo(null);
                showGameToast(
                    validateToastMessage(message.message ?? "Your bet was rejected. Please try again."),
                    "game-error"
                );
                break;

            case "BET_REQUIRED":
                showGameToast(
                    validateToastMessage(message.message ?? "You must place a bet before becoming ready."),
                    "game-error"
                );
                break;

            case "COUNTDOWN": {
                const remaining = message.remainingSeconds;
                if (remaining !== undefined && remaining > 0) {
                    startCountdown(remaining);
                }
                break;
            }

            case "CANCELED":
                clearCountdown();
                setSecondsLeft(null);
                setBetPlaced(false);
                setPlacedBetInfo(null);
                setReady(false);
                showGameToast(
                    validateToastMessage(message.message ?? "Race was canceled — no players in the room."),
                    "game-info"
                );
                break;

            default:
                break;
        }
    }, [clearCountdown, dispatch, guid, processStart, room, roomId, setBetPlaced, setPlacedBetInfo, setReady, setSecondsLeft, showGameToast, startCountdown]);
}
