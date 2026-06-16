import { Box, Stack, Typography, Avatar } from "../../../../ui";
import type { PlayerResponse } from "../../../../models/Room";
import { MiniProfile } from "../../../profile/components/MiniProfile";
import "../styles/DeCoderRoom.css";

interface PlayersPanelProps {
    players?: Record<string, PlayerResponse>;
}

export function PlayersPanel({ players }: PlayersPanelProps) {
    return (
        <Box
            className="custom-scrollbar"
            style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, 200px)",
                justifyContent: "center",
                alignContent: "start",
                gap: "8px",
                padding: "0.75rem",
                paddingRight: "0.9rem",
                flex: 1,
                background: "var(--color-bg-secondary)",
                borderRadius: "var(--radius-md)",
                border: "1px solid var(--color-border)",
                overflowY: "auto",
            }}
        >

            <Typography variant="h3" style={{ gridColumn: "1 / -1", textAlign: "center", marginBottom: "1rem" }}>
                Players
            </Typography>
            {players &&
                Object.entries(players).map(([playerGuid, player]) => (
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
                            className="decoder-player-card"
                        >
                            <Avatar src={player.linkProfilePictureMini} fallback={player.username} size={40} />

                            <Typography variant="body" style={{ fontWeight: "bold", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
                                {player.username}
                            </Typography>
                        </Stack>
                    </MiniProfile>
                ))}
        </Box>
    );
}
