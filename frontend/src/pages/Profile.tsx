import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { findByGuid, update } from "../store/slices/UserSlice";
import type { Icons } from "../assets/icons";

import {
    Box, Container, Card, Typography, Button,
    Stack, Divider, Grid, Icon, Textfield, Modal, Img, Input
} from "../ui";

import { useThemedIcon } from "../ui";

const getStatusIconName = (status: string): keyof typeof Icons.light => {
    return `${status.toLowerCase()}Status` as keyof typeof Icons.light;
};

export default function Profile() {
    const dispatch = useDispatch<AppDispatch>();
    
    const { profile, isLoading } = useSelector((state: RootState) => state.user);
    
    const { getInverseIcon } = useThemedIcon();

    const [isEditingUsername, setIsEditingUsername] = useState(false);
    const [tempUsername, setTempUsername] = useState("");

    const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
    const [isAvatarHovered, setIsAvatarHovered] = useState(false);

    const [historyModalOpen, setHistoryModalOpen] = useState(false);
    const [achievementsModalOpen, setAchievementsModalOpen] = useState(false);

    useEffect(() => {
        dispatch(findByGuid());
    }, [dispatch]);

    useEffect(() => {
        if (profile?.username) {
            setTempUsername(profile.username);
        }
    }, [profile]);


    const handleEditClick = () => setIsEditingUsername(true);
    const handleSaveUsername = () => {
        if (tempUsername.trim() && tempUsername !== profile?.username) {
            dispatch(update({ username: tempUsername }))
                .unwrap()
                .then(() => {
                    console.log("Username updated successfully!");
                })
                .catch((error) => {
                    console.error("Failed to update username:", error);
                });
        }
        setIsEditingUsername(false);
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files?.[0]) {
            setAvatarPreview(URL.createObjectURL(e.target.files[0]));
        }
    };


    const username = profile?.username || "User";
    const email = profile?.email || "";
    const balance = profile?.balance ?? 0;
    const status = profile?.status || "default";
    const achievements = profile?.achievements || [];
    const history = profile?.history || [];
    
    const formattedDate = profile?.createdAt 
        ? new Date(profile.createdAt).toLocaleDateString() 
        : "Unknown";

    const statusIconName = getStatusIconName(status);
    const statusIconSrc = getInverseIcon(statusIconName) || getInverseIcon("defaultStatus");

    const infoBlockStyle = {
        background: "var(--color-bg-glass)", 
        backdropFilter: "blur(10px)",
        padding: "1.5rem",
        borderRadius: "var(--radius-md)",
        border: "1px solid var(--color-border)",
        boxShadow: "var(--shadow-sm)"
    };

    if (isLoading && !profile) {
        return (
            <Box style={{ padding: "4rem 0", display: "flex", justifyContent: "center" }}>
                <Typography variant="h2">Loading profile...</Typography>
            </Box>
        );
    }

    return (
        <Box style={{ padding: "2rem 0" }}>
            <Container maxWidth="1000px">
                <Card style={{ minHeight: "100%" }}>

                    <Grid
                        columns="280px 1px 1fr"
                        gap="0" 
                        style={{ height: "100%" }}
                        className="profile-grid"
                    >
                        <Stack align="center" gap="1.5rem" style={{ paddingRight: "1rem" }}>
                            
                            <Box 
                                style={{ position: "relative", cursor: "pointer" }}
                                onMouseEnter={() => setIsAvatarHovered(true)}
                                onMouseLeave={() => setIsAvatarHovered(false)}
                            >
                                <label htmlFor="avatar-upload">
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
                                        cursor: "pointer"
                                    }}>
                                    {avatarPreview || profile?.avatarUrl ? (
                                        <Img
                                        src={avatarPreview || profile?.avatarUrl || ""} 
                                        alt="Avatar" 
                                        style={{ width: "100%", height: "100%", objectFit: "cover" }} 
                                    />
                                    ) : (
                                        username.substring(0, 2).toUpperCase()
                                    )}
                                </Box>

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
                                            transition: "all 0.2s ease"
                                        }}
                                    >
                                        <Icon src={getInverseIcon("edit")} alt="edit avatar" size={20} />
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
                            </Box>

                            <Stack align="center" gap="0.5rem">
                                <Icon src={statusIconSrc} alt={status} size={40} />
                                <Typography variant="h3" style={{ textTransform: "capitalize" }}>
                                    {status} Member
                                </Typography>
                            </Stack>

                            <Button variant="solid" style={{ width: "100%" }}>
                                Upgrade
                            </Button>

                            <Box style={{ marginTop: "auto", paddingTop: "2rem" }}>
                                <Typography variant="caption" style={{ opacity: 0.6 }}>
                                    Date: {formattedDate}
                                </Typography>
                            </Box>
                        </Stack>

                        <Box style={{ display: "flex", justifyContent: "center", height: "100%" }}>
                            <Divider orientation="vertical" />
                        </Box>

                        <Stack gap="2rem" style={{ width: "100%", paddingLeft: "1rem" }}>

                            <Box style={infoBlockStyle}>
                                <Stack gap="1rem">
                                    <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", minHeight: "40px" }}>
                                        <Box style={{ flex: 1, marginRight: "1rem" }}>
                                            <Typography variant="caption" style={{ opacity: 0.7 }}>Username:</Typography>
                                            
                                            {isEditingUsername ? (
                                                <Textfield 
                                                    value={tempUsername} 
                                                    onChange={setTempUsername} 
                                                    placeholder="Enter username"
                                                />
                                            ) : (
                                                <Typography variant="h3">{username}</Typography>
                                            )}
                                        </Box>
                                        
                                        {isEditingUsername ? (
                                            <Button 
                                                variant="solid" 
                                                onClick={handleSaveUsername}
                                                // Добавим disabled, пока идет загрузка
                                                disabled={isLoading} 
                                                style={{ display: "flex", alignItems: "center", gap: "5px" }}
                                            >
                                                {isLoading ? "Saving..." : "Save"}
                                            </Button>
                                        ) : (
                                            <Button 
                                                variant="outline" 
                                                onClick={handleEditClick}
                                                style={{ display: "flex", gap: "8px", alignItems: "center" }}
                                            >
                                                EDIT
                                                <Icon src={getInverseIcon("edit")} alt="edit" size={16} />
                                            </Button>
                                        )}
                                    </Box>

                                    <Box>
                                        <Typography variant="caption" style={{ opacity: 0.7 }}>Email:</Typography>
                                        <Typography variant="body">{email}</Typography>
                                    </Box>

                                    <Divider />

                                    <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                                        <Box>
                                            <Typography variant="caption" style={{ opacity: 0.7 }}>Balance:</Typography>
                                            <Typography variant="h2" style={{ color: "var(--color-primary)" }}>
                                                {balance} 
                                                <Typography variant="caption" style={{ marginLeft: "5px" }}>CG Coins</Typography>
                                            </Typography>
                                        </Box>
                                        <Button variant="ghost">
                                            Пополнить
                                        </Button>
                                    </Box>
                                </Stack>
                            </Box>

                            <Box style={infoBlockStyle}>
                                <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                                    <Typography variant="h3">Achievements</Typography>
                                    <Button 
                                        variant="ghost" 
                                        onClick={() => setAchievementsModalOpen(true)} 
                                        disabled={achievements.length === 0}
                                        style={{ fontSize: "0.8rem" }}
                                    >
                                        See All
                                    </Button>
                                </Box>
                                
                                {achievements.length > 0 ? (
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
                                )}
                            </Box>

                            <Box style={infoBlockStyle}>
                                <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
                                    <Typography variant="h3">History</Typography>
                                    <Button 
                                        variant="ghost" 
                                        onClick={() => setHistoryModalOpen(true)} 
                                        disabled={history.length === 0}
                                        style={{ fontSize: "0.8rem" }}
                                    >
                                        See All
                                    </Button>
                                </Box>

                                {history.length > 0 ? (
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
                                )}
                            </Box>

                        </Stack>
                    </Grid>
                </Card>
            </Container>

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
        </Box>
    );
}