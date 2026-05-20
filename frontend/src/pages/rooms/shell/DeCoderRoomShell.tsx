import { useRoomLoader } from "../../../hooks/useRoomLoader";
import { getRoomById } from "../../../store/slices/DeCoderRoomSlice";
import DeCoderRoom from "../DeCoderRoom";
import RoomShell from "./RoomShell";

export default function DeCoderRoomShell() {
    const { isLoading, error } = useRoomLoader({
        fetchRoom: (roomId) => getRoomById({ roomId }) as never,
        selectRoom: (state) => state.deCoderRoom.room,
        selectError: (state) => state.deCoderRoom.error,
    });

    return (
        <RoomShell isLoading={isLoading} error={error}>
            <DeCoderRoom />
        </RoomShell>
    );
}
