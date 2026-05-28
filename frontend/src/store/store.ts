import { configureStore, combineReducers } from "@reduxjs/toolkit";
import authReducer, { logout } from "./slices/AuthSlice";
import roomReducer from "./slices/RoomSlice";
import userReducer from "./slices/UserSlice";
import bankReducer from "./slices/BankSlice";
import ticTacToeReducer from "./slices/TicTacToeRoomSlice";
import deCoderReducer from "./slices/DeCoderRoomSlice";
import horseRaceReducer from "./slices/HorseRaceRoomSlice";
import durakReducer from "./slices/DurakRoomSlice";

const rootReducer = combineReducers({
    auth: authReducer,
    rooms: roomReducer,
    user: userReducer,
    bank: bankReducer,
    ticTacToeRoom: ticTacToeReducer,
    deCoderRoom: deCoderReducer,
    horseRaceRoom: horseRaceReducer,
    durakRoom: durakReducer,
});

export const store = configureStore({
    reducer: (state: ReturnType<typeof rootReducer> | undefined, action) => {
        if (logout.fulfilled.match(action) || logout.rejected.match(action)) {
            return rootReducer(undefined, action);
        }

        return rootReducer(state, action);
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export const selectRoomsState = (state: RootState) => state.rooms;
