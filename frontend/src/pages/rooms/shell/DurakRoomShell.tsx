import { useRoomLoader } from "../../../hooks/useRoomLoader";
import { DURAK_OPERATION_KEYS, getRoomById } from "../../../store/slices/DurakRoomSlice";
import DurakRoom from "../DurakRoom";
import RoomShell from "./RoomShell";

export default function DurakRoomShell() {
    const { isLoading, error } = useRoomLoader({
        fetchRoom: (roomId) => getRoomById({ roomId }) as never,
        selectRoom: (state) => state.durakRoom.room,
        selectError: (state) => state.durakRoom.errors[DURAK_OPERATION_KEYS.GET_ROOM],
    });

    return (
        <RoomShell isLoading={isLoading} error={error}>
            <DurakRoom />
        </RoomShell>
    );
}
