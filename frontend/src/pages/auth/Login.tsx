import { useEffect, useState } from "react";
import { Box, Button, Card, Container, Divider, Form, FormField, Typography } from "../../ui";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../store/store";
import { login } from "../../store/slices/AuthSlice";
import { useThemedIcon } from "../../ui/hooks/useThemedIcon";
import { isValidEmail, validateEmail } from "../../utils/SecurityUtils";

export default function Login() {
   const [form, setForm] = useState({ email: "", password: "" });
   const [validationError, setValidationError] = useState<string>("");
   const { isAuthenticated, error } = useSelector((state: RootState) => state.auth);

   const { getIcon } = useThemedIcon();

   const dispatch = useDispatch<AppDispatch>();
   const navigate = useNavigate();
   const location = useLocation();

   const from = (location.state as { from?: Location })?.from?.pathname || "/";

   useEffect(() => {
      if (isAuthenticated) {
         navigate(from, { replace: true });
      }
   }, [isAuthenticated, navigate, from]);

   const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const { name, value } = e.target;

      if (name === "email") {
         const sanitized = validateEmail(value);
         setForm({ ...form, [name]: sanitized });
      } else {
         setForm({ ...form, [name]: value });
      }

      setValidationError("");
   };

   const handleSubmit = async (e: React.FormEvent) => {
      e.preventDefault();
      setValidationError("");

      if (!isValidEmail(form.email)) {
         setValidationError("Please enter a valid email address");
         return;
      }

      if (form.password.length < 4) {
         setValidationError("Password must be at least 4 characters");
         return;
      }

      await dispatch(login(form)).unwrap();
   };

   return (
      <Box style={{ padding: "56px 0" }}>
         <Container>
            <Box style={{ display: "grid", placeItems: "center", minWidth: "500px" }}>
               <Card style={{ width: "min(420px, 100%)", textAlign: "center", padding: "30px 40px" }}>
                  <Typography variant="h2">Sign In</Typography>
                  <Form onSubmit={handleSubmit} gap="16px" style={{ marginTop: 16 }}>
                     <FormField
                        placeholder="Email"
                        type="email"
                        name="email"
                        value={form.email}
                        onChange={handleChange}
                        required
                        rounded
                        endAdornmentSrc={getIcon("apersant")}
                        endAdornmentAlt="email"
                     />
                     <FormField
                        placeholder="Password"
                        type="password"
                        name="password"
                        value={form.password}
                        onChange={handleChange}
                        required
                        rounded
                     />
                     <Button type="submit" variant="solid">
                        Sign In
                     </Button>
                  </Form>

                  {(error || validationError) && (
                     <Typography variant="caption" style={{ color: "red", marginTop: "1rem", display: "block" }}>
                        {error || validationError}
                     </Typography>
                  )}

                  <Divider variant="middle" style={{ marginTop: "1rem", marginBottom: "1rem" }} />

                  <Typography variant="caption" style={{ display: "block" }}>
                     Don't have an account?
                     <Link to="/register" className="link" style={{ marginLeft: 5 }}>
                        Sign Up
                     </Link>
                  </Typography>
               </Card>
            </Box>
         </Container>
      </Box>
   );
}
