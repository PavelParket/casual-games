import { Link } from "react-router-dom";
import { Box, Container, Stack, Typography } from "../ui";

export default function Footer() {
    return (
        <footer style={{
            marginTop: "auto",
            background: "var(--glass-surface)",
            backdropFilter: `blur(var(--glass-blur))`,
            boxShadow: "var(--shadow-sm)",
            paddingTop: "1.5rem",
        }}>
            <Container>
                <Stack gap="1rem">
                    <Box style={{
                        display: "flex",
                        flexWrap: "wrap",
                        justifyContent: "space-between",
                        alignItems: "center",
                        gap: "1rem"
                    }}>
                        <Stack direction="row" gap="1rem" align="center" wrap="wrap">
                            <Typography variant="caption" style={{ opacity: 0.5, fontSize: "0.75rem" }}>
                                Copyright © {new Date().getFullYear()} Casual Games. All right reserved.
                            </Typography>

                            <Stack direction="row" gap="0.75rem" align="center" style={{ fontSize: "0.75rem" }}>
                                <Link to="/privacy" className="link footer-link" style={{
                                    fontSize: "0.75rem",
                                    opacity: 0.6,
                                    transition: "opacity 0.2s ease"
                                }}>
                                    Privacy Policy
                                </Link>
                                <span style={{ opacity: 0.3, color: "var(--color-text)" }}>|</span>
                                <Link to="/terms" className="link footer-link" style={{
                                    fontSize: "0.75rem",
                                    opacity: 0.6,
                                    transition: "opacity 0.2s ease"
                                }}>
                                    Terms of Use
                                </Link>
                            </Stack>
                        </Stack>

                        <Typography variant="caption" style={{ opacity: 0.5, fontSize: "0.75rem" }}>
                            Built with custom UI: Pavel & Timofei
                        </Typography>
                    </Box>

                </Stack>
            </Container >
        </footer >
    );
}
