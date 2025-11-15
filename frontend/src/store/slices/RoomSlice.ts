import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { RoomAPI } from "../../api/WsHubApi";
import type { AxiosError } from "axios";

export interface RoomState {
   rooms: string[];
   isLoading: boolean;
   error: string | null;
}

const initialState: RoomState = {
   rooms: [],
   isLoading: false,
   error: null,
};

export const fetchRooms = createAsyncThunk<string[], void, { rejectValue: string }>(
   "rooms/fetchAll",
   async (_, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.getAllRooms();
         return response.data;
      } catch (err: unknown) {
         const error = err as AxiosError<{ message?: string }>;
         return rejectWithValue(error.response?.data?.message ?? "Failed to fetch rooms");
      }
   }
);

const roomSlice = createSlice({
   name: "rooms",
   initialState,
   reducers: {
      clearRooms: (state) => {
         state.rooms = [];
         state.error = null;
      },
   },
   extraReducers: (builder) => {
      builder
         .addCase(fetchRooms.pending, (state) => {
            state.isLoading = true;
            state.error = null;
         })
         .addCase(fetchRooms.fulfilled, (state, action: PayloadAction<string[]>) => {
            state.isLoading = false;
            state.rooms = action.payload;
         })
         .addCase(fetchRooms.rejected, (state, action) => {
            state.isLoading = false;
            state.error = action.payload ?? "Failed to fetch rooms";
         });
   },
});

export const { clearRooms } = roomSlice.actions;
export default roomSlice.reducer;