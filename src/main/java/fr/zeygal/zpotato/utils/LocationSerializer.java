package fr.zeygal.zpotato.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationSerializer {

    private static final String SEPARATOR = ":";

    public static String locationToString(Location location) {
        if (location == null) {
            return null;
        }

        return location.getWorld().getName() + SEPARATOR +
                location.getX() + SEPARATOR +
                location.getY() + SEPARATOR +
                location.getZ() + SEPARATOR +
                location.getYaw() + SEPARATOR +
                location.getPitch();
    }

    public static Location stringToLocation(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }

        String[] parts = string.split(SEPARATOR);

        if (parts.length < 4) {
            return null;
        }

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);

            float yaw = 0;
            float pitch = 0;

            if (parts.length >= 5) {
                yaw = Float.parseFloat(parts[4]);
            }

            if (parts.length >= 6) {
                pitch = Float.parseFloat(parts[5]);
            }

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}