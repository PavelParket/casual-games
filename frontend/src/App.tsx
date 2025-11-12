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
import { refresh } from './store/slices/AuthSlice'
import { ProtectedRoute } from './router/ProtectedRoute'
import Rooms from './pages/rooms/Rooms'
import TicTacToeRoom from './pages/rooms/TicTacToeRoom'

export default function App() {
   const dispatch = useDispatch<AppDispatch>();

   useEffect(() => {
      dispatch(refresh());
   }, [dispatch]);

   return (
      <BrowserRouter>
         <ThemeProvider>
            <Routes>
               {/* Public Routes */}
               <Route element={<Layout />}>
                  <Route path="/" element={<Home />} />
               </Route>

               {/* Protected Routes */}
               <Route element={<ProtectedRoute roles={["ADMIN", "USER"]} />}>
                  <Route element={<Layout />}>
                     <Route path="/rooms" element={<Rooms />} />
                     <Route path="/room/game/:roomName" element={<TicTacToeRoom />} />
                     {/* <Route path="/ws" element={<WebSocketComponent />} /> */}
                  </Route>
               </Route>

               {/* Auth and Error Routes*/}
               <Route element={<Layout centered />}>
                  <Route path="/register" element={<Register />} />
                  <Route path="/login" element={<Login />} />
                  <Route path="/forbidden" element={<Forbidden />} />
                  <Route path="*" element={<NotFound />} />
               </Route>
            </Routes>
         </ThemeProvider>
      </BrowserRouter>
   );
}