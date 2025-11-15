import Header from "./Header"
import Footer from "./Footer"
import { Box } from "../ui"
import { Outlet } from "react-router-dom";

interface LayoutProps {
   centered?: boolean;
}

export default function Layout({ centered }: LayoutProps) {
   return (
      <Box style={{ minHeight: "100dvh", display: "flex", flexDirection: "column" }}>
         <Header />
         <Box component="main"
            style={{
               flex: 1,
               display: centered ? "flex" : "block",
               justifyContent: centered ? "center" : undefined,
               alignItems: centered ? "center" : undefined,
            }}
         >
            <Outlet />
         </Box>
         <Footer />
      </Box>
   );
}
