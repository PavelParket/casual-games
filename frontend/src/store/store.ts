import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/AuthSlice";
import roomReducer from "./slices/RoomSlice";
import userReducer from "./slices/UserSlice";
import bankReducer from "./slices/BankSlice";
import ticTacToeReducer from "./slices/TicTacToeRoomSlice";
import horseRaceReducer from "./slices/HorseRaceRoomSlice";

export const store = configureStore({
   reducer: {
      auth: authReducer,
      rooms: roomReducer,
      user: userReducer,
      bank: bankReducer,
      ticTacToeRoom: ticTacToeReducer,
      horseRaceRoom: horseRaceReducer,
   },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export const selectRoomsState = (state: RootState) => state.rooms;
