import { useEffect, useRef, useState, type ReactNode } from "react";
import "../styles/menu.css";

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

export function Menu({ trigger, children, className = "", style }: MenuProps) {
    const [open, setOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

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
        <div className={`menu-container ${className}`} ref={ref} style={{ ...style }}>
            <div className="menu-trigger" onClick={() => setOpen(prev => !prev)}>
                {trigger}
            </div>

            {open && (
                <div className="menu-dropdown">
                    {children}
                </div>
            )}
        </div>
    );
}

export function MenuList({ children, className = "", style }: MenuListProps) {
    return (
        <div className={`menu-list ${className}`} style={{ ...style }}>
            {children}
        </div>
    );
}

export function MenuItem({ children, onClick, className = "", style }: MenuItemProps) {
    return (
        <div
            className={`menu-item ${className}`}
            style={{ ...style }}
            onClick={onClick}
        >
            {children}
        </div>
    );
}
