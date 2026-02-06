export interface AuthUser {
    guid: string;
    username: string;
    email: string;
    role: string;
    accessToken: string;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}
