import { useState } from "react";
import { Link } from "react-router-dom";
import { Box, Card, Typography, Stack, Divider, Accordion, List } from "../../ui";

export default function PrivacyPolicy() {
    const [openIndex, setOpenIndex] = useState<number | null>(null);

    const handleToggle = (index: number) => {
        setOpenIndex(prev => (prev === index ? null : index));
    };

    return (
        <Box className="page-wrapper">
            <Box style={{ padding: "2rem 0" }}>
                <Card style={{ padding: "2.5rem", boxShadow: "var(--shadow-lg)" }}>
                    <Typography variant="h1" style={{ marginBottom: "1rem", fontWeight: 700 }}>
                        Privacy Policy
                    </Typography>

                    <Typography variant="caption" style={{ display: "block", marginBottom: "3rem" }}>
                        Last updated: June 12, 2026
                    </Typography>

                    <Typography variant="body" style={{ opacity: 0.8, fontSize: "1.2rem", textAlign: "center", marginBottom: "2rem", padding: "0 auto" }}>
                        This Privacy Policy explains what information the Casual Games platform collects,
                        why we collect it, how long we keep it, and what rights you have over it.
                        Casual Games is a commercial project; we collect the minimum data
                        required to operate the service and nothing more.
                    </Typography>

                    <Divider style={{ margin: "2rem 0" }} />

                    <Box>
                        <Accordion
                            title="1. Who we are"
                            isOpen={openIndex === 0}
                            onToggle={() => handleToggle(0)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.1. Casual Games (the "Platform", "we") is a commercial
                                    web project operated by its development team (the "Administration").
                                    The Platform is available at <Link to="/" className="link" style={{
                                        fontWeight: 700,
                                    }}>casual-games.win</Link>.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.2. For the purposes of data protection law, the Administration acts
                                    as the data controller for the personal data described in this Policy.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.3. For any privacy-related question or request,
                                    contact us at <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a>.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="2. What data we collect and why"
                            isOpen={openIndex === 1}
                            onToggle={() => handleToggle(1)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    We collect only the data that is necessary to provide the service:
                                </Typography>
                                <Typography variant="body" style={{ fontWeight: 600 }}>2.1. Account data:</Typography>
                                <List gap="0.75rem" items={[
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        <strong>Email address</strong> — used to identify your account,
                                        prevent duplicate registrations, and process account-related requests.
                                        It is not displayed publicly and is not used for marketing.
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        <strong>Username</strong> — a public identifier shown
                                        to other players in game rooms, participant lists, leaderboards, and your profile.
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        <strong>Password</strong> — stored only as a cryptographic hash.
                                        We cannot see or recover your password in plain text.
                                    </Typography>
                                ]} />
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    2.2. <strong>Profile picture (avatar)</strong> — optional. If you upload one,
                                    the image is cropped to square proportions and stored in two sizes (full-size and thumbnail).
                                    Your avatar is visible to other players. Uploading an avatar is entirely voluntary.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    2.3. <strong>Game and transaction history</strong> — we record the matches
                                    you play (game type, result, room, date) and the history
                                    of your virtual balance (deposits, bets, winnings, subscription charges).
                                    This data powers the "Game History" and "Balance History" panels in your profile.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    2.4. <strong>Technical data</strong> — server request logs, including IP addresses and basic browser metadata.
                                    We use this data solely to keep the service secure and operational:
                                    detecting abuse, diagnosing errors, and protecting against denial-of-service attacks.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    We do <strong>not</strong> collect your real name, phone number, payment details
                                    (the Platform has no payment processing at all), precise location,
                                    or any data from third-party accounts.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="3. Legal basis for processing"
                            isOpen={openIndex === 2}
                            onToggle={() => handleToggle(2)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.1. Performance of a contract — processing your account data,
                                    game history, and balance history is necessary to provide
                                    the service you signed up for.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.2. Legitimate interests — processing technical logs
                                    and IP addresses is necessary to keep the Platform secure and functional.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.3. Consent — uploading an avatar is optional and constitutes your consent
                                    to its processing and public display; you can withdraw it at any time by deleting the picture.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="4. Cookies and browser storage"
                            isOpen={openIndex === 3}
                            onToggle={() => handleToggle(3)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.1. The Platform uses a single strictly necessary cookie.
                                    It cannot be read by scripts running in the page and contains no tracking information.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.2. We do not use advertising, marketing, or cross-site tracking cookies of any kind.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.3. The Platform stores limited technical data in your browser
                                    (such as session state and interface preferences) to make the application work.
                                    This data stays on your device.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.4. Our infrastructure provider Cloudflare may collect aggregated,
                                    privacy-preserving traffic statistics (such as request counts and country-level origin)
                                    as part of operating its network.
                                    These statistics are not used by us to identify individual users.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="5. Where your data is stored"
                            isOpen={openIndex === 4}
                            onToggle={() => handleToggle(4)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.1. All Platform data is hosted on servers located in <strong>Frankfurt, Germany (European Union)</strong>.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.2. Network traffic to the Platform passes through Cloudflare's infrastructure,
                                    which provides DNS, TLS encryption, and protection against attacks.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="6. Who has access to your data"
                            isOpen={openIndex === 5}
                            onToggle={() => handleToggle(5)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.1. We do not sell, rent, or share your personal data with advertisers,
                                    marketing companies, data brokers, or any other third parties for their own purposes.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.2. The following service providers process data on our behalf, strictly to operate the Platform:
                                </Typography>
                                <List gap="0.75rem" items={[
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        <strong>DigitalOcean, LLC</strong> — server hosting and data storage (EU region).
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        <strong>Cloudflare, Inc.</strong> — DNS, traffic proxying, TLS,
                                        and DDoS protection (as a traffic proxy,
                                        Cloudflare technically processes IP addresses of incoming requests).
                                    </Typography>,
                                ]} />
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.3. We may disclose data if required to do so by law
                                    or a valid request from a competent authority.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="7. How long we keep your data"
                            isOpen={openIndex === 6}
                            onToggle={() => handleToggle(6)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.1. <strong>Account data, game history, and balance history</strong>
                                    are kept for as long as your account exists.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.2. <strong>Technical logs</strong> (including IP addresses) are kept for a limited period
                                    needed for security and diagnostics, after which they are deleted or rotated.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.3. <strong>Avatars</strong> (including IP addresses) are kept for a limited period
                                    needed for security and diagnostics, after which they are deleted or rotated.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.4. The Administration may delete accounts that have been inactive for more than 6 months.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.5. Because the Platform is an educational project, the Administration may occasionally reset
                                    the database as part of major technical updates. Such resets remove stored user data.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="8. Your rights"
                            isOpen={openIndex === 7}
                            onToggle={() => handleToggle(7)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    You have the right to:
                                </Typography>
                                <List gap="0.75rem" items={[
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        8.1. <strong>Access</strong> — request a copy of the personal data we hold about you.
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        8.2. <strong>Rectification</strong> — ask us to correct inaccurate data.
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        8.3. <strong>Erasure</strong> — ask us to delete your account and associated data.
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        8.4. <strong>Restriction and objection</strong> — ask us to limit or stop certain processing.
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        8.5. <strong>Data portability </strong> — receive your data in a structured, machine-readable format.
                                    </Typography>,
                                ]} />
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    To exercise any of these rights, email <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a> from
                                    the address associated with your account. We will respond
                                    within a reasonable time and in any case within the period required by applicable law.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>Account deletion:</strong> the Platform currently does not provide
                                    a self-service "delete account" function. To delete your account,
                                    send a request to the email above;
                                    your account and associated personal data will be removed.
                                    You can delete your avatar yourself at any time from your profile.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    If you believe your rights have been violated,
                                    you also have the right to lodge a complaint with your
                                    local data protection supervisory authority.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="9. How we protect your data"
                            isOpen={openIndex === 8}
                            onToggle={() => handleToggle(8)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    We apply industry-standard technical measures,
                                    including encryption of all traffic in transit (TLS),
                                    cryptographic password hashing, short-lived access credentials,
                                    and strict isolation of internal services from the public internet.
                                    No system is perfectly secure, but the Platform is designed to minimize
                                    both the amount of data collected and the impact of any potential incident.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="10. Children"
                            isOpen={openIndex === 9}
                            onToggle={() => handleToggle(9)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    The Platform is intended for users aged 16 and older.
                                    We do not knowingly collect personal data from anyone under 16.
                                    If you believe a child under 16 has created an account,
                                    contact us at <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a> and we will delete it.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="11. Changes to this Policy"
                            isOpen={openIndex === 10}
                            onToggle={() => handleToggle(10)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    We may update this Policy as the Platform evolves.
                                    The current version is always available at <Link to="/privacy" className="link" style={{ fontWeight: 700, }}>/privacy</Link>,
                                    with the "Last updated" date at the top.
                                    Material changes will be reflected in that date;
                                    continued use of the Platform after an update constitutes acceptance of the revised Policy.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="12. Contact"
                            isOpen={openIndex === 11}
                            onToggle={() => handleToggle(11)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    For any question about this Policy or your data: <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a>.
                                </Typography>
                            </Stack>
                        </Accordion>
                    </Box>
                </Card>
            </Box>
        </Box>
    );
}
