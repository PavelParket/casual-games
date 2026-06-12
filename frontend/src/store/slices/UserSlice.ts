import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { AxiosError } from "axios";
import { UserAPI } from "../../api/UserApi";
import { GameAPI } from "../../api/GameApi";
import type { UpdateUserRequest, User, SubscriptionRequest, SubscriptionResponse, SubscriptionPlanResponse } from "../../models/User";
import type { GameMatchRequestFilter, GameMatchResponse, GamePageResponse } from "../../models/GameMatch";
import { deposit } from './BankSlice';

export interface UserState {
    user?: User;
    subscription?: SubscriptionResponse;
    subscriptionPlans: SubscriptionPlanResponse[];
    isLoading: boolean;
    isLoadingSubscription: boolean;
    isLoadingPlans: boolean;
    isPurchasing: boolean;
    error?: string;
    gameHistory: GameMatchResponse[];
    isLoadingGameHistory: boolean;
    gameHistoryPage: number;
    gameHistoryTotalPages: number;
}

const initialState: UserState = {
    user: undefined,
    subscription: undefined,
    subscriptionPlans: [],
    isLoading: false,
    isLoadingSubscription: false,
    isLoadingPlans: false,
    isPurchasing: false,
    error: undefined,
    gameHistory: [],
    isLoadingGameHistory: false,
    gameHistoryPage: 0,
    gameHistoryTotalPages: 0,
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

export const getCurrentSubscription = createAsyncThunk<SubscriptionResponse, void, { rejectValue: string }>(
    "user/getSubscription",
    async (_, { rejectWithValue }) => {
        try {
            const response = await UserAPI.getSubscription();
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch subscription");
        }
    }
);

export const getSubscriptionPlans = createAsyncThunk<SubscriptionPlanResponse[], void, { rejectValue: string }>(
    "user/getSubscriptionPlans",
    async (_, { rejectWithValue }) => {
        try {
            const response = await UserAPI.getSubscriptionPlans();
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch subscription plans");
        }
    }
);

export const purchase = createAsyncThunk<SubscriptionResponse, SubscriptionRequest, { rejectValue: string }>(
    "user/purchaseSubscription",
    async (requestData, { rejectWithValue }) => {
        try {
            const response = await UserAPI.purchase(requestData);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to purchase subscription");
        }
    }
);

export const getMatches = createAsyncThunk<
    GamePageResponse<GameMatchResponse>,
    { guid: string; filter: GameMatchRequestFilter; page?: number; size?: number },
    { rejectValue: string }
>(
    "user/getGameHistory",
    async ({ guid, filter, page = 0, size = 4 }, { rejectWithValue }) => {
        try {
            const response = await GameAPI.getMatches(guid, filter, page, size);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch game history");
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
            state.subscription = undefined;
            state.subscriptionPlans = [];
            state.error = undefined;
            state.isLoading = false;
            state.isLoadingSubscription = false;
            state.isLoadingPlans = false;
            state.isPurchasing = false;
            state.gameHistory = [];
            state.gameHistoryPage = 0;
            state.gameHistoryTotalPages = 0;
        },
        clearGameHistoryState: (state) => {
            state.gameHistory = [];
            state.gameHistoryPage = 0;
            state.gameHistoryTotalPages = 0;
        }
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

            /* === Get Subscription === */
            .addCase(getCurrentSubscription.pending, (state) => {
                state.isLoadingSubscription = true;
                state.error = undefined;
            })
            .addCase(getCurrentSubscription.fulfilled, (state, action) => {
                state.isLoadingSubscription = false;
                state.subscription = action.payload;
                if (state.user) {
                    state.user.status = action.payload.status;
                }
            })
            .addCase(getCurrentSubscription.rejected, (state, action) => {
                state.isLoadingSubscription = false;
                state.error = action.payload ?? "Failed to fetch subscription";
            })

            /* === Get Subscription Plans=== */
            .addCase(getSubscriptionPlans.pending, (state) => {
                state.isLoadingPlans = true;
                state.error = undefined;
            })
            .addCase(getSubscriptionPlans.fulfilled, (state, action) => {
                state.isLoadingPlans = false;
                state.subscriptionPlans = action.payload;
            })
            .addCase(getSubscriptionPlans.rejected, (state, action) => {
                state.isLoadingPlans = false;
                state.error = action.payload ?? "Failed to fetch subscription plans";
            })

            /* === Purchase Subscription === */
            .addCase(purchase.pending, (state) => {
                state.isPurchasing = true;
                state.error = undefined;
            })
            .addCase(purchase.fulfilled, (state, action) => {
                state.isPurchasing = false;
                if (state.user) {
                    state.user.status = action.payload.status;
                }
            })
            .addCase(purchase.rejected, (state, action) => {
                state.isPurchasing = false;
                state.error = action.payload ?? "Purchase failed";
            })

            /* === Get Matches === */
            .addCase(getMatches.pending, (state) => {
                state.isLoadingGameHistory = true;
                state.error = undefined;
            })
            .addCase(getMatches.fulfilled, (state, action) => {
                state.isLoadingGameHistory = false;
                state.gameHistory = action.payload.content || [];
                state.gameHistoryPage = action.payload.page.number;
                state.gameHistoryTotalPages = action.payload.page.totalPages;
            })
            .addCase(getMatches.rejected, (state, action) => {
                state.isLoadingGameHistory = false;
                state.error = action.payload ?? "Failed to fetch game history";
                state.gameHistory = [];
                state.gameHistoryPage = 0;
                state.gameHistoryTotalPages = 0;
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

export const { clearUser, clearGameHistoryState } = userSlice.actions;

export default userSlice.reducer;
