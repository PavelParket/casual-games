import type { AxiosError } from 'axios';
import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { AuthAPI } from '../../api/AuthApi';
import { setAccessToken as setGlobalToken, startTokenTimer, stopTokenTimer } from '../../utils/TokenManager';
import { update } from './UserSlice';

export interface User {
   guid: string;
   username: string;
   email: string;
   role: string;
}

export interface AuthState {
   user: User | null;
   accessToken: string | null;
   isAuthenticated: boolean;
   isLoading: boolean;
   error: string | null;
}

export interface LoginRequest {
   email: string;
   password: string;
}

export interface RegisterRequest {
   username: string;
   email: string;
   password: string;
}

export interface AuthResponse {
   guid: string;
   username: string;
   email: string;
   role: string;
   accessToken: string;
}

const mapAuthResponseToState = (data: AuthResponse): { user: User; accessToken: string } => ({
   user: {
      guid: data.guid,
      username: data.username,
      email: data.email,
      role: data.role,
   },
   accessToken: data.accessToken,
});

// ------------------ Thunks ------------------

export const login = createAsyncThunk<AuthResponse, LoginRequest, { rejectValue: string }>(
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

export const register = createAsyncThunk<AuthResponse, RegisterRequest, { rejectValue: string }>(
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
         setGlobalToken(null);
         stopTokenTimer();
      }
   }
);

export const refresh = createAsyncThunk<AuthResponse, void, { rejectValue: string }>(
   "auth/refresh",
   async (_, { rejectWithValue }) => {
      try {
         const response = await AuthAPI.refresh();
         if (response.status === 204 || !response.data) {
            return rejectWithValue("");
         }
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "");
      }
   }
);

// ------------------ Slice ------------------

const initialState: AuthState = {
   user: null,
   accessToken: null,
   isAuthenticated: false,
   isLoading: false,
   error: null,
};

const authSlice = createSlice({
   name: "auth",
   initialState,
   reducers: {
      clearError: (state) => {
         state.error = null;
      },
      setAccessToken: (state, action: PayloadAction<string>) => {
         state.accessToken = action.payload;
         state.isAuthenticated = true;
         setGlobalToken(action.payload);
      },
   },
   extraReducers: (builder) => {
      builder
         // Login
         .addCase(login.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(login.fulfilled, (state, action) => {
            const { user, accessToken } = mapAuthResponseToState(action.payload);
            state.isLoading = false;
            state.user = user;
            state.accessToken = accessToken;
            state.isAuthenticated = true;
            setGlobalToken(accessToken);
            startTokenTimer(accessToken);
         })
         .addCase(login.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Login failed";
         })
         // Register
         .addCase(register.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(register.fulfilled, (state, action) => {
            const { user, accessToken } = mapAuthResponseToState(action.payload);
            state.isLoading = false;
            state.user = user;
            state.accessToken = accessToken;
            state.isAuthenticated = true;
            setGlobalToken(accessToken);
            startTokenTimer(accessToken);
         })
         .addCase(register.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Registration failed";
         })
         // Logout
         .addCase(logout.pending, (state) => {
            state.isLoading = true;
         })
         .addCase(logout.fulfilled, (state) => {
            state.isLoading = false;
            state.user = null;
            state.accessToken = null;
            state.isAuthenticated = false;
            state.error = null;
         })
         .addCase(logout.rejected, (state) => {
            state.isLoading = false;
            state.user = null;
            state.accessToken = null;
            state.isAuthenticated = false;
            state.error = null;
         })
         // Refresh
         .addCase(refresh.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(refresh.fulfilled, (state, action) => {
            const { user, accessToken } = mapAuthResponseToState(action.payload);
            state.isLoading = false;
            state.user = user;
            state.accessToken = accessToken;
            state.isAuthenticated = true;
            setGlobalToken(accessToken);
            startTokenTimer(accessToken);
         })
         .addCase(refresh.rejected, (state, action) => {
            state.isLoading = false;
            state.user = null;
            state.accessToken = null;
            state.isAuthenticated = false;
            state.error = action.payload ?? "Session expired";
         })
         // If update username
         .addCase(update.fulfilled, (state, action) => {
            if (state.user) {
                state.user.username = action.payload.username;
            }
         });
   },
});

export const { clearError, setAccessToken } = authSlice.actions;

export default authSlice.reducer;