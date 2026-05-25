import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { findByGuid, update, getMatches } from "../store/slices/UserSlice";
import { deposit, getByUserGuid } from "../store/slices/BankSlice";
import type { Icons } from "../assets/icons";
import { Box, Container, Card, Typography, Button, Stack, Divider, Grid, Icon, Textfield, Modal, Input, Toast, FormField, Avatar, ComboBox } from "../ui";
import { useThemedIcon } from "../ui";
import { validateUsername } from "../utils/SecurityUtils";
import { Skeleton } from "../ui/components/common/Skeleton";
import { ROOM_TYPE_LABELS, type RoomType } from "../models/Room";
import { PageablePanel } from "../components/PageablePanel";
import { HistoryItem } from "../components/HistoryItem";

const AVAILABLE_ROOM_TYPES = Object.keys(ROOM_TYPE_LABELS) as RoomType[];

const getStatusIconName = (status: string): keyof typeof Icons.light => {
    return `${status.toLowerCase()}Status` as keyof typeof Icons.light;
};

export default function Profile() {
    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();

    const { user, isLoading, gameHistory, isLoadingGameHistory, gameHistoryPage, gameHistoryTotalPages } =
        useSelector((state: RootState) => state.user);
    const authUser = useSelector((state: RootState) => state.auth.user);
    const { isDepositing, error: bankError, transactions, isLoadingTransactions, currentPage, totalPages } =
        useSelector((state: RootState) => state.bank);

    const { getIcon } = useThemedIcon();

    const [isEditingUsername, setIsEditingUsername] = useState(false);
    const [tempUsername, setTempUsername] = useState("");

    const [validationError, setValidationError] = useState<string | null>(null);
    const [toast, setToast] = useState<{ text: string, type: "success" | "error" } | null>(null);
    const [activeTab, setActiveTab] = useState<'games' | 'balanceHistory'>('games');

    const [selectedGameType, setSelectedGameType] = useState<RoomType>('DURAK');

    const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
    const [isAvatarHovered, setIsAvatarHovered] = useState(false);

    const [historyModalOpen, setHistoryModalOpen] = useState(false);

    const [depositModalOpen, setDepositModalOpen] = useState(false);
    const [depositAmount, setDepositAmount] = useState("");

    const [loadingAvatar, setLoadingAvatar] = useState(false);

    const userGuid = authUser?.guid;

    useEffect(() => {
        if (userGuid) {
            dispatch(findByGuid(userGuid));
        }
    }, [dispatch, userGuid]);

    useEffect(() => {
        if (user?.username) {
            setTempUsername(user.username);
        }
    }, [user]);

    useEffect(() => {
        if (userGuid && activeTab === 'games') {
            dispatch(getMatches({ guid: userGuid, filter: { gameType: selectedGameType }, size: 4 }));
        }
    }, [dispatch, userGuid, activeTab, selectedGameType]);

    const handleGameHistoryPageChange = (newPage: number) => {
        if (userGuid) {
            dispatch(getMatches({ guid: userGuid, filter: { gameType: selectedGameType }, page: newPage, size: 4 }));
        }
    };

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

        if (!userGuid) {
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

        if (!userGuid) {
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
        if (userGuid) {
            dispatch(getByUserGuid({ guid: authUser.guid, page: newPage, size: 4 }));
        }
    };

    const username = user?.username || "User";
    const email = user?.email || "";
    const balance = user?.balance ?? 0;
    const status = user?.status || "default";
    const history = user?.history || [];

    const formattedDate = user?.createdAt
        ? new Date(user.createdAt).toLocaleDateString()
        : "Unknown";

    const statusIconSrc = getIcon(getStatusIconName(status));

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
                                        <Avatar
                                            src={avatarPreview || user?.avatarUrl}
                                            fallback={username}
                                            size={150}
                                            isLoading={loadingAvatar}
                                        />


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
                                <Button variant="solid" style={{ width: "100%" }} onClick={() => navigate('/upgrade')}>
                                    Upgrade
                                </Button>
                            )}

                            <Box style={{ marginTop: "auto", paddingTop: "2rem" }}>
                                {isLoading ? (
                                    <Skeleton variant="text" width={80} height={16} />
                                ) : (
                                    <Typography variant="caption" style={{ opacity: 0.6 }}>
                                        Registered on: {formattedDate}
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
                                                            setActiveTab('games');
                                                        } else {
                                                            if (userGuid) {
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

                            {activeTab === 'games' ? (
                                <PageablePanel
                                    title="Game History"
                                    isLoading={isLoadingGameHistory}
                                    isEmpty={!gameHistory || gameHistory.length === 0}
                                    emptyMessage={`No matches found for ${ROOM_TYPE_LABELS[selectedGameType]}. It's time to play!`}
                                    currentPage={gameHistoryPage}
                                    totalPages={gameHistoryTotalPages}
                                    onPageChange={handleGameHistoryPageChange}
                                    headerActions={
                                        <>
                                            <ComboBox
                                                options={AVAILABLE_ROOM_TYPES.map(t => ({ value: t, label: ROOM_TYPE_LABELS[t] }))}
                                                value={selectedGameType}
                                                onValueChange={(val) => setSelectedGameType(val as RoomType)}
                                                style={{ width: '190px' }}
                                            />
                                            <Button variant="ghost" onClick={() => dispatch(getMatches({ guid: userGuid!, filter: { gameType: selectedGameType } }))}>
                                                <Icon src={getIcon("refresh")} size={16} />
                                            </Button>
                                        </>
                                    }
                                >
                                    {gameHistory.map(m => (
                                        <HistoryItem
                                            key={m.id}
                                            variant={m.winnerId === userGuid ? 'income' : !m.winnerId ? 'neutral' : 'expense'}
                                            iconText={m.winnerId === userGuid ? '+' : !m.winnerId ? '=' : '-'}
                                            title={ROOM_TYPE_LABELS[m.gameType]}
                                            date={`${m.createdAt.substring(0, 10)} • ${m.createdAt.substring(11, 16)} UTC`}
                                            rightText={m.winnerId === userGuid ? 'Victory' : !m.winnerId ? 'Draw' : 'Defeat'}
                                        />
                                    ))}
                                </PageablePanel>
                            ) : (
                                <PageablePanel
                                    title="Balance History"
                                    isLoading={isLoadingTransactions}
                                    isEmpty={!transactions || transactions.length === 0}
                                    emptyMessage="No transactions found."
                                    currentPage={currentPage}
                                    totalPages={totalPages}
                                    onPageChange={handlePageChange}
                                    headerActions={
                                        <Button variant="ghost" onClick={() => dispatch(getByUserGuid({ guid: userGuid! }))}>
                                            <Icon src={getIcon("refresh")} size={16} />
                                        </Button>}
                                >
                                    {transactions.map(t => (
                                        <HistoryItem
                                            key={t.id}
                                            variant={t.type === 'ADDITION' ? 'income' : 'expense'}
                                            iconText={t.type === 'ADDITION' ? '+' : '-'}
                                            title={t.roomType ? ROOM_TYPE_LABELS[t.roomType] : 'Deposit'}
                                            date={`${t.createdAtDate} • ${t.createdAtTime.substring(0, 5)} UTC`}
                                            rightText={String(t.amount)}
                                            rightSubText={`Before: ${t.balanceBefore} \n After: ${t.balanceAfter}`}
                                        />
                                    ))}
                                </PageablePanel>
                            )}
                        </Stack>
                    </Grid>
                </Card>
            </Container>

            {toast && (
                <Toast
                    message={toast.text}
                    onClose={() => setToast(null)}
                />
            )
            }

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
