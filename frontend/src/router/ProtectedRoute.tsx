import type { RootState } from "../store/store";
import { useSelector } from "react-redux";
import { Navigate, Outlet, useLocation } from "react-router-dom";

interface ProtectedRouteProps {
   roles?: string[];
}

export function ProtectedRoute({ roles }: ProtectedRouteProps) {
   const { user, isAuthenticated } = useSelector((state: RootState) => state.auth);
   const location = useLocation();

   if (!isAuthenticated) {
      return <Navigate to="/login" state={{ from: location }} replace />;
   }

   if (roles && user?.role && !roles.includes(user.role)) {
      return <Navigate to="/forbidden" replace />;
   }

   return <Outlet />;;
}