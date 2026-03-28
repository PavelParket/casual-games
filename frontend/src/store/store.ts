import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/AuthSlice";
import roomReducer from "./slices/RoomSlice";
import userReducer from "./slices/UserSlice";
import bankReducer from "./slices/BankSlice";
import ticTacToeReducer from "./slices/TicTacToeRoomSlice";
import deCoderReducer from "./slices/DeCoderRoomSlice";
import horseRaceReducer from "./slices/HorseRaceRoomSlice";
import durakReducer from "./slices/DurakRoomSlice";

export const store = configureStore({
   reducer: {
      auth: authReducer,
      rooms: roomReducer,
      user: userReducer,
      bank: bankReducer,
      ticTacToeRoom: ticTacToeReducer,
      deCoderRoom: deCoderReducer,
      horseRaceRoom: horseRaceReducer,
      durakRoom: durakReducer,
   },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export const selectRoomsState = (state: RootState) => state.rooms;
