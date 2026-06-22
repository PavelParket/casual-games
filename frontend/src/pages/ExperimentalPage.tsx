import { useState } from "react";
import { Box, Button, Card, Divider, Stack, Typography } from "../ui";
import axios, { AxiosError } from "axios";
import { USER_SERVICE_URL } from "../api/ApiDictionary";
import { Skeleton } from "../ui/components/common/Skeleton";
import { ToastContainer } from "../ui/components/common/ToastContainer";
import { useGameToast } from "../hooks/useGameToast";
import type { CardRank, CardSuit, DurakAction, DurakCard, DurakPhase, DurakTablePair } from "../models/Durak";
import { PlayingCard } from "./rooms/durak/components/PlayingCard";
import { CARD_BACK } from "./rooms/durak/utils/CardUtils";
import { DurakBoard } from "./rooms/durak/components/DurakBoard";
import type { TableExitMode } from "./rooms/DurakRoom";
import { useSystemToastContext } from "../hooks/useSystemToastContext";

interface User {
    guid: string;
    username: string;
    email: string;
    balance: number;
    role: string;
    status: string;
}

const SUITS: CardSuit[] = ["HEARTS", "DIAMONDS", "CLUBS", "SPADES"];
const RANKS: CardRank[] = ["SIX", "SEVEN", "EIGHT", "NINE", "TEN", "JACK", "QUEEN", "KING", "ACE"];

const SUIT_LABELS: Record<CardSuit, string> = {
    HEARTS: "♥ Hearts",
    DIAMONDS: "♦ Diamonds",
    CLUBS: "♣ Clubs",
    SPADES: "♠ Spades",
};

const SUIT_COLORS: Record<CardSuit, string> = {
    HEARTS: "#e74c3c",
    DIAMONDS: "#e74c3c",
    CLUBS: "var(--color-text)",
    SPADES: "var(--color-text)",
};

// ── Mock data for DurakBoard preview ──

const MOCK_MY_CARDS: DurakCard[] = [
    { rank: "JACK", suit: "HEARTS" },
    { rank: "QUEEN", suit: "SPADES" },
    { rank: "SIX", suit: "DIAMONDS" },
    { rank: "ACE", suit: "CLUBS" },
    { rank: "NINE", suit: "HEARTS" },
    { rank: "KING", suit: "SPADES" },
];

const MOCK_TRUMP_CARD: DurakCard = { rank: "SEVEN", suit: "SPADES" };

const MOCK_TABLE_EMPTY: DurakTablePair[] = [];

const MOCK_TABLE_WITH_PAIRS: DurakTablePair[] = [
    { attackCard: { rank: "SEVEN", suit: "CLUBS" }, defendCard: { rank: "JACK", suit: "CLUBS" } },
    { attackCard: { rank: "NINE", suit: "DIAMONDS" }, defendCard: null },
    { attackCard: { rank: "SIX", suit: "HEARTS" }, defendCard: { rank: "ACE", suit: "HEARTS" } },
];

type BoardScenario = "lobby" | "my-turn-attack" | "my-turn-defend" | "opponent-turn" | "deck-empty";

const SCENARIOS: { key: BoardScenario; label: string }[] = [
    { key: "lobby", label: "Empty table" },
    { key: "my-turn-attack", label: "My turn (attack)" },
    { key: "my-turn-defend", label: "My turn (defend)" },
    { key: "opponent-turn", label: "Opponent's turn" },
    { key: "deck-empty", label: "Deck empty" },
];

