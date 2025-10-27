import { Button, Card, Container, Typography } from "../../ui";
import { Link } from "react-router-dom";

export default function Forbidden() {
   return (
      <Container>
         <Card style={{ width: "min(420px, 100%)", textAlign: "center" }}>
            <Typography variant="h2" style={{ marginBottom: "20px" }}>403 — Access Forbidden</Typography>
            <Typography variant="body" style={{ marginBottom: "20px" }}>
               You don&apos;t have permission to access this page.
            </Typography>
            <Link to="/" style={{ textDecoration: "none" }}>
               <Button variant="solid">
                  Back to Home
               </Button>
            </Link>
         </Card>
      </Container>
   );
}
