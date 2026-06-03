export const AUTH_ERROR_CODE = {
    NO_SESSION: 'NO_SESSION',
    INVALID_TOKEN: 'INVALID_TOKEN',
    SESSION_REVOKED: 'SESSION_REVOKED',
} as const;

export type AuthErrorCode = keyof typeof AUTH_ERROR_CODE;

export interface AuthErrorPolicyConfig {
    action: 'silent' | 'logout';
    notify: boolean;
    messageKey?: string;
}

export const AUTH_ERROR_POLICY: Record<AuthErrorCode, AuthErrorPolicyConfig> = {
    NO_SESSION: { action: 'silent', notify: false },
    INVALID_TOKEN: { action: 'logout', notify: false },
    SESSION_REVOKED: { action: 'logout', notify: true, messageKey: 'SESSION_REVOKED' },
};

export const isAuthErrorCode = (code: string): code is AuthErrorCode =>
    code in AUTH_ERROR_POLICY;
