package com.oblixorprime.ioe.expeditioncompass.client;

import com.oblixorprime.ioe.core.LoadedResourceScanner;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget;
import com.oblixorprime.ioe.expeditioncompass.IoeExpeditionCompassMod;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public final class JourneyMapWaypointBridge {
    private static final String JOURNEYMAP_MOD_ID = "journeymap";
    private static final String PLUGIN_CLASS =
            "com.oblixorprime.ioe.expeditioncompass.client.journeymap.JourneyMapWaypointPlugin";

    private JourneyMapWaypointBridge() {
    }

    static Result createWaypoint(ExpeditionCompassTarget target) {
        Objects.requireNonNull(target, "target");
        if (!target.playable()) {
            return Result.NOT_PLAYABLE;
        }
        if (!LoadedResourceScanner.runtime().isModLoaded(JOURNEYMAP_MOD_ID)) {
            return Result.NOT_LOADED;
        }

        try {
            Object result = Class.forName(PLUGIN_CLASS)
                    .getMethod("createWaypoint", ExpeditionCompassTarget.class)
                    .invoke(null, target);
            if (result instanceof Result waypointResult) {
                return waypointResult;
            }
            return Result.FAILED;
        } catch (ClassNotFoundException | NoClassDefFoundError exception) {
            IoeExpeditionCompassMod.LOGGER.debug("JourneyMap waypoint plugin is not ready", exception);
            return Result.NOT_READY;
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            IoeExpeditionCompassMod.LOGGER.warn("JourneyMap waypoint bridge is invalid", exception);
            return Result.FAILED;
        } catch (InvocationTargetException exception) {
            IoeExpeditionCompassMod.LOGGER.warn("JourneyMap waypoint creation failed", exception.getCause());
            return Result.FAILED;
        } catch (LinkageError error) {
            IoeExpeditionCompassMod.LOGGER.warn("JourneyMap waypoint API linkage failed", error);
            return Result.NOT_READY;
        }
    }

    public enum Result {
        CREATED,
        NOT_PLAYABLE,
        NOT_LOADED,
        NOT_READY,
        FAILED
    }
}
