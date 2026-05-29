import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { getTopWins } from "../store/slices/BankSlice";
import { ROOM_TYPE_LABELS } from "../models/Room";
import { Box, Button, Container, Card, Typography, Grid, Stack, Img, Icon } from "../ui";
import { Skeleton } from "../ui/components/common/Skeleton";
import { useTheme } from "../ui";
import { useThemedIcon } from "../ui";
import ticTacToeImg from "../assets/images/img-tic-tac-toe.png";
import deCoderImg from "../assets/images/img-de-coder.png";
import durakImg from "../assets/images/img-durak.png";
import horseRaceImg from "../assets/images/img-horse-race.png";

export default function Home() {
    const { theme } = useTheme();
    const { getIcon } = useThemedIcon();
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();
    const { isAuthenticated, user } = useSelector((state: RootState) => state.auth,);
    const { topWins, isLoadingTopWins } = useSelector((state: RootState) => state.bank);

    useEffect(() => {
        dispatch(getTopWins(10));
    }, [dispatch, isAuthenticated]);

    return (
        <Box style={{
            minHeight: "calc(100vh - 60px - 50px)",
            margin: "0 10rem",
            padding: "0 1rem",
            background: "var(--color-bg-glass)",
            backdropFilter: "blur(2px)",
            borderRadius: "var(--radius-md)",
            boxShadow: "var(--shadow-lg)",
        }}>
            <Container>
                <Stack
                    gap="1.5rem"
                    style={{
                        height: "100%",
                        paddingTop: "1.5rem",
                    }}
                >
                    <Box
                        style={{
                            flexShrink: 0,
                            borderRadius: "var(--radius-md)",
                            boxShadow: "var(--shadow-md)",
                            backdropFilter: "blur(2px)",
                            padding: "1.25rem 1.5rem",
                        }}
                    >
                        <Stack
                            direction="row"
                            justify="space-between"
                            gap="0.5rem"
                            align="center"
                        >
                            <Typography variant="h1">
                                {isAuthenticated
                                    ? `Welcome back, ${user?.username}!`
                                    : "Welcome to Casual Games!"}
                            </Typography>
                            <Typography
                                variant="body"
                                style={{ opacity: 0.7, maxWidth: "600px", lineHeight: "1.5" }}
                            >
                                {isAuthenticated
                                    ? "Jump into a room, place your bets, and start climbing the global leaderboards today."
                                    : "The ultimate real-time multiplayer platform. Challenge players worldwide, test your skills, and win CG Coins."}
                            </Typography>
                        </Stack>
                    </Box>

                    <Grid
                        columns="280px 1fr"
                        gap="1.5rem"
                        style={{ flex: 1, minHeight: "520px" }}
                    >
                        <Card
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                overflow: "hidden",
                            }}
                        >
                            <Typography variant="h3" style={{ marginBottom: "1rem", textAlign: "center" }}>
                                Top winners
                            </Typography>

                            <Stack
                                gap="0.5rem"
                                style={{ flex: 1, overflowY: "auto", paddingRight: "4px" }}
                            >
                                {isLoadingTopWins ? (
                                    <Skeleton variant="rectangular" height={41.58} count={10} />
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
                                            <Stack gap="0">
                                                <Typography variant="body" style={{ fontWeight: 600, fontSize: "0.9rem" }}>
                                                    {ROOM_TYPE_LABELS[topWin.roomType] || topWin.roomType}
                                                </Typography>
                                            </Stack>
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
                        </Card>

                        <Grid
                            columns="1fr 1fr"
                            rows="1fr 1fr"
                            gap="1.5rem"
                            style={{ height: "100%" }}
                        >
                            <Card
                                style={{
                                    position: "relative",
                                    overflow: "hidden",
                                    display: "flex",
                                    flexDirection: "column",
                                    padding: "1.5rem",
                                }}
                            >
                                <Img
                                    src={deCoderImg}
                                    style={{
                                        position: "absolute",
                                        right: "0",
                                        bottom: "0",
                                        width: "100%",
                                        height: "100%",
                                        filter: theme === "dark" ? "invert(1)" : "invert(0)",
                                        background: "rgba(128, 128, 128, 0.1)",
                                        borderRadius: "var(--radius-lg) 0 0 0",
                                        zIndex: 0,
                                    }}
                                />

                                <Stack
                                    justify="space-between"
                                    style={{ flex: 1, zIndex: 1, position: "relative" }}
                                >
                                    <Stack gap="0.5rem" style={{ maxWidth: "49%" }}>
                                        <Typography variant="h2">De-Coder</Typography>
                                        <Typography variant="body">
                                            Use pure logic to deduce the secret combination.
                                        </Typography>
                                    </Stack>

                                    <Box style={{ marginTop: "auto" }}>
                                        <Button
                                            variant="outline"
                                            onClick={() => navigate("/rooms", { state: { preselectRoomType: "DE_CODER" } })}
                                            style={{
                                                padding: "0.5rem 1.5rem",
                                            }}
                                        >
                                            Play Now
                                        </Button>
                                    </Box>
                                </Stack>
                            </Card>


                            <Card
                                style={{
                                    position: "relative",
                                    overflow: "hidden",
                                    display: "flex",
                                    flexDirection: "column",
                                    padding: "1.5rem",
                                }}
                            >
                                <Img
                                    src={durakImg}
                                    style={{
                                        position: "absolute",
                                        right: "0",
                                        bottom: "0",
                                        width: "100%",
                                        height: "100%",
                                        background: "rgba(128, 128, 128, 0.1)",
                                        borderRadius: "var(--radius-lg) 0 0 0",
                                        zIndex: 0,
                                    }}
                                />
                                <Icon
                                    src={getIcon("new")}
                                    alt="New"
                                    size={50}
                                    style={{ position: "absolute", top: 0, left: 0, }}
                                />

                                <Stack
                                    justify="space-between"
                                    style={{ flex: 1, zIndex: 1, position: "relative" }}
                                >
                                    <Stack gap="0.5rem" style={{ maxWidth: "49%" }}>
                                        <Typography variant="h2">Durak</Typography>
                                        <Typography variant="body">
                                            Clear your hand, and don't end up the fool.
                                        </Typography>
                                    </Stack>

                                    <Box style={{ marginTop: "auto" }}>
                                        <Button
                                            variant="outline"
                                            onClick={() => navigate("/rooms", { state: { preselectRoomType: "DURAK" } })}
                                            style={{
                                                padding: "0.5rem 1.5rem",
                                            }}
                                        >
                                            Play Now
                                        </Button>
                                    </Box>
                                </Stack>
                            </Card>

                            <Card
                                style={{
                                    position: "relative",
                                    overflow: "hidden",
                                    display: "flex",
                                    flexDirection: "column",
                                    padding: "1.5rem",
                                }}
                            >
                                <Img
                                    src={horseRaceImg}
                                    style={{
                                        position: "absolute",
                                        right: "0",
                                        bottom: "0",
                                        width: "100%",
                                        height: "100%",
                                        filter: theme === "dark" ? "invert(1)" : "invert(0)",
                                        background: "rgba(128, 128, 128, 0.1)",
                                        borderRadius: "var(--radius-lg) 0 0 0",
                                        zIndex: 0,
                                    }}
                                />

                                <Stack
                                    justify="space-between"
                                    style={{ flex: 1, zIndex: 1, position: "relative" }}
                                >
                                    <Stack gap="0.5rem" style={{ maxWidth: "49%" }}>
                                        <Typography variant="h2">Horse Race</Typography>
                                        <Typography variant="body">
                                            Trust your gut, and cheer your champion to the finish line.
                                        </Typography>
                                    </Stack>

                                    <Box style={{ marginTop: "auto" }}>
                                        <Button
                                            variant="outline"
                                            onClick={() => navigate("/rooms", { state: { preselectRoomType: "HORSE_RACE" } })}
                                            style={{
                                                padding: "0.5rem 1.5rem",
                                            }}
                                        >
                                            Play Now
                                        </Button>
                                    </Box>
                                </Stack>
                            </Card>

                            <Card
                                style={{
                                    position: "relative",
                                    overflow: "hidden",
                                    display: "flex",
                                    flexDirection: "column",
                                    padding: "1.5rem",
                                }}
                            >
                                <Img
                                    src={ticTacToeImg}
                                    style={{
                                        position: "absolute",
                                        right: "0",
                                        bottom: "0",
                                        width: "100%",
                                        height: "100%",
                                        filter: theme === "dark" ? "invert(1)" : "invert(0)",
                                        background: "rgba(128, 128, 128, 0.1)",
                                        borderRadius: "var(--radius-lg) 0 0 0",
                                        zIndex: 0,
                                    }}
                                />

                                <Stack
                                    justify="space-between"
                                    style={{ flex: 1, zIndex: 1, position: "relative" }}
                                >
                                    <Stack gap="0.5rem" style={{ maxWidth: "49%" }}>
                                        <Typography variant="h2">Tic-Tac-Toe</Typography>
                                        <Typography variant="body">
                                            Trap your opponent in this fast-paced, highly competitive classic.
                                        </Typography>
                                    </Stack>

                                    <Box style={{ marginTop: "auto" }}>
                                        <Button
                                            variant="outline"
                                            onClick={() => navigate("/rooms", { state: { preselectRoomType: "TIC_TAC_TOE" } })}
                                            style={{
                                                padding: "0.5rem 1.5rem",
                                            }}
                                        >
                                            Play Now
                                        </Button>
                                    </Box>
                                </Stack>
                            </Card>
                        </Grid>
                    </Grid>
                </Stack>
            </Container>
        </Box>
    );
}
