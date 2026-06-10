import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Box, Container, Card, Typography, Button, Stack, Divider, Accordion, List } from "../../ui";

export default function PrivacyPolicy() {
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
                        Privacy Policy
                    </Typography>

                    <Typography variant="body" style={{ opacity: 0.8, marginBottom: "2rem" }}>
                        We take the confidentiality of your data seriously. Our main goal is to minimize
                        the collection of personal information and ensure its maximum security using modern technical solutions.
                    </Typography>

                    <Divider style={{ margin: "2rem 0" }} />

                    <Box>
                        <Accordion
                            title="1. General provisions"
                            isOpen={openIndex === 0}
                            onToggle={() => handleToggle(0)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.1. This Privacy Policy (hereinafter referred to as the "Policy")
                                    describes how the Administration of the Casual Games web platform
                                    (hereinafter referred to as the "Platform", "we") collects, processes,
                                    stores and protects information about Users (hereinafter referred to as "you")
                                    obtained during registration and use of the Platform.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.2. We take the confidentiality of your data seriously.
                                    Our main goal is to minimize the collection of personal information
                                    and ensure its maximum security using modern technical solutions.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.3. Using the Platform means your unconditional consent
                                    to the data processing rules described in this Policy.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="2. What data do we collect and why"
                            isOpen={openIndex === 1}
                            onToggle={() => handleToggle(1)}
                        >
                            <Stack gap="1rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6, fontWeight: 500 }}>
                                    We process only the information that is technically necessary
                                    to maintain the gameplay and the operation of the personal account:
                                </Typography>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>2.1. Account information:</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            <strong>E-mail address (email):</strong> It is collected to identify the User,
                                            restore access to the account, and protect against duplicate profiles.
                                        </Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            <strong>Username:</strong> A public identifier that is displayed in game rooms,
                                            lists of participants, the rating of wins ("Top Wins") and profile.
                                        </Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            <strong>Password:</strong> Stored exclusively in encrypted form
                                            on the side of the authorization server. We do not have access to your password in clear text.
                                        </Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>2.2. Downloadable media files (Avatars):</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            When changing the avatar, the User uploads a graphic file.
                                        </Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            The platform processes the image locally (cuts to square proportions)
                                            and generates two files: <code>full</code> (full-size profile picture)
                                            and <code>mini</code> (thumbnail for player lists).
                                        </Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>2.3. Game and transaction history:</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            We save the history of the matches you played (game type, result, room ID, date)
                                            to display the "Game History" panel in your profile.
                                        </Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            We save the balance's transaction history (deposits, subscription charges, winnings)
                                            for display in the Balance History panel.
                                        </Typography>
                                    ]} />
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>2.4. Technical and system data:</Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Request logs, IP addresses, browser metadata (to detect DoS attacks and protect the server).
                                        </Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            <strong>Access Tokens (Access/Refresh Tokens):</strong>
                                            Temporary session keys for the secure execution of API requests to microservices.
                                        </Typography>
                                    ]} />
                                </Stack>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="3. Technical methods of data processing and security"
                            isOpen={openIndex === 2}
                            onToggle={() => handleToggle(2)}
                        >
                            <Stack gap="1rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6, fontWeight: 500 }}>
                                    To ensure the protection of your data, the following mechanisms
                                    are embedded in the architecture of the Platform:
                                </Typography>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>
                                        3.1. Data Sanitation (XSS/SQL injection Protection):
                                    </Typography>
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        All text fields (username, email, room names)
                                        are heavily filtered (sanitized) before being saved and displayed.
                                        Potentially dangerous tags (such as <code>&lt;script&gt;</code>),
                                        <code>javascript:</code> links, and <code>onerror=</code>
                                        and <code>onload=</code> event handler attributes are removed.
                                    </Typography>
                                </Stack>

                                <Stack gap="0.5rem">
                                    <Typography variant="body" style={{ fontWeight: 600 }}>
                                        3.2. Security of tokens and local storage:
                                    </Typography>
                                    <List gap="0.25rem" items={[
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Access to local storage (localStorage) is protected by
                                            a built-in parser that blocks the execution of suspicious JS code.
                                        </Typography>,
                                        <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                            Tokens are updated proactively (Proactive Refresh)
                                            5 minutes before their expiration date. To avoid conflicting requests
                                            in multiple browser tabs, the Navigator Locks API
                                            (<code>navigator.locks.request</code>) is used to ensure
                                            that only one tab executes the token update request, while the rest receive it synchronously.
                                        </Typography>
                                    ]} />
                                </Stack>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="4. Tab synchronization and session preemption (BroadcastChannel)"
                            isOpen={openIndex === 3}
                            onToggle={() => handleToggle(3)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.1. To maintain the current authorization status in all
                                    tabs of your browser, the Platform uses the channel
                                    <code>BroadcastChannel</code> under the name <code>'auth'</code>.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.2. When updating the token or logging out (Logout)
                                    in one tab, the signal is instantly transmitted to all
                                    other open browser tabs, synchronously unblocking the
                                    user or updating his session key. This eliminates storing
                                    outdated sessions in the browser's memory.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="5. Transfer of data to third parties"
                            isOpen={openIndex === 4}
                            onToggle={() => handleToggle(4)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.1. The Casual Games platform is a non-commercial project.
                                    We never sell, transfer, or disclose Users' personal information
                                    to advertising agencies, marketing companies, or any other third parties.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.2. The only exception may be an official request from
                                    law enforcement agencies in accordance with applicable law.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="6. Terms of data storage and deletion"
                            isOpen={openIndex === 5}
                            onToggle={() => handleToggle(5)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.1. Account data is stored as long as the account is active.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.2. The User has the right at any time to request the deletion
                                    of his profile by contacting the Administration, or to independently
                                    clear the avatar using the "Delete picture" function in his
                                    Personal Account, which will lead to the instant deletion of
                                    image files from the storage servers.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.3. The Administration has the right to delete inactive accounts
                                    (which have not logged in for more than 6 months) or completely
                                    clear the database as part of technical updates to the Platform.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="7. Changes to the privacy policy"
                            isOpen={openIndex === 6}
                            onToggle={() => handleToggle(6)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.1. This Policy may be updated as the functionality
                                    of the Platform evolves. We recommend checking this page regularly for changes.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.2. The new version of the Policy comes into force
                                    from the moment of its publication at <code>/privacy</code>.
                                </Typography>
                            </Stack>
                        </Accordion>
                    </Box>

                    <Typography variant="caption" style={{ display: "block", marginTop: "3rem", opacity: 0.5, textAlign: "center" }}>
                        Last updated: June 8, 2026
                    </Typography>
                </Card>
            </Container>
        </Box>
    );
}
