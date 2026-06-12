import { useState, useEffect, type ComponentProps } from "react";
import { FormField } from "./FormField";
import { useThemedIcon } from "../../hooks/useThemedIcon";
import { Icon } from "../common/Icon";

type PasswordFieldProps = Omit<ComponentProps<typeof FormField>, "type" | "endAdornment" | "endAdornmentSrc" | "interactiveEndAdornment">;

export function PasswordField(props: PasswordFieldProps) {
    const [showPassword, setShowPassword] = useState(false);
    const { getIcon } = useThemedIcon();

    const hasValue = Boolean(props.value);

    useEffect(() => {
        if (!hasValue) {
            setShowPassword(false);
        }
    }, [hasValue]);

    const toggleButton = hasValue ? (
        <button
            type="button"
            tabIndex={-1}
            className="password-toggle-btn"
            onClick={() => setShowPassword(!showPassword)}
            title={showPassword ? "Hide password" : "Show password"}
        >
            <Icon src={showPassword ? getIcon("hide") : getIcon("show")} size={20} />
        </button>
    ) : null;

    return (
        <FormField
            {...props}
            type={showPassword ? "text" : "password"}
            endAdornment={toggleButton}
            interactiveEndAdornment={true}
        />
    );
}
