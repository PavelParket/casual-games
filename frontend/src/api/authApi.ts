import type { AuthResponse, LoginRequest, RegisterRequest } from "../store/slices/authSlice";
import { client } from "./axiosConfig";

export const AuthAPI = {
   login: (data: LoginRequest) => client.post<AuthResponse>("auth/login", data),
   register: (data: RegisterRequest) => client.post<AuthResponse>("auth/register", data),
};