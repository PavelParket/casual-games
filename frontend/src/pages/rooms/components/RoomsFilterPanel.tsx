import { Box, Button, Stack, FormField, Typography, Icon, CheckBox, Divider, useThemedIcon } from "../../../ui";
import { ROOM_TYPE_LABELS, type RoomType, type RoomSortField, type SortDirection } from "../../../models/Room";

const AVAILABLE_ROOM_TYPES = Object.keys(ROOM_TYPE_LABELS) as RoomType[];

interface RoomsFilterPanelProps {
    searchQuery: string;
    onSearchChange: (value: string) => void;
    isRefreshing: boolean;
    onRefresh: () => void;
    sortOption: RoomSortField;
    sortDirection: SortDirection;
    onSortClick: (field: RoomSortField) => void;
    isTypesExpanded: boolean;
    onToggleTypesExpanded: () => void;
    selectedTypes: RoomType[];
    onToggleType: (type: RoomType) => void;
    hasUnappliedFilters: boolean;
    onApplyFilters: () => void;
    inDrawer?: boolean;
}

export function RoomsFilterPanel({
    searchQuery,
    onSearchChange,
    isRefreshing,
    onRefresh,
    sortOption,
    sortDirection,
    onSortClick,
    isTypesExpanded,
    onToggleTypesExpanded,
    selectedTypes,
    onToggleType,
    hasUnappliedFilters,
    onApplyFilters,
    inDrawer = false,
}: RoomsFilterPanelProps) {
    const { getIcon, getInverseIcon } = useThemedIcon();

    return (
        <Stack
            className="custom-scrollbar"
            gap="1.5rem"
            style={{
                height: "100%",
                overflowY: "auto",
                padding: inDrawer ? "0 1.5rem 1.5rem 1.5rem" : "1.25rem 1.5rem",
                borderRadius: inDrawer ? "0" : "var(--radius-md)",
                boxShadow: inDrawer ? "none" : "var(--shadow-md)",
                backdropFilter: inDrawer ? "none" : "blur(2px)",
                background: inDrawer ? "transparent" : "var(--color-bg-glass)",
            }}
        >
            <Stack direction="row" gap="0.5rem" align="stretch" style={{ flexShrink: 0 }}>
                <FormField
                    value={searchQuery}
                    onChange={(e) => onSearchChange(e.target.value)}
                    placeholder="Search by name"
                    rounded
                    endAdornmentSrc={getIcon("search")}
                    endAdornmentAlt="search"
                    style={{ flex: 1, minWidth: 0 }}
                />

                <Button
                    variant="ghost"
                    onClick={onRefresh}
                    disabled={isRefreshing}
                    style={{ padding: "0.4rem" }}
                >
                    <Icon src={getIcon("refresh")} alt="refresh" size={20} />
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
                        onClick={() => onSortClick('CREATED_AT')}
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
                        onClick={() => onSortClick('NAME')}
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
                    onClick={onToggleTypesExpanded}
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
                                    onChange={() => onToggleType(t)}
                                />
                            </Stack>
                        ))}
                    </Stack>
                )}
                <Button
                    variant="solid"
                    disabled={!hasUnappliedFilters || isRefreshing}
                    onClick={onApplyFilters}
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
    );
}
