import { Box, Stack, Typography, Avatar } from "../../../../ui";
import type { PlayerResponse } from "../../../../models/Room";
import { MiniProfile } from "../../../profile/components/MiniProfile";
import "../styles/TicTacToeRoom.css"

interface TicTacToePlayersPanelProps {
    players?: Record<string, PlayerResponse>;
    playersWithSymbols?: Record<string, string>;
    isGame: boolean;
    inDrawer?: boolean;
}

export function TicTacToePlayersPanel({ players, playersWithSymbols, isGame, inDrawer = false }: TicTacToePlayersPanelProps) {
    return (
        <Box
            className="custom-scrollbar"
            style={{
                display: "grid",
                gridTemplateColumns: inDrawer ? "1fr" : "repeat(auto-fit, minmax(180px, 1fr))",
                justifyContent: "center",
                alignContent: "start",
                gap: "8px",
                padding: inDrawer ? "1rem 1.5rem" : "0.75rem",
                paddingRight: inDrawer ? "1.5rem" : "0.9rem",
                flex: 1,
                background: inDrawer ? "transparent" : "var(--color-bg-secondary)",
                borderRadius: inDrawer ? "0" : "var(--radius-md)",
                border: inDrawer ? "none" : "1px solid var(--color-border)",
                overflowY: "auto",
            }}
        >
            {!inDrawer && (
                <Typography variant="h3" style={{ gridColumn: "1 / -1", textAlign: "center", marginBottom: "1rem" }}>
                    Players
                </Typography>
            )}

            {players &&
                Object.entries(players).map(([playerGuid, player]) => {
                    const symbol = isGame && playersWithSymbols ? playersWithSymbols[player.username] : null;

                    return (
                        <MiniProfile
                            key={playerGuid}
                            guid={playerGuid}
                            username={player.username}
                            status={player.status}
                            avatarUrl={player.linkProfilePictureMini}
                            avatarUrlFull={player.linkProfilePicture}
                        >
                            <Stack
                                direction="row"
                                align="center"
                                gap="0.75rem"
                                className="ttt-player-card"
                            >
                                <Avatar src={player.linkProfilePictureMini} fallback={player.username} size={40} />

                                <Typography variant="body" style={{ fontWeight: "bold", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                                    {player.username}{symbol ? `: ${symbol}` : ""}
                                </Typography>
                            </Stack>
                        </MiniProfile>
                    );
                })}
        </Box>
    );
}
