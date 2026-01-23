import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { store } from '../store/store';
import { logout, refresh } from '../store/slices/AuthSlice';

const API_BASE_URL = 'http://localhost:8080';

export const client = axios.create({
   baseURL: API_BASE_URL,
   withCredentials: true,
   headers: {
      'Content-Type': 'application/json',
   },
});

client.interceptors.request.use((config) => {
   const token = store.getState().auth.user?.accessToken;

   if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
   }

   return config;
});

client.interceptors.response.use(
   (response) => response,
   async (error: AxiosError) => {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

      if (error.response?.status === 401 && !originalRequest._retry) {
         originalRequest._retry = true;

         try {
            const action = await store.dispatch(refresh());

            if (refresh.fulfilled.match(action)) {
               const newToken = action.payload.accessToken;

               if (originalRequest.headers) {
                  originalRequest.headers.Authorization = `Bearer ${newToken}`;
               }

               return client(originalRequest);
            }

            store.dispatch(logout());
            return Promise.reject(error);
         } catch {
            store.dispatch(logout());
            return Promise.reject(error);
         }
      }

      return Promise.reject(error);
   }
);
