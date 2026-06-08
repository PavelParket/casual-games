import { useRoomLoader } from "../../../hooks/useRoomLoader";
import { getRoomById, clearHorseRaceRoomState } from "../../../store/slices/HorseRaceRoomSlice";
import HorseRaceRoom from "../HorseRaceRoom";
import RoomShell from "./RoomShell";

export default function HorseRaceRoomShell() {
    const { isLoading, error } = useRoomLoader({
        fetchRoom: (roomId) => getRoomById({ roomId }) as never,
        selectRoom: (state) => state.horseRaceRoom.room,
        selectError: (state) => state.horseRaceRoom.error,
        clearRoomState: clearHorseRaceRoomState,
    });

    return (
        <RoomShell isLoading={isLoading} error={error}>
            <HorseRaceRoom />
        </RoomShell>
    );
}
