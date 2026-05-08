import type { AuthUser, LoginRequest, RegisterRequest } from "../models/AuthenticationUser";
import { SECURITY_SERVICE_URL } from "./ApiDictionary";
import { client } from "./AxiosConfig";

export const AuthAPI = {
   login: (data: LoginRequest) => client.post<AuthUser>(`${SECURITY_SERVICE_URL}/auth/login`, data),

   register: (data: RegisterRequest) => client.post<AuthUser>(`${SECURITY_SERVICE_URL}/auth/register`, data),

   logout: () => client.post(`${SECURITY_SERVICE_URL}/auth/logout`),

   refresh: () => client.post<AuthUser>(`${SECURITY_SERVICE_URL}/auth/refresh`),
};
