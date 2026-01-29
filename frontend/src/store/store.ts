import { configureStore } from "@reduxjs/toolkit";
import authReducer, { logout, setAccessToken } from "./slices/AuthSlice";
import roomReducer from "./slices/RoomSlice";
import userReducer, { clearUser } from "./slices/UserSlice";
import bankReducer from "./slices/BankSlice";
import { setOnTokenRefresh } from "../utils/TokenManager";

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

setOnTokenRefresh((token) => {
   if (token) {
      store.dispatch(setAccessToken(token));
   } else {
      store.dispatch(logout());
      store.dispatch(clearUser());
   }
});
