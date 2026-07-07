package com.oblixorprime.ioe.expeditioncompass;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ExpeditionCompassAngle {
    public static final float UNBOUND_ANGLE = 0.0F;
    public static final float CROSS_DIMENSION_ANGLE = 0.5F;

    private static final double FULL_TURN_DEGREES = 360.0D;
    private static final double FULL_TURN_RADIANS = Math.PI * 2.0D;

    private ExpeditionCompassAngle() {
    }

    public static float angleToTarget(
            ResourceKey<Level> viewerDimension,
            double viewerX,
            double viewerZ,
            float viewerYRot,
            ExpeditionCompassTarget target
    ) {
        if (target == null || viewerDimension == null) {
            return UNBOUND_ANGLE;
        }
        if (!target.dimension().equals(viewerDimension)) {
            return CROSS_DIMENSION_ANGLE;
        }

        BlockPos targetPos = target.pos();
        double dx = targetPos.getX() + 0.5D - viewerX;
        double dz = targetPos.getZ() + 0.5D - viewerZ;
        if (dx == 0.0D && dz == 0.0D) {
            return UNBOUND_ANGLE;
        }

        double targetAngle = Math.atan2(dz, dx) / FULL_TURN_RADIANS;
        double visualRotation = viewerYRot / FULL_TURN_DEGREES;
        return positiveModulo(0.75D - visualRotation - targetAngle);
    }

    static float positiveModulo(double value) {
        double result = value % 1.0D;
        if (result < 0.0D) {
            result += 1.0D;
        }
        return (float) result;
    }
}
