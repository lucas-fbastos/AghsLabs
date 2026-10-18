package io.luck.AghsLabs.model.Enums;

import java.util.Arrays;
import java.util.Optional;

public enum ProPlayerRole {
    CORE(1, "Core"),
    MID(4, "Mid"),
    OFFLANE(3, "Offlane"),
    SUPPORT(2, "Support");

    private final int value;
    private final String label;

    ProPlayerRole(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static Optional<ProPlayerRole> fromValue(Integer value) {
        if (value == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(role -> role.value == value)
                .findFirst();
    }
}
