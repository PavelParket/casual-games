import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { AxiosError } from "axios";
import { UserAPI } from "../../api/UserApi";
import type { UpdateUserRequest, User } from "../../models/User";
import { deposit } from './BankSlice';

export interface UserState {
    user?: User;
    isLoading: boolean;
    error?: string;
    isLoadingPlayersMiniProfiles: Record<string, boolean>;
}

const initialState: UserState = {
    user: undefined,
    isLoading: false,
    error: undefined,
    isLoadingPlayersMiniProfiles: {},
};

// ------------------ Thunks ------------------

export const findByGuid = createAsyncThunk<User, string, { rejectValue: string }>(
    "user/findByGuid",
    async (guid, { rejectWithValue }) => {
        try {
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

export const update = createAsyncThunk<User, { guid: string; updateData: UpdateUserRequest }, { rejectValue: string }>(
    "user/updateProfile",
    async ({ guid, updateData }, { rejectWithValue }) => {
        try {
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

export const getBalance = createAsyncThunk<number, string, { rejectValue: string }>(
    "user/getBalance",
    async (guid, { rejectWithValue }) => {
        try {
            if (!guid) {
                return rejectWithValue("Cannot get balance: no user GUID");
            }

            const response = await UserAPI.getBalance(guid);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to get balance");
        }
    }
);

export const uploadProfilePicture = createAsyncThunk<User, { guid: string; files: { full: File; mini: File } }, { rejectValue: string }>(
    "user/uploadProfilePicture",
     async ({ guid, files }, { rejectWithValue }) => {
        try {
            const response = await UserAPI.uploadProfilePicture(guid, files);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to upload avatar");
        }
    }
);

export const deleteProfilePicture = createAsyncThunk<void, string, { rejectValue: string }>(
    "user/deleteProfilePicture",
    async (guid, { rejectWithValue }) => {
        try {
            await UserAPI.deleteProfilePicture(guid);
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to delete avatar");
        }
    }
);

// ------------------ Slice ------------------

const userSlice = createSlice({
    name: "user",
    initialState,
    reducers: {
        clearUser: (state) => {
            state.user = undefined;
            state.error = undefined;
            state.isLoading = false;
        },
    },
    extraReducers: (builder) => {
        builder
            /* === Find By Guid === */
            .addCase(findByGuid.pending, (state) => {
                state.isLoading = true;
                state.error = undefined;
            })
            .addCase(findByGuid.fulfilled, (state, action) => {
                state.isLoading = false;
                state.user = action.payload;
            })
            .addCase(findByGuid.rejected, (state, action) => {
                state.isLoading = false;
                state.error = action.payload ?? "Unknown error";
            })

            /* === Update === */
            .addCase(update.pending, (state) => {
                state.isLoading = true;
                state.error = undefined;
            })
            .addCase(update.fulfilled, (state, action) => {
                state.isLoading = false;
                state.user = action.payload;
            })
            .addCase(update.rejected, (state, action) => {
                state.isLoading = false;
                state.error = action.payload ?? "Update failed";
            })

            /* === Get Balance === */
            .addCase(getBalance.fulfilled, (state, action) => {
                if (state.user) {
                    state.user.balance = action.payload;
                }
            })

            /* === Deposit === */
            .addCase(deposit.fulfilled, (state, action) => {
                if (state.user) {
                    state.user.balance = action.payload.balanceAfter;
                }
            })

            /* === Upload Profile Picture === */
            .addCase(uploadProfilePicture.pending, (state) => {
                state.isLoading = true;
            })
            .addCase(uploadProfilePicture.fulfilled, (state, action) => {
                state.isLoading = false;
                if (state.user) {
                    state.user.linkProfilePicture = action.payload.linkProfilePicture;
                    state.user.linkProfilePictureMini = action.payload.linkProfilePictureMini;
                }
            })
            .addCase(uploadProfilePicture.rejected, (state, action) => {
                state.isLoading = false;
                state.error = action.payload ?? "Avatar upload failed";
            })

            /* === Delete Profile Picture === */
            .addCase(deleteProfilePicture.pending, (state) => {
                state.isLoading = true;
            })
            .addCase(deleteProfilePicture.fulfilled, (state) => {
                state.isLoading = false;
                if (state.user) {
                    state.user.linkProfilePicture = null;
                    state.user.linkProfilePictureMini = null;
                }
            })
            .addCase(deleteProfilePicture.rejected, (state, action) => {
                state.isLoading = false;
                state.error = action.payload ?? "Avatar delete failed";
            });

    },
});

export const { clearUser } = userSlice.actions;

export default userSlice.reducer;
