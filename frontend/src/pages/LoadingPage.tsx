import { Box, Container, Typography } from "../ui";

export default function LoadingPage() {
    return (
        <Box
            style={{
                minHeight: "calc(100vh - 60px - 50px)",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
            }}
        >
            <style>{`
                .loading-spinner-container {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    width: 100%;
                    height: 120px;
                }

                .loading-spinner {
                    width: 60px;
                    height: 60px;
                    border: 4px solid var(--color-border);
                    border-top-color: var(--color-primary);
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                }

                @keyframes spin {
                    to {
                        transform: rotate(360deg);
                    }
                }

                .loading-spinner-container + * {
                    animation: pulse 2s ease-in-out infinite;
                }

                @keyframes pulse {
                    0%, 100% { opacity: 1; }
                    50% { opacity: 0.6; }
                }
            `}</style>

            <Container>
                <Box
                    style={{
                        textAlign: "center",
                        padding: "3rem",
                        background: "var(--glass-surface)",
                        backdropFilter: "blur(var(--glass-blur))",
                        borderRadius: "var(--radius-lg)",
                        boxShadow: "var(--shadow-lg)",
                    }}
                >
                    <div className="loading-spinner-container">
                        <div className="loading-spinner"></div>
                    </div>

                    <Typography variant="h2" style={{ marginTop: "2rem", marginBottom: "1rem" }}>
                        Loading...
                    </Typography>

                    <Typography variant="body" style={{ opacity: 0.8 }}>
                        Everything will be ready soon
                    </Typography>
                </Box>
            </Container>
        </Box>
    );
}
