import { useNavigate } from "react-router-dom";
import { useCallback, useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { findByGuid, update, getMatches, uploadProfilePicture, deleteProfilePicture, clearGameHistoryState } from "../../store/slices/UserSlice";
import { deposit, getByUserGuid } from "../../store/slices/BankSlice";
import type { Icons } from "../../assets/icons";
import { Box, Container, Card, Typography, Button, Stack, Divider, Icon, Textfield, Modal, Input, FormField, Avatar, ComboBox, Menu, MenuList, MenuItem } from "../../ui";
import { useThemedIcon } from "../../ui";
import { validateAndReadJpeg, validateUsername, validateAmountInput } from "../../utils/SecurityUtils";
import { Skeleton } from "../../ui/components/common/Skeleton";
import { ROOM_TYPE_LABELS, type RoomType } from "../../models/Room";
import { PageablePanel } from "./components/PageablePanel";
import { HistoryItem } from "./components/HistoryItem";
import { AvatarEditorModal } from "./components/AvatarEditorModal.tsx";
import { ImageViewerModal } from "./components/ImageViewerModal";
import { useSystemToastContext } from "../../providers/SystemToastContext";
import { type GameMatchRequestFilter, type ResultFilter, RESULT_FILTER_LABELS } from "../../models/GameMatch.ts";
import "./style/Profile.css";

const AVAILABLE_ROOM_TYPES = (Object.keys(ROOM_TYPE_LABELS) as RoomType[])
    .filter(type => type !== "DE_CODER");

const RESULT_FILTER_OPTIONS = (Object.keys(RESULT_FILTER_LABELS) as ResultFilter[]).map(key => ({
    value: key,
    label: RESULT_FILTER_LABELS[key]
}));

const ROOM_ICON_NAMES: Record<RoomType, keyof typeof Icons.light> = {
    DURAK: "durak",
    TIC_TAC_TOE: "ticTacToe",
    HORSE_RACE: "horse",
};

const getStatusIconName = (status: string): keyof typeof Icons.light => {
    return `${status.toLowerCase()}Status` as keyof typeof Icons.light;
};

export default function Profile() {
    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();

    const { user, isLoading, gameHistory, isLoadingGameHistory, gameHistoryPage, gameHistoryTotalPages } =
        useSelector((state: RootState) => state.user);
    const authUser = useSelector((state: RootState) => state.auth.user);
    const { isDepositing, transactions, isLoadingTransactions, currentPage, totalPages } =
        useSelector((state: RootState) => state.bank);

    const { getIcon, getInverseIcon } = useThemedIcon();
    const { showSystemToast } = useSystemToastContext();

    const [isEditingUsername, setIsEditingUsername] = useState(false);
    const [tempUsername, setTempUsername] = useState("");

    const [validationError, setValidationError] = useState<string | null>(null);
    const [depositError, setDepositError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState<'games' | 'balanceHistory'>('games');

    const [selectedGameType, setSelectedGameType] = useState<RoomType>('DURAK');
    const [resultFilter, setResultFilter] = useState<ResultFilter>("ALL");

    const [isAvatarHovered, setIsAvatarHovered] = useState(false);

    const [depositModalOpen, setDepositModalOpen] = useState(false);
    const [depositAmount, setDepositAmount] = useState("");


    const [selectedImage, setSelectedImage] = useState<string | null>(null);
    const [isEditorOpen, setIsEditorOpen] = useState(false);

    const fileInputRef = useRef<HTMLInputElement>(null);
    const [isDeleteConfirmOpen, setIsDeleteConfirmOpen] = useState(false);
    const [isViewerOpen, setIsViewerOpen] = useState(false);

    const userGuid = authUser?.guid;

    const [windowWidth, setWindowWidth] = useState(window.innerWidth);
    useEffect(() => {
        const handleResize = () => setWindowWidth(window.innerWidth);
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    const isMobile = windowWidth <= 840;

    const getFilterParams = useCallback((resFilter: ResultFilter): GameMatchRequestFilter => {
        const filter: GameMatchRequestFilter = { gameType: selectedGameType };
        if (resFilter === "WINS") filter.isWinner = true;
        if (resFilter === "LOSSES") filter.isWinner = false;
        return filter;
    }, [selectedGameType]);

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
            dispatch(clearGameHistoryState());
            dispatch(getMatches({ guid: userGuid, filter: getFilterParams(resultFilter), size: 4 }));
        }
    }, [dispatch, userGuid, activeTab, getFilterParams, resultFilter]);

    const handleGameHistoryPageChange = (newPage: number) => {
        if (userGuid) {
            dispatch(getMatches({ guid: userGuid, filter: getFilterParams(resultFilter), page: newPage, size: 4 }));
        }
    };

    const handleEditClick = () => {
        setValidationError(null);
        setIsEditingUsername(true);
    };

    const handleUploadClick = () => {
        fileInputRef.current?.click();
    };

    const confirmDelete = async () => {
        await handleDeleteProfilePicture();
        setIsDeleteConfirmOpen(false);
    };

    const handleSaveUsername = async () => {
        const sanitizedUsername = validateUsername(tempUsername);

        if (sanitizedUsername.length < 3) {
            setValidationError("Username must be at least 3 characters long.");
            return;
        }

        if (sanitizedUsername === user?.username) {
            setIsEditingUsername(false);
            return;
        }

        if (!userGuid) {
            showSystemToast("User not authenticated", "system-error");
            return;
        }

        try {
            await dispatch(update({
                guid: authUser.guid,
                updateData: { username: sanitizedUsername }
            })).unwrap();
            showSystemToast("Username updated successfully!", "system-info");
        } catch (error) {
            showSystemToast(typeof error === "string" ? error : "Update failed", "system-error");
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

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];

            e.target.value = "";

            try {
                const dataUrl = await validateAndReadJpeg(file);
                setSelectedImage(dataUrl);
                setIsEditorOpen(true);
            } catch (error) {
                showSystemToast(error as string, "system-error");
            }
        } else {
            e.target.value = "";
        }
    };

    const handleUploadProfilePicture = async (files: { full: File; mini: File }) => {
        if (!userGuid) return;
        try {
            await dispatch(uploadProfilePicture({ guid: userGuid, files })).unwrap();
            showSystemToast("Profile picture updated!", "system-info");
            setIsEditorOpen(false);
            setSelectedImage(null);
        } catch (err) {
            showSystemToast(typeof err === "string" ? err : "Upload failed", "system-error");
        }
    };

    const handleDeleteProfilePicture = async () => {
        if (!userGuid) return;
        try {
            await dispatch(deleteProfilePicture(userGuid)).unwrap();
            showSystemToast("Profile picture deleted!", "system-info");
        } catch (err) {
            showSystemToast(typeof err === "string" ? err : "Failed to delete profile picture", "system-error");
        }
    };

    const handleDepositAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const validated = validateAmountInput(e.target.value);
        if (validated !== null) {
            setDepositAmount(validated);
            setDepositError(null);
        }
    };

    const handleDeposit = async () => {
        setDepositError(null);
        const amount = parseFloat(depositAmount);

        if (isNaN(amount) || amount <= 0) {
            setDepositError("Please enter a valid amount greater than 0.");
            return;
        }

        if (!userGuid) {
            showSystemToast("User not identified", "system-error");
            return;
        }

        try {
            await dispatch(deposit({ userGuid: authUser.guid, amount })).unwrap();
            showSystemToast("Deposit successful!", "system-info");
            setDepositModalOpen(false);
            setDepositAmount("");
        } catch (err) {
            showSystemToast(typeof err === "string" ? err : "Deposit failed", "system-error");
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
        <Box className="page-wrapper profile-page" style={{ padding: "2rem 0" }}>
            <Container>
                <Card className="profile-main-card">

                    <Box className="profile-grid">

                        <Stack className="profile-left-panel" align="center" gap="1.5rem">
                            <Box
                                style={{ position: "relative" }}
                                onMouseEnter={() => setIsAvatarHovered(true)}
                                onMouseLeave={() => setIsAvatarHovered(false)}
                            >
                                <Box
                                    onClick={() => user?.linkProfilePicture && setIsViewerOpen(true)}
                                    style={{ cursor: user?.linkProfilePicture ? "pointer" : "default" }}
                                    title={user?.linkProfilePicture ? "View full picture" : undefined}
                                >
                                    <Avatar src={user?.linkProfilePictureMini} fallback={username} size={150} isLoading={isLoading} />
                                </Box>

                                <Box style={{
                                    position: "absolute", bottom: 5, right: 5,
                                    opacity: isAvatarHovered || isMobile ? 1 : 0,
                                    transform: isAvatarHovered || isMobile ? "scale(1)" : "scale(0.8)",
                                    transition: "all 0.2s ease", zIndex: 2,
                                }}>
                                    <Menu
                                        className="menu-align-left"
                                        trigger={
                                            <Box style={{
                                                borderRadius: "50%", width: "40px", height: "40px",
                                                background: "var(--color-bg)", border: "1px solid var(--color-border)",
                                                display: "flex", alignItems: "center", justifyContent: "center",
                                                boxShadow: "var(--shadow-sm)", cursor: "pointer"
                                            }} title="Edit Settings">
                                                <Icon src={getIcon("edit")} alt="edit avatar" size={20} />
                                            </Box>
                                        }
                                    >
                                        <MenuList>
                                            <MenuItem onClick={handleUploadClick}>Upload picture</MenuItem>
                                            {user?.linkProfilePicture && (
                                                <MenuItem onClick={() => setIsDeleteConfirmOpen(true)}>
                                                    <span style={{ color: "var(--color-expense-text)" }}>Delete picture</span>
                                                </MenuItem>
                                            )}
                                        </MenuList>
                                    </Menu>
                                </Box>

                                <Input
                                    type="file" ref={fileInputRef} style={{ display: "none" }}
                                    accept="image/jpeg, image/jpg" onChange={handleFileChange}
                                />
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
                                <Button variant="solid" style={{ width: "100%", maxWidth: "280px" }} onClick={() => navigate('/upgrade')}>
                                    Upgrade
                                </Button>
                            )}

                            <Box style={{ marginTop: isMobile ? "0" : "auto", paddingTop: isMobile ? "0" : "2rem" }}>
                                {isLoading ? (
                                    <Skeleton variant="text" width={80} height={16} />
                                ) : (
                                    <Typography variant="caption" style={{ opacity: 0.6 }}>
                                        Registered on: {formattedDate}
                                    </Typography>
                                )}
                            </Box>
                        </Stack>

                        <Box className="profile-divider">
                            <Divider orientation="vertical" />
                        </Box>

                        <Stack gap="2rem" className="profile-right-panel">

                            <Box style={infoBlockStyle}>
                                <Stack gap="1rem">
                                    <Box className="username-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center", minHeight: "45px" }}>
                                        {isLoading ? (
                                            <Skeleton variant="text" width={150} height={28} />
                                        ) : (
                                            <Box style={{ flex: 1, marginRight: "1rem" }}>
                                                <Typography variant="caption" style={{ opacity: 0.7 }}>Username:</Typography>

                                                {isEditingUsername ? (
                                                    <>
                                                        <Textfield value={tempUsername} onChange={handleUsernameChange} placeholder="Enter username" />
                                                        {validationError && (
                                                            <Typography variant="caption" style={{ color: 'red', marginTop: '4px', display: 'block' }}>
                                                                {validationError}
                                                            </Typography>
                                                        )}
                                                    </>
                                                ) : (
                                                    <Typography variant="h3" title={username} style={{ overflow: "hidden", textOverflow: "ellipsis", display: 'block', maxWidth: isMobile ? '100%' : '150px' }}>
                                                        {username}
                                                    </Typography>
                                                )}
                                            </Box>
                                        )}

                                        {isEditingUsername ? (
                                            <Stack direction="row">
                                                <Button variant="solid" onClick={handleSaveUsername} disabled={isLoading} style={{ display: "flex", alignItems: "center", gap: "5px" }}>
                                                    {isLoading ? "Saving..." : "Save"}
                                                </Button>
                                                <Button variant="outline" disabled={isLoading} onClick={() => { setIsEditingUsername(false); setValidationError(null); }}>
                                                    Cancel
                                                </Button>
                                            </Stack>
                                        ) : (
                                            isLoading ? (
                                                <Skeleton variant="rectangular" width={75} height={34} />
                                            ) : (
                                                <Button variant="outline" onClick={handleEditClick} style={{ display: "flex", gap: "8px", alignItems: "center" }}>
                                                    Edit <Icon src={getIcon("edit")} alt="edit" size={16} />
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
                                        <Box className="balance-section">
                                            <Box>
                                                <Typography variant="caption" style={{ opacity: 0.7 }}>Balance:</Typography>
                                                <Typography variant="h2" style={{ color: "var(--color-primary)" }}>
                                                    {balance}
                                                    <Typography variant="caption" style={{ marginLeft: "5px" }}>CG Coins</Typography>
                                                </Typography>
                                            </Box>

                                            <div className="balance-buttons">
                                                <Button variant="ghost" onClick={() => setDepositModalOpen(true)}>
                                                    Deposit
                                                </Button>
                                                <Button
                                                    variant={activeTab === 'balanceHistory' ? "solid" : "outline"}
                                                    onClick={() => {
                                                        if (activeTab === 'balanceHistory') setActiveTab('games');
                                                        else {
                                                            if (userGuid) dispatch(getByUserGuid({ guid: authUser.guid, size: 4 }));
                                                            setActiveTab('balanceHistory');
                                                        }
                                                    }}
                                                    style={{ width: "135px" }}
                                                >
                                                    {activeTab === 'balanceHistory' ? 'Close History' : 'History'}
                                                </Button>
                                            </div>
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
                                    topRightAction={
                                        <Button variant="ghost" style={{ padding: "0.35rem" }} onClick={() => dispatch(getMatches({ guid: userGuid!, filter: getFilterParams(resultFilter) }))}>
                                            <Icon src={getIcon("refresh")} size={16} />
                                        </Button>
                                    }
                                    headerActions={
                                        <div className="profile-filter-controls">
                                            <ComboBox
                                                className="filter-result"
                                                options={RESULT_FILTER_OPTIONS.map(opt => ({
                                                    value: opt.value,
                                                    label: opt.label,
                                                    searchLabel: opt.label
                                                }))}
                                                value={resultFilter}
                                                onValueChange={(val) => setResultFilter(val as ResultFilter)}
                                            />
                                            <ComboBox
                                                className="filter-room"
                                                options={AVAILABLE_ROOM_TYPES.map(t => ({
                                                    value: t,
                                                    label: (selected: boolean) => isMobile ? (
                                                        <Icon
                                                            src={selected ? getInverseIcon(ROOM_ICON_NAMES[t]) : getIcon(ROOM_ICON_NAMES[t])}
                                                            size={20}
                                                        />
                                                    ) : ROOM_TYPE_LABELS[t],
                                                    searchLabel: ROOM_TYPE_LABELS[t]
                                                }))}
                                                value={selectedGameType}
                                                onValueChange={(val) => setSelectedGameType(val as RoomType)}
                                            />
                                        </div>
                                    }
                                >
                                    {gameHistory.map(m => {
                                        const isWin = m.gameResult === 'WIN';
                                        const isLoss = m.gameResult === 'LOSS';

                                        return (
                                            <HistoryItem
                                                key={m.id}
                                                variant={isWin ? 'income' : isLoss ? 'expense' : 'neutral'}
                                                iconText={isWin ? '+' : isLoss ? '-' : '='}
                                                title={ROOM_TYPE_LABELS[m.gameType]}
                                                date={`${m.createdAt.substring(0, 10)}`}
                                                time={`${m.createdAt.substring(11, 16)} UTC`}
                                                rightText={isWin ? 'Victory' : isLoss ? 'Defeat' : 'Draw'}
                                            />
                                        );
                                    })}
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
                                    topRightAction={
                                        <Button variant="ghost" style={{ padding: "0.35rem" }} onClick={() => dispatch(getByUserGuid({ guid: userGuid! }))}>
                                            <Icon src={getIcon("refresh")} size={16} />
                                        </Button>
                                    }
                                >
                                    {transactions.map(t => (
                                        <HistoryItem
                                            key={t.id}
                                            variant={t.type === 'ADDITION' ? 'income' : 'expense'}
                                            iconText={t.type === 'ADDITION' ? '+' : '-'}
                                            title={t.roomType ? ROOM_TYPE_LABELS[t.roomType] : 'Deposit'}
                                            date={`${t.createdAtDate}`}
                                            time={`${t.createdAtTime.substring(0, 5)} UTC`}
                                            rightText={String(t.amount)}
                                            rightSubText={
                                                <span className="history-subtext-grid">
                                                    <span>Before:</span> <span>{t.balanceBefore}</span>
                                                    <span>After:</span>  <span>{t.balanceAfter}</span>
                                                </span>
                                            } />
                                    ))}
                                </PageablePanel>
                            )}
                        </Stack>
                    </Box>
                </Card>
            </Container>

            <AvatarEditorModal isOpen={isEditorOpen} imageSrc={selectedImage} onClose={() => { setIsEditorOpen(false); setSelectedImage(null); }} onUpload={handleUploadProfilePicture} isLoading={isLoading} />
            <Modal isOpen={depositModalOpen} onClose={() => { setDepositModalOpen(false); setDepositAmount(''); setDepositError(null); }} title="Deposit Funds">
                <Stack gap="1rem">
                    <Typography variant="body">Enter the amount you wish to add to your balance.</Typography>
                    <FormField type="text" inputMode="decimal" value={depositAmount} onChange={(e) => {
                        const val = e.target.value.replace(',', '.');
                        if (val !== '' && !isNaN(Number(val)) && parseFloat(val) > 5000) {
                            return;
                        }
                        handleDepositAmountChange(e);
                    }} onFocus={(e) => e.target.select()} placeholder="Amount" rounded />
                    {depositError && <Typography variant="caption" style={{ color: 'var(--color-expense-text)' }}>{depositError}</Typography>}
                    <Button variant="solid" onClick={handleDeposit} disabled={isDepositing || !depositAmount || depositAmount === '.'}>{isDepositing ? "Processing..." : "Confirm Deposit"}</Button>
                </Stack>
            </Modal>
            <ImageViewerModal isOpen={isViewerOpen} src={user?.linkProfilePicture || ""} alt="Profile Picture" onClose={() => setIsViewerOpen(false)} />
            <Modal isOpen={isDeleteConfirmOpen} onClose={() => setIsDeleteConfirmOpen(false)} title="Delete Profile Picture">
                <Stack gap="1rem">
                    <Typography variant="body">Are you sure you want to delete your profile picture? This action cannot be undone.</Typography>
                    <Stack direction="row" gap="1rem" justify="flex-end" style={{ marginTop: "1rem" }}>
                        <Button variant="outline" onClick={() => setIsDeleteConfirmOpen(false)} disabled={isLoading}>Cancel</Button>
                        <Button variant="solid" onClick={confirmDelete} disabled={isLoading} style={{ background: "var(--color-expense-text)" }}>{isLoading ? "Deleting..." : "Delete"}</Button>
                    </Stack>
                </Stack>
            </Modal>
        </Box>
    );
}
