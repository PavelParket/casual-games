import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { RoomAPI } from "../../api/WsHubApi";
import type { AxiosError } from "axios";
import type { Room, RoomRequest, RoomType } from "../../models/Room";
import { extractErrorResponseMessage } from "../../helpers/ApiErrorHelper";

export const ROOM_OPERATION_KEYS = {
   GET_ROOMS: "getRooms",
   GET_TYPES: "getTypes",
   CREATE_ROOM: "createRoom",
} as const;

export interface RoomState {
   rooms?: Room[];
   roomTypes?: RoomType[];
   errors: Record<string, string | null>;
}

// ------------------ Thunks ------------------

export const getRooms = createAsyncThunk<Room[], void, { rejectValue: string }>(
   "rooms/getRooms",
   async (_, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.getRooms();
         return response.data;
      } catch (err: unknown) {
         return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch rooms"));
      }
   }
);

export const getTypes = createAsyncThunk<RoomType[], void, { rejectValue: string }>(
   "rooms/getTypes",
   async (_, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.getTypes();
         return response.data;
      } catch (err: unknown) {
         return rejectWithValue(extractErrorResponseMessage(err, "Failed to fetch room types"));
      }
   }
);

export const createRoom = createAsyncThunk<Room, RoomRequest, { rejectValue: string }>(
   "rooms/createRoom",
   async (roomRequest, { rejectWithValue }) => {
      try {
         const response = await RoomAPI.createRoom(roomRequest);
         return response.data;
      } catch (err: unknown) {
         return rejectWithValue(extractErrorResponseMessage(err, "Failed to create room"));
      }
   }
);

// ------------------ Slice ------------------

const initialState: RoomState = {
   rooms: [],
   roomTypes: [],
   errors: {},
};

const roomSlice = createSlice({
   name: "rooms",
   initialState,
   reducers: {
      clearRooms: (state) => {
         state.rooms = [];
      },
      clearRoomTypes: (state) => {
         state.roomTypes = [];
      },
      clearError: (state, action: PayloadAction<string>) => {
         state.errors[action.payload] = null;
      },
      clearAllErrors: (state) => {
         state.errors = {};
      },
   },
   extraReducers: (builder) => {
      builder

         /* === Get Rooms === */
         .addCase(getRooms.pending, (state) => {
            state.errors[ROOM_OPERATION_KEYS.GET_ROOMS] = null;
         })
         .addCase(getRooms.fulfilled, (state, action) => {
            state.rooms = action.payload;
         })
         .addCase(getRooms.rejected, (state, action) => {
            state.errors[ROOM_OPERATION_KEYS.GET_ROOMS] = action.payload ?? "Failed to fetch rooms";
         })

         /* === Get Room Types === */
         .addCase(getTypes.pending, (state) => {
            state.errors[ROOM_OPERATION_KEYS.GET_TYPES] = null;
         })
         .addCase(getTypes.fulfilled, (state, action) => {
            state.roomTypes = action.payload;
         })
         .addCase(getTypes.rejected, (state, action) => {
            state.errors[ROOM_OPERATION_KEYS.GET_TYPES] = action.payload ?? "Failed to fetch room types";
         })

         /* === Create Room === */
         .addCase(createRoom.pending, (state) => {
            state.errors[ROOM_OPERATION_KEYS.CREATE_ROOM] = null;
         })
         .addCase(createRoom.fulfilled, (state, action) => {
            if (state.rooms) {
               state.rooms.push(action.payload);
            }
         })
         .addCase(createRoom.rejected, (state, action) => {
            state.errors[ROOM_OPERATION_KEYS.CREATE_ROOM] = action.payload ?? "Failed to create room";
         });
   },
});

export const { clearRooms, clearRoomTypes, clearError, clearAllErrors } = roomSlice.actions;

export default roomSlice.reducer;
