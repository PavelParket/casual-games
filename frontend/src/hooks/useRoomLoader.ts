import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AsyncThunkAction } from "@reduxjs/toolkit";
import type { Room } from "../models/Room";
import type { AppDispatch, RootState } from "../store/store";
import { findByGuid } from "../store/slices/UserSlice";

interface UseRoomLoaderOptions {
    fetchRoom: (roomId: string) => AsyncThunkAction<Room, unknown, object>;
    selectRoom: (state: RootState) => Room | undefined;
    selectError: (state: RootState) => string | null | undefined;
    clearRoomState: () => { type: string };
}

interface UseRoomLoaderResult {
    isLoading: boolean;
    error: string | null;
    roomId: string | undefined;
    guid: string | undefined;
}

function extractMessage(err: unknown): string {
    if (typeof err === "string") {
        return err;
    }

    if (typeof err === "object" && err !== null && "message" in err) {
        const m = (err as Record<string, unknown>)["message"];

        if (typeof m === "string") {
            return m;
        }
    }

    return "Failed to load room";
}

export function useRoomLoader({
    fetchRoom,
    selectRoom,
    selectError,
    clearRoomState,
}: UseRoomLoaderOptions): UseRoomLoaderResult {
    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();

    const roomId = useParams<{ roomId?: string }>().roomId;
    const guid = useSelector((state: RootState) => state.auth.user?.guid);

    const room = useSelector(selectRoom);
    const reduxError = useSelector(selectError);

    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);

    const fetchRoomRef = useRef(fetchRoom);
    useEffect(() => { fetchRoomRef.current = fetchRoom; }, [fetchRoom]);

    const clearRoomStateRef = useRef(clearRoomState);
    useEffect(() => { clearRoomStateRef.current = clearRoomState; }, [clearRoomState]);

    useEffect(() => {
        if (!roomId || !guid) {
            console.warn(`[RoomLoader] missing params: roomId=${!!roomId} guid=${!!guid}`);
            navigate("/rooms");
            return;
        }

        let cancelled = false;
        setIsLoading(true);
        setLoadError(null);

        dispatch(clearRoomStateRef.current());

        console.debug(`[RoomLoader] loading roomId=${roomId}`);

        Promise.all([
            dispatch(fetchRoomRef.current(roomId)).unwrap(),
            dispatch(findByGuid(guid)).unwrap(),
        ])
            .then(() => {
                if (!cancelled) {
                    console.debug(`[RoomLoader] loaded roomId=${roomId}`);
                }
            })
            .catch((err: unknown) => {
                if (cancelled) {
                    return;
                }
                const message = extractMessage(err);
                console.warn(`[RoomLoader] failed roomId=${roomId}:`, message);
                setLoadError(message);
            })
            .finally(() => {
                if (!cancelled) {
                    setIsLoading(false);
                }
            });

        return () => {
            console.debug(`[RoomLoader] cleanup roomId=${roomId}`);
            cancelled = true;
            dispatch(clearRoomStateRef.current());
        };
    }, [dispatch, guid, navigate, roomId]);

    const effectivelyLoading = isLoading && !room;
    const effectiveError = loadError || reduxError || null;

    return {
        isLoading: effectivelyLoading,
        error: effectiveError,
        roomId,
        guid,
    };
}