function getBoardProps(scenario: BoardScenario) {
    const base = {
        myCards: MOCK_MY_CARDS,
        opponentCardCount: 5,
        trumpCard: MOCK_TRUMP_CARD,
        trumpSuit: "SPADES" as CardSuit,
        playerName: "You",
        opponentName: "Opponent",
        discardCount: 4,
        remainingSeconds: 28,
        disabled: false,
        isOpponentAttacker: false,
        tableExitMode: null as TableExitMode,
        isDealAnimation: false,
        onDealComplete: () => { },
        onPlayCard: (c: DurakCard) => console.log("playCard", c),
        onPass: () => console.log("pass"),
        onTakeCards: () => console.log("takeCards"),

    };

    switch (scenario) {
        case "lobby":
            return {
                ...base,
                deckCardsLeft: 24,
                table: MOCK_TABLE_EMPTY,
                phase: "ATTACKING" as DurakPhase,
                isMyTurn: false,
                availableActions: [] as DurakAction[],
            };
        case "my-turn-attack":
            return {
                ...base,
                deckCardsLeft: 24,
                table: MOCK_TABLE_EMPTY,
                phase: "ATTACKING" as DurakPhase,
                isMyTurn: true,
                availableActions: ["PLAY_CARD"] as DurakAction[],
            };
        case "my-turn-defend":
            return {
                ...base,
                deckCardsLeft: 16,
                table: MOCK_TABLE_WITH_PAIRS,
                phase: "DEFENDING" as DurakPhase,
                isMyTurn: true,
                availableActions: ["PLAY_CARD", "TAKE_CARDS"] as DurakAction[],
            };
        case "opponent-turn":
            return {
                ...base,
                deckCardsLeft: 16,
                table: MOCK_TABLE_WITH_PAIRS,
                phase: "DEFENDING" as DurakPhase,
                isMyTurn: false,
                availableActions: [] as DurakAction[],
            };
        case "deck-empty":
            return {
                ...base,
                deckCardsLeft: 0,
                trumpCard: null,
                table: MOCK_TABLE_WITH_PAIRS,
                phase: "THROWING_MORE" as DurakPhase,
                isMyTurn: true,
                availableActions: ["PLAY_CARD", "PASS"] as DurakAction[],
                discardCount: 18,
                remainingSeconds: 8,
            };
    }
}

