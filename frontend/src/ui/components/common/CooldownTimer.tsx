import type { HTMLAttributes } from "react";
import "../styles/cooldowntimer.css";
import { classNames } from "../../utils/classNames";

type CooldownTimerProps = HTMLAttributes<HTMLDivElement> & {
   timeLeft: number;
   maxTime?: number;
   size?: number;
};

export function CooldownTimer({
   timeLeft,
   maxTime = 5,
   size = 28,
   className,
   style,
   ...rest
}: CooldownTimerProps) {
   const strokeWidth = 3;
   const radius = (size / 2) - (strokeWidth / 2);
   const circumference = 2 * Math.PI * radius;
   
   const strokeDashoffset = circumference + (timeLeft / maxTime) * circumference;

   return (
      <div 
         className={classNames("cooldown-timer", className)} 
         style={{ width: size, height: size, ...style }} 
         {...rest}
      >
         <svg width={size} height={size} className="cooldown-timer-svg">
            <circle 
               cx={size / 2} 
               cy={size / 2} 
               r={radius} 
               strokeWidth={strokeWidth} 
               className="cooldown-timer-bg" 
            />
            <circle
               cx={size / 2}
               cy={size / 2}
               r={radius}
               strokeWidth={strokeWidth}
               strokeDasharray={circumference}
               strokeDashoffset={strokeDashoffset}
               className="cooldown-timer-progress"
               style={{ 
                  transition: timeLeft === 0 || timeLeft === maxTime ? "none" : "stroke-dashoffset 1s linear" 
               }}
            />
         </svg>
         <span className="cooldown-timer-text">
            {timeLeft > 0 ? timeLeft : ""}
         </span>
      </div>
   );
}