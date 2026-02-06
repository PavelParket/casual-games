import { configureStore } from "@reduxjs/toolkit";
import authReducer from "./slices/AuthSlice";
import roomReducer from "./slices/RoomSlice";

export const store = configureStore({
   reducer: {
      auth: authReducer,
      rooms: roomReducer,
   },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

export const selectRoomsState = (state: RootState) => state.rooms;
