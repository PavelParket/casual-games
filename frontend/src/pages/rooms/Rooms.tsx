import "./style/Rooms.css"
import { useDispatch, useSelector } from "react-redux";
import { useNavigate, useLocation } from "react-router-dom";
import type { AppDispatch, RootState } from "../../store/store";
import { useEffect, useState, useMemo } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Box, Button, Card, Container, Icon, Modal, ComboBox, Stack, FormField, Typography, useThemedIcon, Grid, Divider } from "../../ui";
import { CreateRoomCard } from "./components/CreateRoomCard";
import { RoomsFilterPanel } from "./components/RoomsFilterPanel";
import { ROOM_TYPE_HANDLERS, ROOM_TYPE_LABELS, type Room, type RoomType, type RoomSortField, type SortDirection, type RoomFilterRequest } from "../../models/Room";
import { validateRoomName } from "../../utils/SecurityUtils";
import { clearError, createRoom, searchRooms } from "../../store/slices/RoomSlice";
import { useSliceErrorToast } from "../../hooks/useSliceErrorToast";
import { useSystemToastContext } from "../../hooks/useSystemToastContext";

const AVAILABLE_ROOM_TYPES = Object.keys(ROOM_TYPE_LABELS) as RoomType[];

export default function Rooms() {
    const navigate = useNavigate();
    const location = useLocation();
    const dispatch = useDispatch<AppDispatch>();

    const authentication = useSelector((state: RootState) => state.auth);
    const { groupedRooms: backendGroupedRooms } = useSelector((state: RootState) => state.rooms);

    const [isCreateRoomModalOpen, setIsCreateRoomModalOpen] = useState<boolean>(false);
    const [isMobileFiltersOpen, setIsMobileFiltersOpen] = useState<boolean>(false);

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

    const [windowWidth, setWindowWidth] = useState(window.innerWidth);
    useEffect(() => {
        const handleResize = () => setWindowWidth(window.innerWidth);
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);
    const isMobile = windowWidth <= 900;

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

        if (isMobileFiltersOpen) {
            setIsMobileFiltersOpen(false);
        }
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

    const filterPanelProps = {
        searchQuery,
        onSearchChange: setSearchQuery,
        isRefreshing,
        onRefresh: handleRefresh,
        sortOption,
        sortDirection,
        onSortClick: handleSortClick,
        isTypesExpanded,
        onToggleTypesExpanded: () => setIsTypesExpanded(!isTypesExpanded),
        selectedTypes,
        onToggleType: handleToggleType,
        hasUnappliedFilters,
        onApplyFilters: handleApplyFilters,
    };


    return (
        <Box className="page-wrapper rooms-page" style={{ overflow: "hidden" }}>
            <Container>

                <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1.5rem 0 1rem", flexWrap: "wrap", gap: "1rem" }}>

                    <Box style={{ display: "flex", alignItems: "center", justifyContent: "space-between", flex: isMobile ? "1 1 100%" : "0 1 auto" }}>
                        <Typography variant="h2">Rooms</Typography>

                        {isMobile && (
                            <Button variant="ghost" onClick={handleRefresh} disabled={isRefreshing} style={{ padding: "0.4rem" }}>
                                <Icon src={getIcon("refresh")} alt="refresh" size={24} />
                            </Button>
                        )}
                    </Box>

                    <Stack direction="row" gap="1rem" justify="end">
                        {isMobile && (
                            <Button variant="outline" onClick={() => setIsMobileFiltersOpen(true)} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                                <Icon src={getIcon("filter")} alt="filters" size={17} />
                                <Typography variant="body" style={{ fontSize: "16px", fontWeight: 500 }}>
                                    Filters
                                </Typography>
                            </Button>
                        )}
                        <Button variant="solid" onClick={() => handleOpenCreateModal()} style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                            <Icon src={getInverseIcon("add")} alt="add" size={17} />
                            <Typography variant="body" inverse style={{ fontSize: "16px", fontWeight: 500 }}>
                                Create Room
                            </Typography>
                        </Button>
                    </Stack>
                </Box>

                <Divider style={{ margin: "1rem 0" }} />

                <div className="rooms-grid">

                    <div className="rooms-sidebar">
                        <RoomsFilterPanel {...filterPanelProps} inDrawer={false} />
                    </div>

                    <Box className="custom-scrollbar rooms-main-content">
                        {((appliedFilters.name?.trim().length || 0) > 0) && displayGroups.every(g => g.rooms.length === 0) ? (
                            <Box style={{ textAlign: "center", padding: "4rem 2rem", display: "flex", flexDirection: "column", alignItems: "center", gap: "1rem" }}>
                                <Typography variant="h3" style={{ opacity: 0.8 }}>No rooms found matching your filters.</Typography>
                                <Button variant="outline" onClick={handleResetFilters}>Reset Filters</Button>
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
                                            <Card key={room.id} style={{ width: "180px", height: "180px", textAlign: "center", padding: "20px", display: "flex", flexDirection: "column", gap: "10px" }}>
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
                </div>
            </Container>

            <AnimatePresence>
                {isMobileFiltersOpen && isMobile && (
                    <>
                        <motion.div
                            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} transition={{ duration: 0.2 }}
                            style={{ position: "fixed", inset: 0, background: "rgba(0, 0, 0, 0.4)", zIndex: 1000, backdropFilter: "blur(4px)" }}
                            onClick={() => setIsMobileFiltersOpen(false)}
                        />
                        <motion.div
                            initial={{ x: "-100%" }} animate={{ x: 0 }} exit={{ x: "-100%" }} transition={{ type: "spring", bounce: 0, duration: 0.4 }}
                            style={{ position: "fixed", top: 0, left: 0, bottom: 0, width: "300px", maxWidth: "85vw", background: "var(--color-bg)", zIndex: 1001, boxShadow: "var(--shadow-lg)", display: "flex", flexDirection: "column" }}
                        >
                            <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1.5rem", borderBottom: "1px solid var(--color-border)" }}>
                                <Typography variant="h2">Filters</Typography>
                                <Button variant="ghost" onClick={() => setIsMobileFiltersOpen(false)} style={{ padding: "0.25rem", boxShadow: "none" }}>
                                    <Icon src={getIcon("close")} size={20} alt="close" />
                                </Button>
                            </Box>
                            <Box style={{ flex: 1, overflow: "hidden", padding: "1rem 0" }}>
                                <RoomsFilterPanel {...filterPanelProps} inDrawer={true} />
                            </Box>
                        </motion.div>
                    </>
                )}
            </AnimatePresence>

            <Modal isOpen={isCreateRoomModalOpen} onClose={() => { setIsCreateRoomModalOpen(false); setValidationError(""); }} title="Create Room">
                <Box style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                    <FormField value={roomName} onChange={(e) => handleRoomNameChange(e.target.value)} placeholder="Room name" rounded />
                    <ComboBox options={AVAILABLE_ROOM_TYPES.map((type) => ({ value: type, label: ROOM_TYPE_LABELS[type] }))} value={roomType} onValueChange={setRoomType} placeholder="Choose room type" searchable />
                    {validationError && <Typography variant="caption" style={{ color: "red", textAlign: "center" }}>{validationError}</Typography>}
                    <Button variant="solid" onClick={handleCreateRoom}>Create</Button>
                </Box>
            </Modal>
        </Box>
    );
}
