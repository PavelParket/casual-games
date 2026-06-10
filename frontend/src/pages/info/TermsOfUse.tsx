import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Box, Container, Card, Typography, Button, Stack, Divider, Accordion, List } from "../../ui";

export default function TermsOfUse() {
    const navigate = useNavigate();

    const [openIndex, setOpenIndex] = useState<number | null>(null);

    const handleToggle = (index: number) => {
        setOpenIndex(prev => (prev === index ? null : index));
    };

    return (
        <Box style={{
            minHeight: "100vh",
            background: "var(--color-bg)",
            backgroundImage: "var(--bg-image)",
            backgroundSize: "cover",
            backgroundPosition: "center",
            backgroundAttachment: "fixed",
            padding: "3rem 0"
        }}>
            <Container>
                <Stack direction="row" align="center" justify="space-between" style={{ marginBottom: "2rem" }}>
                    <Button variant="outline" onClick={() => navigate("/")}>
                        ← Back to Home
                    </Button>
                </Stack>

                <Card style={{ padding: "2.5rem", boxShadow: "var(--shadow-lg)" }}>
                    <Typography variant="h1" style={{ marginBottom: "1rem", fontWeight: 700 }}>
                        Terms of Use
                    </Typography>

                    <Typography variant="body" style={{ opacity: 0.8, marginBottom: "2rem" }}>
                        Please read these Terms of Use carefully before using the Casual Games platform.
                        By accessing or using our services, you agree to be bound by these rules.
                    </Typography>

                    <Divider style={{ margin: "2rem 0" }} />

                    <Box>
                        <Accordion
                            title="1. General provisions and status of the project"
                            isOpen={openIndex === 0}
                            onToggle={() => handleToggle(0)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.1. This User Agreement (hereinafter referred to as the "Agreement")
                                    regulates the relationship between the Administration (developers)
                                    of the Casual Games web platform (hereinafter referred to as the "Platform")
                                    and any individual using the Platform (hereinafter referred to as the "User").
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.2. The Casual Games platform is a non-commercial, demonstration and educational
                                    project created to demonstrate the skills of web development,
                                    interface design and programming of real-time systems.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.3. The Platform and all its services are provided solely for entertainment and informational purposes.
                                    The Platform is not a gambling establishment, casino, or gambling organizer.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.4. The registration procedure on the Platform or the actual use of any of its functions means
                                    the User's full, unconditional and informed consent to the terms of this Agreement.
                                    If the User does not agree with the terms, he is obliged to immediately stop using the Platform.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="2. Terms and definitions"
                            isOpen={openIndex === 1}
                            onToggle={() => handleToggle(1)}
                        >
                            <List gap="0.75rem" items={[
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>2.1. Game Account</strong> is a unique entry in the Platform's database containing
                                    the User's username, email address, encrypted password, avatar, game balance, and User activity history.
                                </Typography>,
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>2.2. CG Coins (CG Coins)</strong> is an in-game virtual currency (points)
                                    used by the Platform to simulate gaming bets, pay for gaming sessions, and purchase premium statuses.
                                </Typography>,
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>2.3. Game Rooms</strong> are virtual lobbies for conducting
                                    gaming sessions between Users or the User and the server.
                                </Typography>,
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>2.4. Premium statuses (Subscriptions)</strong> are virtual account
                                    levels (PRO, VIP) that temporarily expand the cosmetic capabilities of the account.
                                </Typography>
                            ]} />
                        </Accordion>

                        <Accordion
                            title="3. Registration, security, and session preemption"
                            isOpen={openIndex === 2}
                            onToggle={() => handleToggle(2)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.1. For the full use of the Platform, the User creates a unique Gaming account.
                                    The username must contain from 3 to 50 characters, and the password must contain at least 4 characters.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.2. The User is solely responsible for the security of his password
                                    and the protection of access to his email. The Administration
                                    is not responsible for hacking the User's account by third parties.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.3. The Platform has a one-time session policy (Session Displacement).
                                    When authorization is detected under one account from another device
                                    or in another browser tab, the current WebSocket session is automatically
                                    closed with the closing code 4001, and the User is redirected to the authorization page.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="4. The rules of the gameplay in the rooms"
                            isOpen={openIndex === 3}
                            onToggle={() => handleToggle(3)}
                        >
                            <Stack gap="1rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6, fontWeight: 500 }}>
                                    Each Game room works according to individual scenarios and WebSockets protocols:
                                </Typography>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>4.1. Tic-Tac-Toe:</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The game is designed for 2 players.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Before the game starts, both participants must place the same bet in CG Coins.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The playing field is 3x3 in size. The moves are made one at a time.
                                            Time is allocated for each turn, controlled by a countdown timer.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The winner takes the total pot of bets minus the commission (if applicable).
                                            In case of a tie, the bets are returned to the players' balance.</Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>4.2. Durak ("Fool" Card Game):</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The game uses a deck of 36 cards and is designed for 2 players.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The gameplay is divided into phases: ATTACKING, DEFENDING, THROWING_MORE, PICKING_UP, BOUT_END, and GAME_OVER.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The sequence of moves and available actions (Play Card, Pass, Take)
                                            are determined dynamically by the server and broadcast via sockets.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            When an opponent exits during an active game, the match is
                                            declared interrupted, and the remaining player's bet is refunded.</Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>4.3. De-Coder (Decoder):</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            A single-player or multiplayer guessing game of a secret 4-letter code (symbols A-Z).</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Each code entry attempt costs a fixed amount of 10 CG Coins.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The cost of each attempt partially forms the cumulative virtual Jackpot of the room.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The server returns the number of exact matches ("Exact Match")
                                            and partial matches ("Partial Match"). The first user to solve
                                            the code (4 Exact Match) wins the accumulated Jackpot.</Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>4.4. Horse Race (Racetrack / Horse Racing):</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Users place bets on virtual horses with pre-determined Odds.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Runs are generated on the server using secure hashing (Server Seed/RNG).
                                            The movement paths (Keyframes) are calculated by the server before the start of the race.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The race animation lasts for a fixed time (12,000 ms) and is played in the browser.
                                            The result is determined by the server and is final.</Typography>
                                    ]} />
                                </Stack>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="5. Features of virtual balance and cg coins"
                            isOpen={openIndex === 4}
                            onToggle={() => handleToggle(4)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.1. Balance replenishment (Deposit): The "Deposit" function on the Platform
                                    is exclusively a simulator. No payment gateways, bank cards, or e-wallets
                                    are connected to the Platform. The "deposit" takes place instantly and
                                    for free at the User's request in order to test the functionality.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.2. Lack of real value: The virtual currency CG Coins is not a means of payment,
                                    cannot be exchanged for real fiat money, cryptocurrency or other tangible assets.
                                    Withdrawal of funds from the Platform is technically and legally impossible.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.3. Resetting the balance: The Administration reserves the right to reset
                                    the CG Coins balances of all Users at any time as part of technical work,
                                    database resetting or Platform updates without compensation.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="6. Subscriptions and premium statuses (default, pro, vip)"
                            isOpen={openIndex === 5}
                            onToggle={() => handleToggle(5)}
                        >
                            <Stack gap="1rem">
                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        6.1. The Platform offers three account statuses:
                                    </Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Default (Default Free status).</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            PRO (Advanced status, acquired with CG Coins).</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            VIP (The maximum status acquired for CG Coins).</Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        6.2. Pricing and debit rules:
                                    </Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            All statuses are acquired for a billing period of 30 calendar days.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            If there is a sufficient amount of CG Coins in the balance on the 31st day,
                                            the status is automatically extended. If there is a shortage of balance,
                                            the User's status is automatically and safely lowered to Default.</Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        6.3. Rules for changing the status (Upgrade / Downgrade):
                                    </Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The transition from PRO to VIP (Upgrade) occurs with the dynamic
                                            calculation of the discount. The discount is calculated based on
                                            the number of unused days of active PRO status.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The transition to a lower status (Downgrade) is queued (Scheduled).
                                            The current benefits are valid until the end of the paid 30-day cycle,
                                            after which a new, cheaper status is activated.</Typography>
                                    ]} />
                                </Stack>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="7. Rules of conduct and user content"
                            isOpen={openIndex === 6}
                            onToggle={() => handleToggle(6)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.1. It is prohibited to use obscene language, insults or discriminatory
                                    statements in the names of users (usernames) and the names of created Game Rooms.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.2. Uploaded user avatars must comply with ethical standards. It is prohibited
                                    to upload pornographic images, violent scenes, calls for violence,
                                    images with symbols of banned organizations, and copyrighted content.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.3. The User is prohibited from interfering with the operation of the Platform code,
                                    carrying out DoS attacks on microservices (`user-service`, `game-service`, `bank-service`, `websocket-hub-service`),
                                    using vulnerabilities (bugs) to cheat the CG Coins balance or interfere
                                    with WebSocket traffic in order to change the results of the games.
                                    If such activity is detected, the account is blocked without warning.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="8. Limitation of liability and technical risks"
                            isOpen={openIndex === 7}
                            onToggle={() => handleToggle(7)}
                        >
                            <Stack gap="1rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    8.1. The Platform is provided on an "AS IS" basis. The developers do not
                                    guarantee the continuous, uninterrupted and error-free operation of the services.
                                </Typography>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        8.2. The platform uses complex network mechanisms
                                        (WebSockets, BroadcastChannel, JWT Refresh Locks).
                                        The Administration is not responsible for:
                                    </Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Loss of game progress or bets in CG Coins due to sudden disconnection
                                            from the User (Connection Lost) or from the server.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Problems caused by network latency (ping), due to which the User's
                                            move in games ("Tic-Tac-Toe", "Fool") could be skipped by the timer.</Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Animation rendering errors in the Hippodrome game on the side of
                                            older mobile device models or outdated browsers.</Typography>
                                    ]} />
                                </Stack>

                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    8.3. Due to the educational nature of the project,
                                    the Administration has the right at any time, without prior notice,
                                    to completely close the project, reset the database, or temporarily
                                    disable the Platform for technical work.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="9. Final provisions"
                            isOpen={openIndex === 8}
                            onToggle={() => handleToggle(8)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    9.1. This Agreement may be changed unilaterally by the Administration at any time.
                                    The new version takes effect from the moment it is published on the `/terms` page.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    9.2. Any disputes arising within the framework of using the Platform
                                    are resolved through negotiations and are of an informal nature.
                                </Typography>
                            </Stack>
                        </Accordion>
                    </Box>

                    <Typography variant="caption" style={{ display: "block", marginTop: "3rem", opacity: 0.5, textAlign: "center" }}>
                        Last updated: June 07, 2026
                    </Typography>
                </Card>
            </Container>
        </Box>
    );
}
