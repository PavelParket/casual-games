import type { AuthUser, LoginRequest, RegisterRequest } from "../models/AuthenticationUser";
import { client } from "./AxiosConfig";

export const AuthAPI = {
   login: (data: LoginRequest) => client.post<AuthUser>("auth/login", data),
   
   register: (data: RegisterRequest) => client.post<AuthUser>("auth/register", data),
   
   logout: () => client.post("auth/logout"),
   
   refresh: () => client.post<AuthUser>("auth/refresh"),
};
