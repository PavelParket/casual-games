export type TransactionType = "ADDITION" | "SUBTRACTION";
export type TransactionStatus = "PENDING" | "SUCCESS" | "REJECTED";

export interface DepositRequest {
    userGuid: string;
    amount: number;
}

export interface TransactionResponse {
    id: number;
    roomId: string | null;
    roomType: string | null;
    type: TransactionType;
    status: TransactionStatus;
    amount: number;
    balanceBefore: number;
    balanceAfter: number;
    createdAtDate: string;
    createdAtTime: string;
}

export interface TopWinsResponse {
    id: number;
    amount: number;
    roomType: string;
}

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}
