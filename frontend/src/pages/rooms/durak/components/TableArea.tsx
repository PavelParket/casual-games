import { AnimatePresence } from "framer-motion";
import { Box, Typography } from "../../../../ui";
import { cardId } from "../utils/CardUtils";
import { TablePairSlot } from "./TablePairSlot";
import type { DurakTablePair } from "../../../../models/Durak";
import type { TableExitMode } from "../../DurakRoom";

interface TableAreaProps {
    table: DurakTablePair[];
    tableRef: React.RefObject<HTMLDivElement | null>;
    isOpponentAttacker: boolean;
    tableExitMode: TableExitMode;
    discardPileRef: React.RefObject<HTMLDivElement | null>;
}

export function TableArea({ table, tableRef, isOpponentAttacker, tableExitMode, discardPileRef }: TableAreaProps) {
    return (
        <div
            ref={tableRef}
            style={{
                display: "flex",
                flexWrap: "wrap",
                gap: "0.5rem",
                justifyContent: "center",
                minWidth: "400px",
                minHeight: "140px",
                padding: "0.5rem",
                borderRadius: "var(--radius-md)",
                border: "1px dashed var(--color-border)",
                background: "var(--color-bg-glass)",
            }}
        >
            <AnimatePresence mode="popLayout">
                {table.map((pair) => (
                    <TablePairSlot
                        key={cardId(pair.attackCard)}
                        pair={pair}
                        isOpponentAttacker={isOpponentAttacker}
                        tableExitMode={tableExitMode}
                        discardPileRef={discardPileRef}
                    />
                ))}
            </AnimatePresence>

            {table.length === 0 && (
                <Box style={{
                    width: "100%",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    minHeight: "120px",
                }}>
                    <Typography variant="caption" style={{ opacity: 0.35, fontSize: "0.8rem" }}>
                        Table is empty
                    </Typography>
                </Box>
            )}
        </div>
    );
}
