import { useRoomLoader } from "../../../hooks/useRoomLoader";
import { getRoomById, TTT_OPERATION_KEYS } from "../../../store/slices/TicTacToeRoomSlice";
import TicTacToeRoom from "../TicTacToeRoom";
import RoomShell from "./RoomShell";

export default function TicTacToeRoomShell() {
    const { isLoading, error } = useRoomLoader({
        fetchRoom: (roomId) => getRoomById({ roomId }) as never,
        selectRoom: (state) => state.ticTacToeRoom.room,
        selectError: (state) => state.ticTacToeRoom.errors[TTT_OPERATION_KEYS.GET_ROOM],
    });

    return (
        <RoomShell isLoading={isLoading} error={error}>
            <TicTacToeRoom />
        </RoomShell>
    );
}
