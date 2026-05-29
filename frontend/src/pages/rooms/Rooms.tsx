import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useLocation } from "react-router-dom";
import type { AppDispatch, RootState } from "../../store/store";
import { useEffect, useState, useMemo } from "react";
import { Box, Button, Card, Container, Icon, Modal, ComboBox, Stack, FormField, Textfield, Typography, useThemedIcon, Grid, CheckBox, Divider } from "../../ui";
import { ROOM_TYPE_HANDLERS, ROOM_TYPE_LABELS, type Room, type RoomType, type RoomSortField, type SortDirection, type RoomFilterRequest } from "../../models/Room";
import { validateRoomName } from "../../utils/SecurityUtils";
import { clearError, createRoom, searchRooms } from "../../store/slices/RoomSlice";
import { useSliceErrorToast } from "../../hooks/useSliceErrorToast";
import { useSystemToastContext } from "../../providers/SystemToastContext";

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
    const { groupedRooms: backendGroupedRooms } = useSelector((state: RootState) => state.rooms);

    const [isCreateRoomModalOpen, setIsCreateRoomModalOpen] = useState<boolean>(false);
    const [roomName, setRoomName] = useState<string>("");
    const [roomType, setRoomType] = useState<RoomType>();

    const [searchQuery, setSearchQuery] = useState("");
    const [sortOption, setSortOption] = useState<RoomSortField>('CREATED_AT');
    const [sortDirection, setSortDirection] = useState<SortDirection>('DESC');
    const [selectedTypes, setSelectedTypes] = useState<RoomType[]>([]);

    const [appliedFilters, setAppliedFilters] = useState<RoomFilterRequest>({
        name: "",
        types: [],
        sortField: "CREATED_AT",
        sortDirection: "DESC"
    });

    const [isTypesExpanded, setIsTypesExpanded] = useState<boolean>(true);
    const [isRefreshing, setIsRefreshing] = useState(false);

    const { getIcon, getInverseIcon } = useThemedIcon();
    const { showSystemToast } = useSystemToastContext();

    const [validationError, setValidationError] = useState<string>("");

    useSliceErrorToast((state: RootState) => state.rooms.errors, clearError);

    useEffect(() => {
        dispatch(searchRooms(appliedFilters));

        const intervalId = setInterval(() => {
            if (!document.hidden) {
                dispatch(searchRooms(appliedFilters));
            }
        }, 60000);

        return () => clearInterval(intervalId);
    }, [dispatch, appliedFilters]);

    useEffect(() => {
        const state = location.state as { preselectRoomType?: RoomType } | null;

        if (state?.preselectRoomType) {
            const types = [state.preselectRoomType];
            setSelectedTypes(types);

            setAppliedFilters(prev => ({ ...prev, types }));

            setIsTypesExpanded(true);
            navigate(location.pathname, { replace: true, state: {} });
        }
    }, [location.pathname, location.state, navigate]);

    const handleResetFilters = () => {
        setSearchQuery("");
        setSelectedTypes([]);
        setSortOption('CREATED_AT');
        setSortDirection('DESC');

        setAppliedFilters({
            name: "",
            types: [],
            sortField: "CREATED_AT",
            sortDirection: "DESC"
        });
    };
    const hasUnappliedFilters = useMemo(() => {
        if (searchQuery !== (appliedFilters.name || "")) return true;
        if (sortOption !== appliedFilters.sortField) return true;
        if (sortDirection !== appliedFilters.sortDirection) return true;

        const appliedSet = new Set(appliedFilters.types || []);
        if (selectedTypes.length !== appliedSet.size) return true;
        return selectedTypes.some(t => !appliedSet.has(t));
    }, [searchQuery, sortOption, sortDirection, selectedTypes, appliedFilters]);

    const handleRefresh = async () => {
        setIsRefreshing(true);
        try {
            await dispatch(searchRooms(appliedFilters)).unwrap();
            showSystemToast("Rooms list updated successfully", "system-info");
        } catch { /* empty */ } finally {
            setIsRefreshing(false);
        }
    };

    const handleApplyFilters = () => {
        setAppliedFilters({
            name: searchQuery,
            types: selectedTypes,
            sortField: sortOption,
            sortDirection: sortDirection
        });
    };

    const displayGroups = useMemo(() => {
        if (!backendGroupedRooms) return [];

        const typesToRender = appliedFilters.types && appliedFilters.types.length > 0
            ? appliedFilters.types
            : AVAILABLE_ROOM_TYPES;

        return [...typesToRender]
            .sort((a, b) => ROOM_TYPE_LABELS[a].localeCompare(ROOM_TYPE_LABELS[b]))
            .map(type => ({
                type,
                rooms: backendGroupedRooms[type] || []
            }));
    }, [backendGroupedRooms, appliedFilters.types]);

    const handleSortClick = (field: RoomSortField) => {
        if (sortOption === field) {
            setSortDirection(prev => prev === 'ASC' ? 'DESC' : 'ASC');
        } else {
            setSortOption(field);
            setSortDirection(field === 'CREATED_AT' ? 'DESC' : 'ASC');
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

        try {
            const roomResponse = await dispatch(createRoom({ roomName: validatedName, roomType })).unwrap();
            setRoomName("");
            setRoomType(undefined);
            setIsCreateRoomModalOpen(false);
            navigateToRoom(roomResponse);
        } catch { /* empty */ }
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
            boxShadow: "var(--shadow-lg)",
            overflow: "hidden"
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
                        className="custom-scrollbar"
                        gap="1.5rem"
                        style={{
                            height: "auto",
                            overflowY: "auto",
                            paddingRight: "0.5rem",
                            borderRadius: "var(--radius-md)",
                            boxShadow: "var(--shadow-md)",
                            backdropFilter: "blur(2px)",
                            padding: "1.25rem 1.5rem",
                        }}
                    >
                        <Stack direction="row" gap="0.5rem" align="stretch" style={{ flexShrink: 0 }}>
                            <FormField
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                placeholder="Search by name"
                                rounded
                                endAdornmentSrc={getIcon("search")}
                                endAdornmentAlt="search"
                                style={{ flex: 1, minWidth: 0 }}
                            />

                            <Button
                                variant="ghost"
                                onClick={handleRefresh}
                                disabled={isRefreshing}
                                style={{ padding: "0.4rem" }}
                            >
                                <Icon
                                    src={getIcon("refresh")}
                                    alt="refresh"
                                    size={20}
                                />
                            </Button>
                        </Stack>

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
                                    variant={sortOption === 'CREATED_AT' ? 'solid' : 'ghost'}
                                    onClick={() => handleSortClick('CREATED_AT')}
                                    style={{ flex: 1, padding: "0.5rem 0.2rem", boxShadow: 'none', minWidth: 0 }}
                                >
                                    <Stack direction="row" align="center" justify="center" gap="4px" style={{ width: "100%" }}>
                                        <Typography
                                            variant="body"
                                            style={{
                                                fontSize: "0.85rem",
                                                fontWeight: sortOption === 'CREATED_AT' ? 600 : 500,
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
                                                transform: sortOption === 'CREATED_AT' && sortDirection === 'ASC' ? 'rotate(180deg)' : 'none',
                                                opacity: sortOption === 'CREATED_AT' ? 0.8 : 0,
                                                transition: 'transform 0.2s ease'
                                            }}
                                        />
                                    </Stack>
                                </Button>

                                <Button
                                    variant={sortOption === 'NAME' ? 'solid' : 'ghost'}
                                    onClick={() => handleSortClick('NAME')}
                                    style={{ flex: 1, padding: "0.5rem 0.2rem", boxShadow: 'none', minWidth: 0 }}
                                >
                                    <Stack direction="row" align="center" justify="center" gap="4px" style={{ width: "100%" }}>
                                        <Typography
                                            variant="body"
                                            style={{
                                                fontSize: "0.85rem",
                                                fontWeight: sortOption === 'NAME' ? 600 : 500,
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
                                                transform: sortOption === 'NAME' && sortDirection === 'DESC' ? 'rotate(180deg)' : 'none',
                                                opacity: sortOption === 'NAME' ? 0.8 : 0,
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
                            <Button
                                variant="solid"
                                disabled={!hasUnappliedFilters || isRefreshing}
                                onClick={handleApplyFilters}
                                style={{
                                    marginTop: "0.5rem",
                                    padding: "0.4rem",
                                    fontSize: "0.85rem"
                                }}
                            >
                                Apply Filters
                            </Button>
                        </Stack>
                    </Stack>

                    <Box
                        className="custom-scrollbar"
                        style={{
                            height: "100%",
                            overflowY: "auto",
                            paddingRight: "0.5rem",
                            display: "flex",
                            flexDirection: "column",
                            gap: "2.5rem",
                            padding: "1.25rem 1.5rem",
                            background: "var(--color-bg-glass)",
                            borderRadius: "var(--radius-md)",
                            border: "1px solid var(--color-border)",
                            boxShadow: "var(--shadow-sm)",
                        }}>
                        {((appliedFilters.name?.trim().length || 0) > 0) && displayGroups.every(g => g.rooms.length === 0) ? (
                            <Box style={{ textAlign: "center", padding: "4rem 2rem", display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
                                <Typography variant="h3" style={{ opacity: 0.8 }}>
                                    No rooms found matching your filters.
                                </Typography>
                                <Button variant="outline" onClick={handleResetFilters}>
                                    Reset Filters
                                </Button>
                            </Box>
                        ) : (
                            displayGroups.map(group => (
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
