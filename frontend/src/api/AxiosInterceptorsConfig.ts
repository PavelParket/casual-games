import { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { client } from './AxiosConfig';
import type { AppDispatch, RootState } from '../store/store';
import { logout } from '../store/slices/AuthSlice';
import { ensureFreshToken, initEnsureFreshToken } from './EnsureFreshToken';
import { extractErrorResponse } from '../helpers/ApiErrorHelper';
import { AUTH_ERROR_POLICY, isAuthErrorCode } from '../models/constants/AuthErrorCode';

const AUTH_ENDPOINTS = ['auth/login', 'auth/register', 'auth/refresh', 'auth/logout'] as const;

const isAuthEndpoint = (url: string): boolean =>
    AUTH_ENDPOINTS.some(endpoint => url.includes(endpoint));

const isRefreshEndpoint = (url: string): boolean =>
    url.includes('auth/refresh');

export const AxiosInterceptorsConfig = (store: { getState: () => RootState; dispatch: AppDispatch }) => {
    initEnsureFreshToken(store);

    client.interceptors.request.use((config) => {
        const token = store.getState().auth.user?.accessToken;

        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    });

    client.interceptors.response.use(
        (response) => response,
        async (error: AxiosError) => {
            const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
            const requestUrl = originalRequest.url ?? '';

            if (error.response?.status === 401 && isRefreshEndpoint(requestUrl)) {
                const { errorCode } = extractErrorResponse(error, 'Session expired');

                const policy = errorCode && isAuthErrorCode(errorCode)
                    ? AUTH_ERROR_POLICY[errorCode]
                    : null;

                const action = policy?.action ?? 'logout';

                if (action !== 'silent') {
                    // Phase 7: if (policy?.notify) showToast(policy.messageKey)
                    await store.dispatch(logout());
                }

                return Promise.reject(error);
            }

            if (isAuthEndpoint(requestUrl)) {
                return Promise.reject(error);
            }

            if (error.response?.status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;

                try {
                    const newToken = await ensureFreshToken();

                    if (originalRequest.headers) {
                        originalRequest.headers.Authorization = `Bearer ${newToken}`;
                    }

                    return client(originalRequest);
                } catch {
                    await store.dispatch(logout());
                    return Promise.reject(error);
                }
            }

            return Promise.reject(error);
        }
    );
};
