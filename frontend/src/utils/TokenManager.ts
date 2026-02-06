import { jwtDecode } from "jwt-decode";

let refreshInterval: ReturnType<typeof setInterval> | null = null;

let accessToken: string | undefined;
let onRefreshRequired: (() => void) | null = null;

export const setTokenForManager = (token?: string) => {
   accessToken = token;
};

export const setOnRefreshRequired = (callback: () => void) => {
   onRefreshRequired = callback;
};

const checkToken = (token: string) => {
   try {
      const decoded = jwtDecode<{ exp: number }>(token);
      const expiresIn = decoded.exp * 1000 - Date.now();
      const fiveMinutes = 5 * 60 * 1000;

      if (expiresIn < fiveMinutes && expiresIn > 0) {
         onRefreshRequired?.();
      }
   } catch (error) {
      console.error('Token check failed:', error);
   }
};

export const startTokenTimer = (token?: string) => {
   if (!token) {
      return;
   }

   stopTokenTimer();

   checkToken(token);

   refreshInterval = setInterval(() => {
      const current = accessToken;

      if (current) {
         checkToken(current);
      } else {
         stopTokenTimer();
      }
   }, 2 * 60 * 1000);
};

export const stopTokenTimer = () => {
   if (refreshInterval) {
      clearInterval(refreshInterval);
      refreshInterval = null;
   }
};
