import type { DepositRequest, TransactionResponse } from "../models/Bank";
import axios from "axios";
import { BANK_SERVICE_URL } from "./ApiDictionary";

export const BankAPI = {
  deposit: (data: DepositRequest) => axios.post<TransactionResponse>(`${BANK_SERVICE_URL}/transactions/deposit`, data),
};
