import { Link, useNavigate } from "react-router-dom"
import { Button, Navbar, ThemeSwitcher, Typography } from "../ui"
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
      }, 200);
   };

   return (
      <Navbar
         brand={(
            <Link to="/" style={{ textDecoration: "none" }}>
               <Typography variant="h3">Casual Games</Typography>
            </Link>
         )}
         right={(
            <>
               {isAuthenticated && (
                  <Link to="/rooms">
                     <Button variant="ghost">Rooms</Button>
                  </Link>
               )}

               {!isAuthenticated && (
                  <>
                     <Link to="/register">
                        <Button variant="solid">Sign Up</Button>
                     </Link>
                     <Link to="/login">
                        <Button variant="solid">Sign In</Button>
                     </Link>
                  </>
               )}

               {isAuthenticated && (
                  <Button variant="ghost" onClick={handleLogout}>
                     Logout
                  </Button>
               )}

               <ThemeSwitcher />
            </>
         )}
      />
   );
}
