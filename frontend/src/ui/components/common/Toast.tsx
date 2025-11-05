import "../styles/toast.css";
import { useEffect } from "react";
import { Box } from "../layout/Box";
import { Typography } from "./Typography";
import { Button } from "./Button";

export interface ToastProps {
   message: string;
   duration?: number;
   onClose: () => void;
}

export function Toast({ message, duration = 3000, onClose }: ToastProps) {
   useEffect(() => {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
   }, [duration, onClose]);

   return (
      <Box
         className="toast"
         style={{
            position: "fixed",
            top: "1.5rem",
            left: "50%",
            transform: "translateX(-50%)",
            display: "flex",
            alignItems: "center",
            gap: "1rem",
            padding: "1rem 1.5rem",
            borderRadius: "var(--radius-md)",
            boxShadow: "var(--shadow-lg)",
            zIndex: 9999,
            backdropFilter: "blur(var(--glass-blur))",
         }}
      >
         <Typography variant="body" inverse>
            {message}
         </Typography>
         <Button variant="ghost" onClick={onClose} style={{ padding: "0.25rem" }}>
            X
         </Button>
      </Box>
   );
}