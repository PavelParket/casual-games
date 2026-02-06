import { Stack, Typography } from "../ui"

export default function Footer() {
   return (
      <footer style={{
         marginTop: "auto",
         background: "var(--glass-surface)",
         backdropFilter: `blur(var(--glass-blur))`,
         boxShadow: "var(--shadow-sm)",
      }}>
         <Stack direction="row" align="center" justify="space-between" style={{ padding: "1rem 2rem", minHeight: "50px" }}>
            <Typography variant="caption">© {new Date().getFullYear()} Casual Games</Typography>
            <Typography variant="caption">Built with custom UI: Pavel & Timofei</Typography>
         </Stack>
      </footer>
   )
}
