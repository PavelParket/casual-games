import { useState } from "react";
import { Link } from "react-router-dom";
import { Box, Card, Typography, Stack, Divider, Accordion, List } from "../../ui";

export default function TermsOfUse() {
    const [openIndex, setOpenIndex] = useState<number | null>(null);

    const handleToggle = (index: number) => {
        setOpenIndex(prev => (prev === index ? null : index));
    };

    return (
        <Box className="page-wrapper">
            <Box style={{ padding: "2rem 0" }}>
                <Card style={{ padding: "2.5rem", boxShadow: "var(--shadow-lg)" }}>
                    <Typography variant="h1" style={{ marginBottom: "1rem", fontWeight: 700 }}>
                        Terms of Use
                    </Typography>

                    <Typography variant="caption" style={{ display: "block", marginBottom: "3rem" }}>
                        Last updated: June 12, 2026
                    </Typography>

                    <Typography variant="body" style={{ opacity: 0.8, fontSize: "1.2rem", textAlign: "center", marginBottom: "2rem", padding: "0 auto" }}>
                        Please read these Terms of Use carefully before using the Casual Games platform.
                        By creating an account or using the Platform, you agree to be bound by these Terms.
                        If you do not agree, do not use the Platform.
                    </Typography>

                    <Divider style={{ margin: "2rem 0" }} />

                    <Box>
                        <Accordion
                            title="1. About the project and acceptance of these Terms"
                            isOpen={openIndex === 0}
                            onToggle={() => handleToggle(0)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.1. These Terms of Use (the "Terms") govern the relationship between
                                    the development team of the Casual Games web platform (the "Administration", "we")
                                    and any person using the Platform (the "User", "you").
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.2. Casual Games is a commercial project.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.3. The Platform is provided solely for entertainment and demonstration purposes.
                                    <strong>The Platform is not a gambling service, casino, or betting operator.</strong>
                                    No real money is involved at any point: nothing can be paid in,
                                    and nothing can be paid out (see Section 6).
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    1.4. By registering an account or using any feature of the Platform,
                                    you confirm that you have read, understood, and accepted these Terms.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="2. Definitions"
                            isOpen={openIndex === 1}
                            onToggle={() => handleToggle(1)}
                        >
                            <List gap="0.75rem" items={[
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>Account</strong> — a unique record
                                    in the Platform's database containing your username, email address,
                                    password hash, avatar, virtual balance, and activity history.
                                </Typography>,
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>CG Coins</strong> — the Platform's virtual in-game currency (points),
                                    used to simulate bets, participate in games, and purchase premium statuses.
                                    CG Coins have no monetary value (see Section 6).
                                </Typography>,
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>Game Rooms</strong> — virtual lobbies in which
                                    game sessions between Users take place.
                                </Typography>,
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    <strong>Premium statuses (Subscriptions)</strong> — optional account
                                    tiers (PRO, VIP) purchased with CG Coins that temporarily extend account features.
                                </Typography>
                            ]} />
                        </Accordion>

                        <Accordion
                            title="3. Eligibility"
                            isOpen={openIndex === 2}
                            onToggle={() => handleToggle(2)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.1. You must be at least 16 years old to use the Platform.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    3.2. You may create only one Account.
                                    Creating multiple accounts to abuse Platform mechanics is prohibited.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="4. Account and security"
                            isOpen={openIndex === 3}
                            onToggle={() => handleToggle(3)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.1. Registration requires a valid email address, a username, and a password.
                                    You are responsible for providing accurate information and keeping it up to date.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.2. You are solely responsible for keeping your password confidential
                                    and for securing access to your email account.
                                    All actions performed under your Account are deemed to be performed by you.
                                    The Administration is not liable for unauthorized access resulting from your
                                    failure to protect your credentials.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.3. For security reasons, the Platform may limit the number of simultaneous
                                    active sessions per Account; an active game connection may be closed when
                                    the same Account connects from elsewhere.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    4.4. The Platform does not provide self-service account deletion.
                                    To delete your Account, contact <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a> (see also the Privacy Policy, Section 8).
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="5. Games"
                            isOpen={openIndex === 4}
                            onToggle={() => handleToggle(4)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.1. The Platform offers a set of multiplayer and single-player games.
                                    The current list of games, their rules, betting mechanics, time limits,
                                    and payout logic are displayed directly in the Platform
                                    interface — on the game pages and inside Game Rooms.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.2. All game outcomes are computed and validated on the server.
                                    The result determined by the server is final and is not subject to revision,
                                    except where the Administration identifies a technical fault on the Platform side.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.3. Game mechanics, rules, costs, and rewards are expressed exclusively
                                    in CG Coins and may be changed by the Administration as the project evolves.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    5.4. If a game session is interrupted due to an opponent leaving an active match,
                                    the Platform aims to settle the session fairly (for example, by refunding the remaining player's bet)
                                    according to the rules of the specific game shown in its interface.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="6. Virtual currency (CG Coins)"
                            isOpen={openIndex === 5}
                            onToggle={() => handleToggle(5)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.1. <strong>The "Deposit" function is a simulator.</strong> No payment gateways,
                                    bank cards, or wallets are connected to the Platform. Topping up the balance is instant,
                                    free, and exists purely to demonstrate the Platform's functionality.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.2. <strong>CG Coins have no real-world value.</strong> They are not money, electronic money,
                                    or a financial instrument; they cannot be exchanged for fiat currency, cryptocurrency,
                                    goods, or services; they cannot be transferred between Users or withdrawn from the Platform in any form.
                                    Any "winnings" are virtual and carry no monetary entitlement.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    6.3. The Administration may reset CG Coin balances of all Users
                                    at any time as part of technical maintenance, database resets,
                                    or Platform updates, without compensation.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="7. Subscriptions and premium statuses"
                            isOpen={openIndex === 6}
                            onToggle={() => handleToggle(6)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.1. The Platform offers three account statuses: Default (free), PRO, and VIP.
                                    PRO and VIP are purchased with CG Coins for a billing period of 30 calendar days.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.2. Subscriptions renew automatically: at the end of the billing period,
                                    the renewal price is charged from your CG Coin balance. If the balance is insufficient,
                                    your status reverts to Default. Auto-renewal can be disabled in your profile.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.3. Upgrading to a higher tier mid-cycle is charged with a proportional discount
                                    for the unused days of the current status. Downgrading to a lower tier is scheduled:
                                    the current status remains active until the end of the paid period,
                                    after which the new status takes effect.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    7.4. Current prices, benefits, and exact upgrade/downgrade conditions are displayed
                                    in the Platform interface and may change as the project evolves. Since subscriptions
                                    are paid in CG Coins, which have no monetary value, no refunds in any real-world form are possible.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="8. User content and rules of conduct"
                            isOpen={openIndex === 7}
                            onToggle={() => handleToggle(7)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    8.1. <strong>Usernames and room names</strong> must not contain obscene language,
                                    insults, discriminatory or hateful statements,
                                    or impersonation of other people or of the Administration.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    8.2. Avatars must not contain pornographic or sexualized content,
                                    depictions of violence, hate symbols or symbols of prohibited organizations,
                                    content that violates third-party rights (including copyrighted characters and images),
                                    or any other unlawful content.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    8.3. By uploading an avatar, you confirm that you hold the necessary rights
                                    to the image and grant the Administration a non-exclusive,
                                    royalty-free license to store, process (including resizing),
                                    and display it within the Platform for as long as it remains on your Account.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    8.4. The following is strictly prohibited:
                                </Typography>
                                <List gap="0.75rem" items={[
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        interfering with the operation of the Platform, its servers, or network infrastructure;
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        attempting denial-of-service attacks or otherwise degrading the service for other Users;
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        exploiting bugs or vulnerabilities to manipulate balances, game outcomes,
                                        or any other Platform state — discovered issues should be reported to <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a>;
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        intercepting, modifying, or forging the Platform's network traffic;
                                    </Typography>,
                                    <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                        using bots, scripts, or other automated means to play games or interact with the Platform.
                                    </Typography>,
                                ]} />
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    8.5. The Administration may remove violating content, restrict features,
                                    or suspend or terminate Accounts that breach these Terms — in serious cases without prior warning.
                                    A User who believes a restriction was applied in error may appeal by writing to <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a>.
                                </Typography>
                            </Stack>
                        </Accordion>

                        <Accordion
                            title="9. Intellectual property"
                            isOpen={openIndex === 8}
                            onToggle={() => handleToggle(8)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    9.1. The Platform — including its software, design, interface, game implementations,
                                    logos, and the "CG Coins" system — belongs to the Administration.
                                    These Terms grant you a limited, non-exclusive, non-transferable right to use
                                    the Platform for personal, non-commercial purposes.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    9.2. You retain all rights to content you upload (your avatar),
                                    subject to the license in Section 8.3.
                                </Typography>
                            </Stack>
                        </Accordion>
                        <Accordion
                            title="10. Changes to these Terms"
                            isOpen={openIndex === 9}
                            onToggle={() => handleToggle(9)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    The Administration may amend these Terms at any time.
                                    The current version is always available at <Link to="/terms" className="link" style={{ fontWeight: 700, }}>/terms</Link>,
                                    with the "Last updated" date at the top.
                                    Continued use of the Platform after changes
                                    are published constitutes acceptance of the updated Terms.
                                    If you do not agree with the changes, stop using the Platform and,
                                    if desired, request account deletion.
                                </Typography>
                            </Stack>
                        </Accordion>
                        <Accordion
                            title="11. Disputes and contact"
                            isOpen={openIndex === 10}
                            onToggle={() => handleToggle(10)}
                        >
                            <Stack gap="0.75rem">
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    11.1. Any question, complaint, or dispute related to the Platform should first
                                    be addressed to <a href="mailto:support@casual-games.win" className="link">support@casual-games.win</a>.
                                    We aim to resolve all issues informally and in good faith.
                                </Typography>
                                <Typography variant="body" style={{ opacity: 0.8, lineHeight: 1.6 }}>
                                    11.2. These Terms and your use of the Platform are governed by applicable law
                                    of the jurisdiction in which the Platform's Administration operates,
                                    without prejudice to mandatory consumer protections of your country of residence.
                                </Typography>
                            </Stack>
                        </Accordion>
                    </Box>
                </Card>
            </Box>
        </Box>
    );
}
