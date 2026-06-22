import type { WSMessage } from "../models/WsMessage";

export const validateInput = (input: string): string => {
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

export const validateRoomName = (name: string): string => {
    if (!name) {
        return "";
    }

    return name
        .replace(/[^a-zA-Zа-яА-Я0-9\s\-_]/g, '')
        .replace(/\s+/g, " ")
        .trim()
        .slice(0, 50);
};

export const validateEmail = (email: string): string => {
    if (!email) {
        return "";
    }

    return email
        .trim()
        .replace(/\s+/g, "")
        .toLowerCase()
        .slice(0, 200);
};

export const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
};

export const validateUsername = (username: string): string => {
    if (!username) {
        return "";
    }

    return username
        .replace(/[^a-zA-Zа-яА-Я0-9_]/g, '')
        .trim()
        .slice(0, 50)
};

export const validateAmountInput = (input: string): string | null => {
    let val = input.replace(',', '.');

    if (val === '') {
        return '';
    }

    if (val.startsWith('.')) {
        val = '0' + val;
    }

    const regex = /^(0|[1-9]\d{0,4})(\.\d{0,2})?$/;

    if (regex.test(val)) {
        return val;
    }

    return null;
};

export const escapeHtml = (text: string): string => {
    if (!text) {
        return "";
    }

    const div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
};

export const validateToastMessage = (message: string): string => {
    if (!message) {
        return "";
    }

    return message
        .replace(/[<>]/g, '')
        .trim()
        .slice(0, 200);
};

export const validateWSMessage = <T extends WSMessage>(
    message: T,
    allowedFields: (keyof T)[]
): Partial<T> => {
    const validated: Partial<T> = {};

    for (const field of allowedFields) {
        const value = message[field];

        if (value === undefined || value === null) {
            continue;
        }

        validated[field] = validateValue(value) as T[keyof T];
    }

    return validated;
};

const validateValue = (value: unknown): unknown => {
    if (typeof value === "string") {
        return validateInput(value);
    }

    if (typeof value === "number" || typeof value === "boolean") {
        return value;
    }

    if (Array.isArray(value)) {
        return value.map(item => validateValue(item));
    }

    if (typeof value === "object" && value !== null) {
        const obj = value as Record<string, unknown>;
        const validatedObj: Record<string, unknown> = {};

        for (const key in obj) {
            if (Object.prototype.hasOwnProperty.call(obj, key)) {
                validatedObj[key] = validateValue(obj[key]);
            }
        }

        return validatedObj;
    }

    return value;
};

export const validateAndReadJpeg = (file: File): Promise<string> => {
    return new Promise((resolve, reject) => {
        const fileName = file.name.toLowerCase();

        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
            return reject("Please select a valid JPEG image (.jpg or .jpeg).");
        }

        const headerReader = new FileReader();

        headerReader.onloadend = (event) => {
            if (event.target?.readyState === FileReader.DONE) {
                const arr = new Uint8Array(event.target.result as ArrayBuffer);

                const hasJpegMagicNumbers = arr.length >= 2 && arr[0] === 0xFF && arr[1] === 0xD8;

                if (!hasJpegMagicNumbers) {
                    return reject("The file content does not match a valid JPEG image.");
                }

                const dataReader = new FileReader();
                dataReader.onload = () => resolve(dataReader.result as string);
                dataReader.onerror = () => reject("Failed to read the file data.");
                dataReader.readAsDataURL(file);
            }
        };

        headerReader.onerror = () => reject("Failed to analyze the file structure.");

        headerReader.readAsArrayBuffer(file.slice(0, 4));
    });
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
            localStorage.setItem(key, validateInput(value));
            return;
        }

        const validated = JSON.parse(
            JSON.stringify(value, (_, val) =>
                typeof val === "string" ? validateInput(val) : val
            )
        );

        localStorage.setItem(key, JSON.stringify(validated));
    } catch (error) {
        console.info(`Error setting localStorage key: ${key}`, error);
    }
};
