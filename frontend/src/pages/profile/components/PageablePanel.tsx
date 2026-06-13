import type { ReactNode } from "react";
import { Box, Typography, Divider, Button, Icon, useThemedIcon } from "../../../ui";
import { Skeleton } from "../../../ui/components/common/Skeleton";
import "../style/pageablepanel.css";

interface PageablePanelProps {
    title: string;
    headerActions?: ReactNode;
    topRightAction?: ReactNode;
    isLoading: boolean;
    isEmpty: boolean;
    emptyMessage?: string | ReactNode;
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    children: ReactNode;
}

export function PageablePanel({
    title,
    headerActions,
    topRightAction,
    isLoading,
    isEmpty,
    emptyMessage = "No records found.",
    currentPage,
    totalPages,
    onPageChange,
    children
}: PageablePanelProps) {
    const { getIcon } = useThemedIcon();

    return (
        <Box className="pageable-panel">
            <Box className="pageable-panel-header">
                <Typography variant="h3" className="pageable-panel-title">{title}</Typography>

                {headerActions && (
                    <Box className="pageable-panel-actions">
                        {headerActions}
                    </Box>
                )}

                {topRightAction && (
                    <Box className="pageable-panel-top-right">
                        {topRightAction}
                    </Box>
                )}
            </Box>

            <Box className="custom-scrollbar pageable-panel-content">
                {isLoading ? (
                    <Box style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                        <Skeleton variant="rectangular" height={45} count={4} />
                    </Box>
                ) : isEmpty ? (
                    typeof emptyMessage === "string" ? (
                        <Typography variant="body" style={{ textAlign: "center", opacity: 0.6, padding: "2rem 0" }}>
                            {emptyMessage}
                        </Typography>
                    ) : (
                        <Box style={{ textAlign: "center", opacity: 0.6, padding: "2rem 0" }}>
                            {emptyMessage}
                        </Box>
                    )
                ) : (
                    children
                )}
            </Box>

            <Divider />

            <Box className="pageable-panel-footer">
                <Button variant="ghost" disabled={currentPage === 0 || isLoading || totalPages <= 1} onClick={() => onPageChange(0)}>
                    <Icon src={getIcon("doubleLeftArrow")} alt="first" size={16} />
                </Button>
                <Button variant="ghost" disabled={currentPage === 0 || isLoading || totalPages <= 1} onClick={() => onPageChange(currentPage - 1)}>
                    <Icon src={getIcon("leftArrow")} alt="prev" size={16} />
                </Button>

                <Typography variant="caption" style={{ fontVariantNumeric: "tabular-nums" }}>
                    Page {totalPages === 0 ? 1 : currentPage + 1} of {totalPages || 1}
                </Typography>

                <Button variant="ghost" disabled={currentPage >= totalPages - 1 || isLoading || totalPages <= 1} onClick={() => onPageChange(currentPage + 1)}>
                    <Icon src={getIcon("rightArrow")} alt="next" size={16} />
                </Button>
                <Button variant="ghost" disabled={currentPage >= totalPages - 1 || isLoading || totalPages <= 1} onClick={() => onPageChange(totalPages - 1)}>
                    <Icon src={getIcon("doubleRightArrow")} alt="last" size={16} />
                </Button>
            </Box>
        </Box>
    );
}
