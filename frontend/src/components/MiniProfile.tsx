import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { getPlayersMiniProfiles } from "../store/slices/UserSlice";
import { Box, Typography, Icon, useThemedIcon, Divider, Avatar } from "../ui";
import { Skeleton } from "../ui/components/common/Skeleton";
import type { Icons } from "../assets/icons";

interface MiniProfileProps {
    guid: string;
    username: string;
    avatarUrl?: string | null;
    children: React.ReactNode;
}

const getStatusIconName = (status: string): keyof typeof Icons.light => {
    return `${status.toLowerCase()}Status` as keyof typeof Icons.light;
};

export function MiniProfile({ guid, username, children }: MiniProfileProps) {
    const dispatch = useDispatch<AppDispatch>();
    const { getIcon } = useThemedIcon();

    const profile = useSelector((state: RootState) => state.user.playersMiniProfiles[guid]);
    const isLoading = useSelector((state: RootState) => state.user.isLoadingPlayersMiniProfiles[guid]);

    const [isOpen, setIsOpen] = useState(false);
    const [coords, setCoords] = useState({ top: 0, left: 0 });

    const triggerRef = useRef<HTMLDivElement>(null);
    const menuRef = useRef<HTMLDivElement>(null);

    const enterTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const leaveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const handleMouseEnter = () => {
        if (leaveTimerRef.current) clearTimeout(leaveTimerRef.current);

        if (!isOpen) {
            enterTimerRef.current = setTimeout(() => {
                if (triggerRef.current) {
                    const rect = triggerRef.current.getBoundingClientRect();
                    setCoords({
                        top: rect.bottom + window.scrollY + 8,
                        left: rect.left + window.scrollX,
                    });
                    dispatch(getPlayersMiniProfiles(guid));
                }
                setIsOpen(true);
            }, 500);
        }
    };

    const handleMouseLeave = () => {
        if (enterTimerRef.current) clearTimeout(enterTimerRef.current);

        leaveTimerRef.current = setTimeout(() => {
            setIsOpen(false);
        }, 500);
    };

    useEffect(() => {
        return () => {
            if (enterTimerRef.current) clearTimeout(enterTimerRef.current);
            if (leaveTimerRef.current) clearTimeout(leaveTimerRef.current);
        };
    }, []);

    useEffect(() => {
        const handleScroll = () => {
            if (isOpen) setIsOpen(false);
        };

        if (isOpen) {
            window.addEventListener("scroll", handleScroll, true);
        }

        return () => {
            window.removeEventListener("scroll", handleScroll, true);
        };
    }, [isOpen]);

    const showSkeleton = isLoading && !profile;

    const menuContent = isOpen ? createPortal(
        <div
            ref={menuRef}
            className="menu-list"
            onMouseEnter={handleMouseEnter}
            onMouseLeave={handleMouseLeave}
            style={{
                position: "absolute",
                top: coords.top,
                left: coords.left,
                zIndex: 9999,
                minWidth: "200px",
                padding: "0.5rem"
            }}
        >
            <Box style={{
                padding: "0.5rem 1rem",
                display: "flex",
                alignItems: "center",
                gap: "1rem"
            }}>
                <Avatar src={profile.avatarUrl} fallback={username} size={40} />
                <Typography
                    variant="body"
                    style={{
                        fontWeight: 600,
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap"
                    }}
                    title={username}
                >
                    {username}
                </Typography>
            </Box>

            <Divider style={{ margin: "0.25rem 0" }} />

            <Box style={{
                padding: "0.5rem 1rem",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: "0.75rem"
            }}>
                <Box style={{
                    width: "24px",
                    height: "24px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    flexShrink: 0
                }}>
                    {showSkeleton ? (
                        <Skeleton variant="circular" width="24px" height="24px" />
                    ) : (
                        <Icon src={getIcon(getStatusIconName(profile.status))} alt={profile.status} size={24} />
                    )}
                </Box>

                <Box style={{ display: "flex", alignItems: "center" }}>
                    {showSkeleton ? (
                        <Skeleton variant="text" width="60px" height="18px" />
                    ) : (
                        <Typography variant="caption" style={{ textTransform: "capitalize", fontWeight: 500, fontSize: "0.9rem" }}>
                            {profile.status}
                        </Typography>
                    )}
                </Box>
            </Box>
        </div>,
        document.body
    ) : null;

    return (
        <>
            <div
                ref={triggerRef}
                onMouseEnter={handleMouseEnter}
                onMouseLeave={handleMouseLeave}
                style={{ display: "inline-block" }}
            >
                {children}
            </div>
            {menuContent}
        </>
    );
}