package com.game_service.mahjong.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game_service.common.exception.GameInternalException;
import com.game_service.mahjong.domain.entity.Slot;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@UtilityClass
@Slf4j
public class LayoutTemplate {

    private static final String LAYOUT_TEMPLATE_PATH = "games/mahjong-layout.json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Slot> slots = load();

    private final Map<String, Set<String>> coveredSlots = buildCoveredIndex();

    public Slot getSlot(String slotId) {
        Slot slot = slots.get(slotId);

        if (slot == null) {
            throw new GameInternalException("Unknown mahjong slot id: " + slotId);
        }

        return slot;
    }

    public Set<String> getSlotIds() {
        return slots.keySet();
    }

    public int getTotalSlots() {
        return slots.size();
    }

    public Set<String> getCoveredSlots(String upperSlotId) {
        return coveredSlots.getOrDefault(upperSlotId, Set.of());
    }

    private Map<String, Slot> load() {
        try (InputStream inputStream = new ClassPathResource(LAYOUT_TEMPLATE_PATH).getInputStream()) {
            List<Slot> loaded = MAPPER.readValue(inputStream, new TypeReference<>() {
            });

            Map<String, Slot> ordered = new LinkedHashMap<>();

            loaded.forEach(slot -> ordered.put(slot.getId(), slot));

            return Collections.unmodifiableMap(ordered);
        } catch (IOException e) {
            throw new GameInternalException("Failed to load mahjong layout template: " + e.getMessage());
        }
    }

    private Map<String, Set<String>> buildCoveredIndex() {
        Map<String, Set<String>> coveredSlots = new HashMap<>();

        slots.values().forEach(slot -> {
            slot.getCovers().forEach(covered -> {
                coveredSlots.computeIfAbsent(covered, id -> new HashSet<>()).add(slot.getId());
            });
        });

        return Collections.unmodifiableMap(coveredSlots);
    }
}
