import { useNavigate } from "react-router-dom";
import { Box, Button, Container, Typography } from "../ui";
import { useSelector } from "react-redux";
import type { RootState } from "../store/store";

export default function Home() {
   const navigate = useNavigate();
   const { isAuthenticated } = useSelector((state: RootState) => state.auth);

   return (
      <Box style={{
         minHeight: "calc(100vh - 60px - 50px)",
         display: "flex",
         alignItems: "center",
         justifyContent: "center"
      }}>
         <Container>
            <Box style={{
               textAlign: "center",
               padding: "3rem",
               background: "var(--color-bg-glass)",
               backdropFilter: "blur(2px)",
               borderRadius: "var(--radius-lg)",
               boxShadow: "var(--shadow-lg)"
            }}>
               <Typography variant="h1" style={{ marginBottom: "1rem" }}>
                  Welcome to Casual Games
               </Typography>
               <Typography variant="body" style={{ marginBottom: "2rem", opacity: 0.8 }}>
                  Play singleplayer and multiplayer games alone or with your friends in real-time
               </Typography>

               {isAuthenticated ? (
                  <Box style={{ display: "flex", gap: "1rem", justifyContent: "center" }}>
                     <Button variant="solid" onClick={() => navigate("/rooms")} style={{ fontSize: "18px", padding: "0.75rem 2rem" }}>
                        Go to Rooms
                     </Button>
                     <Button variant="solid" onClick={() => navigate("/ws")} style={{ fontSize: "18px", padding: "0.75rem 2rem" }}>
                        Go to Test Room
                     </Button>
                  </Box>
               ) : (
                  <Box style={{ display: "flex", gap: "1rem", justifyContent: "center" }}>
                     <Button variant="solid" onClick={() => navigate("/login")} style={{ fontSize: "18px", padding: "0.75rem 2rem" }}>
                        Sign In
                     </Button>
                     <Button variant="outline" onClick={() => navigate("/register")} style={{ fontSize: "18px", padding: "0.75rem 2rem" }}>
                        Sign Up
                     </Button>
                  </Box>
               )}
            </Box>
         </Container>
      </Box>
   );
}
