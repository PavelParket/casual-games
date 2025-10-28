import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { ThemeProvider } from './ui'
import Home from './pages/Home'
import Layout from './components/Layout'
import Register from './pages/auth/Register'
import Login from './pages/auth/Login'
import Forbidden from './pages/error/Forbidden'
import NotFound from './pages/error/NotFound'
import type { AppDispatch } from './store/store'
import { useDispatch } from 'react-redux'
import { useEffect } from 'react'
import { refresh } from './store/slices/authSlice'

export default function App() {
   const dispatch = useDispatch<AppDispatch>();

   useEffect(() => {
      dispatch(refresh());
   }, [dispatch]);

   return (
      <BrowserRouter>
         <ThemeProvider>
            <Routes>
               <Route element={<Layout />}>
                  <Route path="/" element={<Home />} />
               </Route>

               <Route element={<Layout centered />}>
                  <Route path="/register" element={<Register />} />
                  <Route path="/login" element={<Login />} />

                  <Route path="/forbidden" element={<Forbidden />} />
                  <Route path="*" element={<NotFound />} />
               </Route>
            </Routes>
         </ThemeProvider>
      </BrowserRouter>
   )
}