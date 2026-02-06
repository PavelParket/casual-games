import type { AxiosError } from 'axios';
import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import { AuthAPI } from '../../api/AuthApi';
import { setTokenForManager, startTokenTimer, stopTokenTimer } from '../../utils/TokenManager';
import type { AuthUser, LoginRequest, RegisterRequest } from '../../models/AuthenticationUser';
import { ApiHelper } from '../../helpers/ApiHelper';
import { update } from './UserSlice';

export interface AuthState {
   user?: AuthUser;
   isAuthenticated: boolean;
   error?: string;
}

// ------------------ Thunks ------------------

export const login = createAsyncThunk<AuthUser, LoginRequest, { rejectValue: string }>(
   "auth/login",
   async (credentials, { rejectWithValue }) => {
      try {
         const response = await AuthAPI.login(credentials);
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Login failed");
      }
   }
);

export const register = createAsyncThunk<AuthUser, RegisterRequest, { rejectValue: string }>(
   "auth/register",
   async (credentials, { rejectWithValue }) => {
      try {
         const response = await AuthAPI.register(credentials);
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Registration failed");
      }
   }
);

export const logout = createAsyncThunk<void, void, { rejectValue: string }>(
   "auth/logout",
   async (_, { rejectWithValue }) => {
      try {
         await AuthAPI.logout();
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Logout failed");
      } finally {
         setTokenForManager();
         stopTokenTimer();
      }
   }
);

export const refresh = createAsyncThunk<AuthUser, void, { rejectValue: string }>(
   "auth/refresh",
   async (_, { rejectWithValue }) => {
      try {
         return await ApiHelper.refresh();
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "");
      }
   }
);

// ------------------ Slice ------------------

const initialState: AuthState = {
   user: undefined,
   isAuthenticated: false,
   error: undefined,
};

const authSlice = createSlice({
   name: "auth",
   initialState,
   reducers: {
      clearError: (state) => {
         state.error = undefined;
      },
   },
   extraReducers: (builder) => {
      builder

         /* === Login === */
         .addCase(login.pending, (state) => {
            state.error = undefined;
         })
         .addCase(login.fulfilled, (state, action) => {
            state.user = action.payload;
            state.isAuthenticated = true;
            setTokenForManager(action.payload.accessToken);
            startTokenTimer(action.payload.accessToken);
         })
         .addCase(login.rejected, (state, action) => {
            state.error = action.payload ?? "Login failed";
         })

         /* === Register === */
         .addCase(register.pending, (state) => {
            state.error = undefined;
         })
         .addCase(register.fulfilled, (state, action) => {
            state.user = action.payload;
            state.isAuthenticated = true;
            setTokenForManager(action.payload.accessToken);
            startTokenTimer(action.payload.accessToken);
         })
         .addCase(register.rejected, (state, action) => {
            state.error = action.payload ?? "Registration failed";
         })

         /* === Logout === */
         .addCase(logout.fulfilled, (state) => {
            state.user = undefined;
            state.isAuthenticated = false;
            state.error = undefined;
         })
         .addCase(logout.rejected, (state) => {
            state.user = undefined;
            state.isAuthenticated = false;
            state.error = undefined;
         })

         /* === Refresh === */
         .addCase(refresh.pending, (state) => {
            state.error = undefined;
         })
         .addCase(refresh.fulfilled, (state, action) => {
            state.user = action.payload;
            state.isAuthenticated = true;
            setTokenForManager(action.payload.accessToken);
            startTokenTimer(action.payload.accessToken);
         })
         .addCase(refresh.rejected, (state, action) => {
            state.user = undefined;
            state.isAuthenticated = false;
            state.error = action.payload ?? "Session expired";
         })

         /* === On Update Username === */
         .addCase(update.fulfilled, (state, action) => {
            if (state.user) {
               state.user.username = action.payload.username;
            }
         });
   },
});

export const { clearError } = authSlice.actions;

export default authSlice.reducer;
