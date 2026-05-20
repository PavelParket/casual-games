import type { ReactNode } from "react";
import LoadingPage from "../../LoadingPage";
import InvalidRoomPage from "../InvalidRoomPage";

interface RoomShellProps {
    isLoading: boolean;
    error: string | null;
    children: ReactNode;
}

export default function RoomShell({ isLoading, error, children }: RoomShellProps) {
    if (isLoading) {
        return <LoadingPage />;
    }

    if (error) {
        return <InvalidRoomPage message={error} />;
    }

    return <>{children}</>;
}