export default function ExperimentalPage() {
    const [user, setUser] = useState<User>();
    const [error, setError] = useState<string | undefined>("");
    const [flipped, setFlipped] = useState<Record<string, boolean>>({});
    const [scenario, setScenario] = useState<BoardScenario>("my-turn-attack");
    const skeleton = true;

    const { showSystemToast } = useSystemToastContext();
    const { toasts, showGameToast, dismiss } = useGameToast();

    const GUID = "bac51569-dd11-4339-bf50-b92c2f1fe019";

    const handleSend = async () => {
        try {
            setError("");
            const response = await axios.get<User>(`${USER_SERVICE_URL}/users/guid=${GUID}`);
            setUser(response.data);
        } catch (e) {
            console.log(e);
            const err = e as AxiosError<{ message?: string }>;
            setError(err.response?.data?.message);
        }
    };

    const toggleFlip = (id: string) => {
        setFlipped(prev => ({ ...prev, [id]: !prev[id] }));
    };

    const flipAll = (faceDown: boolean) => {
        const next: Record<string, boolean> = {};
        SUITS.forEach(suit => RANKS.forEach(rank => {
            next[`${rank}_${suit}`] = faceDown;
        }));
        setFlipped(next);
    };

    const boardProps = getBoardProps(scenario);

    return (
        <>
            <Box style={{ padding: "1rem 2rem" }}>
                <Button variant="solid" onClick={handleSend}>Send</Button>
                <Divider />

                {/* ── DurakBoard preview ── */}
                <Card style={{ padding: "1.5rem", marginTop: "1.5rem" }}>
                    <Stack gap="1rem">
                        <Stack direction="row" align="center" justify="space-between">
                            <Typography variant="h3">DurakBoard preview</Typography>
                            <Stack direction="row" gap="0.5rem" wrap="wrap">
                                {SCENARIOS.map(s => (
                                    <Button
                                        key={s.key}
                                        variant={scenario === s.key ? "solid" : "outline"}
                                        onClick={() => setScenario(s.key)}
                                    >
                                        {s.label}
                                    </Button>
                                ))}
                            </Stack>
                        </Stack>

                        <Box style={{
                            border: "1px solid var(--color-border)",
                            borderRadius: "var(--radius-md)",
                            overflow: "hidden",
                        }}>
                            {/* key={scenario} forces full remount on scenario switch — resets timer */}
                            <DurakBoard key={scenario} {...boardProps} />
                        </Box>
                    </Stack>
                </Card>

                <Divider />

                {/* ── Card deck preview ── */}
                <Card style={{ padding: "1.5rem", marginTop: "1.5rem" }}>
                    <Stack gap="1.5rem">
                        <Stack direction="row" align="center" justify="space-between">
                            <Typography variant="h3">Card deck preview (36 cards)</Typography>
                            <Stack direction="row" gap="0.5rem">
                                <Button variant="outline" onClick={() => flipAll(false)}>Show all face-up</Button>
                                <Button variant="outline" onClick={() => flipAll(true)}>Show all face-down</Button>
                            </Stack>
                        </Stack>

                        {/* Card back */}
                        <Stack gap="0.5rem">
                            <Typography variant="caption" style={{ fontWeight: 600 }}>Card back</Typography>
                            <Box style={{ width: 62, height: 88, borderRadius: "var(--radius-sm)", overflow: "hidden", boxShadow: "var(--shadow-md)" }}>
                                <img src={CARD_BACK} alt="card back" style={{ width: "100%", height: "100%", objectFit: "contain", display: "block" }} />
                            </Box>
                        </Stack>

                        <Divider />

                        {SUITS.map(suit => (
                            <Stack key={suit} gap="0.75rem">
                                <Typography variant="caption" style={{ fontWeight: 600, color: SUIT_COLORS[suit] }}>
                                    {SUIT_LABELS[suit]}
                                </Typography>
                                <Stack direction="row" gap="0.5rem" wrap="wrap">
                                    {RANKS.map(rank => {
                                        const card: DurakCard = { rank, suit };
                                        const id = `${rank}_${suit}`;
                                        return (
                                            <PlayingCard
                                                key={id}
                                                card={card}
                                                faceDown={flipped[id] ?? false}
                                                size="md"
                                                onClick={() => toggleFlip(id)}
                                            />
                                        );
                                    })}
                                </Stack>
                            </Stack>
                        ))}
                    </Stack>
                </Card>

                <Divider />

                {/* ── System toast playground ── */}
                <Card style={{ padding: "1.5rem", marginTop: "1.5rem", display: "flex", flexDirection: "column", gap: "1rem" }}>
                    <Typography variant="h3">System toast playground</Typography>
                    <Box style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap" }}>
                        <Button variant="outline" onClick={() => showSystemToast("Connection restored successfully", "system-info")}>
                            system-info
                        </Button>
                        <Button variant="outline" onClick={() => showSystemToast("Service is temporarily unavailable", "system-error")}>
                            system-error
                        </Button>
                        <Button
                            variant="outline"
                            onClick={() => {
                                showSystemToast("First notification (oldest)", "system-info");
                                showSystemToast("Second notification", "system-info");
                                showSystemToast("Third notification", "system-info");
                                showSystemToast("Fourth — should evict first", "system-error");
                            }}
                        >
                            Overflow (4 at once)
                        </Button>
                    </Box>
                </Card>

                {/* ── Game toast playground ── */}
                <Card style={{ padding: "1.5rem", marginTop: "1rem", display: "flex", flexDirection: "column", gap: "1rem" }}>
                    <Typography variant="h3">Game toast playground</Typography>
                    <Box style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap" }}>
                        <Button variant="outline" onClick={() => showGameToast("Your bet has been accepted", "game-info")}>
                            game-info
                        </Button>
                        <Button variant="outline" onClick={() => showGameToast("It's not your turn", "game-error")}>
                            game-error
                        </Button>
                        <Button
                            variant="outline"
                            onClick={() => {
                                for (let i = 1; i <= 7; i++) {
                                    showGameToast(`Game event #${i}`, i % 2 === 0 ? "game-error" : "game-info");
                                }
                            }}
                        >
                            Queue overflow (7 at once)
                        </Button>
                    </Box>
                </Card>

                {user ? (
                    <Box>
                        <Typography variant="h1">{user?.guid}</Typography>
                        <Typography variant="h1">{user?.username}</Typography>
                        <Typography variant="h1">{user?.email}</Typography>
                        <Typography variant="h1">{user?.balance}</Typography>
                        <Typography variant="h1">{user?.role}</Typography>
                        <Typography variant="h1">{user?.status}</Typography>
                    </Box>
                ) : (
                    <Box>
                        <Typography variant="h1">{error}</Typography>
                    </Box>
                )}

                {skeleton && (
                    <Card style={{ height: "5rem", width: "7rem", marginTop: "2rem" }}>
                        <Skeleton variant="card" />
                    </Card>
                )}
            </Box>

            <ToastContainer layer="game" toasts={toasts} dismiss={dismiss} />
        </>
    );
}
