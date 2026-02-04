import type { WSMessage } from "../types/ws";

export const sanitizeInput = (input: string): string => {
    if (!input) {
        return "";
    }

    return input
        .replace(/[<>\\"']/g, '')
        .replace(/javascript:/gi, '')
        .replace(/on\w+\s*=/gi, '')
        .trim()
        .slice(0, 255);
};

export const sanitizeRoomName = (name: string): string => {
    if (!name) {
        return "";
    }

    return name
        .replace(/[^a-zA-Zа-яА-Я0-9\s\-_]/g, '')
        .replace(/\s+/g, " ")
        .trim()
        .slice(0, 50);
};

export const sanitizeEmail = (email: string): string => {
    if (!email) {
        return "";
    }

    return email
        .trim()
        .replace(/\s+/g, "")
        .slice(0, 200);
};

export const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

export const sanitizeUsername = (username: string): string => {
    if (!username) {
        return "";
    }

    return username
        .replace(/[^a-zA-Zа-яА-Я0-9_]/g, '')
        .trim()
        .slice(0, 50)
};

export const escapeHtml = (text: string): string => {
    if (!text) {
        return "";
    }

    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
};

export const sanitizeToastMessage = (message: string): string => {
    if (!message) {
        return "";
    }

    return message
        .replace(/[<>]/g, '')
        .trim()
        .slice(0, 200);
};

export const sanitizeWSMessage = <T extends WSMessage>(
    message: T,
    allowedFields: (keyof T)[]
): Partial<T> => {
    const sanitized: Partial<T> = {};

    for (const field of allowedFields) {
        const value = message[field];

        if (value === undefined) {
            continue
        }

        if (typeof value === "string") {
            sanitized[field] = sanitizeInput(value) as T[keyof T];
        } else {
            sanitized[field] = value;
        }
    }

    return sanitized;
};

export const getSecureLocalStorage = <T>(key: string): T | null => {
    try {
        const item = localStorage.getItem(key);

        if (!item) {
            return null;
        }

        if (item.includes('<script') || item.includes('javascript:') || item.includes('onerror=')) {
            localStorage.removeItem(key);
            console.info(`Suspicious data detected in localStorage key: ${key}`);
            return null;
        }

        return JSON.parse(item);
    } catch (error) {
        console.info(`Error reading localStorage key: ${key}`, error);
        return null;
    }
};

export const setSecureLocalStorage = <T>(key: string, value: T): void => {
    try {
        if (typeof value === "string") {
            localStorage.setItem(key, sanitizeInput(value));
            return;
        }

        const sanitized = JSON.parse(
            JSON.stringify(value, (_, val) =>
                typeof val === "string" ? sanitizeInput(val) : val
            )
        );

        localStorage.setItem(key, JSON.stringify(sanitized));
    } catch (error) {
        console.info(`Error setting localStorage key: ${key}`, error);
    }
};
