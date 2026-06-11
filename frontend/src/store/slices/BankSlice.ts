import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { AxiosError } from "axios";
import { BankAPI } from "../../api/BankApi";
import type { TransactionResponse, TopWinsResponse, PageResponse } from "../../models/Bank";

export interface BankState {
    isDepositing: boolean;
    error?: string;
    transactions: TransactionResponse[];
    isLoadingTransactions: boolean;
    currentPage: number;
    totalPages: number;
    topWins: TopWinsResponse[];
    isLoadingTopWins: boolean;
}

const initialState: BankState = {
    isDepositing: false,
    error: undefined,
    transactions: [],
    isLoadingTransactions: false,
    currentPage: 0,
    totalPages: 0,
    topWins: [],
    isLoadingTopWins: false,
};

// ------------------ Thunks ------------------

export const deposit = createAsyncThunk<TransactionResponse, { userGuid: string; amount: number }, { rejectValue: string }
>(
    "bank/deposit",
    async ({ userGuid, amount }, { rejectWithValue }) => {
        try {
            if (!userGuid) {
                return rejectWithValue("Cannot deposit: no user GUID provided");
            }

            const response = await BankAPI.deposit({ userGuid, amount });
            return response.data;

        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Deposit failed");
        }
    }
);

export const getByUserGuid = createAsyncThunk<PageResponse<TransactionResponse>, { guid: string; page?: number; size?: number }, { rejectValue: string }>(
    "bank/getByUserGuid",
    async ({ guid, page = 0, size = 4 }, { rejectWithValue }) => {
        try {
            const response = await BankAPI.getByUserGuid(guid, page, size);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch transactions");
        }
    }
);

export const getTopWins = createAsyncThunk<TopWinsResponse[], number | void, { rejectValue: string }>(
    "bank/getTopWinners",
    async (limit = 10, { rejectWithValue }) => {
        try {
            const response = await BankAPI.getTopWins(limit as number);
            return response.data;
        } catch (err: unknown) {
            const error = err as AxiosError<{ message?: string }>;
            return rejectWithValue(error.response?.data?.message ?? "Failed to fetch top wins");
        }
    }
);

// ------------------ Slice ------------------

const bankSlice = createSlice({
    name: "bank",
    initialState,
    reducers: {
        clearBankError: (state) => {
            state.error = undefined;
        }
    },
    extraReducers: (builder) => {
        builder
            /* === Deposit === */
            .addCase(deposit.pending, (state) => {
                state.isDepositing = true;
                state.error = undefined;
            })
            .addCase(deposit.fulfilled, (state) => {
                state.isDepositing = false;
            })
            .addCase(deposit.rejected, (state, action) => {
                state.isDepositing = false;
                state.error = action.payload ?? "Unknown error";
            })

            /* === GetByUserGuid === */
            .addCase(getByUserGuid.pending, (state) => {
                state.isLoadingTransactions = true;
                state.error = undefined;
            })
            .addCase(getByUserGuid.fulfilled, (state, action) => {
                state.isLoadingTransactions = false;
                state.transactions = action.payload.content;
                state.currentPage = action.payload.page;
                state.totalPages = action.payload.totalPages;
            })
            .addCase(getByUserGuid.rejected, (state, action) => {
                state.isLoadingTransactions = false;
                state.error = action.payload ?? "Failed to fetch history";
            })

            /* === GetTopWins === */
            .addCase(getTopWins.pending, (state) => {
                state.isLoadingTopWins = true;
                state.error = undefined;
            })
            .addCase(getTopWins.fulfilled, (state, action) => {
                state.isLoadingTopWins = false;
                state.topWins = action.payload;
            })
            .addCase(getTopWins.rejected, (state, action) => {
                state.isLoadingTopWins = false;
                state.error = action.payload ?? "Failed to fetch top winners";
            });
    },
});

export const { clearBankError } = bankSlice.actions;
export default bankSlice.reducer;
