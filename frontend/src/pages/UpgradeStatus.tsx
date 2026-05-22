import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { Box, Container, Card, Typography, Button, Grid, Stack, Divider, Modal } from "../ui";
import { Skeleton } from "../ui/components/common/Skeleton";
import { useSystemToastContext } from "../providers/SystemToastContext";
import { purchase, getBalance, getCurrentSubscription } from "../store/slices/UserSlice";
import type { AppDispatch, RootState } from "../store/store";
import type { SubscriptionResponse, UserStatus } from "../models/User";

const STATUSES: { id: UserStatus; title: string; basePrice: number }[] = [
    { id: "DEFAULT", title: "Default", basePrice: 0 },
    { id: "PRO", title: "PRO", basePrice: 5000 },
    { id: "VIP", title: "VIP", basePrice: 10000 }
];

const P_MIN = 0.1;
const P_MAX = 0.9;

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

    const { user, subscription, isPurchasing, isLoadingSubscription } = useSelector((state: RootState) => state.user);

    const currentStatus = subscription?.status;

    const [confirmModal, setConfirmModal] = useState<{ isOpen: boolean; statusId: UserStatus | null; price: number }>({
        isOpen: false,
        statusId: null,
        price: 0
    });

    useEffect(() => {
        dispatch(getCurrentSubscription());
    }, [dispatch]);

    const getPrice = (id: UserStatus) => {
        if (id === "VIP" && subscription) {
            return calculateVipPrice(subscription);
        }
        const statusObj = STATUSES.find(s => s.id === id);
        return statusObj ? statusObj.basePrice : 0;
    };

    const handleOpenConfirm = (id: UserStatus) => {
        const price = getPrice(id);
        setConfirmModal({ isOpen: true, statusId: id, price });
    };

    const handleCloseConfirm = () => {
        if (!isPurchasing) {
            setConfirmModal({ isOpen: false, statusId: null, price: 0 });
        }
    };

    const executePurchase = async () => {
        if (!confirmModal.statusId) return;

        try {
            await dispatch(purchase({ status: confirmModal.statusId })).unwrap();
            showSystemToast(`Status successfully changed to ${confirmModal.statusId}!`, "system-info");

            if (user?.guid) {
                dispatch(getBalance(user.guid));
            }

            setConfirmModal({ isOpen: false, statusId: null, price: 0 });
        } catch (err) {
            showSystemToast(`Failed to upgrade: ${err}`, "system-error");
            setConfirmModal({ isOpen: false, statusId: null, price: 0 });
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
                            <Card key={i} style={{ height: "350px", padding: 0 }}>
                                <Skeleton variant="card" height="100%" />
                            </Card>
                        ))}
                    </Grid>
                ) : (
                    <Grid columns="repeat(auto-fit, minmax(240px, 1fr))" gap="1.5rem">
                        {STATUSES.map((status) => {
                            const isCurrent = currentStatus === status.id;
                            const isDowngrade =
                                (currentStatus === "VIP" && status.id !== "VIP") ||
                                (currentStatus === "PRO" && status.id === "DEFAULT");

                            const price = getPrice(status.id);

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
                                        minHeight: "350px"
                                    }}
                                >
                                    <Typography variant="h3" style={{ textAlign: "center", marginBottom: "0.5rem", fontWeight: 700 }}>
                                        {status.title}
                                    </Typography>

                                    <Divider variant="middle" style={{ margin: "1rem 0" }} />

                                    <Box style={{ flex: 1, marginBottom: "2rem" }}>
                                        {/* Features placeholder - to be added later */}
                                    </Box>

                                    <Box style={{ textAlign: "center", marginTop: "auto" }}>
                                        <Typography variant="h3" style={{ marginBottom: "1rem", fontWeight: "bold", color: price > 0 ? "var(--color-text)" : "var(--color-success)" }}>
                                            {isCurrent ? "Active" : isDowngrade ? "Free" : `${price} CG Coins`}
                                        </Typography>
                                        <Button
                                            variant={isCurrent ? "ghost" : "outline"}
                                            disabled={isCurrent || isPurchasing}
                                            onClick={() => handleOpenConfirm(status.id)}
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

            <Modal isOpen={confirmModal.isOpen} onClose={handleCloseConfirm} title="Confirm Purchase">
                <Stack gap="1.5rem">
                    <Typography variant="body">
                        Are you sure you want to purchase the <span style={{ fontWeight: "bold", color: "var(--color-primary)" }}>{confirmModal.statusId}</span> status?
                    </Typography>

                    {confirmModal.price > 0 && (
                        <Typography variant="caption" style={{ opacity: 0.8 }}>
                            This will deduct <strong>{confirmModal.price} CGC</strong> from your balance.
                        </Typography>
                    )}

                    {confirmModal.price === 0 && confirmModal.statusId !== "DEFAULT" && (
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
