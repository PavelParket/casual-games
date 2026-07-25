package com.game_service.mahjong.util;

import com.game_service.mahjong.domain.entity.Slot;
import lombok.experimental.UtilityClass;

import java.util.Set;

@UtilityClass
public class TileRules {

    public static boolean isFree(String slotId, Set<String> removed) {
        Slot slot = LayoutTemplate.getSlot(slotId);

        boolean uncovered = removed.containsAll(slot.getCovers());
        boolean leftOpen = slot.getLeftId() == null || removed.contains(slot.getLeftId());
        boolean rightOpen = slot.getRightId() == null || removed.contains(slot.getRightId());

        return uncovered && (leftOpen || rightOpen);
    }
}
