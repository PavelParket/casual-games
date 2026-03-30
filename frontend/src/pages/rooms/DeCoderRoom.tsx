import React, {
  useCallback,
  useEffect,
  useRef,
  useState,
  useMemo,
} from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { useWebSocket } from "../../hooks/useWebSocket";
import { getBalance } from "../../store/slices/UserSlice";
import {
  getRoomById,
  getUsernamesInRoom,
} from "../../store/slices/DeCoderRoomSlice";
import { useGameToast } from "../../hooks/useGameToast";
import { useSystemToastContext } from "../../providers/SystemToastContext";
import { errorCodeMessages } from "../../models/constants/ErrorCodeMessages";

import {
  Box,
  Button,
  Card,
  Container,
  Typography,
  ToastContainer,
  Stack,
  Divider,
  Grid,
  CooldownTimer,
  Modal,
  Icon,
  Input,
} from "../../ui";
import { useThemedIcon } from "../../ui";
import { validateRoomName, validateWSMessage } from "../../utils/SecurityUtils";
import type { DeCoderMessage, ErrorWSMessage } from "../../models/WsMessage";

import type { DeCoderGameHistory } from "../../models/DeCoderGameHistory";

export default function DeCoderRoom() {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();

  const { user } = useSelector((state: RootState) => state.auth);
  const guid = user?.guid;

  const { roomName: rawRoomName, roomId } = useParams<{
    roomName?: string;
    roomId?: string;
  }>();
  const roomName = validateRoomName(rawRoomName ?? "");

  const { players } = useSelector((state: RootState) => state.deCoderRoom);
  const playersRef = useRef(players || {});

  const { toasts, showGameToast, dismiss } = useGameToast();
  const { showSystemToast } = useSystemToastContext();
  const { getIcon } = useThemedIcon();

  useEffect(() => {
    playersRef.current = players || {};
  }, [players]);

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
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
  const [cooldown, setCooldown] = useState(0);

  const { isConnected, message, send } = useWebSocket<DeCoderMessage>(
    roomId,
    "DE_CODER",
  );

  const processedMessageRef = useRef<DeCoderMessage | null>(null);

  useEffect(() => {
    if (!roomName || !roomId) navigate("/rooms");
    else {
      dispatch(getRoomById({ roomId }));
      dispatch(getUsernamesInRoom({ roomId, roomType: "DE_CODER" }));
    }
  }, [roomName, roomId, navigate, dispatch]);

  useEffect(() => {
    if (cooldown <= 0) return;
    const timerId = setInterval(() => setCooldown((c) => c - 1), 1000);
    return () => clearInterval(timerId);
  }, [cooldown]);

  const refreshUserBalance = useCallback(() => {
    if (guid) dispatch(getBalance(guid));
  }, [dispatch, guid]);

  const requestSync = useCallback(() => {
    if (isConnected && roomId) {
      send({ type: "SYSTEM", event: "STATE", roomId });
    }
  }, [isConnected, send, roomId]);

  useEffect(() => {
    if (!isConnected || !message) return;

    if (message === processedMessageRef.current) {
      return;
    }

    processedMessageRef.current = message;

    const sanitized = validateWSMessage(message, [
      "type",
      "event",
      "message",
      "code",
      "player",
      "winner",
      "gameState",
      "isGameStarted",
      "jackpot",
    ]) as DeCoderMessage;

    switch (sanitized.event) {
      case "STATE":
        setHistory(sanitized.gameState || []);
        setJackpot(sanitized.jackpot || 0);
        if (sanitized.isGameStarted !== undefined) {
          setGameActive(sanitized.isGameStarted);
        }
        break;

      case "MOVE":
        if (sanitized.gameState && sanitized.gameState.length > 0) {
          setHistory((prev) => [...prev, ...sanitized.gameState!]);
        }
        if (sanitized.jackpot !== undefined) {
          setJackpot(sanitized.jackpot);
        }

        if (sanitized.player !== guid) {
          const playerName = playersRef.current[sanitized.player!] || "Someone";
          showGameToast(`${playerName} made a move`, "game-info");
        } else {
          showGameToast("Move accepted", "game-info");
        }

        refreshUserBalance();
        break;

      case "WINNER": {
        setGameActive(false);
        if (sanitized.gameState && sanitized.gameState.length > 0) {
          setHistory((prev) => [...prev, ...sanitized.gameState!]);
        }
        if (sanitized.jackpot !== undefined) setJackpot(sanitized.jackpot);

        const winnerName =
          playersRef.current[sanitized.winner!] || "Unknown Player";
        const isMe = sanitized.winner === guid;

        setGameOverModal({
          isOpen: true,
          isWin: isMe,
          winnerName: isMe ? "You" : winnerName,
        });

        refreshUserBalance();
        break;
      }

      case "ERROR": {
        const errorMsg = message as ErrorWSMessage;
        const code = errorMsg.errorCode ?? "";
        let text = errorCodeMessages[code];

        if (!text) {
          const msg = sanitized.message || "Error occurred";
          if (msg.includes("not started") || msg.includes("Game not found")) {
            setGameActive(false);
            text = "Game session expired or not started.";
          } else if (msg.includes("already in progress")) {
            setGameActive(true);
            requestSync();
            return;
          } else if (msg.includes("Insufficient funds")) {
            text = "Transaction failed: Insufficient funds!";
          } else {
            text = msg;
          }
        }

        showGameToast(text, "game-error");
        break;
      }

      case "JOIN":
      case "LEAVE":
        if (roomId)
          dispatch(getUsernamesInRoom({ roomId, roomType: "DE_CODER" }));
        break;
    }
  }, [
    isConnected,
    message,
    guid,
    refreshUserBalance,
    dispatch,
    roomId,
    requestSync,
    showGameToast,
    showSystemToast,
  ]);

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

  const displayedHistory = useMemo(() => {
    let filtered = history;
    if (searchQuery) {
      filtered = history.filter((item) => item.code.includes(searchQuery));
    }
    return [...filtered].reverse();
  }, [history, searchQuery]);

  return (
    <Box
      style={{
        minHeight: "calc(100vh - 60px - 50px)",
        margin: "0 10rem",
        padding: "0 1rem",
        background: "var(--color-bg-glass)",
        backdropFilter: "blur(2px)",
        borderRadius: "var(--radius-md)",
        boxShadow: "var(--shadow-lg)",
      }}
    >
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
            columns="250px 1fr 300px"
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
                  Object.entries(players).map(([id, name]) => (
                    <Typography
                      key={id}
                      variant="body"
                      style={{
                        fontWeight: id === guid ? "bold" : "normal",
                        color:
                          id === guid
                            ? "var(--color-primary)"
                            : "var(--color-text)",
                        padding: "8px",
                        background:
                          id === guid ? "var(--color-bg-soft)" : "transparent",
                        borderRadius: "var(--radius-sm)",
                      }}
                    >
                      {name} {id === guid && "(You)"}
                    </Typography>
                  ))}
              </Box>

              <Divider style={{ margin: "1rem 0" }} />
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
