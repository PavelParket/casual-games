import { AxiosError } from 'axios';
export interface ErrorResponse {
    errorCode?: string;
    message: string;
    status?: number;
    timestamp?: string;
    path?: string;
    details?: Record<string, string[]>;
}

export function extractErrorResponse(err: unknown, fallbackMessage: string): ErrorResponse {
    const axiosError = err as AxiosError<ErrorResponse>;

    if (axiosError.response?.data) {
        const data = axiosError.response.data;

        const errorResponse: ErrorResponse = {
            errorCode: data.errorCode,
            message: data.message || fallbackMessage,
            status: data.status,
            timestamp: data.timestamp,
            path: data.path,
            details: data.details,
        };

        console.info("[ApiError]", {
            message: errorResponse.message,
            errorCode: errorResponse.errorCode,
            status: errorResponse.status,
            path: errorResponse.path,
            details: errorResponse.details,
        });

        return errorResponse;
    }

    console.info("[ApiError] Could not parse response, using fallback:", fallbackMessage);

    return { message: fallbackMessage };
}

export function extractErrorResponseMessage(err: unknown, fallback: string): string {
    return extractErrorResponse(err, fallback).message;
}
