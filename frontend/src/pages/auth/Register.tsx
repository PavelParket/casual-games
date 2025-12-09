import { useState } from "react";
import { Box, Button, Card, Container, Divider, Form, FormField, Typography, useThemedIcon } from "../../ui";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../../store/slices/AuthSlice";
import type { AppDispatch, RootState } from "../../store/store";
import { useDispatch, useSelector } from "react-redux";

export default function Register() {
   const dispatch = useDispatch<AppDispatch>();
   const navigate = useNavigate();

   const { isLoading, error } = useSelector((state: RootState) => state.auth);
   const [form, setForm] = useState({ username: "", email: "", password: "", });

   const { getIcon } = useThemedIcon();

   const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      setForm({ ...form, [e.target.name]: e.target.value });
   };

   const handleSubmit = async (e: React.FormEvent) => {
      e.preventDefault();

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
                     <Button type="submit" variant="solid" disabled={isLoading}>{isLoading ? "Loading..." : "Sign Up"}</Button>
                  </Form>

                  {error && (
                     <Typography variant="caption" style={{ color: "red", marginTop: "1rem", display: "block" }}>
                        {error}
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
