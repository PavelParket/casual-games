import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import type { AppDispatch, RootState } from "../../store/store";
import { useEffect, useState } from "react";
import { Box, Button, Card, Container, Icon, Modal, Select, Textfield, Typography, useThemedIcon } from "../../ui";
import { fetchRooms, fetchTypes, findTypeByRoomType, type Room } from "../../store/slices/RoomSlice";

export default function Rooms() {
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();

    const { isAuthenticated } = useSelector((state: RootState) => state.auth);
    const { rooms, types } = useSelector((state: RootState) => state.rooms);

    const [createModalOpen, setCreateModalOpen] = useState(false);
    const [newRoomName, setNewRoomName] = useState("");
    const [selectedRoomType, setSelectedRoomType] = useState<string>("");
    const [roomInfo, setRoomInfo] = useState<Room | null>(null);

    const { getIcon, getInverseIcon } = useThemedIcon();

    const [hovered, setHovered] = useState(false);
    const [pressed, setPressed] = useState(false);

    useEffect(() => {
        dispatch(fetchRooms());
        dispatch(fetchTypes());
    }, [dispatch]);

    const goToRoom = (
        roomName: string,
        roomType: string,
        handlerUrl: string,
        action: "join" | "create"
    ) => {
        const lastRoom = JSON.stringify({ roomName, roomType, handlerUrl });

        localStorage.setItem("lastRoom", lastRoom.toString());
        localStorage.setItem("action", "join" === action ? "join" : "create");

        navigate(`/room/game/${roomName}`, {
            state: { roomType, handlerUrl },
        });
    };

    const handleJoinRoom = (room: Room) => {
        if (!isAuthenticated) {
            return;
        }

        const roomType = findTypeByRoomType(types, room.type);

        if (!roomType) {
            return;
        }

        goToRoom(room.name, roomType.name, roomType.handlerUrl, "join");
    };

    const handleCreateRoom = () => {
        if (!isAuthenticated || !newRoomName.trim() || !selectedRoomType) {
            return;
        }

        const roomType = findTypeByRoomType(types, selectedRoomType);
        if (!roomType) {
            return;
        }

        setNewRoomName("");
        setSelectedRoomType("");
        setCreateModalOpen(false);
        goToRoom(newRoomName, selectedRoomType, roomType.handlerUrl, "create");
    };

    function handleInfo(room: Room): void {
        setRoomInfo(room);
    }

    return (
        <>
            <Box style={{
                minHeight: "calc(100vh - 60px - 50px)",
                margin: "0 10rem",
                padding: "0 1rem",
                background: "var(--color-bg-glass)",
                backdropFilter: "blur(2px)",
                borderRadius: "var(--radius-md)",
                boxShadow: "var(--shadow-lg)"
            }}>
                <Container>
                    <Box style={{
                        padding: "2rem 1rem 0 1rem",
                        marginBottom: "2rem",
                        display: "flex",
                        flexDirection: "row",
                        alignItems: "center",
                        justifyContent: "space-between"
                    }}>
                        <Typography variant="h2" style={{ textAlign: "center" }}>
                            Rooms
                        </Typography>

                        <Button
                            variant="solid"
                            onClick={() => setCreateModalOpen(true)}
                            style={{
                                display: "flex",
                                alignItems: "center",
                                gap: "8px"
                            }}
                        >
                            <Icon src={getInverseIcon("add")} alt="add" size={17} />
                            <Box style={{ textAlign: "center" }}>
                                <Typography variant="body" inverse style={{ fontSize: "16px", fontWeight: 500 }}>
                                    Create Room
                                </Typography>
                            </Box>
                        </Button>
                    </Box>

                    <Box style={{ textAlign: "center" }}>
                        {rooms.length === 0 && (
                            <Typography>No rooms available. Try to create something!</Typography>
                        )}
                    </Box>

                    <Box style={{
                        paddingBottom: "1rem",
                        display: "grid",
                        gridTemplateColumns: "repeat(4, 1fr)",
                        columnGap: "16px",
                        rowGap: "3rem",
                        justifyItems: "center",
                    }}>
                        {rooms.map((room: Room) => (
                            <Card
                                key={room.id}
                                style={{
                                    width: "180px",
                                    height: "180px",
                                    textAlign: "center",
                                    padding: "20px",
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: "10px",
                                }}
                            >
                                <Typography variant="h3">{room.name}</Typography>
                                <Button variant="outline" onClick={() => handleJoinRoom(room)}>Join</Button>
                                <Button variant="ghost" onClick={() => handleInfo(room)}>Info</Button>
                            </Card>
                        ))}

                        {rooms.length > 0 && <Card
                            onClick={() => setCreateModalOpen(true)}
                            onMouseEnter={() => setHovered(true)}
                            onMouseLeave={() => {
                                setHovered(false);
                                setPressed(false);
                            }}
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
                                boxShadow: hovered
                                    ? "var(--shadow-lg)"
                                    : "var(--shadow-md)",
                                transform: pressed
                                    ? "scale(0.95)"
                                    : hovered
                                        ? "scale(1.05)"
                                        : "scale(1)",
                            }}
                        >
                            <Icon src={getIcon("add")} alt="add" size={50} />
                        </Card>}
                    </Box>
                </Container>
            </Box>

            <Modal
                isOpen={!!roomInfo}
                onClose={() => {
                    setRoomInfo(null);
                }}
                title="Room Info"
            >
                <Typography variant="h3">{roomInfo?.name}</Typography>
                <Typography variant="h3">Type: {roomInfo?.type}</Typography>
                <Typography variant="body">Current player count: {roomInfo?.participantCount}</Typography>
            </Modal>

            <Modal isOpen={createModalOpen} onClose={() => setCreateModalOpen(false)} title="Create Room">
                <Box style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                    <Textfield
                        value={newRoomName}
                        onChange={setNewRoomName}
                        placeholder="Room name"
                    />
                    <Select
                        options={types.map((type) => ({
                            value: type.name,
                            label: type.label,
                        }))}
                        value={selectedRoomType}
                        onChange={setSelectedRoomType}
                        placeholder="Choose room type"
                        searchable
                    />
                    <Button variant="solid" onClick={handleCreateRoom}>Create</Button>
                </Box>
            </Modal>
        </>
    );
}
