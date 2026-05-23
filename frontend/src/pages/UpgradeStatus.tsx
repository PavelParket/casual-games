import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { Box, Container, Card, Typography, Button, Grid, Stack, Divider, Modal, Icon, Avatar, useThemedIcon } from "../ui";
import { Skeleton } from "../ui/components/common/Skeleton";
import { useSystemToastContext } from "../providers/SystemToastContext";
import { purchase, getBalance, getCurrentSubscription } from "../store/slices/UserSlice";
import type { AppDispatch, RootState } from "../store/store";
import type { SubscriptionResponse, UserStatus } from "../models/User";
import type { Icons } from "../assets/icons";

const STATUSES: { id: number; title: UserStatus; basePrice: number }[] = [
    { id: 0, title: "DEFAULT", basePrice: 0 },
    { id: 1, title: "PRO", basePrice: 5000 },
    { id: 2, title: "VIP", basePrice: 10000 }
];

const P_MIN = 0.1;
const P_MAX = 0.9;

const getStatusIconName = (status: string): keyof typeof Icons.light => {
    return `${status.toLowerCase()}Status` as keyof typeof Icons.light;
};

const ceilDays = (fromMs: number, toMs: number): number => {
    const seconds = Math.floor((toMs - fromMs) / 1000);
    return Math.max(1, Math.ceil(seconds / 86400));
};

const calculateVipPrice = (subscription: SubscriptionResponse): number => {
    if (subscription.status === "VIP") return 0;
    if (subscription.status !== "PRO") return 10000;

    if (!subscription.startedAt || !subscription.expiresAt) return 10000;

    const now = Date.now();
    const startedAt = new Date(subscription.startedAt).getTime();
    const expiresAt = new Date(subscription.expiresAt).getTime();

    const totalPeriodDays = ceilDays(startedAt, expiresAt);
    const remainingDays = Math.max(0, ceilDays(now, expiresAt));
    const daysUsed = totalPeriodDays - remainingDays;

    const fractionUsed = daysUsed / totalPeriodDays;

    const p = P_MAX - ((P_MAX - P_MIN) * fractionUsed);

    const pricePro = 5000;
    const priceVip = 10000;

    const dailyRate = pricePro / totalPeriodDays;
    const credit = dailyRate * remainingDays * p;

    const finalPrice = Math.max(0, priceVip - credit);

    return Number(finalPrice.toFixed(2));
};

