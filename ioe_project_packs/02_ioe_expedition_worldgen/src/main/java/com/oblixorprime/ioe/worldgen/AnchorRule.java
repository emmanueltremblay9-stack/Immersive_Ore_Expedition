package com.oblixorprime.ioe.worldgen;

public record AnchorRule(int minDistance, int maxDistance, boolean requireTunnelConnection) {
    public AnchorRule {
        if (minDistance < 0) {
            throw new IllegalArgumentException("minDistance must not be negative");
        }
        if (maxDistance < minDistance) {
            throw new IllegalArgumentException("maxDistance must be greater than or equal to minDistance");
        }
    }

    public static AnchorRule fromConfig() {
        return fromConfigValues(
                IoeWorldgenConfig.oreLoadMinDistanceFromAnchor(),
                IoeWorldgenConfig.oreLoadMaxDistanceFromAnchor(),
                IoeWorldgenConfig.requireTunnelConnection()
        );
    }

    static AnchorRule fromConfigValues(int minDistance, int maxDistance, boolean requireTunnelConnection) {
        int effectiveMaxDistance = Math.max(minDistance, maxDistance);
        return new AnchorRule(
                minDistance,
                effectiveMaxDistance,
                requireTunnelConnection
        );
    }

    public boolean isDistanceAllowed(int distance) {
        return distance >= minDistance && distance <= maxDistance;
    }
}
