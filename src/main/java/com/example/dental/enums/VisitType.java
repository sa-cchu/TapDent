package com.example.dental.enums;

public enum VisitType {
    FIRST_VISIT("初診"),
    RE_VISIT("再診");

    private final String name;

    VisitType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
