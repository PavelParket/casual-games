import { useState } from "react";
import { Box, Button, Card, Stack, Typography, ComboBox, Container } from "../../../ui";

import type { MahjongTileData } from "./utils/MahjongTypes";
import { generateTestBoard, getAvailableMoves, generateAllUniqueFaces } from "./utils/MahjongGameUtils";
import { MahjongTile } from "./components/MahjongTile";
import "./styles/MahjongRoom.css";

import { MahjongBoard } from "./components/MahjongBoard";

const TILE_COUNT_OPTIONS = [
    { value: "72", label: "72 Tiles (Half)" },
    { value: "144", label: "144 Tiles (Full)" },
];

export default function MahjongExperimentalPage() {
    const [tiles, setTiles] = useState<MahjongTileData[]>([]);
    const [removedIds, setRemovedIds] = useState<Set<string>>(new Set());
    const [selectedId, setSelectedId] = useState<string | null>(null);
    const [tileCount, setTileCount] = useState<"72" | "144">("72");

    const [previewDisabled, setPreviewDisabled] = useState<Record<string, boolean>>({});
    const allUniqueFaces = generateAllUniqueFaces();

    const handleRestart = () => {
        setTiles(generateTestBoard(Number(tileCount) as 72 | 144));
        setRemovedIds(new Set());
        setSelectedId(null);
    };

    const handleTileClick = (id: string) => {
        if (selectedId === id) {
            setSelectedId(null);
            return;
        }

        if (!selectedId) {
            setSelectedId(id);
            return;
        }

        const tile1 = tiles.find(t => t.slot.id === selectedId);
        const tile2 = tiles.find(t => t.slot.id === id);

        if (tile1 && tile2) {
            setRemovedIds(prev => {
                const next = new Set(prev);
                next.add(tile1.slot.id);
                next.add(tile2.slot.id);
                return next;
            });
            setSelectedId(null);
        } else {
            setSelectedId(id);
        }
    };

    const availableMoves = getAvailableMoves(tiles, removedIds);
    const isWin = removedIds.size === Number(tileCount);
    const isDeadlock = availableMoves === 0 && !isWin;

    return (
        <Box className="page-wrapper">
            <Container>
                <Box style={{ padding: "2rem 0 1rem" }}>
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        Mahjong UI Prototype
                    </Typography>
                </Box>

                <Card style={{ padding: 0, display: "flex", flexDirection: "column" }}>

                    <Box style={{
                        padding: "0.75rem 1.5rem",
                        borderBottom: "1px solid var(--color-border)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "space-between",
                        flexWrap: "wrap",
                        gap: "1rem"
                    }}>
                        <Stack direction="row" gap="1rem" align="center" wrap="wrap">
                            <Box style={{ width: "160px" }}> Count of tiles:
                                <ComboBox
                                    options={TILE_COUNT_OPTIONS}
                                    value={tileCount}
                                    onValueChange={(val) => setTileCount(val as "72" | "144")}
                                />
                            </Box>
                        </Stack>
                        <Button variant="outline" onClick={handleRestart} style={{ padding: "0.25rem 0.75rem" }}>
                            Restart
                        </Button>
                    </Box>

                    <Box className="mahjong-main-content">
                        <Stack gap="1.5rem" style={{ width: "100%" }}>

                            <Box className="mahjong-stats">
                                <Box style={{ textAlign: "center" }}>
                                    <Typography variant="caption">Tiles Remaining</Typography>
                                    <Typography variant="h2" style={{ color: "var(--color-primary)" }}>
                                        {Number(tileCount) - removedIds.size}
                                    </Typography>
                                </Box>
                                <Box style={{ textAlign: "center" }}>
                                    <Typography variant="caption">Available Matches</Typography>
                                    <Typography variant="h2" style={{ color: isDeadlock ? "var(--color-expense-text)" : "var(--color-primary)" }}>
                                        {availableMoves}
                                    </Typography>
                                </Box>
                            </Box>

                            <Box style={{ position: "relative" }}>
                                {isWin && (
                                    <Box style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", zIndex: 10 }}>
                                        <Typography variant="h1" style={{ color: "var(--color-income-text)", textShadow: "0 4px 10px rgba(0,0,0,0.2)" }}>You Win!</Typography>
                                    </Box>
                                )}

                                {isDeadlock && !isWin && (
                                    <Box style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", zIndex: 10, pointerEvents: "none" }}>
                                        <Typography variant="h1" style={{ color: "var(--color-expense-text)", textShadow: "0 4px 10px rgba(0,0,0,0.2)" }}>Deadlock!</Typography>
                                    </Box>
                                )}

                                <MahjongBoard
                                    tiles={tiles}
                                    removedIds={removedIds}
                                    selectedId={selectedId}
                                    onTileClick={handleTileClick}
                                    disabled={false}
                                    myTilesRemaining={0}
                                    availableMoves={0}
                                />
                            </Box>
                        </Stack>
                    </Box>
                </Card>

                <Card style={{ padding: "1.5rem", marginTop: "2rem" }}>
                    <Typography variant="h3" style={{ marginBottom: "1rem" }}>Tile Deck Preview</Typography>
                    <Typography variant="caption" style={{ display: "block", marginBottom: "1.5rem", opacity: 0.8 }}>
                        Click on any tile to toggle its 'is-blocked' state to review the filter effect.
                    </Typography>

                    <Box className="mahjong-preview-grid">
                        {allUniqueFaces.map((face, index) => {
                            const id = `${face.suit}_${face.value}_${index}`;
                            const isBlocked = previewDisabled[id] || false;

                            return (
                                <Box key={id} style={{ width: "48px", height: "64px" }}>
                                    <MahjongTile
                                        face={face}
                                        isFree={!isBlocked}
                                        isSelected={false}
                                        onClick={() => setPreviewDisabled(p => ({ ...p, [id]: !p[id] }))}
                                    />
                                </Box>
                            );
                        })}
                    </Box>
                </Card>
            </Container>
        </Box>
    );
}
