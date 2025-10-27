// Light theme icons
import userLight from './user_light.svg';
import apersantLight from './apersant_light.svg';
import safeLockLight from './safe_lock_light.svg';
import addLight from './add_light.svg';
import checkLigth from './check_light.svg';

// Dark theme icons
import userDark from './user_dark.svg';
import apersantDark from './apersant_dark.svg';
import safeLockDark from './safe_lock_dark.svg';
import addDark from './add_dark.svg';
import checkDark from './check_dark.svg';

export const Icons = {
   light: {
      user: userLight,
      apersant: apersantLight,
      safeLock: safeLockLight,
      add: addLight,
      check: checkLigth,
   },
   dark: {
      user: userDark,
      apersant: apersantDark,
      safeLock: safeLockDark,
      add: addDark,
      check: checkDark,
   },
} as const;