import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { getAccessToken, notifyTokenRefresh, setAccessToken } from '../utils/tokenManager';

const API_BASE_URL = 'http://localhost:8080';

export const client = axios.create({
   baseURL: API_BASE_URL,
   withCredentials: true,
   headers: {
      'Content-Type': 'application/json',
   },
});

client.interceptors.request.use(
   (config: InternalAxiosRequestConfig) => {
      const token = getAccessToken();

      if (token && config.headers) {
         config.headers.Authorization = `Bearer ${token}`;
      }

      return config;
   },
   (error) => Promise.reject(error)
);

client.interceptors.response.use(
   (response) => response,
   async (error: AxiosError) => {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

      if (error.response?.status === 401 && !originalRequest._retry) {
         originalRequest._retry = true;

         try {
            const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {}, { withCredentials: true });
            const newToken = (response.data as { accessToken: string }).accessToken;

            setAccessToken(newToken);
            notifyTokenRefresh(newToken);

            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            return client(originalRequest);
         } catch {
            notifyTokenRefresh(null);
         }
      }

      return Promise.reject(error);
   }
);