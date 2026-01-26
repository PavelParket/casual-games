import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { AxiosError } from "axios";
import { UserAPI } from "../../api/UserApi";
import type { UpdateUserRequest, User } from "../../models/User";
import type { RootState } from "../store";

export interface UserState {
   profile: User | null;
   isLoading: boolean;
   error: string | null;
}

const initialState: UserState = {
   profile: null,
   isLoading: false,
   error: null,
};

// ------------------ Thunks ------------------

export const findByGuid = createAsyncThunk<User, void, { state: RootState; rejectValue: string }>(
   "user/findByGuid",
   async (_, { getState, rejectWithValue }) => {
      try {
         const state = getState();
         const guid = state.auth.user?.guid;


         if (!guid) {
            return rejectWithValue("No user GUID found in auth state");
         }

         const response = await UserAPI.findByGuid(guid);
         return response.data;
         
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Failed to fetch user profile");
      }
   }
);

export const update = createAsyncThunk<User, UpdateUserRequest, { state: RootState; rejectValue: string }>(
    "user/updateProfile",
    async (updateData, { getState, rejectWithValue }) => {
        try {
            const state = getState();
            const guid = state.auth.user?.guid;

            if (!guid) {
                return rejectWithValue("Cannot update profile: no user GUID");
            }
            
            const response = await UserAPI.updateByGuid(guid, updateData);
            return response.data;

        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to update profile");
        }
    }
);
// ------------------ Slice ------------------

const userSlice = createSlice({
   name: "user",
   initialState,
   reducers: {
      clearUser: (state) => {
         state.profile = null;
         state.error = null;
         state.isLoading = false;
      },
   },
   extraReducers: (builder) => {
      builder
         .addCase(findByGuid.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(findByGuid.fulfilled, (state, action) => {
            state.isLoading = false;
            state.profile = action.payload;
         })
         .addCase(findByGuid.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Unknown error";
         })
         // Update
         .addCase(update.pending, (state) => {
            state.isLoading = true; 
            state.error = null;
         })
         .addCase(update.fulfilled, (state, action) => {
            state.isLoading = false;
            state.profile = action.payload; 
         })
         .addCase(update.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Update failed";
         });
   },
});

export const { clearUser } = userSlice.actions;

export default userSlice.reducer;