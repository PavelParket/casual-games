import { motion, type Transition } from "framer-motion";
import { Box, Typography } from "../../../../ui";
import HorseSprite from "../../../../assets/sprites/HorseSprite";
import { HORSE_COLORS, HORSE_SPRITE_SIZE } from "../../../../models/HorseRace";
import type { HorseRaceHorseKeyframes } from "../../../../models/HorseRace";

interface HorseRaceTrackProps {
    phase: string;
    horseCount: number;
    winnerIndex?: number;
    raceKeyframes: HorseRaceHorseKeyframes[] | null;
    onRaceEnd: () => void;
}

const RACE_DURATION_S = 12;

export function HorseRaceTrack({ phase, horseCount, winnerIndex, raceKeyframes, onRaceEnd }: HorseRaceTrackProps) {
    if (horseCount === 0) {
        return <Typography variant="caption" style={{ color: "var(--color-text-secondary)" }}>Loading race...</Typography>;
    }

    return (
        <div style={{ display: "flex", flexDirection: "column", paddingLeft: "24px", position: "relative" }}>
            {Array.from({ length: horseCount }, (_, i) => {
                const color = HORSE_COLORS[i % HORSE_COLORS.length];
                const isWinner = phase === "FINISHED" && winnerIndex === i;
                const isRunning = phase === "RACING";

                const keyframesForHorse = raceKeyframes?.find(k => k.horseIndex === i)?.keyframes;

                let animateTarget: string | string[] = "0%";
                let transitionProps: Transition = { duration: 0.3, ease: "easeOut" };

                if (phase === "RACING" && keyframesForHorse) {
                    animateTarget = keyframesForHorse.map(kf => `${kf.position}%`);
                    transitionProps = {
                        duration: RACE_DURATION_S,
                        times: keyframesForHorse.map(kf => kf.offset),
                        ease: "linear"
                    };
                } else if (phase === "FINISHED" && keyframesForHorse) {
                    animateTarget = `${keyframesForHorse[keyframesForHorse.length - 1].position}%`;
                    transitionProps = { duration: 0 };
                }

                return (
                    <Box key={i} style={{ position: "relative", height: `${HORSE_SPRITE_SIZE}px`, display: "flex", alignItems: "center", overflow: "visible" }}>
                        <span style={{ position: "absolute", left: -24, width: 20, textAlign: "right", fontSize: "16px", fontWeight: 700, color, opacity: 0.85, userSelect: "none", lineHeight: `${HORSE_SPRITE_SIZE}px` }}>
                            #{i + 1}
                        </span>
                        <Box style={{ position: "absolute", left: 0, right: 0, height: "2px", background: "var(--color-border)", borderRadius: "1px" }} />

                        <div style={{ position: "absolute", left: 0, width: `calc(100% - ${HORSE_SPRITE_SIZE}px)`, height: "100%", zIndex: 1 }}>
                            <motion.div
                                initial={{ left: "0%" }}
                                animate={{ left: animateTarget }}
                                transition={transitionProps}
                                onAnimationComplete={() => {
                                    if (phase === "RACING" && i === winnerIndex) {
                                        onRaceEnd();
                                    }
                                }}
                                style={{
                                    position: "absolute",
                                    top: "50%",
                                    y: "-50%",
                                    willChange: "left",
                                }}
                            >
                                <HorseSprite color={color} size={HORSE_SPRITE_SIZE} isRunning={isRunning} isWinner={isWinner} />
                            </motion.div>
                        </div>
                    </Box>
                );
            })}
        </div>
    );
}
