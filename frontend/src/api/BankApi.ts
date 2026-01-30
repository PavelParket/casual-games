import { client } from "./AxiosConfig";
import type { DepositRequest, TransactionResponse } from "../models/Bank";

const BANK_SERVICE_URL = 'http://localhost:8084';

export const BankAPI = {
  deposit: (data: DepositRequest) => 
    client.post<TransactionResponse>(`${BANK_SERVICE_URL}/transactions/deposit`, data),
};