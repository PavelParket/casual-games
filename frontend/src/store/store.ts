import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/AuthSlice";
import roomReducer from "./slices/RoomSlice";
import userReducer from "./slices/UserSlice";
import bankReducer from "./slices/BankSlice";

export const store = configureStore({
   reducer: {
      auth: authReducer,
      rooms: roomReducer,
      user: userReducer,
      bank: bankReducer,
   },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export const selectRoomsState = (state: RootState) => state.rooms;
