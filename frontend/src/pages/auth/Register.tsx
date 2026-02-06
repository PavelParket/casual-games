import { useState } from "react";
import { Box, Button, Card, Container, Divider, Form, FormField, Typography, useThemedIcon } from "../../ui";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../../store/slices/AuthSlice";
import type { AppDispatch, RootState } from "../../store/store";
import { useDispatch, useSelector } from "react-redux";
import { isValidEmail, validateEmail, validateUsername } from "../../utils/SecurityUtils";

export default function Register() {
   const dispatch = useDispatch<AppDispatch>();
   const navigate = useNavigate();

   const { error } = useSelector((state: RootState) => state.auth);
   const [form, setForm] = useState({ username: "", email: "", password: "", });
   const [validationError, setValidationError] = useState<string>("");

   const { getIcon } = useThemedIcon();

   const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const { name, value } = e.target;

      if (name === "username") {
         const sanitized = validateUsername(value);
         setForm({ ...form, [name]: sanitized });
      } else if (name === "email") {
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

      if (form.username.length < 3) {
         setValidationError("Username must be at least 3 characters");
         return;
      }

      if (form.username.length > 50) {
         setValidationError("Username must be less than 50 characters");
         return;
      }

      if (!isValidEmail(form.email)) {
         setValidationError("Please enter a valid email address");
         return;
      }

      if (form.password.length < 4) {
         setValidationError("Password must be at least 4 characters");
         return;
      }

      if (form.password.length > 100) {
         setValidationError("Password is too long");
         return;
      }

      await dispatch(register(form)).unwrap();
      navigate('/');
   };

   return (
      <Box style={{ padding: "56px 0" }}>
         <Container>
            <Box style={{ display: "grid", placeItems: "center", minWidth: "500px" }}>
               <Card style={{ width: "min(420px, 100%)", textAlign: "center", padding: "30px 40px" }}>
                  <Typography variant="h2">Sign Up</Typography>
                  <Form onSubmit={handleSubmit} gap="16px" style={{ marginTop: 16 }}>
                     <FormField
                        placeholder="Username"
                        name="username"
                        value={form.username}
                        onChange={handleChange}
                        required
                        rounded
                        endAdornmentSrc={getIcon("user")}
                        endAdornmentAlt="user"
                     />
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
                     <Button type="submit" variant="solid">Sign Up</Button>
                  </Form>

                  {(error || validationError) && (
                     <Typography variant="caption" style={{ color: "red", marginTop: "1rem", display: "block" }}>
                        {error || validationError}
                     </Typography>
                  )}

                  <Divider variant="middle" style={{ marginTop: "1rem", marginBottom: "1rem" }} />

                  <Typography variant="caption" style={{ display: "block" }}>
                     Already have an account?
                     <Link to="/login" className="link" style={{ marginLeft: 5 }}>
                        Sign In
                     </Link>
                  </Typography>
               </Card>
            </Box>
         </Container>
      </Box>
   );
}
