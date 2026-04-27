import type { CSSProperties, ReactNode } from "react";
import "../styles/checkbox.css";
import { classNames } from "../../utils/classNames";
import { Icon } from "../common/Icon";
import { useThemedIcon } from "../../hooks/useThemedIcon";

type CheckBoxProps = {
    checked: boolean;
    onChange: (checked: boolean) => void;
    variant?: "solid" | "outline" | "ghost";
    disabled?: boolean;
    label?: ReactNode;
    className?: string;
    style?: CSSProperties;
};

export function CheckBox({
    checked,
    onChange,
    variant = "solid",
    disabled = false,
    label,
    className,
    style,
}: CheckBoxProps) {
    const { getIcon, getInverseIcon } = useThemedIcon();

    const checkIconSrc = variant === "solid" ? getInverseIcon("check") : getIcon("check");

    return (
        <label
            className={classNames("checkbox-wrapper", disabled && "disabled", className)}
            style={style}
        >
            <input
                type="checkbox"
                className="checkbox-input"
                checked={checked}
                onChange={(e) => onChange(e.target.checked)}
                disabled={disabled}
            />
            
            <div className={classNames("checkbox-box", `checkbox-${variant}`, checked && "checked")}>
                {checked && (
                    <Icon
                        src={checkIconSrc}
                        alt="check"
                        size={14}
                        className="checkbox-icon"
                    />
                )}
            </div>

            {label && <span className="checkbox-label-text">{label}</span>}
        </label>
    );
}