export default function Upgrade() {
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();
    const { showSystemToast } = useSystemToastContext();
    const { getIcon } = useThemedIcon();

    const { user, subscription, isPurchasing, isLoadingSubscription } = useSelector((state: RootState) => state.user);

    const currentStatus = subscription?.status || user?.status;

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
    }, [dispatch]);

    const getPrice = (targetTitle: UserStatus, isDowngrade: boolean) => {
        if (isDowngrade) return 0;

        if (targetTitle === "VIP" && subscription) {
            return calculateVipPrice(subscription);
        }
        const statusObj = STATUSES.find(s => s.title === targetTitle);
        return statusObj ? statusObj.basePrice : 0;
    };

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
        } catch (err) {
            showSystemToast(`Failed to upgrade: ${err}`, "system-error");
            setConfirmModal({ isOpen: false, statusTitle: null, price: 0, isDowngrade: false });
        }
    };

    return (
        <Box style={{
            minHeight: "calc(100vh - 60px - 50px)",
            margin: "0 10rem",
            padding: "2rem 1rem",
            background: "var(--color-bg-glass)",
            backdropFilter: "blur(2px)",
            borderRadius: "var(--radius-md)",
            boxShadow: "var(--shadow-lg)"
        }}>
            <Container>
                <Stack direction="row" align="center" justify="space-between" style={{ marginBottom: "2rem" }}>
                    <Typography variant="h2">Upgrade Status</Typography>
                    <Button variant="outline" onClick={() => navigate("/profile")}>Back to Profile</Button>
                </Stack>

                {!currentStatus || isLoadingSubscription ? (
                    <Grid columns="repeat(auto-fit, minmax(240px, 1fr))" gap="1.5rem">
                        {Array.from({ length: 3 }).map((_, i) => (
                            <Card key={i} style={{ height: "65vh", padding: "1.5rem", }}>
                                <Skeleton variant="card" height="100%" />
                            </Card>
                        ))}
                    </Grid>
                ) : (
                    <Grid columns="repeat(auto-fit, minmax(240px, 1fr))" gap="1.5rem">
                        {STATUSES.map((status) => {
                            const currentStatusObj = STATUSES.find(s => s.title === currentStatus) || STATUSES[0];
                            const isCurrent = currentStatus === status.title;
                            const isDowngrade = status.id < currentStatusObj.id;

                            const statusIconSrc = getIcon(getStatusIconName(status.title));

                            const price = getPrice(status.title, isDowngrade);

                            return (
                                <Card
                                    key={status.id}
                                    style={{
                                        display: "flex",
                                        flexDirection: "column",
                                        border: isCurrent ? "2px solid var(--color-border)" : "2px solid var(--color-primary)",
                                        opacity: isCurrent ? 0.6 : 1,
                                        boxShadow: "var(--shadow-md)",
                                        transform: !isCurrent ? "scale(1.03)" : "scale(1)",
                                        transition: "all 0.2s ease",
                                        padding: "1.5rem",
                                        height: "65vh"
                                    }}
                                >
                                    <Typography variant="h3" style={{ textAlign: "center", marginBottom: "0.5rem", fontWeight: 700 }}>
                                        {status.title}
                                    </Typography>

                                    <Divider variant="middle" style={{ margin: "1rem 0" }} />

                                    <Box style={{ flex: 1, marginBottom: "2rem", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
                                        <Typography variant="caption" style={{ marginBottom: "1rem", opacity: 0.7 }}>
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
                                                <Avatar src={user?.avatarUrl} fallback={user?.username || "?"} size={40} />
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
                                                    {user?.username || "Player"}
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
                                                    <Icon src={statusIconSrc} alt={status.title} size={24} />
                                                </Box>

                                                <Box style={{ display: "flex", alignItems: "center" }}>
                                                    <Typography variant="caption" style={{ textTransform: "capitalize", fontWeight: 500, fontSize: "0.9rem" }}>
                                                        {status.title}
                                                    </Typography>
                                                </Box>
                                            </Box>
                                        </Box>
                                    </Box>


                                    <Box style={{ textAlign: "center", marginTop: "auto" }}>
                                        <Typography variant="h3" style={{ marginBottom: "1rem", fontWeight: "bold", color: price > 0 ? "var(--color-text)" : "var(--color-success)" }}>
                                            {isCurrent ? "Active" : isDowngrade ? "Free" : `${price} CG Coins`}
                                        </Typography>
                                        <Button
                                            variant={isCurrent ? "ghost" : "outline"}
                                            disabled={isCurrent || isPurchasing}
                                            onClick={() => handleOpenConfirm(status.title, price, isDowngrade)}
                                            style={{ width: "100%", opacity: (isCurrent || isPurchasing) ? 0.5 : 1 }}
                                        >
                                            {isCurrent ? "Current" : isDowngrade ? "Downgrade" : "Get"}
                                        </Button>
                                    </Box>
                                </Card>
                            );
                        })}
                    </Grid>
                )}
            </Container>

            <Modal isOpen={confirmModal.isOpen} onClose={handleCloseConfirm} title="Confirm Status Change">
                <Stack gap="1.5rem">
                    <Typography variant="body">
                        Are you sure you want to change your status to <span style={{ fontWeight: "bold", color: "var(--color-primary)" }}>{confirmModal.statusTitle}</span>?
                    </Typography>

                    {confirmModal.price > 0 && (
                        <Typography variant="caption" style={{ opacity: 0.8 }}>
                            This will deduct <strong>{confirmModal.price} CGC</strong> from your balance.
                        </Typography>
                    )}

                    {confirmModal.price === 0 && confirmModal.isDowngrade && (
                        <Typography variant="caption" style={{ color: "var(--color-text)" }}>
                            This downgrade is completely free.
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
