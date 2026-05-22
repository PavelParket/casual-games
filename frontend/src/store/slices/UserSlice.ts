import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { AxiosError } from "axios";
import { UserAPI } from "../../api/UserApi";
import type { UpdateUserRequest, User, SubscriptionRequest, SubscriptionResponse } from "../../models/User";
import { deposit } from './BankSlice';

export interface UserState {
    user?: User;
    subscription?: SubscriptionResponse;
    isLoading: boolean;
    isLoadingSubscription: boolean;
    isPurchasing: boolean;
    error?: string;
    isLoadingPlayersMiniProfiles: Record<string, boolean>;
}

const initialState: UserState = {
    user: undefined,
    subscription: undefined,
    isLoading: false,
    isLoadingSubscription: false,
    isPurchasing: false,
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

// ------------------ Slice ------------------

const userSlice = createSlice({
    name: "user",
    initialState,
    reducers: {
        clearUser: (state) => {
            state.user = undefined;
            state.subscription = undefined;
            state.error = undefined;
            state.isLoading = false;
            state.isLoadingSubscription = false;
            state.isPurchasing = false;
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

            /* === Deposit === */
            .addCase(deposit.fulfilled, (state, action) => {
                if (state.user) {
                    state.user.balance = action.payload.balanceAfter;
                }
            });

    },
});

export const { clearUser } = userSlice.actions;

export default userSlice.reducer;
