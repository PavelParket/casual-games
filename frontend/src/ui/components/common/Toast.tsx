import "../styles/toast.css";
import { useEffect } from "react";
import { Box } from "../layout/Box";
import { Typography } from "./Typography";
import { Button } from "./Button";
import { classNames } from "../../utils/classNames";

export interface ToastProps {
   message: string;
   duration?: number;
   onClose: () => void;
   className?: string;
}

export function Toast({ message, duration = 3000, onClose, className }: ToastProps) {
   useEffect(() => {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
   }, [duration, onClose]);

   return (
      <Box
         className={classNames("toast", className)}
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
         <Typography variant="body">
            {message}
         </Typography>
         <Button variant="ghost" onClick={onClose} style={{ padding: "0.25rem", boxShadow: "none" }}>
            X
         </Button>
      </Box>
   );
}
