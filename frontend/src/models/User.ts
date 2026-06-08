export type UserStatus = "DEFAULT" | "PRO" | "VIP";

export interface SubscriptionRequest {
    status: UserStatus;
}

export interface SubscriptionResponse {
    status: UserStatus;
    startedAt: string;
    expiresAt: string;
    autoRenew: boolean;
    newStatus?: UserStatus;
    statusChangeAt?: string;
    createdAt: string;
    updatedAt: string;
}

export interface SubscriptionPlanResponse {
    id: number;
    status: UserStatus;
    price: number;
    upgradePrice?: number | null;
    tier: number;
}

export interface UserGameHistory {
    game: string;
    result: "Win" | "Loss" | "Draw";
    date: string;
}

export interface User {
    guid: string;
    username: string;
    email: string;
    role: string;

    balance: number;
    status: string;
    createdAt: string;

    linkProfilePicture?: string | null;
    linkProfilePictureMini?: string | null;

    history?: UserGameHistory[];
}

export interface UpdateUserRequest {
    username?: string;
    email?: string;
    password?: string;
}
