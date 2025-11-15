import { useEffect, useState, type ReactNode } from "react";
import { ThemeContext, type Theme } from "./ThemeContext";

type ThemeProviderProps = {
   children: ReactNode;
};

export function ThemeProvider({ children }: ThemeProviderProps) {
   const [theme, setTheme] = useState<Theme>("light");

   useEffect(() => {
      const root = document.documentElement;
      if (theme === "dark") {
         root.classList.add("dark");
      } else {
         root.classList.remove("dark");
      }
   }, [theme]);

   const toggleTheme = () =>
      setTheme((prev) => (prev === "light" ? "dark" : "light"));

   return (
      <ThemeContext.Provider value={{ theme, toggleTheme }}>
         {children}
      </ThemeContext.Provider>
   );
}
