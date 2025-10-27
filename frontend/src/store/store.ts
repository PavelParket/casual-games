import { configureStore } from "@reduxjs/toolkit";
import authReducer, { logout, setAccessToken } from "./slices/authSlice";
import { setOnTokenRefresh } from "../utils/tokenManager";

export const store = configureStore({
   reducer: {
      auth: authReducer,
   },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

setOnTokenRefresh((token) => {
   if (token) {
      store.dispatch(setAccessToken(token));
   } else {
      store.dispatch(logout());
   }
});