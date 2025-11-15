import { Container, Stack, Typography } from "../ui"

export default function Footer() {
   return (
      <footer style={{
         marginTop: "auto",
         background: "var(--glass-surface)",
         backdropFilter: `blur(var(--glass-blur))`,
         boxShadow: "var(--shadow-sm)",
      }}>
         <Container>
            <Stack direction="row" align="center" justify="space-between" style={{ padding: "16px 0", minHeight: "50px" }}>
               <Typography variant="caption">© {new Date().getFullYear()} WebSockets App</Typography>
               <Typography variant="caption">Built with custom UI</Typography>
            </Stack>
         </Container>
      </footer>
   )
}
