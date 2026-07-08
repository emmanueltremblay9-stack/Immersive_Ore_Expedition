package com.oblixorprime.ioe.expeditioncompass.client.journeymap;

import com.oblixorprime.ioe.ImmersiveOreExpeditionMod;
import com.oblixorprime.ioe.expeditioncompass.ExpeditionCompassTarget;
import com.oblixorprime.ioe.expeditioncompass.IoeExpeditionCompassMod;
import com.oblixorprime.ioe.expeditioncompass.client.JourneyMapWaypointBridge;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.common.JourneyMapPlugin;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;

import java.util.Objects;

@JourneyMapPlugin(apiVersion = "2.0.0")
public final class JourneyMapWaypointPlugin implements IClientPlugin {
    private static final int WAYPOINT_COLOR = 0xD6A800;
    private static final String CUSTOM_TARGET_ID_KEY = ImmersiveOreExpeditionMod.MODID + ":target_id";
    private static final String CUSTOM_TARGET_KIND_KEY = ImmersiveOreExpeditionMod.MODID + ":target_kind";

    private static volatile IClientAPI clientApi;

    @Override
    public void initialize(IClientAPI jmClientApi) {
        clientApi = Objects.requireNonNull(jmClientApi, "jmClientApi");
        IoeExpeditionCompassMod.LOGGER.info("Initialized JourneyMap waypoint bridge");
    }

    @Override
    public String getModId() {
        return ImmersiveOreExpeditionMod.MODID;
    }

    public static JourneyMapWaypointBridge.Result createWaypoint(ExpeditionCompassTarget target) {
        Objects.requireNonNull(target, "target");
        IClientAPI api = clientApi;
        if (api == null) {
            return JourneyMapWaypointBridge.Result.NOT_READY;
        }

        try {
            removeExistingWaypoint(api, target);
            Waypoint waypoint = WaypointFactory.createWaypoint(
                    ImmersiveOreExpeditionMod.MODID,
                    target.pos(),
                    target.waypointName(),
                    target.dimension(),
                    true
            );
            waypoint.setColor(WAYPOINT_COLOR);
            waypoint.setDescription(description(target));
            waypoint.setEnabled(true);
            waypoint.setShowBeacon(true);
            waypoint.setShowOnMap(true);
            waypoint.setShowInWorld(true);
            waypoint.setShowLabel(true);
            waypoint.setCustomData(
                    CUSTOM_TARGET_ID_KEY,
                    target.primaryId().map(Object::toString).orElse("")
            );
            waypoint.setCustomData(CUSTOM_TARGET_KIND_KEY, target.kind().getSerializedName());
            api.addWaypoint(ImmersiveOreExpeditionMod.MODID, waypoint);
            return JourneyMapWaypointBridge.Result.CREATED;
        } catch (Throwable throwable) {
            IoeExpeditionCompassMod.LOGGER.warn("Failed to create JourneyMap waypoint for {}", target, throwable);
            return JourneyMapWaypointBridge.Result.FAILED;
        }
    }

    private static void removeExistingWaypoint(IClientAPI api, ExpeditionCompassTarget target) {
        String targetDimension = target.dimension().location().toString();
        for (Waypoint waypoint : api.getWaypoints(ImmersiveOreExpeditionMod.MODID)) {
            if (waypoint.getBlockPos().equals(target.pos())
                    && Objects.equals(waypoint.getPrimaryDimension(), targetDimension)) {
                api.removeWaypoint(ImmersiveOreExpeditionMod.MODID, waypoint);
            }
        }
    }

    private static String description(ExpeditionCompassTarget target) {
        return "Immersive Ore Expedition "
                + target.kind().messageLabel()
                + " at "
                + target.coordinateText();
    }
}
