import { useState, type ReactNode } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Box } from "../layout/Box";
import { Typography } from "./Typography";
import { Icon } from "./Icon";
import { useThemedIcon } from "../../hooks/useThemedIcon";
import { Divider } from "../..";

export interface AccordionProps {
    title: string;
    children: ReactNode;
    isOpen?: boolean;
    onToggle?: () => void;
}

export function Accordion({ title, children, isOpen: controlledIsOpen, onToggle }: AccordionProps) {
    const [localIsOpen, setLocalIsOpen] = useState(false);
    const { getInverseIcon } = useThemedIcon();

    const isControlled = controlledIsOpen !== undefined;
    const isOpen = isControlled ? controlledIsOpen : localIsOpen;

    const handleToggle = () => {
        if (isControlled && onToggle) {
            onToggle();
        } else {
            setLocalIsOpen(!isOpen);
        }
    };

    return (
        <Box>
            <Box
                onClick={handleToggle}
                style={{
                    padding: "1.5rem 0",
                    cursor: "pointer",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center"
                }}
            >
                <Typography variant="body" style={{ fontWeight: 500, fontSize: "1.5rem" }}>
                    {title}
                </Typography>
                <Icon
                    src={getInverseIcon("expandMore")}
                    alt="toggle"
                    size={24}
                    style={{
                        opacity: 0.6,
                        transform: isOpen ? "rotate(180deg)" : "rotate(0deg)",
                        transition: "transform 0.3s ease"
                    }}
                />
            </Box>

            <AnimatePresence initial={false}>
                {isOpen && (
                    <motion.div
                        initial={{ height: 0, opacity: 0 }}
                        animate={{ height: "auto", opacity: 1 }}
                        exit={{ height: 0, opacity: 0 }}
                        transition={{
                            type: "spring",
                            bounce: 0,
                            duration: 0.3
                        }} style={{ overflow: "hidden" }}
                    >
                        <Box style={{ paddingBottom: "1.5rem", paddingRight: "2rem" }}>
                            {typeof children === "string" ? (
                                <Typography variant="body" style={{ opacity: 0.7, lineHeight: 1.6 }}>
                                    {children}
                                </Typography>
                            ) : (
                                children
                            )}
                        </Box>
                    </motion.div>
                )}
            </AnimatePresence>

            <Divider />
        </Box>
    );
}
