import { Box, Stack, Typography } from "../ui";

interface HistoryItemProps {
    variant: 'income' | 'expense' | 'neutral';
    iconText: string;
    title: string;
    date: string;
    rightText: string;
    rightSubText?: string;
}

export function HistoryItem({ variant, iconText, title, date, rightText, rightSubText }: HistoryItemProps) {
    const colorType = variant === 'neutral' ? 'border' : variant;

    return (
        <Box style={{
            padding: "8px 12px",
            display: "grid",
            gridTemplateColumns: rightSubText ? "32px 1fr auto 90px" : "32px 1fr auto",
            gap: "12px",
            alignItems: "center",
            background: variant === 'neutral' ? 'var(--color-bg)' : `var(--color-${colorType}-bg)`,
            border: `1px solid var(--color-${colorType}-border)`,
            borderRadius: "var(--radius-sm)",
            transition: "transform 0.2s ease"
        }}>
            <Box style={{
                width: "32px", height: "32px", borderRadius: "50%",
                background: variant === 'neutral' ? 'var(--color-border)' : `var(--color-${colorType}-icon-bg)`,
                display: "flex", alignItems: "center", justifyContent: "center",
                color: variant === 'neutral' ? 'var(--color-text)' : "#ffffff",
                fontWeight: "bold", fontSize: "1.1rem"
            }}>
                {iconText}
            </Box>

            <Stack gap="2px" justify="center" style={{ overflow: "hidden" }}>
                <Typography variant="body" style={{ fontWeight: 600, fontSize: "0.95rem", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
                    {title}
                </Typography>
                <Typography variant="caption" style={{ opacity: 0.6, fontSize: "0.75rem" }}>
                    {date}
                </Typography>
            </Stack>

            <Typography variant="body" style={{
                fontWeight: "600",
                color: variant === 'neutral' ? 'var(--color-text)' : `var(--color-${colorType}-text)`,
                fontSize: "1rem", textAlign: "right"
            }}>
                {rightText}
            </Typography>

            {rightSubText && (
                <Stack gap="0px" style={{ alignItems: "flex-start", minWidth: "90px" }}>
                    <Typography variant="caption" style={{ opacity: 0.5, fontSize: "0.65rem" }}>Balance info:</Typography>
                    <Typography variant="caption" style={{ opacity: 0.8, fontSize: "0.7rem", fontFamily: "monospace" }}>
                        {rightSubText}
                    </Typography>
                </Stack>
            )}
        </Box>
    );
}
