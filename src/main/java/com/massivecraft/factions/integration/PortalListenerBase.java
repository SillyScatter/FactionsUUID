package com.massivecraft.factions.integration;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface PortalListenerBase {
    boolean shouldCancel(Location location, Player player);
}