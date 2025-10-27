import type { AxiosError } from 'axios';
import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { AuthAPI } from '../../api/authApi';
import { setAccessToken as setGlobalToken } from '../../utils/tokenManager';

export interface User {
   id: number;
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
   accessToken: string;
   user: User;
}

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
      logout: (state) => {
         state.user = null;
         state.accessToken = null;
         state.isAuthenticated = false;
         state.error = null;
         setGlobalToken(null);
      },
      setAccessToken: (state, action: PayloadAction<string>) => {
         state.accessToken = action.payload;
         state.isAuthenticated = true;
         setGlobalToken(action.payload);
      },
   },
   extraReducers: (builder) => {
      builder
         .addCase(login.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(login.fulfilled, (state, action) => {
            state.isLoading = false;
            state.user = action.payload.user;
            state.accessToken = action.payload.accessToken;
            state.isAuthenticated = true;
            setGlobalToken(action.payload.accessToken);
         })
         .addCase(login.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Login failed";
         })
         .addCase(register.fulfilled, (state, action) => {
            state.user = action.payload.user;
            state.accessToken = action.payload.accessToken;
            state.isAuthenticated = true;
            setGlobalToken(action.payload.accessToken);
         });
   },
});

export const { clearError, logout, setAccessToken } = authSlice.actions;

export default authSlice.reducer;