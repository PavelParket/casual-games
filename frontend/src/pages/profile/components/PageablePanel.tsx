import type { ReactNode } from "react";
import { Box, Typography, Stack, Divider, Button, Icon, useThemedIcon } from "../../../ui";
import { Skeleton } from "../../../ui/components/common/Skeleton";

interface PageablePanelProps {
    title: string;
    headerActions?: ReactNode;
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
        <Box style={{
            background: "var(--color-bg-glass)",
            backdropFilter: "blur(10px)",
            padding: "1rem",
            borderRadius: "var(--radius-md)",
            border: "1px solid var(--color-border)",
            boxShadow: "var(--shadow-sm)",
            display: "flex",
            flexDirection: "column",
            height: "380px"
        }}>
            <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                <Typography variant="h3">{title}</Typography>
                <Stack direction="row" gap="0.5rem" align="center">
                    {headerActions}
                </Stack>
            </Box>

            <Box style={{
                flex: 1,
                overflowY: "auto",
                paddingRight: "8px",
                display: "flex",
                flexDirection: "column",
                gap: "0.5rem"
            }}>
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

            <Box style={{
                marginTop: "auto",
                paddingTop: "0.5rem",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                gap: "1rem",
                flexShrink: 0
            }}>
                <Button
                    variant="ghost"
                    disabled={currentPage === 0 || isLoading || totalPages <= 1}
                    onClick={() => onPageChange(0)}
                    style={{ padding: "0.25rem 0.5rem" }}
                >
                    <Icon src={getIcon("doubleLeftArrow")} alt="first" size={16} />
                </Button>
                <Button
                    variant="ghost"
                    disabled={currentPage === 0 || isLoading || totalPages <= 1}
                    onClick={() => onPageChange(currentPage - 1)}
                    style={{ padding: "0.25rem 0.5rem" }}
                >
                    <Icon src={getIcon("leftArrow")} alt="prev" size={16} />
                </Button>

                <Typography variant="caption" style={{ fontVariantNumeric: "tabular-nums" }}>
                    Page {totalPages === 0 ? 1 : currentPage + 1} of {totalPages || 1}
                </Typography>

                <Button
                    variant="ghost"
                    disabled={currentPage >= totalPages - 1 || isLoading || totalPages <= 1}
                    onClick={() => onPageChange(currentPage + 1)}
                    style={{ padding: "0.25rem 0.5rem" }}
                >
                    <Icon src={getIcon("rightArrow")} alt="next" size={16} />
                </Button>
                <Button
                    variant="ghost"
                    disabled={currentPage >= totalPages - 1 || isLoading || totalPages <= 1}
                    onClick={() => onPageChange(totalPages - 1)}
                    style={{ padding: "0.25rem 0.5rem" }}
                >
                    <Icon src={getIcon("doubleRightArrow")} alt="last" size={16} />
                </Button>
            </Box>
        </Box>
    );
}
