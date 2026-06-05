import { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { client } from './AxiosConfig';
import type { AppDispatch, RootState } from '../store/store';
import { logout } from '../store/slices/AuthSlice';
import { ensureFreshToken, initEnsureFreshToken } from './EnsureFreshToken';
import { extractErrorResponse } from '../helpers/ApiErrorHelper';
import { AUTH_ERROR_POLICY, isAuthErrorCode, type AuthErrorCode } from '../models/constants/AuthErrorCode';
import type { ToastVariant } from '../ui';
import { errorCodeMessages } from '../models/constants/ErrorCodeMessages';

// --- Toast init ---

type ShowToast = (message: string, variant: ToastVariant) => void;

let _showToast: ShowToast | null = null;

export const initAuthToast = (fn: ShowToast): void => {
    _showToast = fn;
};

// --- Endpoint helpers ---

const AUTH_ENDPOINTS = ["auth/login", "auth/register", "auth/refresh", "auth/logout"] as const;

const isAuthEndpoint = (url: string): boolean =>
    AUTH_ENDPOINTS.some(endpoint => url.includes(endpoint));

const isRefreshEndpoint = (url: string): boolean =>
    url.includes("auth/refresh");

// --- Interceptor setup ---

export const AxiosInterceptorsConfig = (store: { getState: () => RootState; dispatch: AppDispatch }) => {
    initEnsureFreshToken(store);

    let _logoutPromise: Promise<void> | null = null;

    const dispatchLogoutOnce = (): Promise<void> => {
        if (!_logoutPromise) {
            _logoutPromise = Promise.resolve()
                .then(() => store.dispatch(logout()))
                .then(() => { })
                .finally(() => { _logoutPromise = null });
        }

        return _logoutPromise;
    };

    const applyAuthErrorPolicy = async (errorCode: AuthErrorCode): Promise<void> => {
        const policy = AUTH_ERROR_POLICY[errorCode];

        if (policy.notify && policy.messageKey) {
            const message = errorCodeMessages[policy.messageKey] ?? errorCodeMessages.DEFAULT;
            _showToast?.(message, "system-error");
        }

        if (policy.action !== "silent") {
            await dispatchLogoutOnce();
        }
    };

    // --- Request interceptor ---

    client.interceptors.request.use((config) => {
        const token = store.getState().auth.user?.accessToken;

        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    });

    // --- Response interceptor ---

    client.interceptors.response.use(
        (response) => response,
        async (error: AxiosError) => {
            const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
            const requestUrl = originalRequest.url ?? '';

            if (error.response?.status === 401 && isRefreshEndpoint(requestUrl)) {
                const { errorCode } = extractErrorResponse(error, "Session expired");

                if (errorCode && isAuthErrorCode(errorCode)) {
                    await applyAuthErrorPolicy(errorCode);
                } else {
                    await dispatchLogoutOnce();
                }

                return Promise.reject(error);
            }

            if (isAuthEndpoint(requestUrl)) {
                return Promise.reject(error);
            }

            if (error.response?.status === 401 && !originalRequest._retry) {
                originalRequest._retry = true;

                const { errorCode } = extractErrorResponse(error, "");

                if (errorCode && isAuthErrorCode(errorCode)) {
                    await applyAuthErrorPolicy(errorCode);
                    return Promise.reject(error);
                }

                try {
                    const newToken = await ensureFreshToken();

                    if (originalRequest.headers) {
                        originalRequest.headers.Authorization = `Bearer ${newToken}`;
                    }

                    return client(originalRequest);
                } catch {
                    await dispatchLogoutOnce();
                    return Promise.reject(error);
                }
            }

            return Promise.reject(error);
        }
    );
};
