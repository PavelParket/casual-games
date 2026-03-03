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
import { useEffect, useState } from 'react'
import { refresh } from './store/slices/AuthSlice'
import { ProtectedRoute } from './router/ProtectedRoute'
import Rooms from './pages/rooms/Rooms'
import TicTacToeRoom from './pages/rooms/TicTacToeRoom'
import DeCoderRoom from './pages/rooms/decoder/DeCoderRoom'
import Profile from './pages/Profile'
//import ExperimentalPage from './pages/ExperimentalPage'
import LoadingPage from './pages/LoadingPage'
import HorseRaceRoom from './pages/rooms/HorseRaceRoom'
import { useScrollbarVisibility } from './hooks/useScrollbarVisibility'

export default function App() {
   const dispatch = useDispatch<AppDispatch>();
   const [isInitialized, setIsInitialized] = useState<boolean>(false);

   useScrollbarVisibility();

   useEffect(() => {
      // todo: Переделать обновление токена и его прокид при вебсокетном подключении
      /* setOnRefreshRequired(() => {
         dispatch(refresh());
      }); */

      const initialize = async () => {
         try {
            await dispatch(refresh()).unwrap();
         } catch (error) {
            console.debug("Auth initialization failed:", error);
         }

         setIsInitialized(true);
      };

      initialize();
   }, [dispatch]);

   return (
      <BrowserRouter>
         <ThemeProvider>
            {!isInitialized ? (
               <Routes>
                  <Route element={<Layout centered />}>
                     <Route path="*" element={<LoadingPage />} />
                  </Route>
               </Routes>
            ) : (
               <Routes>
                  {/* Public Routes */}
                  <Route element={<Layout />}>
                     <Route path="/" element={<Home />} />
                  </Route>

                  {/* Protected Routes */}
                  <Route element={<ProtectedRoute roles={["ADMIN", "USER"]} />}>
                     <Route element={<Layout />}>
                        <Route path="/profile" element={<Profile />} />
                        <Route path="/rooms" element={<Rooms />} />
                        <Route path="/room/t-t-t/:roomName/:roomId" element={<TicTacToeRoom />} />
                        <Route path="/room/horse-race/:roomName/:roomId" element={<HorseRaceRoom />} />
                        <Route path="/room/de-coder/:roomName/:roomId" element={<DeCoderRoom />} />


                        {/* ===== Experiment Room ===== 
                        <Route path="/ws" element={<ExperimentalPage />} />*/}
                     </Route>
                  </Route>

                  {/* Auth and Error Routes*/}
                  <Route element={<Layout centered />}>
                     <Route path="/register" element={<Register />} />
                     <Route path="/login" element={<Login />} />
                     <Route path="/forbidden" element={<Forbidden />} />
                     <Route path="*" element={<NotFound />} />
                  </Route>
               </Routes>)}
         </ThemeProvider>
      </BrowserRouter>
   );
}
