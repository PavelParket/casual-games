import { type RootState } from "../store/store";
import { useSelector } from "react-redux";
import { Navigate, Outlet, useLocation } from "react-router-dom";

interface ProtectedRouteProps {
   roles?: string[];
}

export function ProtectedRoute({ roles }: ProtectedRouteProps) {
   const authentication = useSelector((state: RootState) => state.auth);
   const location = useLocation();

   if (!authentication.isAuthenticated) {
      return <Navigate to="/login" state={{ from: location }} replace />;
   }

   if (roles && authentication?.user?.role && !roles.includes(authentication.user.role)) {
      return <Navigate to="/forbidden" replace />;
   }

   return <Outlet />;;
}
