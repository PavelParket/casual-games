import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { client } from './AxiosConfig';
import type { AppDispatch, RootState } from '../store/store';
import { logout, refresh } from '../store/slices/AuthSlice';

const AUTH_ENDPOINTS = ['auth/login', 'auth/register', 'auth/refresh', 'auth/logout'] as const;

const isAuthEndpoint = (url: string): boolean => {
   return AUTH_ENDPOINTS.some(endpoint => url.includes(endpoint));
};

const isRefreshEndpoint = (url: string): boolean => {
   return url.includes('auth/refresh');
};

export const AxiosInterceptorsConfig = (store: { getState: () => RootState; dispatch: AppDispatch }) => {
   axios.interceptors.request.use((config) => {
      const token = store.getState().auth.user?.accessToken;

      if (token && config.headers) {
         config.headers.Authorization = `Bearer ${token}`
      }

      return config;
   });

   axios.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
         const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
         const requestUrl = originalRequest.url || '';

         if (isRefreshEndpoint(requestUrl) && error.response?.status === 401) {
            await store.dispatch(logout());
            return Promise.reject(error);
         }

         if (isAuthEndpoint(requestUrl)) {
            return Promise.reject(error);
         }

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

               await store.dispatch(logout());
               return Promise.reject(error);
            } catch {
               await store.dispatch(logout());
               return Promise.reject(error);
            }
         }

         return Promise.reject(error);
      }
   );
};
