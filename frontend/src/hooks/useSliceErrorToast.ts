import type { ActionCreatorWithPayload } from "@reduxjs/toolkit";
import type { AppDispatch, RootState } from "../store/store";
import { useDispatch, useSelector } from "react-redux";
import { useSystemToastContext } from "../providers/SystemToastContext";
import { useEffect, useRef } from "react";
import { errorCodeMessages } from "../models/constants/ErrorCodeMessages";

type ErrorsSelector = (state: RootState) => Record<string, string | null>;

export function useSliceErrorToast(errorsSelector: ErrorsSelector, clearError: ActionCreatorWithPayload<string>,) {
    const dispatch = useDispatch<AppDispatch>();
    const { showSystemToast } = useSystemToastContext();

    const errors = useSelector(errorsSelector);
    const prevErrorsRef = useRef<Record<string, string | null>>({});

    useEffect(() => {
        const prevErrors = prevErrorsRef.current;

        Object.entries(errors).forEach(([key, message]) => {
            if (message !== null && message !== prevErrors[key]) {
                const text = message
                    || errorCodeMessages[key]
                    || errorCodeMessages.DEFAULT;
                showSystemToast(text, "system-error");
                dispatch(clearError(key));
            }
        });

        prevErrorsRef.current = errors;
    }, [clearError, dispatch, errors, showSystemToast]);
}
