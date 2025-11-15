import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { getAccessToken, notifyTokenRefresh, setAccessToken } from '../utils/TokenManager';

const API_BASE_URL = 'http://localhost:8080';

export const client = axios.create({
   baseURL: API_BASE_URL,
   withCredentials: true,
   headers: {
      'Content-Type': 'application/json',
   },
});

let isRefreshing = false;
let failedQueue: Array<{
   resolve: (value: unknown) => void;
   reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
   failedQueue.forEach(prom => {
      if (error) {
         prom.reject(error);
      } else {
         prom.resolve(token);
      }
   });

   failedQueue = [];
};

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
         if (isRefreshing) {
            return new Promise((resolve, reject) => {
               failedQueue.push({ resolve, reject });
            }).then(token => {
               if (originalRequest.headers) {
                  originalRequest.headers.Authorization = `Bearer ${token}`;
               }

               return client(originalRequest);
            }).catch(error => {
               return Promise.reject(error);
            });
         }

         originalRequest._retry = true;
         isRefreshing = true;

         try {
            const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {}, { withCredentials: true });
            const newToken = (response.data as { accessToken: string }).accessToken;

            setAccessToken(newToken);
            notifyTokenRefresh(newToken);

            processQueue(null, newToken);

            if (originalRequest.headers) {
               originalRequest.headers.Authorization = `Bearer ${newToken}`;
            }

            return client(originalRequest);
         } catch (refreshError) {
            processQueue(refreshError, null);
            notifyTokenRefresh(null);
            return Promise.reject(refreshError);
         } finally {
            isRefreshing = false;
         }
      }

      return Promise.reject(error);
   }
);