import axios from "axios";
import { jwtDecode } from "jwt-decode";

const API_BASE_URL = 'http://localhost:8080';
let refreshInterval: ReturnType<typeof setInterval> | null = null;

let accessToken: string | null = null;
let onTokenRefresh: ((token: string | null) => void) | null = null;

export const setAccessToken = (token: string | null) => {
   accessToken = token;
}

export const getAccessToken = () => accessToken;

export const setOnTokenRefresh = (callback: (token: string | null) => void) => {
   onTokenRefresh = callback;
};

export const notifyTokenRefresh = (token: string | null) => {
   onTokenRefresh?.(token);
};

const checkToken = async (token: string) => {
   try {
      const decoded = jwtDecode<{ exp: number }>(token);
      const expiresIn = decoded.exp * 1000 - Date.now();
      const fiveMinutes = 5 * 60 * 1000;

      if (expiresIn < fiveMinutes && expiresIn > 0) {
         const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {}, { withCredentials: true });
         const newToken = response.data.accessToken;
         setAccessToken(newToken);
         notifyTokenRefresh(newToken);
      }
   } catch (error) {
      console.error('Proactive token refresh failed:', error);
   }

   return token;
};

export const startTokenTimer = (token: string) => {
   stopTokenTimer();

   checkToken(token);

   refreshInterval = setInterval(async () => {
      const current = getAccessToken();

      if (current) {
         await checkToken(current);
      } else {
         stopTokenTimer();
      }
   }, 2 * 60 * 100);
};

export const stopTokenTimer = () => {
   if (refreshInterval) {
      clearInterval(refreshInterval);
      refreshInterval = null;
   }
};