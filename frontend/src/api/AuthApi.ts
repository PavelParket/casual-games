import type { AuthResponse, LoginRequest, RegisterRequest } from "../store/slices/AuthSlice";
import { client } from "./AxiosConfig";

export const AuthAPI = {
   login: (data: LoginRequest) => client.post<AuthResponse>("auth/login", data),
   register: (data: RegisterRequest) => client.post<AuthResponse>("auth/register", data),
   logout: () => client.post("auth/logout"),
   refresh: () => client.post<AuthResponse>("auth/refresh"),
};