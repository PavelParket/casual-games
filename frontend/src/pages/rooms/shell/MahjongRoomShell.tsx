import { useRoomLoader } from "../../../hooks/useRoomLoader";
import { getRoomById, MAHJONG_OPERATION_KEYS, clearMahjongRoomState } from "../../../store/slices/MahjongRoomSlice";
import MahjongRoom from "../MahjongRoom";
import RoomShell from "./RoomShell";

export default function MahjongRoomShell() {
    const { isLoading, error } = useRoomLoader({
        fetchRoom: (roomId) => getRoomById({ roomId }) as never,
        selectRoom: (state) => state.mahjongRoom.room,
        selectError: (state) => state.mahjongRoom.errors[MAHJONG_OPERATION_KEYS.GET_ROOM],
        clearRoomState: clearMahjongRoomState,
    });

    return (
        <RoomShell isLoading={isLoading} error={error}>
            <MahjongRoom />
        </RoomShell>
    );
}
