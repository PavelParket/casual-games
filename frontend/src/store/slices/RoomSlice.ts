import { createAsyncThunk, createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { RoomAPI } from "../../api/WsHubApi";
import type { Room, RoomFilterRequest, RoomRequest, RoomType } from "../../models/Room";
import { extractErrorResponseMessage } from "../../helpers/ApiErrorHelper";

export const ROOM_OPERATION_KEYS = {
    GET_ROOMS: "getRooms",
    SEARCH_ROOMS: "searchRooms",
    GET_TYPES: "getTypes",
    CREATE_ROOM: "createRoom",
} as const;

export interface RoomState {
    rooms?: Room[];
    groupedRooms?: Record<RoomType, Room[]>;
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

export const searchRooms = createAsyncThunk<Record<RoomType, Room[]>, RoomFilterRequest, { rejectValue: string }>(
    "rooms/searchRooms",
    async (request, { rejectWithValue }) => {
        try {
            const response = await RoomAPI.searchRooms(request);
            return response.data.rooms;
        } catch (err: unknown) {
            return rejectWithValue(extractErrorResponseMessage(err, "Failed to search rooms"));
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
    groupedRooms: undefined,
    roomTypes: [],
    errors: {},
};


const roomSlice = createSlice({
    name: "rooms",
    initialState,
    reducers: {
        clearRooms: (state) => {
            state.rooms = [];
            state.groupedRooms = undefined;
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

            /* === Search Rooms === */
            .addCase(searchRooms.pending, (state) => {
                state.errors[ROOM_OPERATION_KEYS.SEARCH_ROOMS] = null;
            })
            .addCase(searchRooms.fulfilled, (state, action) => {
                state.groupedRooms = action.payload;
            })
            .addCase(searchRooms.rejected, (state, action) => {
                state.errors[ROOM_OPERATION_KEYS.SEARCH_ROOMS] = action.payload ?? "Failed to search rooms";
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
