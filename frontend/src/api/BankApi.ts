import type { DepositRequest, PageResponse, TransactionResponse, TopWinnersResponse, } from "../models/Bank";
import { BANK_SERVICE_URL } from "./ApiDictionary";
import { client } from "./AxiosConfig";

export const BankAPI = {
    deposit: (data: DepositRequest) => client.post<TransactionResponse>(`${BANK_SERVICE_URL}/transactions/deposit`, data),

    getByUserGuid: (guid: string, page: number = 0, size: number = 4) =>
        client.get<PageResponse<TransactionResponse>>(`${BANK_SERVICE_URL}/transactions/${guid}`, {
            params: { page, size }
        }),

    getTopWinners: (limit: number = 10) =>
        client.get<TopWinnersResponse[]>(`${BANK_SERVICE_URL}/transactions/top-winners/`, {
            params: { limit }
        }),
};
