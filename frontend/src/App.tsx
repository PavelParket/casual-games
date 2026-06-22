import { useEffect, useState } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { useDispatch } from 'react-redux'
import { ThemeProvider } from './ui'
import Home from './pages/home/Home'
import Layout from './components/Layout'
import Register from './pages/auth/Register'
import Login from './pages/auth/Login'
import Forbidden from './pages/error/Forbidden'
import NotFound from './pages/error/NotFound'
import type { AppDispatch } from './store/store'
import { localLogout, refresh, setAccessToken } from './store/slices/AuthSlice'
import { ProtectedRoute } from './router/ProtectedRoute'
import Rooms from './pages/rooms/Rooms'
import Profile from './pages/profile/Profile'
import ExperimentalPage from './pages/ExperimentalPage'
import LoadingPage from './pages/LoadingPage'
import TermsOfUse from './pages/info/TermsOfUse'
import PrivacyPolicy from './pages/info/PrivacyPolicy'
import { useScrollbarVisibility } from './hooks/useScrollbarVisibility'
import { SystemToastProvider } from './providers/SystemToastContext'
import TicTacToeRoomShell from './pages/rooms/shell/TicTacToeRoomShell'
import HorseRaceRoomShell from './pages/rooms/shell/HorseRaceRoomShell'
import DeCoderRoomShell from './pages/rooms/shell/DeCoderRoomShell'
import DurakRoomShell from './pages/rooms/shell/DurakRoomShell'
import UpgradeStatus from './pages/profile/UpgradeStatus'
import { setOnRefreshRequired } from './utils/TokenManager'
import { ensureFreshToken } from './api/EnsureFreshToken'
import { AuthBroadcast } from './api/AuthBroadcast'
import { initAuthToast } from './api/AxiosInterceptorsConfig'
import { useSystemToastContext } from './hooks/useSystemToastContext'

function AuthToastInitializer() {
    const { showSystemToast } = useSystemToastContext();

    useEffect(() => {
        initAuthToast(showSystemToast);
    }, [showSystemToast]);

    return null;
}

export default function App() {
    const dispatch = useDispatch<AppDispatch>();
    const [isInitialized, setIsInitialized] = useState<boolean>(false);

    useScrollbarVisibility();

    useEffect(() => {
        setOnRefreshRequired(() => {
            ensureFreshToken().catch(err => console.debug("Proactive refresh failed:", err));
        });

        const unsubscribeBroadcast = AuthBroadcast.onMessage((event) => {
            if (event.type === "LOGGED_OUT") {
                dispatch(localLogout());
            } else if (event.type === "REFRESHED") {
                dispatch(setAccessToken(event.token));
            }
        });

        const initialize = async () => {
            try {
                await dispatch(refresh()).unwrap();
            } catch (error) {
                console.debug("Auth initialization failed:", error);
            }

            setIsInitialized(true);
        };

        initialize();

        return () => {
            unsubscribeBroadcast();
        };
    }, [dispatch]);

    return (
        <BrowserRouter>
            <ThemeProvider>
                <SystemToastProvider>
                    <AuthToastInitializer />

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
                                <Route path="/terms" element={<TermsOfUse />} />
                                <Route path="/privacy" element={<PrivacyPolicy />} />
                            </Route>



                            {/* ===== Experiment Room ===== */}
                            <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
                                <Route path="/ws" element={<ExperimentalPage />} />
                            </Route>

                            {/* Protected Routes */}
                            <Route element={<ProtectedRoute roles={["ADMIN", "USER"]} />}>
                                <Route element={<Layout />}>
                                    <Route path="/profile" element={<Profile />} />
                                    <Route path="/upgrade" element={<UpgradeStatus />} />
                                    <Route path="/rooms" element={<Rooms />} />
                                    <Route path="/room/t-t-t/:roomName/:roomId" element={<TicTacToeRoomShell />} />
                                    <Route path="/room/horse-race/:roomName/:roomId" element={<HorseRaceRoomShell />} />
                                    <Route path="/room/de-coder/:roomName/:roomId" element={<DeCoderRoomShell />} />
                                    <Route path="/room/durak/:roomName/:roomId" element={<DurakRoomShell />} />
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
                    )}
                </SystemToastProvider>
            </ThemeProvider>
        </BrowserRouter>
    );
}
