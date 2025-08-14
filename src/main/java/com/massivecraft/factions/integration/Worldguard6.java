package com.massivecraft.factions.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Worldguard Region Checking.
 *
 * @author Spathizilla
 */
public class Worldguard6 implements IWorldguard {

    private WorldGuardPlugin wg;

    public Worldguard6(Plugin wg) {
        this.wg = (WorldGuardPlugin) wg;
    }

    // PVP Flag check
    // Returns:
    //   True: PVP is allowed
    //   False: PVP is disallowed
    public boolean isPVP(Player player) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        BlockVector3 pt = BukkitAdapter.asBlockVector(loc);

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) return true;
        ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
        return set.testState(null, Flags.PVP);
    }

    // Check if player can build at location by worldguards rules.
    // Returns:
    //	True: Player can build in the region.
    //	False: Player can not build in the region.
    public boolean playerCanBuild(Player player, Location loc) {
        World world = loc.getWorld();
        BlockVector3 pt = BukkitAdapter.asBlockVector(loc);

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) return true;
        ApplicableRegionSet regions = regionManager.getApplicableRegions(pt);
        return regions.testState(WorldGuardPlugin.inst().wrapPlayer(player), Flags.BUILD);
    }

    // Check for Regions in chunk the chunk
    // Returns:
    //   True: Regions found within chunk
    //   False: No regions found within chunk
    public boolean checkForRegionsInChunk(Chunk chunk) {
        World world = chunk.getWorld();
        int minChunkX = chunk.getX() << 4;
        int minChunkZ = chunk.getZ() << 4;
        int maxChunkX = minChunkX + 15;
        int maxChunkZ = minChunkZ + 15;

        int worldHeight = world.getMaxHeight(); // Allow for heights other than default

        BlockVector3 minChunk = BlockVector3.at(minChunkX, 0, minChunkZ);
        BlockVector3 maxChunk = BlockVector3.at(maxChunkX, worldHeight, maxChunkZ);

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) return false;
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("wgfactionoverlapcheck", minChunk, maxChunk);
        Map<String, ProtectedRegion> allregions = regionManager.getRegions();
        Collection<ProtectedRegion> allregionslist = new ArrayList<>(allregions.values());
        List<ProtectedRegion> overlaps;
        boolean foundregions = false;

        try {
            overlaps = region.getIntersectingRegions(allregionslist);
            foundregions = overlaps != null && !overlaps.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return foundregions;
    }

    @Override
    public String getVersion() {
        return wg.getDescription().getVersion();
    }
}