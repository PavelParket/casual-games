import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { getTopWins } from "../../store/slices/BankSlice";
import { ROOM_TYPE_LABELS, type RoomType } from "../../models/Room";
import { Box, Button, Container, Card, Typography, Stack, Img, Accordion } from "../../ui";
import { Skeleton } from "../../ui/components/common/Skeleton";
import { useTheme } from "../../ui";
import "./Home.css";
import ticTacToeImg from "../../assets/images/img-tic-tac-toe.png";
import deCoderImg from "../../assets/images/img-de-coder.png";
import durakImg from "../../assets/images/img-durak.png";
import horseRaceImg from "../../assets/images/img-horse-race.png";

const GAME_IMAGES: Record<string, { src: string; invert: boolean }> = {
    DE_CODER: { src: deCoderImg, invert: true },
    DURAK: { src: durakImg, invert: false },
    HORSE_RACE: { src: horseRaceImg, invert: true },
    TIC_TAC_TOE: { src: ticTacToeImg, invert: true }
};

const AVAILABLE_GAMES: RoomType[] = ["DE_CODER", "DURAK", "HORSE_RACE", "TIC_TAC_TOE"];

export default function Home() {
    const { theme } = useTheme();
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();

    const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);
    const { topWins, isLoadingTopWins } = useSelector((state: RootState) => state.bank);

    const [isAccordionOpen, setIsAccordionOpen] = useState(true);

    const [windowWidth, setWindowWidth] = useState(window.innerWidth);

    useEffect(() => {
        const handleResize = () => setWindowWidth(window.innerWidth);
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    const isMobile = windowWidth < 768;

    useEffect(() => {
        dispatch(getTopWins(10));
    }, [dispatch, isAuthenticated]);

    const renderTopWins = () => (
        <Stack
            gap="0.5rem"
            className="custom-scrollbar"
            style={{
                flex: 1,
                overflowY: "auto",
                paddingRight: "4px",
                minHeight: 0,
                maxHeight: isMobile ? "250px" : "360px"
            }}
        >
            {isLoadingTopWins ? (
                <Skeleton variant="rectangular" height={36} count={9} />
            ) : topWins.length > 0 ? (
                topWins.map((topWin) => (
                    <Stack
                        key={topWin.id}
                        direction="row"
                        justify="space-between"
                        align="center"
                        style={{
                            background: "var(--color-bg-glass)",
                            padding: "0.5rem 0.75rem",
                            borderRadius: "var(--radius-sm)",
                            border: "1px solid var(--color-border)",
                            flexShrink: 0,
                        }}
                    >
                        <Typography variant="body" style={{ fontWeight: 600, fontSize: "0.9rem" }}>
                            {ROOM_TYPE_LABELS[topWin.roomType] || topWin.roomType}
                        </Typography>
                        <Typography variant="body" style={{ color: "var(--color-income-text)", fontWeight: 700 }}>
                            +{topWin.amount}
                        </Typography>
                    </Stack>
                ))
            ) : (
                <Typography variant="body" style={{ opacity: 0.6, textAlign: "center", marginTop: "2rem" }}>
                    No big wins yet. Be the first!
                </Typography>
            )}
        </Stack>
    );

    return (
        <Box className="page-wrapper home-page">
            <Container>
                <Stack gap="1.5rem" style={{ height: "100%", paddingTop: "1.5rem" }}>

                    <Box
                        style={{
                            flexShrink: 0,
                            borderRadius: "var(--radius-md)",
                            boxShadow: "var(--shadow-md)",
                            backdropFilter: "blur(2px)",
                            padding: "1.25rem 1.5rem",
                        }}
                    >
                        <Stack direction={isMobile ? "column" : "row"} justify="space-between" gap="0.5rem" align={isMobile ? "flex-start" : "center"}>
                            <Typography variant="h1">
                                {isAuthenticated ? `Welcome back, ${user?.username}!` : "Welcome to Casual Games!"}
                            </Typography>
                            <Typography variant="body" style={{ opacity: 0.7, maxWidth: "600px", lineHeight: "1.5" }}>
                                {isAuthenticated
                                    ? "Jump into a room, place your bets, and start climbing the global leaderboards today."
                                    : "The ultimate real-time multiplayer platform. Challenge players worldwide, test your skills, and win CG Coins."}
                            </Typography>
                        </Stack>
                    </Box>

                    <div className="main-layout">

                        <Box className="sidebar">
                            {isMobile ? (
                                <Box style={{ background: "var(--glass-surface)", borderRadius: "var(--radius-md)", padding: "0 1rem" }}>
                                    <Accordion
                                        title="Top wins today"
                                        isOpen={isAccordionOpen}
                                        onToggle={() => setIsAccordionOpen(!isAccordionOpen)}
                                    >
                                        {renderTopWins()}
                                    </Accordion>
                                </Box>
                            ) : (
                                <Card className="top-wins-desktop" style={{ display: "flex", flexDirection: "column", overflow: "hidden", height: "100%" }}>
                                    <Typography variant="h3" style={{ marginBottom: "1rem", textAlign: "center" }}>
                                        Top wins today
                                    </Typography>
                                    {renderTopWins()}
                                </Card>
                            )}
                        </Box>

                        <Box className="games-container">
                            {AVAILABLE_GAMES.map((type) => {
                                const imgData = GAME_IMAGES[type];

                                if (!imgData) return null;

                                return (
                                    <Box key={type} className="game-card-wrapper">
                                        <Card
                                            style={{
                                                position: "relative",
                                                overflow: "hidden",
                                                display: "flex",
                                                flexDirection: "column",
                                                padding: "1.5rem",
                                                height: "100%"
                                            }}
                                        >
                                            <Img
                                                src={imgData.src}
                                                style={{
                                                    position: "absolute",
                                                    right: "0",
                                                    bottom: "0",
                                                    width: "100%",
                                                    height: "100%",
                                                    objectFit: "cover",
                                                    filter: (theme === "dark" && imgData.invert) ? "invert(1)" : "invert(0)",
                                                    background: "rgba(128, 128, 128, 0.1)",
                                                    borderRadius: "var(--radius-lg)",
                                                    zIndex: 0,
                                                }}
                                            />

                                            <Stack justify="space-between" style={{ flex: 1, zIndex: 1, position: "relative" }}>
                                                <Stack gap="0.5rem" style={{ maxWidth: "55%" }}>
                                                    <Typography variant="h2" className="game-card-title">{ROOM_TYPE_LABELS[type]}</Typography>
                                                </Stack>

                                                <Box style={{ marginTop: "auto" }}>
                                                    <Button
                                                        variant="outline"
                                                        onClick={() => navigate("/rooms", { state: { preselectRoomType: type } })}
                                                        style={{ padding: "0.5rem 1.5rem" }}
                                                    >
                                                        Play Now
                                                    </Button>
                                                </Box>
                                            </Stack>
                                        </Card>
                                    </Box>
                                );
                            })}
                        </Box>
                    </div>
                </Stack>
            </Container>
        </Box>
    );
}
