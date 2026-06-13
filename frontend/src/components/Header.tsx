import { useEffect, useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom"
import { Button, AppBar, ThemeSwitcher, Typography, Menu, MenuList, MenuItem, Icon, Img, useThemedIcon, Box, useTheme, Avatar } from "../ui"
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { findByGuid } from "../store/slices/UserSlice";
import { logout } from "../store/slices/AuthSlice";
import logoDark from "../assets/images/logo-dark.png";
import logoLight from "../assets/images/logo-light.png";

export default function Header() {
    const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);
    const profileUser = useSelector((state: RootState) => state.user.user);

    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();
    const location = useLocation();
    const { theme } = useTheme();

    const [isRoomsHovered, setIsRoomsHovered] = useState(false);

    const isAuthPage = location.pathname === "/login" || location.pathname === "/register";

    const { getIcon, getInverseIcon } = useThemedIcon();

    useEffect(() => {
        if (isAuthenticated && user?.guid && !profileUser) {
            dispatch(findByGuid(user.guid));
        }
    }, [isAuthenticated, user?.guid, profileUser, dispatch]);

    const formattedBalance = new Intl.NumberFormat('en-US', {
        notation: "compact",
        compactDisplay: "short",
        maximumFractionDigits: 1
    }).format(profileUser?.balance ?? 0);

    const handleLogout = () => {
        dispatch(logout());
        setTimeout(() => {
            navigate("/");
        }, 500);
    };

    return (
        <AppBar
            left={(
                <Box style={{ display: "flex", height: "60px", alignItems: "center" }}>
                    <Link to="/" style={{ textDecoration: "none", display: "flex", alignItems: "center", marginRight: "0.5rem" }}>
                        <Img className="header-logo" src={theme === "dark" ? logoLight : logoDark} style={{ height: "50px" }} />
                    </Link>

                    {isAuthenticated && (
                        <Link
                            to="/rooms"
                            className="link hidden-mobile"
                            onMouseEnter={() => setIsRoomsHovered(true)}
                            onMouseLeave={() => setIsRoomsHovered(false)}
                            style={{ textDecoration: "none", margin: "1.5rem" }}
                        >
                            <Typography variant="h3" style={{ color: isRoomsHovered ? "var(--color-text-hover)" : "inherit" }}>
                                Rooms
                            </Typography>
                        </Link>
                    )}
                </Box>
            )}
            right={(
                <>
                    {isAuthenticated ? (
                        <Box style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                            <Menu
                                trigger={
                                    <Button variant="ghost" style={{ padding: "0.4rem 0.5rem" }}>
                                        <Box style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>

                                            {profileUser?.linkProfilePictureMini ? (
                                                <Avatar src={profileUser.linkProfilePictureMini} fallback={profileUser?.username} size={24} />
                                            ) : (
                                                <Icon src={getIcon("user")} alt="user" size={18} />
                                            )}

                                            <Typography
                                                className="hidden-mobile"
                                                variant="body"
                                                title={user?.username}
                                                style={{
                                                    overflow: "hidden",
                                                    textOverflow: "ellipsis",
                                                    display: 'block',
                                                    maxWidth: '120px'
                                                }}
                                            >
                                                {user?.username}
                                            </Typography>

                                            <Icon
                                                src={getInverseIcon("expandMore")}
                                                alt="menu"
                                                size={16}
                                                className="hidden-mobile menu-chevron-icon"
                                            />

                                            <Box
                                                title={`${profileUser?.balance} CG Coins`}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'baseline',
                                                    gap: '2px',
                                                    padding: '0 4px'
                                                }}
                                            >
                                                <Typography variant="body" style={{ fontWeight: 700, color: 'var(--color-primary)', lineHeight: 1 }}>
                                                    {formattedBalance}
                                                </Typography>
                                                <Typography variant="caption" style={{ opacity: 0.7, fontWeight: 600, lineHeight: 1 }}>
                                                    CG
                                                </Typography>
                                            </Box>
                                        </Box>
                                    </Button>
                                }
                            >
                                <MenuList>
                                    <MenuItem onClick={() => navigate("/profile")}>
                                        Profile
                                    </MenuItem>
                                    <MenuItem className="hidden-desktop" onClick={() => navigate("/rooms")}>
                                        Rooms
                                    </MenuItem>

                                    <Box
                                        style={{
                                            height: "2.5rem",
                                            margin: "0 0.5rem",
                                            padding: "0 1rem",
                                            display: "flex",
                                            alignContent: "center"
                                        }}
                                    >
                                        <ThemeSwitcher size="md" />
                                    </Box>

                                    <MenuItem onClick={handleLogout}>
                                        Logout
                                    </MenuItem>
                                </MenuList>
                            </Menu>
                        </Box>
                    ) : (
                        !isAuthPage && (
                            <Button variant="ghost" onClick={() => navigate("/login")}>
                                Sign In
                            </Button>
                        )
                    )}
                </>
            )}
        />
    );
}
