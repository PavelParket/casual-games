import { useNavigate } from "react-router-dom";
import { Button, Card, Container, Typography } from "../../ui";

interface InvalidRoomPageProps {
    message: string;
}

export default function InvalidRoomPage({ message }: InvalidRoomPageProps) {
    const navigate = useNavigate();

    return (
        <Container>
            <Card style={{ textAlign: "center", padding: "2rem", marginTop: "4rem" }}>
                <Typography variant="h2" style={{ marginBottom: "1rem" }}>
                    Unable to join room
                </Typography>
                <Typography
                    variant="body"
                    style={{ color: "var(--color-text-secondary)", marginBottom: "2rem" }}
                >
                    {message}
                </Typography>
                <Button onClick={() => navigate("/rooms")}>
                    Back to Rooms
                </Button>
            </Card>
        </Container>
    );
}
