export interface DepositRequest {
    userGuid: string;
    amount: number;
}

export interface TransactionResponse {
    id: number;
    balanceAfter: number;
}