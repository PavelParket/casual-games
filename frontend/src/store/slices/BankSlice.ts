import { createAsyncThunk, createSlice } from "@reduxjs/toolkit";
import type { AxiosError } from "axios";
import { BankAPI } from "../../api/BankApi";
import type { TransactionResponse } from "../../models/Bank";

export interface BankState {
    isDepositing: boolean;
    error: string | null;
}

const initialState: BankState = {
    isDepositing: false,
    error: null,
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

// ------------------ Slice ------------------

const bankSlice = createSlice({
    name: "bank",
    initialState,
    reducers: {
        clearBankError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            // Deposit
            .addCase(deposit.pending, (state) => {
                state.isDepositing = true;
                state.error = null;
            })
            .addCase(deposit.fulfilled, (state) => {
                state.isDepositing = false;
            })
            .addCase(deposit.rejected, (state, action) => {
                state.isDepositing = false;
                state.error = action.payload ?? "Unknown error";
            });
    },
});

export const { clearBankError } = bankSlice.actions;
export default bankSlice.reducer;