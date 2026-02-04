import type { CSSProperties } from "react";
import "../styles/switch.css";
import { classNames } from "../../utils/classNames";

type SwitchProps = {
    size?: "sm" | "md" | "lg";
    style?: CSSProperties;
    className?: string;
    checked: boolean;
    onChange: () => void;
    disabled?: boolean;
};

export function Switch({
    size = "sm",
    style,
    className,
    checked,
    onChange,
    disabled = false
}: SwitchProps) {
    return (
        <label
            className={classNames("switch", `switch-${size}`, disabled && "disabled", className)}
            style={style}
        >
            <input
                type="checkbox"
                checked={checked}
                onChange={onChange}
                disabled={disabled}
            />
            <span className="switch-slider" />
        </label>
    );
}
