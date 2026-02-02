import type { DepositRequest, TransactionResponse } from "../models/Bank";
import axios from "axios";

const BANK_SERVICE_URL = 'http://localhost:8084';

export const BankAPI = {
  deposit: (data: DepositRequest) => axios.post<TransactionResponse>(`${BANK_SERVICE_URL}/transactions/deposit`, data),
};
