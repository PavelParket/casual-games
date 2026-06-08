import { motion } from "framer-motion";
import { cardId } from "../utils/CardUtils";
import { PlayingCard } from "./PlayingCard";
import { Box } from "../../../../ui";
import type { DurakTablePair } from "../../../../models/Durak";
import { useRef } from "react";
import type { TableExitMode } from "../../DurakRoom";

interface TablePairSlotProps {
    pair: DurakTablePair;
    isOpponentAttacker: boolean;
    tableExitMode: TableExitMode;
    discardPileRef: React.RefObject<HTMLDivElement | null>;
}

export function TablePairSlot({ pair, isOpponentAttacker, tableExitMode, discardPileRef }: TablePairSlotProps) {
    const slotRef = useRef<HTMLDivElement>(null);

    const slotInitial = isOpponentAttacker
        ? { y: -180, opacity: 0, scale: 0.88 }
        : { opacity: 0 };

    const slotAnimate = isOpponentAttacker
        ? { y: 0, opacity: 1, scale: 1 }
        : { opacity: 1 };

    const attackLayoutId = isOpponentAttacker ? undefined : cardId(pair.attackCard);

    const defendLayoutId = isOpponentAttacker
        ? (pair.defendCard ? cardId(pair.defendCard) : undefined)
        : undefined;

    const defendInitial = isOpponentAttacker
        ? { opacity: 0 }
        : { y: -160, opacity: 0, scale: 0.85 };

    const defendAnimate = { y: 0, opacity: 1, scale: 1 };

    const getExitAnimation = () => {
        if (tableExitMode === "pickup") {
            return {
                y: 80,
                opacity: 0,
                scale: 0.6,
                transition: { duration: 0.3, ease: "easeIn" },
            } as const;
        }

        if (tableExitMode === "bita" && slotRef.current && discardPileRef.current) {
            const slotRect = slotRef.current.getBoundingClientRect();
            const discardRect = discardPileRef.current.getBoundingClientRect();

            const dx = (discardRect.left + discardRect.width / 2) - (slotRect.left + slotRect.width / 2);
            const dy = (discardRect.top + discardRect.height / 2) - (slotRect.top + slotRect.height / 2);

            return {
                x: dx,
                y: dy,
                rotateY: 180,
                opacity: 0,
                scale: 0.5,
                transition: { duration: 0.4, ease: "easeIn" },
            } as const;
        }

        return { scale: 0.5, opacity: 0 };
    };

    return (
        <motion.div
            ref={slotRef}
            initial={slotInitial}
            animate={slotAnimate}
            exit={getExitAnimation()}
            transition={{ duration: 0.28, ease: "easeOut" }}
            style={{
                position: "relative",
                width: 62,
                height: 110,
                flexShrink: 0,
                margin: "0 1rem",
            }}
        >
            {/* Attack card */}
            <Box style={{ position: "absolute", top: 0, left: 0 }}>
                <PlayingCard
                    card={pair.attackCard}
                    faceDown={false}
                    size="md"
                    layoutId={attackLayoutId}
                />
            </Box>

            {/* Defend card */}
            {pair.defendCard && (
                <motion.div
                    initial={defendInitial}
                    animate={defendAnimate}
                    transition={{ duration: 0.25, ease: "easeOut" }}
                    style={{
                        position: "absolute",
                        top: 18,
                        left: 16,
                        zIndex: 1,
                    }}
                >
                    <PlayingCard
                        card={pair.defendCard}
                        faceDown={false}
                        size="md"
                        layoutId={defendLayoutId}
                    />
                </motion.div>
            )}
        </motion.div>
    );
}
