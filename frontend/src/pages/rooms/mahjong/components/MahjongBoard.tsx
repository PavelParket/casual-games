import { useEffect, useRef, useState } from "react";
import { Box, Typography } from "../../../../ui";
import type { MahjongTileData } from "../utils/MahjongTypes";
import { MahjongTile } from "./MahjongTile";
import { isTileFree } from "../utils/MahjongGameUtils";
import { AnimatePresence, motion, type Variants } from "framer-motion";

interface MahjongBoardProps {
    tiles: MahjongTileData[];
    removedIds: Set<string>;
    selectedId: string | null;
    disabled: boolean;
    myTilesRemaining: number;
    availableMoves: number;
    opponentTilesRemaining?: number;
    onTileClick: (id: string) => void;
}

const TILE_WIDTH = 48;
const TILE_HEIGHT = 64;
const STEP_X = TILE_WIDTH / 2;
const STEP_Y = TILE_HEIGHT / 2;

const Z_OFFSET_X = 5;
const Z_OFFSET_Y = -5;

interface BoardVariantCustomProps {
    left: number;
    top: number;
}

const boardVariants: Variants = {
    hidden: { opacity: 0, scale: 0.8, y: 20 },
    visible: (custom: BoardVariantCustomProps) => ({
        opacity: 1,
        scale: 1,
        x: custom.left,
        y: custom.top,
        transition: { type: "spring", stiffness: 400, damping: 25 }
    }),
    exit: (custom: BoardVariantCustomProps) => ({
        y: custom.top - 60,
        opacity: 0,
        scale: 1.2,
        transition: { duration: 0.4 }
    })
};

export function MahjongBoard({
    tiles,
    removedIds,
    selectedId,
    disabled,
    myTilesRemaining,
    availableMoves,
    opponentTilesRemaining,
    onTileClick
}: MahjongBoardProps) {
    const containerRef = useRef<HTMLDivElement>(null);
    const [scale, setScale] = useState(1);

    const maxCol = tiles.length > 0 ? Math.max(...tiles.map(t => t.slot.col)) : 14;
    const maxRow = tiles.length > 0 ? Math.max(...tiles.map(t => t.slot.row)) : 6;

    const boardWidth = maxCol * STEP_X + TILE_WIDTH;
    const boardHeight = maxRow * STEP_Y + TILE_HEIGHT;

    const sortedTiles = [...tiles].sort((a, b) => {
        if (a.slot.layer !== b.slot.layer) return a.slot.layer - b.slot.layer;
        if (a.slot.row !== b.slot.row) return a.slot.row - b.slot.row;
        return b.slot.col - a.slot.col;
    });

    useEffect(() => {
        const observer = new ResizeObserver((entries) => {
            const { width } = entries[0].contentRect;
            const padding = 32;
            const availableWidth = width - padding;

            if (availableWidth < boardWidth) {
                setScale(availableWidth / boardWidth);
            } else {
                setScale(1);
            }
        });

        if (containerRef.current) {
            observer.observe(containerRef.current);
        }
        return () => observer.disconnect();
    }, [boardWidth]);

    return (
        <Box style={{ display: "flex", flexDirection: "column", width: "100%", gap: "1rem" }}>
            <Box style={{
                display: "flex",
                justifyContent: "space-between",
                padding: "0.75rem 1rem",
                background: "var(--color-bg-secondary)",
                borderRadius: "var(--radius-md)",
                border: "1px solid var(--color-border)"
            }}>
                <Box style={{ textAlign: "center" }}>
                    <Typography variant="caption" style={{ opacity: 0.7 }}>My Tiles</Typography>
                    <Typography variant="body" style={{ fontWeight: 600 }}>{myTilesRemaining}</Typography>
                </Box>
                <Box style={{ textAlign: "center" }}>
                    <Typography variant="caption" style={{ opacity: 0.7 }}>Available Moves</Typography>
                    <Typography variant="body" style={{ fontWeight: 600, color: availableMoves === 0 ? "var(--color-expense-text)" : "var(--color-primary)" }}>
                        {availableMoves}
                    </Typography>
                </Box>
                {opponentTilesRemaining !== undefined && (
                    <Box style={{ textAlign: "center" }}>
                        <Typography variant="caption" style={{ opacity: 0.7 }}>Opponent Tiles</Typography>
                        <Typography variant="body" style={{ fontWeight: 600 }}>{opponentTilesRemaining}</Typography>
                    </Box>
                )}
            </Box>

            <Box
                className="mahjong-board-container"
                ref={containerRef}
                style={{ height: boardHeight * scale + 48, opacity: disabled ? 0.7 : 1, transition: "opacity 0.2s" }}
            >
                <Box style={{
                    position: "relative",
                    width: boardWidth,
                    height: boardHeight,
                    transform: `scale(${scale})`,
                    transformOrigin: "center top",
                    margin: "0 auto",
                    pointerEvents: disabled ? "none" : "auto"
                }}>
                    <Box style={{ width: boardWidth }}>
                        <AnimatePresence>
                            {sortedTiles.map(({ slot, face }) => {
                                if (removedIds.has(slot.id)) return null;

                                const isFree = isTileFree(slot, removedIds);
                                const isSelected = selectedId === slot.id;

                                const left = slot.col * STEP_X + (slot.layer * Z_OFFSET_X);
                                const top = slot.row * STEP_Y + (slot.layer * Z_OFFSET_Y);
                                const zIndex = slot.layer;

                                const customProps: BoardVariantCustomProps = { left, top };

                                return (
                                    <motion.div
                                        key={slot.id}
                                        custom={customProps}
                                        variants={boardVariants}
                                        initial="hidden"
                                        animate="visible"
                                        exit="exit"
                                        style={{
                                            position: "absolute",
                                            top: 0,
                                            left: 0,
                                            width: TILE_WIDTH,
                                            height: TILE_HEIGHT,
                                            zIndex: isSelected ? 100 : zIndex,
                                        }}
                                    >
                                        <MahjongTile
                                            face={face}
                                            isFree={isFree}
                                            isSelected={isSelected}
                                            onClick={() => onTileClick(slot.id)}
                                        />
                                    </motion.div>
                                );
                            })}
                        </AnimatePresence>
                    </Box>
                </Box>
            </Box>
        </Box>
    );
}
