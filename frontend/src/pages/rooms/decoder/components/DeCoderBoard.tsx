import React, { useEffect, useRef, useState } from "react";
import { Box, Button, Typography, CooldownTimer } from "../../../../ui";
import "../styles/DeCoderRoom.css";

interface DeCoderBoardProps {
    gameActive: boolean;
    balanceBefore?: number;
    spent: number;
    onSendMove: (code: string) => void;
}

export function DeCoderBoard({ gameActive, balanceBefore, spent, onSendMove }: DeCoderBoardProps) {
    const [chars, setChars] = useState<string[]>(["", "", "", ""]);
    const [cooldown, setCooldown] = useState(0);
    const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

    useEffect(() => {
        if (cooldown <= 0) return;
        const timerId = setInterval(() => setCooldown((c) => c - 1), 1000);
        return () => clearInterval(timerId);
    }, [cooldown]);

    const handleCharChange = (index: number, val: string) => {
        const char = val.replace(/[^A-Za-z]/g, "").toUpperCase().slice(-1);
        const newChars = [...chars];
        newChars[index] = char;
        setChars(newChars);

        if (char !== "" && index < 3) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "Backspace" && chars[index] === "" && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
        if (e.key === "ArrowLeft" && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
        if (e.key === "ArrowRight" && index < 3) {
            inputRefs.current[index + 1]?.focus();
        }
        if (e.key === "Enter") {
            handleSendClick();
        }
    };

    const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
        e.preventDefault();
        const pastedData = e.clipboardData
            .getData("Text")
            .replace(/[^A-Za-z]/g, "")
            .toUpperCase()
            .slice(0, 4);

        if (!pastedData) return;

        const newChars = [...chars];
        for (let i = 0; i < pastedData.length; i++) {
            newChars[i] = pastedData[i];
        }
        setChars(newChars);

        const nextIndex = Math.min(pastedData.length, 3);
        inputRefs.current[nextIndex]?.focus();
    };

    const handleSendClick = () => {
        const codeStr = chars.join("");
        if (codeStr.length !== 4) return;
        if (cooldown > 0) return;

        onSendMove(codeStr);
        setCooldown(2);
        setChars(["", "", "", ""]);
        inputRefs.current[0]?.focus();
    };

    if (!gameActive) {
        return (
            <Box style={{ textAlign: "center" }}>
                <Typography variant="h2" style={{ marginBottom: "1.5rem" }}>
                    Connecting to game...
                </Typography>
            </Box>
        );
    }

    return (
        <Box
            style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                minHeight: 0,
            }}
        >
            <Box className="decoder-board-stack">
                <Box className="decoder-inputs-wrapper">
                    {chars.map((char, index) => (
                        <input
                            key={index}
                            ref={(el) => { inputRefs.current[index] = el; }}
                            type="text"
                            value={char}
                            className="decoder-char-input"
                            onChange={(e) => handleCharChange(index, e.target.value)}
                            onKeyDown={(e) => handleKeyDown(index, e)}
                            onPaste={handlePaste}
                            onFocus={(e) => e.target.select()}
                        />
                    ))}
                </Box>

                <Button
                    variant="solid"
                    onClick={handleSendClick}
                    disabled={cooldown > 0 || chars.join("").length !== 4}
                    style={{
                        display: "inline-flex",
                        alignItems: "center",
                        justifyContent: "center",
                        gap: "12px",
                        borderRadius: "40px",
                        padding: "8px 16px 8px 24px",
                        fontSize: "1.1rem",
                        height: "56px",
                        whiteSpace: "nowrap",
                    }}
                >
                    <Typography variant="caption" style={{ fontWeight: "bold", fontSize: "inherit", color: "inherit" }}>
                        Send
                    </Typography>
                    <Typography variant="caption" style={{ opacity: 0.8, fontSize: "0.85rem", minWidth: "55px", color: "inherit" }}>
                        10 CG Coins
                    </Typography>
                    <Box
                        style={{
                            width: "1px",
                            height: "24px",
                            background: "currentColor",
                            opacity: 0.3,
                            margin: "0 4px",
                        }}
                    />
                    <CooldownTimer timeLeft={cooldown} maxTime={2} />
                </Button>

                <Box style={{ display: "flex", gap: "2rem", justifyContent: "center" }}>
                    <Box style={{ textAlign: "center" }}>
                        <Typography variant="caption" style={{ opacity: 0.7 }}>Balance</Typography>
                        <Typography variant="body" style={{ fontWeight: 600, fontVariantNumeric: "tabular-nums" }}>
                            {balanceBefore !== undefined ? `${balanceBefore}` : "—"}
                        </Typography>
                    </Box>
                    <Box style={{ textAlign: "center" }}>
                        <Typography variant="caption" style={{ opacity: 0.7 }}>Spent</Typography>
                        <Typography variant="body" style={{ fontWeight: 600, fontVariantNumeric: "tabular-nums" }}>
                            {spent > 0 ? `${spent}` : "0"}
                        </Typography>
                    </Box>
                </Box>
            </Box>
        </Box>
    );
}
