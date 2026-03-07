import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { findByGuid, update } from "../store/slices/UserSlice";
import { deposit, getByUserGuid } from "../store/slices/BankSlice";
import type { Icons } from "../assets/icons";
import { Box, Container, Card, Typography, Button, Stack, Divider, Grid, Icon, Textfield, Modal, Img, Input, Toast, FormField } from "../ui";
import { useThemedIcon } from "../ui";
import { validateUsername } from "../utils/SecurityUtils";
import { Skeleton } from "../ui/components/common/Skeleton";
import { ROOM_TYPE_LABELS } from "../models/Room";

const getStatusIconName = (status: string): keyof typeof Icons.light => {
    return `${status.toLowerCase()}Status` as keyof typeof Icons.light;
};

export default function Profile() {
    const dispatch = useDispatch<AppDispatch>();

    const { user, isLoading } = useSelector((state: RootState) => state.user);
    const authUser = useSelector((state: RootState) => state.auth.user);
    const { isDepositing, error: bankError, transactions, isLoadingTransactions, currentPage, totalPages } = useSelector((state: RootState) => state.bank);

    const { getIcon } = useThemedIcon();

    const [isEditingUsername, setIsEditingUsername] = useState(false);
    const [tempUsername, setTempUsername] = useState("");

    const [validationError, setValidationError] = useState<string | null>(null);
    const [toast, setToast] = useState<{ text: string, type: "success" | "error" } | null>(null);
    const [activeTab, setActiveTab] = useState<'default' | 'balanceHistory'>('default');

    const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
    const [isAvatarHovered, setIsAvatarHovered] = useState(false);

    const [historyModalOpen, setHistoryModalOpen] = useState(false);
    const [achievementsModalOpen, setAchievementsModalOpen] = useState(false);

    const [depositModalOpen, setDepositModalOpen] = useState(false);
    const [depositAmount, setDepositAmount] = useState("");

    const [loadingAvatar, setLoadingAvatar] = useState(false);
    const [loadingAchievements, setLoadingAchievements] = useState(false);
    const [loadingHistory, setLoadingHistory] = useState(false);

    useEffect(() => {
        if (authUser?.guid) {
            dispatch(findByGuid(authUser.guid));
        }
    }, [dispatch, authUser?.guid]);

    useEffect(() => {
        if (user?.username) {
            setTempUsername(user.username);
        }
    }, [user]);

    const handleEditClick = () => {
        setValidationError(null);
        setIsEditingUsername(true);
    };
    const handleSaveUsername = async () => {
        const sanitizedUsername = validateUsername(tempUsername);

        if (sanitizedUsername.length < 3) {
            setToast({ text: "Username must be at least 3 characters long.", type: "error" });
            return;
        }

        if (sanitizedUsername === user?.username) {
            setIsEditingUsername(false);
            return;
        }

        if (!authUser?.guid) {
            setToast({ text: "User not authenticated", type: "error" });
            return;
        }

        try {
            await dispatch(update({
                guid: authUser.guid,
                updateData: { username: sanitizedUsername }
            })).unwrap();
            setToast({ text: "Username updated successfully!", type: "success" });
        } catch (error) {
            setToast({ text: `Update failed: ${error}`, type: "error" });
        } finally {
            setIsEditingUsername(false);
            setValidationError(null);
        }
    };

    const handleUsernameChange = (value: string) => {
        const sanitized = validateUsername(value);
        setTempUsername(sanitized);

        if (validationError) {
            setValidationError(null);
        }
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files?.[0]) {
            setAvatarPreview(URL.createObjectURL(e.target.files[0]));
        }
    };

    const handleDepositAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const val = e.target.value.replace(',', '.');

        if (val === '') {
            setDepositAmount('');
            return;
        }

        const regex = /^\d{0,5}(\.\d{0,2})?$/;

        if (regex.test(val)) {
            setDepositAmount(val);
        }
    };

    const handleDeposit = async () => {
        const amount = parseFloat(depositAmount);

        if (isNaN(amount) || amount <= 0) {
            return;
        }

        if (!authUser?.guid) {
            setToast({ text: "User not identified", type: "error" });
            return;
        }

        try {
            await dispatch(deposit({ userGuid: authUser.guid, amount })).unwrap();
            setToast({ text: "Deposit successful!", type: "success" });
            setDepositModalOpen(false);
            setDepositAmount("");
        } catch (err) {
            setToast({ text: `Deposit failed: ${err}`, type: "error" });
        }
    };

    const handlePageChange = (newPage: number) => {
        if (authUser?.guid) {
            dispatch(getByUserGuid({ guid: authUser.guid, page: newPage, size: 4 }));
        }
    };

    const username = user?.username || "User";
    const email = user?.email || "";
    const balance = user?.balance ?? 0;
    const status = user?.status || "default";
    const achievements = user?.achievements || [];
    const history = user?.history || [];

    const formattedDate = user?.createdAt
        ? new Date(user.createdAt).toLocaleDateString()
        : "Unknown";

    const statusIconName = getStatusIconName(status);
    const statusIconSrc = getIcon(statusIconName) || getIcon("defaultStatus");

    const infoBlockStyle = {
        background: "var(--color-bg-glass)",
        backdropFilter: "blur(10px)",
        padding: "1rem",
        borderRadius: "var(--radius-md)",
        border: "1px solid var(--color-border)",
        boxShadow: "var(--shadow-sm)"
    };

    return (
        <Box style={{ padding: "2rem 0" }}>
            <Container >
                <Card style={{ minHeight: "100%" }}>

                    <Grid
                        columns="280px 1px 1fr"
                        gap="0"
                        style={{ height: "100%" }}
                        className="profile-grid"
                    >
                        <Stack align="center" gap="1.5rem" style={{ paddingRight: "1rem" }}>

                            <Box
                                style={{ position: "relative" }}
                                onMouseEnter={() => setIsAvatarHovered(true)}
                                onMouseLeave={() => setIsAvatarHovered(false)}
                            >
                                {loadingAvatar ? (
                                    <Skeleton variant="circular" height={150} width={150} />
                                ) : (
                                    <>
                                        <Box style={{
                                            width: "150px",
                                            height: "150px",
                                            borderRadius: "50%",
                                            background: "var(--color-bg)",
                                            display: "flex", alignItems: "center", justifyContent: "center",
                                            fontSize: "3rem", fontWeight: "bold",
                                            color: "var(--color-text)",
                                            boxShadow: "var(--shadow-md)",
                                            overflow: "hidden",
                                            position: "relative",
                                        }}>
                                            {avatarPreview || user?.avatarUrl ? (
                                                <Img
                                                    src={avatarPreview || user?.avatarUrl || ""}
                                                    alt="Avatar"
                                                    style={{ width: "100%", height: "100%", objectFit: "cover" }}
                                                />
                                            ) : (
                                                username.substring(0, 1).toUpperCase()
                                            )}
                                        </Box>

                                        <label htmlFor="avatar-upload">
                                            <Box
                                                style={{
                                                    position: "absolute",
                                                    bottom: 5, right: 5,
                                                    borderRadius: "50%",
                                                    width: "40px", height: "40px",
                                                    background: "var(--color-bg)",
                                                    border: "1px solid var(--glass-border)",
                                                    display: "flex", alignItems: "center", justifyContent: "center",
                                                    zIndex: 2, boxShadow: "var(--shadow-sm)",
                                                    opacity: isAvatarHovered ? 1 : 0,
                                                    transform: isAvatarHovered ? "scale(1)" : "scale(0.8)",
                                                    transition: "all 0.2s ease",
                                                    cursor: "pointer"
                                                }}
                                            >
                                                <Icon src={getIcon("edit")} alt="edit avatar" size={20} />
                                            </Box>
                                        </label>

                                        <Input
                                            id="avatar-upload"
                                            type="file"
                                            style={{
                                                width: 0,
                                                height: 0,
                                                opacity: 0,
                                                position: "absolute",
                                                zIndex: -1,
                                            }}
                                            accept="image/*"
                                            onChange={handleFileChange}
                                        />
                                    </>)}
                            </Box>

                            {isLoading ? (
                                <Stack align="center" gap="0.5rem">
                                    <Skeleton variant="circular" width={40} height={40} />
                                    <Skeleton variant="text" width={130} height={24} />
                                </Stack>
                            ) : (
                                <Stack align="center" gap="0.5rem">
                                    <Icon src={statusIconSrc} alt={status} size={40} />
                                    <Typography variant="h3" style={{ textTransform: "capitalize" }}>
                                        {status} Member
                                    </Typography>
                                </Stack>
                            )}

                            {isLoading ? (
                                <Skeleton variant="rectangular" width="100%" height={38} />
                            ) : (
                                <Button variant="solid" style={{ width: "100%" }}>
                                    Upgrade
                                </Button>
                            )}

                            <Box style={{ marginTop: "auto", paddingTop: "2rem" }}>
                                {isLoading ? (
                                    <Skeleton variant="text" width={80} height={16} />
                                ) : (
                                    <Typography variant="caption" style={{ opacity: 0.6 }}>
                                        Date: {formattedDate}
                                    </Typography>
                                )}
                            </Box>
                        </Stack>

                        <Box style={{ display: "flex", justifyContent: "center", height: "100%" }}>
                            <Divider orientation="vertical" />
                        </Box>

                        <Stack gap="2rem" style={{ width: "100%", paddingLeft: "1rem" }}>

                            <Box style={infoBlockStyle}>
                                <Stack gap="1rem">
                                    <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", minHeight: "45px" }}>
                                        {isLoading ? (
                                            <Skeleton variant="text" width={150} height={28} />
                                        ) : (
                                            <Box style={{ flex: 1, marginRight: "1rem" }}>
                                                <Typography variant="caption" style={{ opacity: 0.7 }}>Username:</Typography>

                                                {isEditingUsername ? (
                                                    <>
                                                        <Textfield
                                                            value={tempUsername}
                                                            onChange={handleUsernameChange}
                                                            placeholder="Enter username"
                                                        />
                                                        {validationError && (
                                                            <Typography variant="caption" style={{ color: 'red', marginTop: '4px' }}>
                                                                {validationError}
                                                            </Typography>
                                                        )}
                                                    </>
                                                ) : (
                                                    <Typography
                                                        variant="h3"
                                                        title={username}
                                                        style={{
                                                            overflow: "hidden",
                                                            textOverflow: "ellipsis",
                                                            display: 'block',
                                                            maxWidth: '150px'
                                                        }}
                                                    >
                                                        {username}
                                                    </Typography>
                                                )}
                                            </Box>
                                        )}

                                        {isEditingUsername ? (
                                            <Stack
                                                direction="row">
                                                <Button
                                                    variant="solid"
                                                    onClick={handleSaveUsername}
                                                    disabled={isLoading}
                                                    style={{ display: "flex", alignItems: "center", gap: "5px" }}
                                                >
                                                    {isLoading ? "Saving..." : "Save"}
                                                </Button>

                                                <Button
                                                    variant="outline"
                                                    disabled={isLoading}
                                                    onClick={() => {
                                                        setIsEditingUsername(false);
                                                        setValidationError(null);
                                                    }}
                                                >
                                                    Cancel
                                                </Button>
                                            </Stack>
                                        ) : (
                                            isLoading ? (
                                                <Skeleton variant="rectangular" width={75} height={34} />
                                            ) : (
                                                <Button
                                                    variant="outline"
                                                    onClick={handleEditClick}
                                                    style={{ display: "flex", gap: "8px", alignItems: "center" }}
                                                >
                                                    Edit
                                                    <Icon src={getIcon("edit")} alt="edit" size={16} />
                                                </Button>
                                            )
                                        )}
                                    </Box>

                                    {isLoading ? (
                                        <Skeleton variant="text" width={150} height={28} />
                                    ) : (
                                        <Box>
                                            <Typography variant="caption" style={{ opacity: 0.7 }}>Email:</Typography>
                                            <Typography variant="body">{email}</Typography>
                                        </Box>
                                    )}

                                    <Divider />

                                    {isLoading ? (
                                        <Skeleton variant="text" width={150} height={36} />
                                    ) : (
                                        <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                            <Box>
                                                <Typography variant="caption" style={{ opacity: 0.7 }}>Balance:</Typography>
                                                <Typography variant="h2" style={{ color: "var(--color-primary)" }}>
                                                    {balance}
                                                    <Typography variant="caption" style={{ marginLeft: "5px" }}>CG Coins</Typography>
                                                </Typography>
                                            </Box>

                                            <Stack direction="row" gap="10px" align="center">

                                                <Button
                                                    variant="ghost"
                                                    onClick={() => setDepositModalOpen(true)}
                                                >
                                                    Deposit
                                                </Button>

                                                {/*todo: рассмотреть вариант с кэшированием, на данный момент кнопка подвергает DoS-атаке bank-service*/}
                                                <Button
                                                    variant={activeTab === 'balanceHistory' ? "solid" : "outline"}
                                                    onClick={() => {
                                                        if (activeTab === 'balanceHistory') {
                                                            setActiveTab('default');
                                                        } else {
                                                            if (authUser?.guid) {
                                                                dispatch(getByUserGuid({ guid: authUser.guid, size: 4 }));
                                                            }
                                                            setActiveTab('balanceHistory');
                                                        }
                                                    }}
                                                    style={{ width: "135px" }}
                                                >
                                                    {activeTab === 'balanceHistory' ? 'Close History' : 'History'}
                                                </Button>

                                            </Stack>

                                        </Box>
                                    )}
                                </Stack>
                            </Box>

                            {activeTab === 'default' && (
                                <>
                                    <Box style={infoBlockStyle}>
                                        <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                                            <Typography variant="h3">Achievements</Typography>
                                            <Button
                                                variant="ghost"
                                                onClick={() => setAchievementsModalOpen(true)}
                                                disabled={!achievements || achievements.length === 0}
                                                style={{ fontSize: "0.8rem" }}
                                            >
                                                See All
                                            </Button>
                                        </Box>


                                        {loadingAchievements ? (
                                            <Box style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                                                <Skeleton variant="rectangular" height={30} />
                                                <Skeleton variant="rectangular" height={30} />
                                                <Skeleton variant="rectangular" height={30} />
                                            </Box>
                                        ) : (
                                            achievements.length > 0 ? (
                                                <Box style={{
                                                    display: "flex",
                                                    gap: "10px",
                                                    flexWrap: "wrap",
                                                    overflow: "hidden",
                                                    maxHeight: "130px"
                                                }}>
                                                    {achievements.slice(0, 5).map((ach, i) => (
                                                        <Box key={i} style={{
                                                            padding: "5px 12px",
                                                            background: "var(--color-primary)",
                                                            color: "var(--on-primary)",
                                                            borderRadius: "20px",
                                                            fontSize: "0.9rem",
                                                            fontWeight: 500
                                                        }}>
                                                            {ach}
                                                        </Box>
                                                    ))}
                                                    {achievements.length > 5 && (
                                                        <Box style={{ padding: "5px 10px", fontSize: "0.9rem", opacity: 0.7, alignSelf: "center" }}>
                                                            +{achievements.length - 5} more...
                                                        </Box>
                                                    )}
                                                </Box>
                                            ) : (
                                                <Typography variant="caption" style={{ fontStyle: "italic", opacity: 0.6 }}>
                                                    No achievements yet. Go play some games!
                                                </Typography>
                                            )
                                        )}
                                    </Box>

                                    <Box style={infoBlockStyle}>
                                        <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                                            <Typography variant="h3">Game History</Typography>
                                            <Button
                                                variant="ghost"
                                                onClick={() => setHistoryModalOpen(true)}
                                                disabled={history.length === 0}
                                                style={{ fontSize: "0.8rem" }}
                                            >
                                                See All
                                            </Button>
                                        </Box>

                                        {loadingHistory ? (
                                            <Box style={{ display: "flex", gap: "10px", flexWrap: "wrap" }}>
                                                <Skeleton variant="rectangular" height={30} />
                                                <Skeleton variant="rectangular" height={30} />
                                                <Skeleton variant="rectangular" height={30} />
                                            </Box>
                                        ) : (
                                            history.length > 0 ? (
                                                <Stack gap="0.5rem">
                                                    {history.slice(0, 3).map((item, i) => (
                                                        <Box key={i} style={{
                                                            display: "flex",
                                                            justifyContent: "space-between",
                                                            borderBottom: i === 2 ? "none" : "1px solid var(--color-border)",
                                                            paddingBottom: "10px",
                                                            paddingTop: i === 0 ? "0" : "5px"
                                                        }}>
                                                            <Box>
                                                                <Typography variant="body" style={{ fontWeight: 500 }}>{item.game}</Typography>
                                                                <Typography variant="caption">{new Date(item.date).toLocaleDateString()}</Typography>
                                                            </Box>
                                                            <Typography variant="body" style={{
                                                                fontWeight: "bold",
                                                                color: item.result === "Win" ? "green" : item.result === "Loss" ? "red" : "gray"
                                                            }}>
                                                                {item.result}
                                                            </Typography>
                                                        </Box>
                                                    ))}
                                                </Stack>
                                            ) : (
                                                <Typography variant="caption" style={{ fontStyle: "italic", opacity: 0.6 }}>
                                                    No match history available.
                                                </Typography>
                                            )
                                        )}
                                    </Box>
                                </>
                            )}
                            {activeTab === 'balanceHistory' && (
                                <Box style={{
                                    ...infoBlockStyle,
                                    display: "flex",
                                    flexDirection: "column",
                                    maxHeight: "360px"
                                }}>
                                    <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                                        <Typography variant="h3">Balance History</Typography>
                                        <Button
                                            variant="ghost"
                                            onClick={() => {
                                                if (authUser?.guid) {
                                                    dispatch(getByUserGuid({ guid: authUser.guid, size: 4 }));
                                                }
                                            }}
                                            disabled={isLoadingTransactions}
                                            style={{ fontSize: "0.8rem", padding: "7px" }}
                                        >
                                            {isLoadingTransactions ? 'Updating...' : <Icon src={getIcon("refresh")} alt="refresh" size={16} />}
                                        </Button>
                                    </Box>

                                    <Box style={{
                                        flex: 1,
                                        overflowY: "auto",
                                        paddingRight: "12px",
                                        display: "flex",
                                        flexDirection: "column",
                                        gap: "0.5rem"
                                    }}>
                                        {isLoadingTransactions ? (
                                            <Box style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                                                <Skeleton variant="rectangular" height={45} />
                                                <Skeleton variant="rectangular" height={45} />
                                                <Skeleton variant="rectangular" height={45} />
                                            </Box>
                                        ) : transactions && transactions.length > 0 ? (
                                            transactions.map(transaction => {
                                                const typeTransaction = transaction.type === 'ADDITION' ? 'income' : 'expense';
                                                const sign = typeTransaction === 'income' ? '+' : '-';
                                                const roomName = transaction.roomType ? (ROOM_TYPE_LABELS[transaction.roomType] || transaction.roomType) : 'Deposit';
                                                const time = transaction.createdAtTime ? transaction.createdAtTime.substring(0, 5) : '';

                                                return (
                                                    <Box key={transaction.id} style={{
                                                        padding: "8px 12px",
                                                        display: "grid",
                                                        gridTemplateColumns: "32px 1fr auto 90px",
                                                        gap: "12px",
                                                        alignItems: "center",
                                                        background: `var(--color-${typeTransaction}-bg)`,
                                                        border: `1px solid var(--color-${typeTransaction}-border)`,
                                                        borderRadius: "var(--radius-sm)",
                                                        flexShrink: 0,
                                                        transition: "transform 0.2s ease"
                                                    }}>
                                                        <Box style={{
                                                            width: "32px",
                                                            height: "32px",
                                                            borderRadius: "50%",
                                                            background: `var(--color-${typeTransaction}-icon-bg)`,
                                                            display: "flex",
                                                            alignItems: "center",
                                                            justifyContent: "center",
                                                            color: "#ffffff",
                                                            fontWeight: "bold",
                                                            fontSize: "1.2rem"
                                                        }}>
                                                            {sign}
                                                        </Box>

                                                        <Stack gap="2px" justify="center" style={{ overflow: "hidden" }}>
                                                            <Typography
                                                                variant="body"
                                                                style={{
                                                                    fontWeight: 600,
                                                                    fontSize: "0.95rem",
                                                                    whiteSpace: "nowrap",
                                                                    overflow: "hidden",
                                                                    textOverflow: "ellipsis"
                                                                }}
                                                            >
                                                                {roomName}
                                                            </Typography>
                                                            <Typography variant="caption" style={{ opacity: 0.6, fontSize: "0.75rem" }}>
                                                                {transaction.createdAtDate} • {time} UTC
                                                            </Typography>
                                                        </Stack>

                                                        <Typography
                                                            variant="body"
                                                            style={{
                                                                fontWeight: "100",
                                                                color: `var(--color-${typeTransaction}-text)`,
                                                                fontSize: "1.1rem",
                                                                textAlign: "right",
                                                                paddingRight: "4px"
                                                            }}
                                                        >
                                                            {transaction.amount}
                                                        </Typography>

                                                        <Stack gap="0px" style={{ alignItems: "flex-start", minWidth: "90px" }}>
                                                            <Stack direction="row" justify="space-between" style={{ width: "100%" }}>
                                                                <Typography variant="caption" style={{ opacity: 0.5, fontSize: "0.7rem" }}>
                                                                    Before:
                                                                </Typography>
                                                                <Typography variant="caption"
                                                                    style={{
                                                                        opacity: 0.8,
                                                                        fontWeight: "500",
                                                                        fontFamily: "monospace",
                                                                        fontSize: "0.7rem"
                                                                    }}
                                                                >
                                                                    {transaction.balanceBefore}
                                                                </Typography>
                                                            </Stack>

                                                            <Stack direction="row" justify="space-between" style={{ width: "100%" }}>
                                                                <Typography variant="caption" style={{ opacity: 0.5, fontSize: "0.7rem" }}>
                                                                    After:
                                                                </Typography>
                                                                <Typography
                                                                    variant="caption"
                                                                    style={{
                                                                        opacity: 0.8,
                                                                        fontWeight: "500",
                                                                        fontFamily: "monospace",
                                                                        fontSize: "0.7rem"
                                                                    }}
                                                                >
                                                                    {transaction.balanceAfter}
                                                                </Typography>
                                                            </Stack>
                                                        </Stack>
                                                    </Box>
                                                );
                                            })
                                        ) : (
                                            <Typography variant="body" style={{ textAlign: "center", opacity: 0.6, padding: "2rem 0" }}>
                                                No transactions found.
                                            </Typography>
                                        )}
                                    </Box>
                                    {totalPages > 1 && (
                                        <Box style={{
                                            marginTop: "1rem",
                                            paddingTop: "0.5rem",
                                            borderTop: "1px solid var(--color-border)",
                                            display: "flex",
                                            justifyContent: "center",
                                            alignItems: "center",
                                            gap: "1rem"
                                        }}>
                                            <Button
                                                variant="ghost"
                                                disabled={currentPage === 0 || isLoadingTransactions}
                                                onClick={() => handlePageChange(0)}
                                                style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: "0.25rem 0.5rem" }}
                                            >
                                                <Icon src={getIcon("doubleLeftArrow")} alt="to the first page" size={16} />
                                            </Button>
                                            <Button
                                                variant="ghost"
                                                disabled={currentPage === 0 || isLoadingTransactions}
                                                onClick={() => handlePageChange(currentPage - 1)}
                                                style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: "0.25rem 0.5rem" }}
                                            >
                                                <Icon src={getIcon("leftArrow")} alt="prev page" size={16} />
                                            </Button>

                                            <Typography variant="caption" style={{ fontVariantNumeric: "tabular-nums" }}>
                                                Page {currentPage + 1} of {totalPages}
                                            </Typography>

                                            <Button
                                                variant="ghost"
                                                disabled={currentPage >= totalPages - 1 || isLoadingTransactions}
                                                onClick={() => handlePageChange(currentPage + 1)}
                                                style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: "0.25rem 0.5rem" }}
                                            >
                                                <Icon src={getIcon("rightArrow")} alt="next page" size={16} />
                                            </Button>
                                            <Button
                                                variant="ghost"
                                                disabled={currentPage >= totalPages - 1 || isLoadingTransactions}
                                                onClick={() => handlePageChange(totalPages - 1)}
                                                style={{ display: "flex", alignItems: "center", justifyContent: "center", padding: "0.25rem 0.5rem" }}
                                            >
                                                <Icon src={getIcon("doubleRightArrow")} alt="to the last page" size={16} />
                                            </Button>
                                        </Box>
                                    )}
                                </Box>
                            )}

                        </Stack>
                    </Grid>
                </Card >
            </Container >

            {toast && (
                <Toast
                    message={toast.text}
                    onClose={() => setToast(null)}
                />
            )
            }

            <Modal isOpen={achievementsModalOpen} onClose={() => setAchievementsModalOpen(false)} title="All Achievements">
                <Box style={{ display: "flex", gap: "10px", flexWrap: "wrap", padding: "1rem 0" }}>
                    {achievements.map((ach, i) => (
                        <Box key={i} style={{ padding: "8px 16px", background: "var(--color-primary)", color: "var(--on-primary)", borderRadius: "20px", fontSize: "1rem", boxShadow: "var(--shadow-sm)" }}>
                            {ach}
                        </Box>
                    ))}
                </Box>
            </Modal>

            <Modal isOpen={historyModalOpen} onClose={() => setHistoryModalOpen(false)} title="Match History">
                <Stack gap="0.8rem" style={{ padding: "0.5rem 0" }}>
                    {history.map((item, i) => (
                        <Card key={i} style={{ padding: "10px 15px", display: "flex", justifyContent: "space-between", alignItems: "center", background: "var(--color-bg)" }}>
                            <Box>
                                <Typography variant="h3" style={{ fontSize: "1.1rem" }}>{item.game}</Typography>
                                <Typography variant="caption" style={{ opacity: 0.6 }}>{new Date(item.date).toLocaleString()}</Typography>
                            </Box>
                            <Typography variant="h3" style={{ color: item.result === "Win" ? "green" : item.result === "Loss" ? "red" : "gray" }}>{item.result}</Typography>
                        </Card>
                    ))}
                </Stack>
            </Modal>

            <Modal
                isOpen={depositModalOpen}
                onClose={() => {
                    setDepositModalOpen(false)
                    setDepositAmount('');
                }}
                title="Deposit Funds"
            >
                <Stack gap="1rem">
                    <Typography variant="body">
                        Enter the amount you wish to add to your balance.
                    </Typography>
                    <FormField
                        type="text"
                        inputMode="decimal"
                        value={depositAmount}
                        onChange={handleDepositAmountChange}
                        onFocus={(e) => e.target.select()}
                        placeholder="Amount"
                        rounded
                    />
                    {bankError && (
                        <Typography variant="caption" style={{ color: 'red' }}>
                            {bankError}
                        </Typography>
                    )}
                    <Button
                        variant="solid"
                        onClick={handleDeposit}
                        disabled={isDepositing || !depositAmount || depositAmount === '.'}
                    >
                        {isDepositing ? "Processing..." : "Confirm Deposit"}
                    </Button>
                </Stack>
            </Modal>
        </Box >
    );
}
