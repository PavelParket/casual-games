import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { Box, Container, Card, Typography, Button, Grid, Stack, Divider, Modal, Icon, Avatar, useThemedIcon, List, Accordion } from "../../ui";
import { Skeleton } from "../../ui/components/common/Skeleton";
import { purchase, getBalance, getCurrentSubscription, getSubscriptionPlans, findByGuid } from "../../store/slices/UserSlice";
import type { AppDispatch, RootState } from "../../store/store";
import type { UserStatus } from "../../models/User";
import type { Icons } from "../../assets/icons";
import { UPGRADE_FAQ } from "../../models/constants/UpgradeFAQ";
import { useSystemToastContext } from "../../hooks/useSystemToastContext";

const PLAN_FEATURES: Record<string, string[]> = {
    DEFAULT: [],
    PRO: [
        "Developer's approval",
        "Respect in the school"
    ],
    VIP: [
        "Increases your coolness by 20%",
        "B. H. recommends",
        "Developer's eternal gratitude",
        "May (or may not) improve your luck"
    ]
};

const getStatusIconName = (status: UserStatus): keyof typeof Icons.light => {
    return `${status.toLowerCase()}Status` as keyof typeof Icons.light;
};

export default function Upgrade() {
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();
    const { showSystemToast } = useSystemToastContext();
    const { getIcon } = useThemedIcon();

    const authUser = useSelector((state: RootState) => state.auth.user);
    const {
        user,
        subscription,
        subscriptionPlans,
        isPurchasing,
        isLoadingSubscription,
        isLoadingPlans,
        isLoading
    } = useSelector((state: RootState) => state.user);

    const activeStatus = subscription?.status ?? user?.status;
    const isDataReady = !!activeStatus && subscriptionPlans.length > 0 && !isLoadingSubscription && !isLoadingPlans && !isLoading;

    const [openFaqIndex, setOpenFaqIndex] = useState<number | null>(null);

    const [confirmModal, setConfirmModal] = useState<{
        isOpen: boolean;
        statusTitle: UserStatus | null;
        price: number;
        isDowngrade: boolean;
    }>({
        isOpen: false,
        statusTitle: null,
        price: 0,
        isDowngrade: false
    });

    useEffect(() => {
        dispatch(getCurrentSubscription());
        dispatch(getSubscriptionPlans());
    }, [dispatch]);

    useEffect(() => {
        if (authUser?.guid && !user) {
            dispatch(findByGuid(authUser.guid));
        }
    }, [dispatch, authUser?.guid, user]);

    const handleOpenConfirm = (targetTitle: UserStatus, price: number, isDowngrade: boolean) => {
        setConfirmModal({ isOpen: true, statusTitle: targetTitle, price, isDowngrade });
    };

    const handleCloseConfirm = () => {
        if (!isPurchasing) {
            setConfirmModal({ isOpen: false, statusTitle: null, price: 0, isDowngrade: false });
        }
    };

    const executePurchase = async () => {
        if (!confirmModal.statusTitle) return;

        try {
            await dispatch(purchase({ status: confirmModal.statusTitle })).unwrap();
            showSystemToast(`Status successfully changed to ${confirmModal.statusTitle}!`, "system-info");

            if (user?.guid) {
                dispatch(getBalance(user.guid));
            }

            setConfirmModal({ isOpen: false, statusTitle: null, price: 0, isDowngrade: false });
            dispatch(getCurrentSubscription());
        } catch (err) {
            showSystemToast(`Failed to upgrade: ${err}`, "system-error");
            setConfirmModal({ isOpen: false, statusTitle: null, price: 0, isDowngrade: false });
        }
    };

    const sortedPlans = [...subscriptionPlans].sort((a, b) => a.tier - b.tier);

    return (
        <Box className="custom-scrollbar page-wrapper" style={{ overflowY: "auto", padding: "2rem 1rem" }}>
            <Container>
                <Stack direction="row" align="center" justify="space-between" style={{ marginBottom: "3rem" }}>
                    <Typography variant="h2">Upgrade Status</Typography>
                    <Button variant="outline" onClick={() => navigate("/profile")}>Back to Profile</Button>
                </Stack>

                {!isDataReady ? (
                    <Grid columns="repeat(auto-fit, minmax(240px, 1fr))" gap="1.5rem">
                        {Array.from({ length: 3 }).map((_, i) => (
                            <Card key={i} style={{ height: "35rem", padding: "1.5rem" }}>
                                <Skeleton variant="card" height="100%" />
                            </Card>
                        ))}
                    </Grid>
                ) : (
                    <Grid columns="repeat(auto-fit, minmax(240px, 1fr))" gap="1.5rem">
                        {sortedPlans.map((plan) => {
                            const currentPlanObj = sortedPlans.find(p => p.status === activeStatus);
                            if (!currentPlanObj) return null;

                            const isCurrent = activeStatus === plan.status;
                            const isScheduled = subscription?.newStatus === plan.status && !isCurrent;
                            const isDowngrade = plan.tier < currentPlanObj.tier;

                            const displayPrice = (plan.upgradePrice !== null && plan.upgradePrice !== undefined) ? plan.upgradePrice : plan.price;

                            const statusIconSrc = getIcon(getStatusIconName(plan.status));
                            const features = PLAN_FEATURES[plan.status] || [];

                            let buttonText = "Get";
                            let isButtonDisabled = isPurchasing;

                            if (isCurrent) {
                                isButtonDisabled = true;
                                if (subscription?.expiresAt && plan.status !== "DEFAULT") {
                                    buttonText = `Active until ${new Date(subscription.expiresAt).toLocaleDateString()}`;
                                } else {
                                    buttonText = "Current";
                                }
                            } else if (isScheduled) {
                                isButtonDisabled = true;
                                if (subscription?.statusChangeAt) {
                                    buttonText = `Activates ${new Date(subscription.statusChangeAt).toLocaleDateString()}`;
                                } else {
                                    buttonText = "Scheduled";
                                }
                            } else if (isDowngrade) {
                                buttonText = "Downgrade";
                            }

                            const displayPriceText = displayPrice === 0 ? "Free" : `${displayPrice} CG Coins`;

                            return (
                                <Card
                                    key={plan.id}
                                    style={{
                                        display: "flex",
                                        flexDirection: "column",
                                        border: isCurrent ? "2px solid var(--color-border)" : "2px solid var(--color-primary)",
                                        opacity: isCurrent ? 0.6 : 1,
                                        boxShadow: "var(--shadow-md)",
                                        transform: !isCurrent ? "scale(1.03)" : "scale(1)",
                                        transition: "all 0.2s ease",
                                        padding: "1.5rem",
                                        minHeight: "35rem"
                                    }}
                                >
                                    <Typography variant="h3" style={{ textAlign: "center", marginBottom: "0.5rem", fontWeight: 700 }}>
                                        {plan.status}
                                    </Typography>

                                    <Divider variant="middle" style={{ margin: "1rem 0" }} />

                                    <Box style={{
                                        flex: 1,
                                        display: "flex",
                                        flexDirection: "column",
                                        gap: "3rem"
                                    }}>
                                        <Box style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                                            <Typography variant="caption" style={{ marginBottom: "0.75rem", opacity: 0.7 }}>
                                                Profile Preview
                                            </Typography>
                                            <Box style={{
                                                width: "200px",
                                                background: "var(--color-bg)",
                                                borderRadius: "var(--radius-md)",
                                                border: "1px solid var(--color-border)",
                                                boxShadow: "var(--shadow-sm)",
                                                padding: "0.5rem"
                                            }}>
                                                <Box style={{
                                                    padding: "0.5rem 1rem",
                                                    display: "flex",
                                                    alignItems: "center",
                                                    gap: "1rem"
                                                }}>
                                                    <Avatar src={user?.linkProfilePictureMini} fallback={user?.username || "?"} size={40} />
                                                    <Typography
                                                        variant="body"
                                                        style={{
                                                            fontWeight: 600,
                                                            overflow: "hidden",
                                                            textOverflow: "ellipsis",
                                                            whiteSpace: "nowrap"
                                                        }}
                                                        title={user?.username}
                                                    >
                                                        {user?.username}
                                                    </Typography>
                                                </Box>
                                                <Divider style={{ margin: "0.25rem 0" }} />
                                                <Box style={{
                                                    padding: "0.5rem 1rem",
                                                    display: "flex",
                                                    alignItems: "center",
                                                    justifyContent: "center",
                                                    gap: "0.75rem"
                                                }}>
                                                    <Box style={{
                                                        width: "24px",
                                                        height: "24px",
                                                        display: "flex",
                                                        alignItems: "center",
                                                        justifyContent: "center",
                                                        flexShrink: 0
                                                    }}>
                                                        <Icon src={statusIconSrc} alt={plan.status} size={24} />
                                                    </Box>
                                                    <Typography variant="caption" style={{ textTransform: "capitalize", fontWeight: 500, fontSize: "0.9rem" }}>
                                                        {plan.status}
                                                    </Typography>
                                                </Box>
                                            </Box>
                                        </Box>

                                        {features.length > 0 && (
                                            <Box style={{ padding: "0 0.5rem" }}>
                                                <List
                                                    items={features.map((f, i) => (
                                                        <Typography key={i} variant="caption" style={{ fontSize: "0.85rem", opacity: 0.9 }}>
                                                            {f}
                                                        </Typography>
                                                    ))}
                                                    gap="0.5rem"
                                                />
                                            </Box>
                                        )}
                                    </Box>

                                    <Box style={{ textAlign: "center", marginTop: "auto", paddingTop: "1rem" }}>
                                        <Typography variant="h3" style={{ marginBottom: "1rem", fontWeight: "bold", color: displayPrice > 0 ? "var(--color-text)" : "var(--color-success)" }}>
                                            {displayPriceText}
                                        </Typography>
                                        <Button
                                            variant={isCurrent ? "ghost" : "outline"}
                                            disabled={isButtonDisabled}
                                            onClick={() => handleOpenConfirm(plan.status, displayPrice, isDowngrade)}
                                            style={{ width: "100%", opacity: isButtonDisabled ? 0.5 : 1 }}
                                        >
                                            {buttonText}
                                        </Button>
                                    </Box>
                                </Card>
                            );
                        })}
                    </Grid>
                )}
                <Box style={{ marginTop: "4rem", maxWidth: "800px", marginLeft: "auto", marginRight: "auto" }}>
                    <Typography variant="h2" style={{ textAlign: "center", marginBottom: "2rem" }}>
                        Frequently Asked Questions
                    </Typography>

                    <Box style={{ borderTop: "1px solid var(--color-border)" }}>
                        {UPGRADE_FAQ.map((item, index) => (
                            <Accordion
                                key={index}
                                title={item.question}
                                isOpen={openFaqIndex === index}
                                onToggle={() => setOpenFaqIndex(openFaqIndex === index ? null : index)}
                            >
                                {item.answer}
                            </Accordion>
                        ))}
                    </Box>
                </Box>
            </Container>

            <Modal isOpen={confirmModal.isOpen} onClose={handleCloseConfirm} title="Confirm Status Change">
                <Stack gap="1.5rem">
                    <Typography variant="body">
                        Are you sure you want to change your status to <span style={{ fontWeight: "bold", color: "var(--color-primary)" }}>{confirmModal.statusTitle}</span>?
                    </Typography>

                    {confirmModal.price > 0 && !confirmModal.isDowngrade && (
                        <Typography variant="caption" style={{ opacity: 0.8 }}>
                            This will deduct <strong>{confirmModal.price} CG Coins</strong> from your balance immediately.
                        </Typography>
                    )}

                    {confirmModal.price > 0 && confirmModal.isDowngrade && (
                        <Typography variant="caption" style={{ color: "var(--color-text)" }}>
                            This downgrade will take effect at the end of your current billing period. Until then, you will retain your current benefits.
                            <br /><br />
                            You will be charged <strong>{confirmModal.price} CG Coins</strong> only when the new billing period begins.
                        </Typography>
                    )}

                    {confirmModal.price === 0 && confirmModal.isDowngrade && (
                        <Typography variant="caption" style={{ color: "var(--color-text)" }}>
                            This downgrade will take effect at the end of your current billing period. Until then, you will retain your current benefits. You will not be charged.
                        </Typography>
                    )}

                    {confirmModal.price === 0 && !confirmModal.isDowngrade && confirmModal.statusTitle !== "DEFAULT" && (
                        <Typography variant="caption" style={{ color: "var(--color-success)" }}>
                            This upgrade is available for free!
                        </Typography>
                    )}

                    <Stack direction="row" gap="1rem" justify="flex-end" style={{ marginTop: "0.5rem" }}>
                        <Button variant="outline" onClick={handleCloseConfirm} disabled={isPurchasing}>
                            Cancel
                        </Button>
                        <Button variant="solid" onClick={executePurchase} disabled={isPurchasing}>
                            {isPurchasing ? "Processing..." : "Confirm"}
                        </Button>
                    </Stack>
                </Stack>
            </Modal>
        </Box>
    );
}
