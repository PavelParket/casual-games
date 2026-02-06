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

    avatarUrl?: string | null;
    achievements?: string[];
    history?: UserGameHistory[];
}

export interface UpdateUserRequest {
    username?: string;
    email?: string;
    password?: string;
}
