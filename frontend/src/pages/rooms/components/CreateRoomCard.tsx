import { useState } from "react";
import { Card, Icon, useThemedIcon } from "../../../ui";

interface CreateRoomCardProps {
    onClick: () => void;
}

export function CreateRoomCard({ onClick }: CreateRoomCardProps) {
    const [hovered, setHovered] = useState(false);
    const [pressed, setPressed] = useState(false);
    const { getIcon } = useThemedIcon();

    return (
        <Card
            onClick={onClick}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => { setHovered(false); setPressed(false); }}
            onMouseDown={() => setPressed(true)}
            onMouseUp={() => setPressed(false)}
            style={{
                width: "180px",
                height: "180px",
                textAlign: "center",
                padding: "20px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
                transition: "transform 0.15s ease, box-shadow 0.15s ease",
                background: "var(--color-bg-glass)",
                borderRadius: "var(--radius-md)",
                boxShadow: hovered ? "var(--shadow-lg)" : "var(--shadow-md)",
                transform: pressed ? "scale(0.95)" : hovered ? "scale(1.05)" : "scale(1)",
            }}
        >
            <Icon src={getIcon("add")} alt="add" size={50} />
        </Card>
    );
}
