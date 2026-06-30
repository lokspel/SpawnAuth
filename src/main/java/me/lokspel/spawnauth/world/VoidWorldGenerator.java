package me.lokspel.spawnauth.world;

import org.bukkit.Material;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class VoidWorldGenerator extends ChunkGenerator {
    private final int spawnX;
    private final int platformY;
    private final int spawnZ;
    private final int platformRadius;

    public VoidWorldGenerator(int spawnX, int platformY, int spawnZ, int platformRadius) {
        this.spawnX = spawnX;
        this.platformY = platformY;
        this.spawnZ = spawnZ;
        this.platformRadius = Math.max(0, platformRadius);
    }

    @Override
    public void generateSurface(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX,
            int chunkZ,
            @NotNull ChunkData chunkData
    ) {
        int minBlockX = spawnX - platformRadius;
        int maxBlockX = spawnX + platformRadius;
        int minBlockZ = spawnZ - platformRadius;
        int maxBlockZ = spawnZ + platformRadius;

        for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
            if (Math.floorDiv(blockX, 16) != chunkX) {
                continue;
            }

            for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
                if (Math.floorDiv(blockZ, 16) == chunkZ) {
                    chunkData.setBlock(Math.floorMod(blockX, 16), platformY, Math.floorMod(blockZ, 16), Material.BARRIER);
                }
            }
        }
    }

}
