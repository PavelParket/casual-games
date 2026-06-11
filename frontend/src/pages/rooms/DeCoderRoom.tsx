import React, { useCallback, useEffect, useRef, useState, useMemo, } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { getPlayers, } from "../../store/slices/DeCoderRoomSlice";
import { useGameToast } from "../../hooks/useGameToast";
import { useSystemToastContext } from "../../providers/SystemToastContext";
import { Box, Button, Card, Container, Typography, ToastContainer, Stack, Divider, Grid, CooldownTimer, Modal, Icon, Input, Avatar, } from "../../ui";
import { useThemedIcon } from "../../ui";
import { validateRoomName } from "../../utils/SecurityUtils";
import type { DeCoderMessage } from "../../models/WsMessage";
import type { DeCoderGameHistory } from "../../models/DeCoderGameHistory";
import { MiniProfile } from "../profile/components/MiniProfile";
import { useDeCoderMessages } from "../../hooks/useDeCoderMessages";
import { useGameSocket } from "../../hooks/useGameSocket";

export default function DeCoderRoom() {
    const { getIcon } = useThemedIcon();

    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();

    const { players, room } = useSelector((state: RootState) => state.deCoderRoom);

    const { roomName: rawRoomName, roomId } = useParams<{
        roomName?: string;
        roomId?: string;
    }>();

    const roomName = validateRoomName(rawRoomName ?? "");

    const { toasts, showGameToast, dismiss } = useGameToast();
    const { showSystemToast } = useSystemToastContext();

    const [gameActive, setGameActive] = useState<boolean>(false);
    const [history, setHistory] = useState<DeCoderGameHistory[]>([]);
    const [jackpot, setJackpot] = useState<number>(0);
    const [searchQuery, setSearchQuery] = useState("");

    const [gameOverModal, setGameOverModal] = useState<{
        isOpen: boolean;
        isWin: boolean;
        winnerName?: string;
    } | null>(null);

    const [chars, setChars] = useState<string[]>(["", "", "", ""]);
    const [cooldown, setCooldown] = useState(0);

    const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
    const requestSyncRef = useRef<() => void>(() => { });

    const handleDisplaced = useCallback(() => {
        showSystemToast("Your session was opened in another window", "system-error");
        navigate("/rooms");
    }, [navigate, showSystemToast]);

    const handleDisconnect = useCallback(() => {
        showSystemToast("Connection lost. Redirecting to rooms...", "system-error");
        setTimeout(() => navigate("/rooms"), 3000);
    }, [navigate, showSystemToast]);

    const requestSync = useCallback(() => {
        requestSyncRef.current();
    }, []);

    const handleMessage = useDeCoderMessages({
        roomId,
        setGameActive,
        setHistory,
        setJackpot,
        setGameOverModal,
        showGameToast,
    });

    const { isConnected, send } = useGameSocket<DeCoderMessage>({
        roomId,
        roomType: room?.type,
        showGameToast,
        onGameMessage: handleMessage,
        onDisplaced: handleDisplaced,
        onConnectionLost: handleDisconnect,
    });

    useEffect(() => {
        requestSyncRef.current = () => {
            if (isConnected && roomId) {
                send({ type: "SYSTEM", event: "STATE", roomId });
            }
        };
    }, [isConnected, roomId, send]);

    const handleCharChange = (index: number, val: string) => {
        const char = val
            .replace(/[^A-Za-z]/g, "")
            .toUpperCase()
            .slice(-1);
        const newChars = [...chars];
        newChars[index] = char;
        setChars(newChars);

        if (char !== "" && index < 3) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handleKeyDown = (
        index: number,
        e: React.KeyboardEvent<HTMLInputElement>,
    ) => {
        if (e.key === "Backspace" && chars[index] === "" && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handleSendMove = () => {
        const codeStr = chars.join("");
        if (codeStr.length !== 4) return;
        if (cooldown > 0) return;

        send({ type: "SYSTEM", event: "MOVE", roomId: roomId!, code: codeStr });
        setCooldown(2);
        setChars(["", "", "", ""]);
        inputRefs.current[0]?.focus();
    };

    useEffect(() => {
        if (roomId && room?.type) {
            dispatch(getPlayers({ roomId, roomType: room.type }));
        }
    }, [dispatch, roomId, room?.type]);

    useEffect(() => {
        if (cooldown <= 0) return;
        const timerId = setInterval(() => setCooldown((c) => c - 1), 1000);
        return () => clearInterval(timerId);
    }, [cooldown]);

    const displayedHistory = useMemo(() => {
        let filtered = history;
        if (searchQuery) {
            filtered = history.filter((item) => item.code.includes(searchQuery));
        }
        return [...filtered].reverse();
    }, [history, searchQuery]);

    return (
        <Box className="page-wrapper">
            <Container
                maxWidth="1100px"
                style={{
                    display: "flex",
                    flexDirection: "column",
                    flex: 1,
                    minHeight: 0,
                }}
            >
                <Box
                    style={{
                        padding: "2rem 0",
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                    }}
                >
                    <Typography variant="h2" style={{ textAlign: "center" }}>
                        {roomName}
                    </Typography>
                </Box>

                <Card
                    style={{
                        flex: 1,
                        display: "flex",
                        flexDirection: "column",
                        minHeight: 0,
                        padding: "1.5rem",
                    }}
                >
                    <Grid
                        columns="220px 1fr 300px"
                        gap="2rem"
                        style={{
                            height: "65vh",
                            minHeight: "500px",
                            alignItems: "stretch",
                        }}
                    >
                        <Box
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                height: "100%",
                                minHeight: 0,
                                borderRight: "1px solid var(--color-border)",
                                paddingRight: "1rem",
                            }}
                        >
                            <Typography
                                variant="h3"
                                style={{ textAlign: "center", marginBottom: "1rem" }}
                            >
                                Players
                            </Typography>

                            <Box
                                style={{
                                    flex: 1,
                                    overflowY: "auto",
                                    minHeight: 0,
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: "8px",
                                }}
                            >
                                {players &&
                                    Object.entries(players ?? {}).map(([playerGuid, player]) => (
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
                                                style={{
                                                    cursor: "pointer",
                                                    width: "200px",
                                                    padding: "0.35rem",
                                                    paddingRight: "1rem",
                                                    background: "var(--color-bg-glass)",
                                                    border: "1px solid var(--color-border)",
                                                    borderRadius: "var(--radius-md)",
                                                    boxShadow: "var(--shadow-sm)",
                                                    transition: "all 0.2s ease",
                                                    userSelect: "none"
                                                }}
                                                onMouseEnter={(e) => {
                                                    e.currentTarget.style.boxShadow = "var(--shadow-md)";
                                                }}
                                                onMouseLeave={(e) => {
                                                    e.currentTarget.style.boxShadow = "var(--shadow-sm)";
                                                }}
                                            >
                                                <Avatar src={player.linkProfilePictureMini} fallback={player.username} size={40} />

                                                <Typography variant="body" style={{ fontWeight: "bold" }}>
                                                    {player.username}
                                                </Typography>
                                            </Stack>
                                        </MiniProfile>
                                    ))}
                            </Box>

                            <Divider style={{ margin: "0.5rem 0" }} />
                            <Button
                                variant="outline"
                                onClick={() => navigate("/rooms")}
                                style={{ width: "100%" }}
                            >
                                Leave
                            </Button>
                        </Box>

                        <Box
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                alignItems: "center",
                                justifyContent: "center",
                                minHeight: 0,
                            }}
                        >
                            {!gameActive ? (
                                <Box style={{ textAlign: "center" }}>
                                    <Typography variant="h2" style={{ marginBottom: "1.5rem" }}>
                                        Connecting to game...
                                    </Typography>
                                </Box>
                            ) : (
                                <Stack align="center" gap="3rem">
                                    <Box style={{ display: "flex", gap: "15px" }}>
                                        {chars.map((char, index) => (
                                            <input
                                                key={index}
                                                ref={(el) => {
                                                    inputRefs.current[index] = el;
                                                }}
                                                type="text"
                                                placeholder="A"
                                                value={char}
                                                onChange={(e) =>
                                                    handleCharChange(index, e.target.value)
                                                }
                                                onKeyDown={(e) => handleKeyDown(index, e)}
                                                style={{
                                                    width: "70px",
                                                    height: "90px",
                                                    fontSize: "3rem",
                                                    textAlign: "center",
                                                    borderRadius: "var(--radius-md)",
                                                    border: "2px solid var(--color-border)",
                                                    background: "var(--glass-surface)",
                                                    color: "var(--color-text)",
                                                    outline: "none",
                                                    boxShadow: "var(--shadow-sm)",
                                                    transition: "border-color 0.2s",
                                                    caretColor: "transparent",
                                                    textTransform: "uppercase",
                                                }}
                                                onFocus={(e) =>
                                                    (e.target.style.borderColor = "var(--color-primary)")
                                                }
                                                onBlur={(e) =>
                                                    (e.target.style.borderColor = "var(--color-border)")
                                                }
                                            />
                                        ))}
                                    </Box>

                                    <Button
                                        variant="solid"
                                        onClick={handleSendMove}
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
                                        <Typography
                                            variant="caption"
                                            style={{
                                                fontWeight: "bold",
                                                fontSize: "inherit",
                                                color: "inherit",
                                            }}
                                        >
                                            Send
                                        </Typography>
                                        <Typography
                                            variant="caption"
                                            style={{
                                                opacity: 0.8,
                                                fontSize: "0.85rem",
                                                minWidth: "55px",
                                                color: "inherit",
                                            }}
                                        >
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
                                        <CooldownTimer timeLeft={cooldown} maxTime={5} />
                                    </Button>
                                </Stack>
                            )}
                        </Box>

                        <Box
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                height: "100%",
                                minHeight: 0,
                                borderLeft: "1px solid var(--color-border)",
                                paddingLeft: "1rem",
                                overflow: "hidden",
                            }}
                        >
                            <Box
                                style={{
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "center",
                                    marginBottom: "1rem",
                                }}
                            >
                                <Typography variant="h3">Code Terminal</Typography>
                                <Button
                                    variant="ghost"
                                    onClick={requestSync}
                                    style={{ fontSize: "0.8rem", padding: "7px" }}
                                    title="Sync State"
                                >
                                    <Icon src={getIcon("refresh")} alt="refresh" size={18} />
                                </Button>
                            </Box>
                            <Input
                                value={searchQuery}
                                onChange={(e) =>
                                    setSearchQuery(
                                        e.target.value
                                            .replace(/[^A-Za-z]/g, "")
                                            .toUpperCase()
                                            .slice(0, 4),
                                    )
                                }
                                placeholder="Search history (e.g. ABCD)"
                                style={{ marginBottom: "1rem", width: "100%" }}
                            />

                            <Box
                                style={{
                                    flex: 1,
                                    minHeight: 0,
                                    overflowY: "auto",
                                    background: "var(--color-bg-soft)",
                                    borderRadius: "var(--radius-md)",
                                    border: "1px solid var(--color-border)",
                                    padding: "10px",
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: "8px",
                                    boxShadow: "inset 0 2px 4px rgba(0,0,0,0.05)",
                                }}
                            >
                                {displayedHistory.length === 0 ? (
                                    <Typography
                                        variant="body"
                                        style={{
                                            textAlign: "center",
                                            opacity: 0.5,
                                            marginTop: "2rem",
                                        }}
                                    >
                                        {history.length === 0
                                            ? "No moves yet. Be the first!"
                                            : "No matches found"}
                                    </Typography>
                                ) : (
                                    displayedHistory.map((item, idx) => (
                                        <Box
                                            key={idx}
                                            style={{
                                                display: "flex",
                                                justifyContent: "space-between",
                                                alignItems: "center",
                                                padding: "8px 12px",
                                                background: "var(--color-bg)",
                                                borderRadius: "var(--radius-sm)",
                                                border: "1px solid var(--color-border)",
                                            }}
                                        >
                                            <Typography
                                                variant="body"
                                                style={{
                                                    fontFamily: "monospace",
                                                    fontSize: "1.2rem",
                                                    letterSpacing: "2px",
                                                    fontWeight: "bold",
                                                }}
                                            >
                                                {item.code}
                                            </Typography>
                                            <Stack direction="row" gap="1rem">
                                                <Box
                                                    style={{
                                                        display: "flex",
                                                        alignItems: "center",
                                                        gap: "4px",
                                                    }}
                                                >
                                                    <span
                                                        title="Exact Match (Bulls)"
                                                        style={{ fontSize: "1.2rem" }}
                                                    >
                                                        Exact:
                                                    </span>
                                                    <Typography
                                                        variant="body"
                                                        style={{
                                                            color: "var(--color-success, #2ecc71)",
                                                            fontWeight: "bold",
                                                        }}
                                                    >
                                                        {item.exactMatch}
                                                    </Typography>
                                                </Box>
                                                <Box
                                                    style={{
                                                        display: "flex",
                                                        alignItems: "center",
                                                        gap: "4px",
                                                    }}
                                                >
                                                    <span
                                                        title="Partial Match (Cows)"
                                                        style={{ fontSize: "1.2rem" }}
                                                    >
                                                        Partial:
                                                    </span>
                                                    <Typography
                                                        variant="body"
                                                        style={{
                                                            color: "var(--color-warning, #f1c40f)",
                                                            fontWeight: "bold",
                                                        }}
                                                    >
                                                        {item.partialMatch}
                                                    </Typography>
                                                </Box>
                                            </Stack>
                                        </Box>
                                    ))
                                )}
                            </Box>
                        </Box>
                    </Grid>
                </Card>
            </Container>

            {gameOverModal && (
                <Modal
                    isOpen={gameOverModal.isOpen}
                    onClose={() => {
                        navigate("/rooms");
                    }}
                    title={gameOverModal.isWin ? "Victory!" : "Code Cracked"}
                >
                    <Box style={{ textAlign: "center", padding: "1rem 0" }}>
                        <Typography
                            variant="h2"
                            style={{
                                color: gameOverModal.isWin
                                    ? "var(--color-success)"
                                    : "var(--color-text)",
                                marginBottom: "1rem",
                            }}
                        >
                            {gameOverModal.isWin
                                ? "You Cracked the Code!"
                                : `${gameOverModal.winnerName} won!`}
                        </Typography>
                        <Typography
                            variant="body"
                            style={{ marginBottom: "2rem", opacity: 0.8 }}
                        >
                            {gameOverModal.isWin
                                ? `Congratulations! You won the Jackpot of ${jackpot} CGC!`
                                : "Better luck next time. The code has been deciphered."}
                        </Typography>
                        <Button
                            variant="solid"
                            onClick={() => navigate("/rooms")}
                            style={{ width: "100%", padding: "12px" }}
                        >
                            Leave Room
                        </Button>
                    </Box>
                </Modal>
            )}

            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </Box>
    );
}
