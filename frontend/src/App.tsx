import { useEffect, useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { ThemeProvider } from './ui';
import Home from './pages/Home';
import Layout from './components/Layout';
import Register from './pages/auth/Register';
import Login from './pages/auth/Login';
import Forbidden from './pages/error/Forbidden';
import NotFound from './pages/error/NotFound';
import { ProtectedRoute } from './router/ProtectedRoute';
import Rooms from './pages/rooms/Rooms';
import TicTacToeRoom from './pages/rooms/TicTacToeRoom';
import type { AppDispatch } from './store/store';
//import { setOnRefreshRequired } from './utils/TokenManager';
import { refresh } from './store/slices/AuthSlice';

export default function App() {
   const dispatch = useDispatch<AppDispatch>();
   const [isInitialized, setIsInitialized] = useState<boolean>(false);

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


   // todo: сделать нормальный компонент ожидания загрузки
   if (!isInitialized) {
      return (
         <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            height: '100vh'
         }}>
            Loading...
         </div>
      );
   }

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
                     <Route path="/room/t-t-t/:roomName/:roomId" element={<TicTacToeRoom />} />
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
