import { useEffect, useRef, useState, type ReactNode } from "react";
import "../styles/menu.css";
import { classNames } from "../../utils/classNames";

type MenuProps = {
    trigger: ReactNode;
    children: ReactNode;
    className?: string;
    style?: React.CSSProperties;
};

type MenuListProps = {
    children: ReactNode;
    className?: string;
    style?: React.CSSProperties;
};

type MenuItemProps = {
    children: ReactNode;
    onClick?: () => void;
    disabled?: boolean;
    className?: string;
    style?: React.CSSProperties;
};

export function Menu({ trigger, children, className, style }: MenuProps) {
    const [open, setOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);
    const triggerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (ref.current && !ref.current.contains(event.target as Node)) {
                setOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    return (
        <div
            className={classNames("menu-container", className)}
            ref={ref}
            style={style}
        >
            <div
                className={classNames("menu-trigger", open && "open")}
                onClick={() => setOpen(prev => !prev)}
                ref={triggerRef}
                role="button"
                aria-haspopup="menu"
                aria-expanded={open}
            >
                {trigger}
            </div>

            {open && (
                <div
                    className="menu-dropdown"
                    role="menu"
                    aria-orientation="vertical"
                    aria-labelledby={triggerRef.current?.id}
                >
                    {children}
                </div>
            )}
        </div>
    );
}

export function MenuList({ children, className, style }: MenuListProps) {
    return (
        <div
            className={classNames("menu-list", className)}
            style={style}
        >
            {children}
        </div>
    );
}

export function MenuItem({ children, onClick, disabled, className, style }: MenuItemProps) {
    const handleClick = () => {
        if (!disabled && onClick) {
            onClick();
        }
    };

    return (
        <div
            className={classNames("menu-item", disabled && "disabled", className)}
            style={style}
            onClick={handleClick}
            role="menuitem"
            aria-disabled={disabled}
            tabIndex={disabled ? -1 : 0}
        >
            {children}
        </div>
    );
}
