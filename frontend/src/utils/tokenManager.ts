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