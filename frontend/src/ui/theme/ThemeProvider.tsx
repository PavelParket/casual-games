import { useEffect, useState, type ReactNode } from "react";
import { ThemeContext, type Theme } from "./ThemeContext";

type ThemeProviderProps = {
   children: ReactNode;
};

export function ThemeProvider({ children }: ThemeProviderProps) {
   const [theme, setTheme] = useState<Theme>(() => {
      return (localStorage.getItem("theme") as Theme) ?? "light"
   });

   useEffect(() => {
      const root = document.documentElement;
      if (theme === "dark") {
         root.classList.add("dark");
      } else {
         root.classList.remove("dark");
      }
   }, [theme]);

   const toggleTheme = () =>
      setTheme((prev) => {
         const next = prev === "light" ? "dark" : "light";
         localStorage.setItem("theme", next);
         return next;
      });

   return (
      <ThemeContext.Provider value={{ theme, toggleTheme }}>
         {children}
      </ThemeContext.Provider>
   );
}
