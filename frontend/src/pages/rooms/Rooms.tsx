import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import type { AppDispatch, RootState } from "../../store/store";
import { useEffect, useState } from "react";
import { Box, Button, Card, Container, Icon, Modal, ComboBox, Textfield, Typography, useThemedIcon } from "../../ui";
import { ROOM_TYPE_HANDLERS, ROOM_TYPE_LABELS, type Room, type RoomRequest, type RoomType } from "../../models/Room";
import { validateRoomName } from "../../utils/SecurityUtils";
import { createRoom, getRooms, getTypes } from "../../store/slices/RoomSlice";

export default function Rooms() {
    const navigate = useNavigate();
    const dispatch = useDispatch<AppDispatch>();

    const authentication = useSelector((state: RootState) => state.auth);
    const { rooms, roomTypes } = useSelector((state: RootState) => state.rooms);

    const [isCreateRoomModalOpen, setIsCreateRoomModalOpen] = useState<boolean>(false);
    const [roomName, setRoomName] = useState<string>("");
    const [roomType, setRoomType] = useState<RoomType>();

    const [roomInfo, setRoomInfo] = useState<Room>();
    const [isRoomInfoModalOpen, setIsRoomInfoModalOpen] = useState<boolean>(false);

    const { getIcon, getInverseIcon } = useThemedIcon();

    const [hovered, setHovered] = useState(false);
    const [pressed, setPressed] = useState(false);

    const [error, setError] = useState<string>("");

    useEffect(() => {
        dispatch(getRooms());
        dispatch(getTypes());
    }, [dispatch]);

    const handleRoomNameChange = (value: string): void => {
        const validatedRoomName = validateRoomName(value);
        setRoomName(validatedRoomName);
        setError("");
    };

    const handleInfo = (room: Room): void => {
        setRoomInfo(room);
        setIsRoomInfoModalOpen(true);
    }

    const handleJoinRoom = (room: Room) => {
        if (!authentication.isAuthenticated || !room) {
            return;
        }

        navigateToRoom(room);
    };

    const handleCreateRoom = async () => {
        setError("");

        if (!authentication.isAuthenticated) {
            setError("You must be authenticated!");
            return;
        }

        if (!roomName) {
            setError("Room name must not be empty!")
            return;
        }

        const validatedName = validateRoomName(roomName);

        if (!validatedName) {
            setError("Room name is required and must contain only letters, numbers, spaces, hyphens and underscores!");
            return;
        }

        if (validatedName.length < 3) {
            setError("Room name must be at least 3 characters long!");
            return;
        }

        if (!roomType) {
            setError("Select a room type!");
            return;
        }

        const roomRequest: RoomRequest = {
            roomName: validatedName,
            roomType: roomType
        };

        try {
            const roomResponse = await dispatch(createRoom(roomRequest)).unwrap();

            setRoomName("");
            setRoomType(undefined);
            setError("");
            setIsCreateRoomModalOpen(false);
            navigateToRoom(roomResponse);
        } catch (error) {
            setError((error as string) || "Failed to create room");
            return;
        }
    };

    const navigateToRoom = (room: Room) => {
        if (!room) {
            setError("Room not found");
            return;
        }

        navigate(`/room/${ROOM_TYPE_HANDLERS[room.type]}/${encodeURIComponent(room.name)}/${room.id}`);
    };

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
                            onClick={() => setIsCreateRoomModalOpen(true)}
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
                        {!rooms || rooms.length === 0 && (
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
                        {rooms && rooms.map((room: Room) => (
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

                        {rooms && (rooms.length > 0) &&
                            <Card
                                onClick={() => setIsCreateRoomModalOpen(true)}
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
                isOpen={isRoomInfoModalOpen}
                onClose={() => {
                    setRoomInfo(undefined);
                    setIsRoomInfoModalOpen(false);
                }}
                title="Room Info"
            >
                <Typography variant="h3">{roomInfo?.name}</Typography>
                <Typography variant="h3">Type: {roomInfo?.type}</Typography>
                <Typography variant="body">Current player count: {roomInfo?.participantCount}</Typography>
            </Modal>

            <Modal
                isOpen={isCreateRoomModalOpen}
                onClose={() => {
                    setIsCreateRoomModalOpen(false);
                    setError("");
                }}
                title="Create Room"
            >
                <Box style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                    <Textfield
                        value={roomName}
                        onChange={handleRoomNameChange}
                        placeholder="Room name"
                    />
                    <ComboBox
                        options={(roomTypes ?? []).map((type) => ({
                            value: type,
                            label: ROOM_TYPE_LABELS[type],
                        }))}
                        value={roomType}
                        onValueChange={setRoomType}
                        placeholder="Choose room type"
                        searchable
                    />

                    {error && (
                        <Typography variant="caption" style={{ color: "red", textAlign: "center" }}>
                            {error}
                        </Typography>
                    )}

                    <Button variant="solid" onClick={handleCreateRoom}>Create</Button>
                </Box>
            </Modal>
        </>
    );
}
