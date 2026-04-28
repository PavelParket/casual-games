import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useLocation } from "react-router-dom";
import type { AppDispatch, RootState } from "../../store/store";
import { useEffect, useState, useMemo } from "react";
import { Box, Button, Card, Container, Icon, Modal, ComboBox, Stack, FormField, Textfield, Typography, useThemedIcon, Grid, CheckBox, Divider } from "../../ui";
import { ROOM_TYPE_HANDLERS, ROOM_TYPE_LABELS, type Room, type RoomType } from "../../models/Room";
import { validateRoomName } from "../../utils/SecurityUtils";
import { clearError, createRoom, getRooms } from "../../store/slices/RoomSlice";
import { useSliceErrorToast } from "../../hooks/useSliceErrorToast";

type SortOption = 'newest' | 'alphabet';
type SortDirection = 'asc' | 'desc';

const AVAILABLE_ROOM_TYPES = Object.keys(ROOM_TYPE_LABELS) as RoomType[];

function CreateRoomCard({ onClick }: { onClick: () => void }) {
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

export default function Rooms() {
    const navigate = useNavigate();
    const location = useLocation();
    const dispatch = useDispatch<AppDispatch>();

    const authentication = useSelector((state: RootState) => state.auth);
    const { rooms } = useSelector((state: RootState) => state.rooms);

    const [isCreateRoomModalOpen, setIsCreateRoomModalOpen] = useState<boolean>(false);
    const [roomName, setRoomName] = useState<string>("");
    const [roomType, setRoomType] = useState<RoomType>();

    const [searchQuery, setSearchQuery] = useState("");
    const [sortOption, setSortOption] = useState<SortOption>('newest');
    const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
    const [selectedTypes, setSelectedTypes] = useState<RoomType[]>([]);
    const [isTypesExpanded, setIsTypesExpanded] = useState<boolean>(true);

    const { getIcon, getInverseIcon } = useThemedIcon();
    const [validationError, setValidationError] = useState<string>("");

    useSliceErrorToast((state: RootState) => state.rooms.errors, clearError);

    useEffect(() => {
        dispatch(getRooms());
    }, [dispatch]);

    useEffect(() => {
        const state = location.state as { preselectRoomType?: RoomType } | null;

        if (state?.preselectRoomType) {
            setSelectedTypes([state.preselectRoomType]);

            setIsTypesExpanded(true);

            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [location.pathname, location.state, navigate]);

    const groupedRooms = useMemo(() => {
        if (!rooms) return [];

        let result = rooms.map((room, index) => ({ room, index }));

        if (searchQuery) {
            const q = searchQuery.toLowerCase();
            result = result.filter(r => r.room.name.toLowerCase().includes(q));
        }

        const activeTypes = selectedTypes.length > 0 ? [...selectedTypes] : [...AVAILABLE_ROOM_TYPES];
        result = result.filter(r => activeTypes.includes(r.room.type));

        const groups: Record<string, typeof result> = {};

        activeTypes.forEach(type => {
            groups[type] = [];
        });

        result.forEach(r => {
            if (groups[r.room.type]) {
                groups[r.room.type].push(r);
            }
        });

        return activeTypes
            .map(type => {
                const groupRooms = groups[type];

                groupRooms.sort((a, b) => {
                    let cmp = 0;
                    if (sortOption === 'alphabet') {
                        cmp = a.room.name.localeCompare(b.room.name, 'en', { numeric: true });
                    } else {
                        cmp = a.index - b.index;
                    }
                    return sortDirection === 'asc' ? cmp : -cmp;
                });

                return {
                    type: type as RoomType,
                    rooms: groupRooms.map(r => r.room)
                };
            });
    }, [rooms, searchQuery, selectedTypes, sortOption, sortDirection]);

    const handleSortClick = (option: SortOption) => {
        if (sortOption === option) {
            setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
        } else {
            setSortOption(option);
            setSortDirection('asc');
        }
    };

    const handleToggleType = (type: RoomType) => {
        setSelectedTypes(prev =>
            prev.includes(type) ? prev.filter(t => t !== type) : [...prev, type]
        );
    };

    const handleRoomNameChange = (value: string): void => {
        const validatedRoomName = validateRoomName(value);
        setRoomName(validatedRoomName);
        setValidationError("");
    };

    const handleJoinRoom = (room: Room) => {
        if (!authentication.isAuthenticated || !room) return;
        navigateToRoom(room);
    };

    const handleOpenCreateModal = (presetType?: RoomType) => {
        setRoomType(presetType);
        setRoomName("");
        setValidationError("");
        setIsCreateRoomModalOpen(true);
    };

    const handleCreateRoom = async () => {
        setValidationError("");

        if (!authentication.isAuthenticated) {
            setValidationError("You must be authenticated!");
            return;
        }

        const validatedName = validateRoomName(roomName);

        if (!validatedName || validatedName.length < 3) {
            setValidationError("Room name must be at least 3 characters long!");
            return;
        }

        if (!roomType) {
            setValidationError("Select a room type!");
            return;
        }

        const roomResponse = await dispatch(createRoom({ roomName: validatedName, roomType })).unwrap().catch(() => null);
        if (!roomResponse) return;

        setRoomName("");
        setRoomType(undefined);
        setIsCreateRoomModalOpen(false);
        navigateToRoom(roomResponse);
    };

    const navigateToRoom = (room: Room) => {
        navigate(`/room/${ROOM_TYPE_HANDLERS[room.type]}/${encodeURIComponent(room.name)}/${room.id}`);
    };

    return (
        <Box style={{
            height: "calc(100vh - 60px - 50px)",
            margin: "0 10rem",
            padding: "0 1rem",
            background: "var(--color-bg-glass)",
            backdropFilter: "blur(2px)",
            borderRadius: "var(--radius-md)",
            boxShadow: "var(--shadow-lg)"
        }}>
            <Container>

                <Box style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    padding: "1.5rem 0 1rem",
                }}>
                    <Typography variant="h2">Rooms</Typography>
                    <Button variant="solid" onClick={() => handleOpenCreateModal()} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                        <Icon src={getInverseIcon("add")} alt="add" size={17} />
                        <Typography variant="body" inverse style={{ fontSize: "16px", fontWeight: 500 }}>
                            Create Room
                        </Typography>
                    </Button>
                </Box>

                <Divider style={{ margin: "1rem 0" }} />

                <Grid
                    columns="280px 1fr"
                    gap="2rem"
                    style={{
                        height: "calc(100vh - 60px - 50px - 8.5rem)",
                        alignItems: "start"
                    }}
                >

                    <Stack
                        gap="1.5rem"
                        style={{
                            height: "100%",
                            overflowY: "auto",
                            paddingRight: "0.5rem",
                            borderRadius: "var(--radius-md)",
                            boxShadow: "var(--shadow-md)",
                            backdropFilter: "blur(2px)",
                            padding: "1.25rem 1.5rem",
                        }}
                    >
                        <FormField
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            placeholder="Search by name"
                            rounded
                            endAdornmentSrc={getIcon("search")}
                            endAdornmentAlt="search"
                            style={{ flexShrink: 0 }}
                        />

                        <Box>
                            <Stack
                                direction="row"
                                gap="4px"
                                style={{
                                    width: "100%",
                                    background: "var(--color-bg-glass)",
                                    borderRadius: "var(--radius-md)",
                                    padding: "4px",
                                    border: "1px solid var(--color-border)",
                                }}
                            >
                                <Button
                                    variant={sortOption === 'newest' ? 'solid' : 'ghost'}
                                    onClick={() => handleSortClick('newest')}
                                    style={{ flex: 1, padding: "0.5rem 0.2rem", boxShadow: 'none', minWidth: 0 }}
                                >
                                    <Stack direction="row" align="center" justify="center" gap="4px" style={{ width: "100%" }}>
                                        <Typography
                                            variant="body"
                                            style={{
                                                fontSize: "0.85rem",
                                                fontWeight: sortOption === 'newest' ? 600 : 500,
                                                color: 'inherit',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis'
                                            }}
                                        >
                                            New
                                        </Typography>
                                        <Icon
                                            src={getIcon('expandMore')}
                                            alt="sort dir"
                                            size={16}
                                            style={{
                                                transform: sortOption === 'newest' && sortDirection === 'asc' ? 'rotate(180deg)' : 'none',
                                                opacity: sortOption === 'newest' ? 0.8 : 0,
                                                transition: 'transform 0.2s ease'
                                            }}
                                        />
                                    </Stack>
                                </Button>

                                <Button
                                    variant={sortOption === 'alphabet' ? 'solid' : 'ghost'}
                                    onClick={() => handleSortClick('alphabet')}
                                    style={{ flex: 1, padding: "0.5rem 0.2rem", boxShadow: 'none', minWidth: 0 }}
                                >
                                    <Stack direction="row" align="center" justify="center" gap="4px" style={{ width: "100%" }}>
                                        <Typography
                                            variant="body"
                                            style={{
                                                fontSize: "0.85rem",
                                                fontWeight: sortOption === 'alphabet' ? 600 : 500,
                                                color: 'inherit',
                                                overflow: 'hidden',
                                                textOverflow: 'ellipsis'
                                            }}
                                        >
                                            A-Z
                                        </Typography>
                                        <Icon
                                            src={getIcon('expandMore')}
                                            alt="sort dir"
                                            size={16}
                                            style={{
                                                transform: sortOption === 'alphabet' && sortDirection === 'asc' ? 'rotate(180deg)' : 'none',
                                                opacity: sortOption === 'alphabet' ? 0.8 : 0,
                                                transition: 'transform 0.2s ease'
                                            }}
                                        />
                                    </Stack>
                                </Button>
                            </Stack>
                        </Box>

                        <Divider />

                        <Stack gap="0.5rem" style={{ paddingBottom: "1.5rem", flexShrink: 0 }}>
                            <Button
                                variant="ghost"
                                onClick={() => setIsTypesExpanded(!isTypesExpanded)}
                                style={{ padding: "0.5rem", boxShadow: "none" }}
                            >
                                <Stack direction="row" justify="space-between" align="center" style={{ width: "100%" }}>
                                    <Typography variant="body" style={{ fontWeight: 600, fontSize: "0.95rem", color: "inherit" }}>
                                        Game Types
                                    </Typography>
                                    <Icon
                                        src={getInverseIcon("expandMore")}
                                        size={16}
                                        style={{ transform: isTypesExpanded ? "rotate(180deg)" : "none", transition: "transform 0.2s" }}
                                    />
                                </Stack>
                            </Button>

                            {isTypesExpanded && (
                                <Stack gap="0.5rem" style={{ padding: "0.25rem 0.5rem" }}>
                                    {AVAILABLE_ROOM_TYPES.map(t => (
                                        <Stack key={t} direction="row" justify="space-between" align="center">
                                            <Typography variant="body" style={{ fontSize: "0.85rem" }}>{ROOM_TYPE_LABELS[t]}</Typography>
                                            <CheckBox
                                                variant="outline"
                                                checked={selectedTypes.includes(t)}
                                                onChange={() => handleToggleType(t)}
                                            />
                                        </Stack>
                                    ))}
                                </Stack>
                            )}
                        </Stack>
                    </Stack>

                    <Box style={{
                        height: "100%",
                        overflowY: "auto",
                        paddingRight: "0.5rem",
                        display: "flex",
                        flexDirection: "column",
                        gap: "2.5rem",
                        padding: "1.25rem 1.5rem",
                    }}>
                        {!rooms || rooms.length === 0 ? (
                            <Box style={{ textAlign: "center", padding: "3rem" }}>
                                <Typography variant="body" style={{ opacity: 0.7 }}>No rooms available. Be the first to create one!</Typography>
                            </Box>
                        ) : (
                            groupedRooms.map(group => (
                                <Box key={group.type}>
                                    <Box style={{ display: "flex", alignItems: "center", gap: "1rem", marginBottom: "1rem" }}>
                                        <Typography variant="h2">{ROOM_TYPE_LABELS[group.type]}</Typography>
                                        <Box style={{ flex: 1, height: "1px", background: "var(--color-border)" }} />
                                    </Box>

                                    <Grid columns="repeat(auto-fill, minmax(180px, 1fr))" gap="1.5rem" justifyItems="center" style={{ justifyContent: "start" }}>
                                        {group.rooms.map(room => (
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
                                                <Typography variant="h3" style={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} title={room.name}>
                                                    {room.name}
                                                </Typography>
                                                <Typography variant="caption" style={{ marginTop: "auto" }}>
                                                    {ROOM_TYPE_LABELS[room.type] || room.type}
                                                </Typography>
                                                <Typography variant="caption" style={{ fontWeight: 600 }}>
                                                    {room.participantCount} players
                                                </Typography>
                                                <Box style={{ display: "flex", gap: "0.5rem", justifyContent: "center" }}>
                                                    <Button variant="outline" onClick={() => handleJoinRoom(room)} style={{ flex: 1, padding: "0.5rem" }}>Join</Button>
                                                </Box>
                                            </Card>
                                        ))}

                                        <CreateRoomCard onClick={() => handleOpenCreateModal(group.type)} />
                                    </Grid>
                                </Box>
                            ))
                        )}
                    </Box>

                </Grid>
            </Container>


            <Modal isOpen={isCreateRoomModalOpen} onClose={() => { setIsCreateRoomModalOpen(false); setValidationError(""); }} title="Create Room">
                <Box style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                    <Textfield value={roomName} onChange={handleRoomNameChange} placeholder="Room name" />
                    <ComboBox
                        options={AVAILABLE_ROOM_TYPES.map((type) => ({
                            value: type,
                            label: ROOM_TYPE_LABELS[type],
                        }))}
                        value={roomType}
                        onValueChange={setRoomType}
                        placeholder="Choose room type"
                        searchable
                    />

                    {validationError && (
                        <Typography variant="caption" style={{ color: "red", textAlign: "center" }}>
                            {validationError}
                        </Typography>
                    )}

                    <Button variant="solid" onClick={handleCreateRoom}>Create</Button>
                </Box>
            </Modal>
        </Box>
    );
}
