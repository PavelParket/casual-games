import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom"
import { Button, AppBar, ThemeSwitcher, Typography, Menu, MenuList, MenuItem, Icon, Img, useThemedIcon, Box } from "../ui"
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { logout } from "../store/slices/AuthSlice";
import logoImg from "../assets/images/logo.png";

export default function Header() {
    const { isAuthenticated, user } = useSelector((state: RootState) => state.auth);
    const dispatch = useDispatch<AppDispatch>();
    const navigate = useNavigate();
    const location = useLocation();

    const [isRoomsHovered, setIsRoomsHovered] = useState(false);

    const isAuthPage = location.pathname === "/login" || location.pathname === "/register";

    const { getInverseIcon } = useThemedIcon();

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
                        <Img src={logoImg} style={{ height: "50px" }} />
                    </Link>

                    {isAuthenticated && (
                        <Button
                            variant="ghost"
                            onClick={() => navigate("/rooms")}
                            onMouseEnter={() => setIsRoomsHovered(true)}
                            onMouseLeave={() => setIsRoomsHovered(false)}
                            style={{
                                height: "60px",
                                borderRadius: 0,
                                borderLeft: "none",
                                boxShadow: "none",
                                background: isRoomsHovered ? undefined : "rgba(0, 0, 0, 0.05)",
                                padding: "0 1.25rem",
                            }}
                        >
                            <Typography variant="body" style={{ fontWeight: 600, color: "inherit" }}>
                                Rooms
                            </Typography>
                        </Button>
                    )}
                </Box>
            )}
            right={(
                <>
                    {isAuthenticated ? (
                        <Menu
                            trigger={
                                <Button variant="ghost">
                                    <Box style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                        <Typography
                                            variant="body"
                                            title={user?.username || "User"}
                                            style={{
                                                overflow: "hidden",
                                                textOverflow: "ellipsis",
                                                display: 'block',
                                                maxWidth: '150px'
                                            }}
                                        >
                                            {user?.username || "User"}
                                        </Typography>
                                        <Icon
                                            src={getInverseIcon("expandMore")}
                                            alt="menu"
                                            size={16}
                                            className="menu-chevron-icon"
                                        />
                                    </Box>
                                </Button>
                            }
                        >
                            <MenuList>
                                <MenuItem onClick={() => navigate("/profile")}>
                                    Profile
                                </MenuItem>
                                <MenuItem onClick={() => navigate("/rooms")}>
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
