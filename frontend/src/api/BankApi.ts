import type { DepositRequest, PageResponse, TransactionResponse } from "../models/Bank";
import axios from "axios";
import { BANK_SERVICE_URL } from "./ApiDictionary";

export const BankAPI = {
  deposit: (data: DepositRequest) => axios.post<TransactionResponse>(`${BANK_SERVICE_URL}/transactions/deposit`, data),

  getByUserGuid: (guid: string, page: number = 0, size: number = 4) => 
    axios.get<PageResponse<TransactionResponse>>(`${BANK_SERVICE_URL}/transactions/${guid}`, {
      params: { page, size }
    }),
};
