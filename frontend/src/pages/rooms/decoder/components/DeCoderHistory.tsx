import { useMemo, useState } from "react";
import { Box, Button, Icon, Input, Stack, Typography, useThemedIcon } from "../../../../ui";
import type { DeCoderGameHistory } from "../../../../models/DeCoderGameHistory";
import "../styles/DeCoderRoom.css";

interface DeCoderHistoryProps {
    history: DeCoderGameHistory[];
    onRequestSync: () => void;
}

export function DeCoderHistory({ history, onRequestSync }: DeCoderHistoryProps) {
    const { getIcon } = useThemedIcon();
    const [searchQuery, setSearchQuery] = useState("");

    const displayedHistory = useMemo(() => {
        let filtered = history;
        if (searchQuery) {
            filtered = history.filter((item) => item.code.includes(searchQuery));
        }
        return [...filtered].reverse();
    }, [history, searchQuery]);

    return (
        <Box
            style={{
                display: "flex",
                flexDirection: "column",
                height: "100%",
                minHeight: 0,
                padding: "0.75rem",
                background: "var(--color-bg-secondary)",
                borderRadius: "var(--radius-md)",
                border: "1px solid var(--color-border)",
            }}
        >
            <Box
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginBottom: "1rem",
                }}
            >
                <Typography variant="h3">Code Terminal</Typography>
                <Button
                    variant="ghost"
                    onClick={onRequestSync}
                    style={{ fontSize: "0.8rem", padding: "7px" }}
                    title="Sync State"
                >
                    <Icon src={getIcon("refresh")} alt="refresh" size={18} />
                </Button>
            </Box>

            <Input
                value={searchQuery}
                onChange={(e) =>
                    setSearchQuery(
                        e.target.value
                            .replace(/[^A-Za-z]/g, "")
                            .toUpperCase()
                            .slice(0, 4)
                    )
                }
                placeholder="Search history (e.g. ABCD)"
                style={{ marginBottom: "1rem", width: "100%" }}
            />

            <Box
                className="custom-scrollbar"
                style={{
                    flex: 1,
                    minHeight: 0,
                    maxHeight: "390px",
                    overflowY: "auto",
                    paddingRight: "8px",
                    background: "var(--color-bg-soft)",
                    display: "grid",
                    alignContent: "start",
                    gap: "8px",
                    boxShadow: "inset 0 2px 4px rgba(0,0,0,0.05)",
                }}
            >
                {displayedHistory.length === 0 ? (
                    <Typography
                        variant="body"
                        style={{ textAlign: "center", opacity: 0.5, marginTop: "2rem" }}
                    >
                        {history.length === 0 ? "No moves yet. Be the first!" : "No matches found"}
                    </Typography>
                ) : (
                    displayedHistory.map((item, idx) => (
                        <Box
                            key={idx}
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                padding: "8px 12px",
                                background: "var(--color-bg)",
                                borderRadius: "var(--radius-sm)",
                                border: "1px solid var(--color-border)",
                            }}
                        >
                            <Typography
                                variant="body"
                                style={{
                                    fontFamily: "monospace",
                                    fontSize: "clamp(0.95rem, 0.8rem + 0.7vw, 1.2rem)",
                                    letterSpacing: "1px",
                                    fontWeight: "bold",
                                }}
                            >
                                {item.code}
                            </Typography>
                            <Stack direction="row" gap="1rem">
                                <Box style={{ display: "flex", alignItems: "center", gap: "4px" }}>
                                    <span title="Exact Match" style={{ fontSize: "0.8rem" }}>
                                        Exact:
                                    </span>
                                    <Typography
                                        variant="body"
                                        style={{ color: "var(--color-success, #2ecc71)", fontWeight: "bold" }}
                                    >
                                        {item.exactMatch}
                                    </Typography>
                                </Box>
                                <Box style={{ display: "flex", alignItems: "center", gap: "4px" }}>
                                    <span title="Partial Match" style={{ fontSize: "0.8rem" }}>
                                        Partial:
                                    </span>
                                    <Typography
                                        variant="body"
                                        style={{ color: "var(--color-warning, #f1c40f)", fontWeight: "bold" }}
                                    >
                                        {item.partialMatch}
                                    </Typography>
                                </Box>
                            </Stack>
                        </Box>
                    ))
                )}
            </Box>
        </Box>
    );
}
