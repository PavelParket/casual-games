import { useCallback } from 'react';
import { Icons } from '../../assets/icons';
import { useTheme } from '../theme/useTheme';

export const useThemedIcon = () => {
   const { theme } = useTheme();

   const getIcon = useCallback((iconName: keyof typeof Icons.light) => {
      return theme === 'dark' ? Icons.light[iconName] : Icons.dark[iconName];
   }, [theme]);

   const getInverseIcon = useCallback((iconName: keyof typeof Icons.light) => {
      return theme === 'dark' ? Icons.dark[iconName] : Icons.light[iconName];
   }, [theme]);

   return { getIcon, getInverseIcon };
};