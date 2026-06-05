import { jwtDecode } from "jwt-decode";
import type { AppDispatch, RootState } from "../store/store";
import { refresh } from "../store/slices/AuthSlice";
import { AuthBroadcast } from "./AuthBroadcast";

type Store = { getState: () => RootState; dispatch: AppDispatch };

const EXPIRING_SOON_MS = 5 * 60 * 1000;
const LOCK_NAME = 'auth-refresh';

let _store: Store | null = null;

export const initEnsureFreshToken = (store: Store): void => {
    _store = store;
};

const isExpiringSoon = (token: string): boolean => {
    try {
        const { exp } = jwtDecode<{ exp: number }>(token);
        return exp * 1000 - Date.now() < EXPIRING_SOON_MS;
    } catch {
        return true;
    }
};

const doRefresh = async (): Promise<string> => {
    const store = _store!;

    const current = store.getState().auth.user?.accessToken;
    if (current && !isExpiringSoon(current)) {
        return current;
    }

    const action = await store.dispatch(refresh());

    if (refresh.fulfilled.match(action)) {
        AuthBroadcast.postRefreshed(action.payload.accessToken);
        return action.payload.accessToken;
    }

    return Promise.reject(action.payload ?? 'Refresh failed');
};

export const ensureFreshToken = (): Promise<string> => {
    const tokenBeforeLock = _store?.getState().auth.user?.accessToken;

    if (!navigator?.locks) {
        return doRefresh();
    }

    return new Promise<string>((resolve, reject) => {
        navigator.locks.request(LOCK_NAME, async () => {
            try {
                const current = _store!.getState().auth.user?.accessToken;

                if (current && current !== tokenBeforeLock) {
                    resolve(current);
                    return;
                }

                resolve(await doRefresh());
            } catch (err) {
                reject(err);
            }
        });
    });
};
