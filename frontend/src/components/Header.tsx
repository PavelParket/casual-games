import { Link, useNavigate } from "react-router-dom"
import { Button, AppBar, ThemeSwitcher, Typography, Menu, MenuList, MenuItem, Box } from "../ui"
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { logout } from "../store/slices/AuthSlice";

export default function Header() {
   const { isAuthenticated } = useSelector((state: RootState) => state.auth);
   const dispatch = useDispatch<AppDispatch>();
   const navigate = useNavigate();

   const { theme, getIcon } = useThemedIcon();

   const handleLogout = () => {
      dispatch(logout());
      setTimeout(() => {
         navigate("/");
      }, 500);
   };

   return (
      <AppBar
         left={(
            <Link to="/" style={{ textDecoration: "none" }}>
               <Typography variant="h3">Casual Games</Typography>
            </Link>
         )}
         right={(
            <>
               {isAuthenticated && (
                  <Menu
                     trigger={
                        <Button variant="ghost">
                           User ▼
                        </Button>
                     }
                  >
                     <MenuList>
                        <MenuItem onClick={() => navigate("/profile")}>
                           Profile
                        </MenuItem>
                        <MenuItem onClick={() => navigate("/settings")}>
                           Settings
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
               )}
            </>
         )}
      />
   );
}
