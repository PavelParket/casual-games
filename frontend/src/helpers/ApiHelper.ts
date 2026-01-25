import { AuthAPI } from "../api/AuthApi";
import type { AuthUser } from "../models/AuthenticationUser";

const createApiHelper = () => {
    let refreshPromise: Promise<AuthUser> | null = null;

    const refresh = (): Promise<AuthUser> => {
        if (!refreshPromise) {
            refreshPromise = AuthAPI.refresh()
                .then(response => {
                    if (response.status === 200 && response.data) {
                        return response.data;
                    }

                    return Promise.reject("Refresh failed!");
                })
                .finally(() => {
                    refreshPromise = null;
                });
        }

        return refreshPromise;
    };

    return { refresh };
};

export const ApiHelper = {
    ...createApiHelper(),
};
