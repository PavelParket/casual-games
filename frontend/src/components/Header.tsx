import { Link, useNavigate } from "react-router-dom"
import { Button, AppBar, ThemeSwitcher, Typography, Menu, MenuList, MenuItem } from "../ui"
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../store/store";
import { logout } from "../store/slices/AuthSlice";

export default function Header() {
   const { isAuthenticated } = useSelector((state: RootState) => state.auth);
   const dispatch = useDispatch<AppDispatch>();
   const navigate = useNavigate();

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
                        <MenuItem onClick={handleLogout}>
                           Logout
                        </MenuItem>

                        <MenuItem>
                           <ThemeSwitcher />
                        </MenuItem>
                     </MenuList>
                  </Menu>
               )}
            </>
         )}
      />
   );
}
