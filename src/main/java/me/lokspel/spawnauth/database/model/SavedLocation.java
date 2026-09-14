package me.lokspel.spawnauth.database.model;

public record SavedLocation(
        String name,
        String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}