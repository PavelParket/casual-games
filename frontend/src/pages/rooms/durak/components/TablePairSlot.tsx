import { motion } from "framer-motion";
import { cardId } from "../utils/CardUtils";
import { PlayingCard } from "./PlayingCard";
import { Box } from "../../../../ui";
import type { DurakTablePair } from "../../../../models/Durak";

interface TablePairSlotProps {
    pair: DurakTablePair;
    index: number;
}

export function TablePairSlot({ pair, index }: TablePairSlotProps) {
    return (
        <motion.div
            key={index}
            initial={{ scale: 0.5, opacity: 0, y: -16 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.5, opacity: 0 }}
            transition={{ duration: 0.2, ease: "easeOut" }}
            style={{
                position: "relative",
                width: 62,
                height: 110,
                flexShrink: 0,
                margin: "0 1rem 0 1rem",
            }}
        >
            {/* Attack card — base position */}
            <Box style={{ position: "absolute", top: 0, left: 0 }}>
                <PlayingCard
                    card={pair.attackCard}
                    faceDown={false}
                    size="md"
                    layoutId={cardId(pair.attackCard)}
                />
            </Box>

            {pair.defendCard && (
                <motion.div
                    initial={{ scale: 0, opacity: 0 }}
                    animate={{ scale: 1, opacity: 1 }}
                    transition={{ duration: 0.2, ease: "easeOut" }}
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
                        layoutId={cardId(pair.defendCard)}
                    />
                </motion.div>
            )}
        </motion.div>
    );
}
