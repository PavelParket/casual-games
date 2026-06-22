import { useEffect, useState, type HTMLAttributes } from "react";
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

    const [animKey, setAnimKey] = useState(0);

    useEffect(() => {
        if (timeLeft === maxTime) {
            setAnimKey(prev => prev + 1);
        }
    }, [timeLeft, maxTime]);

    const isRunning = timeLeft > 0;

    return (
        <div
            className={classNames("cooldown-timer", className)}
            style={{ width: size, height: size, opacity: isRunning ? 1 : 0.2, ...style }}
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
                    key={animKey}
                    cx={size / 2}
                    cy={size / 2}
                    r={radius}
                    strokeWidth={strokeWidth}
                    strokeDasharray={circumference}
                    className="cooldown-timer-progress"
                    style={{
                        '--circumference': circumference,
                        strokeDashoffset: 0,
                        animation: isRunning ? `cooldown-progress ${maxTime}s linear forwards` : 'none',
                    } as React.CSSProperties}
                />
            </svg>
            <span className="cooldown-timer-text">
                {timeLeft > 0 ? timeLeft : ""}
            </span>
        </div>
    );
}
