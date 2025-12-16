package org.frags.complexInteractions.objects.walking;

import javax.annotation.Nullable;

public enum WalkingMode {

    WAYPOINT,
    WANDER;

    @Nullable
    public static WalkingMode getMode(String mode) {
        try {
            return WalkingMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
