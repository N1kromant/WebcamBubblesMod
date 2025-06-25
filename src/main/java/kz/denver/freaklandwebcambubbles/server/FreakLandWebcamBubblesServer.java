package kz.denver.freaklandwebcambubbles.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FreakLandWebcamBubblesServer implements DedicatedServerModInitializer {
    public static final String MOD_ID = "freaklandwebcambubbles";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    // Store webcam states for each player
    private static final Map<UUID, WebcamState> playerWebcamStates = new ConcurrentHashMap<>();
    
    @Override
    public void onInitializeServer() {
        LOGGER.info("[WB-Server] FreakLand Webcam Bubbles Server is initializing!");
        
        // Register the payload types
        PayloadTypeRegistry.playC2S().register(WebcamImagePayload.ID, WebcamImagePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WebcamImagePayload.ID, WebcamImagePayload.CODEC);
        
        // Handle incoming webcam data from clients
        ServerPlayNetworking.registerGlobalReceiver(WebcamImagePayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            
            context.server().execute(() -> {
                // Update the sender's webcam state
                WebcamState state = new WebcamState(
                    payload.video().uuid,
                    payload.video().isVisible,
                    System.currentTimeMillis()
                );
                playerWebcamStates.put(sender.getUuid(), state);
                
                // Broadcast to nearby players (100 block radius)
                for (ServerPlayerEntity player : sender.getServerWorld().getPlayers()) {
                    if (!player.getUuid().equals(sender.getUuid()) && 
                        player.squaredDistanceTo(sender) <= 100.0 * 100.0) {
                        
                        // Check if target player has permission to see webcams
                        if (hasWebcamPermission(player)) {
                            ServerPlayNetworking.send(player, payload);
                        }
                    }
                }
                
                LOGGER.debug("[WB-Server] Relayed webcam from {} to nearby players", sender.getName().getString());
            });
        });
        
        // Clean up disconnected players
        ServerPlayNetworking.registerGlobalReceiver(
            new Identifier(MOD_ID, "player_disconnect"),
            (payload, context) -> {
                playerWebcamStates.remove(context.player().getUuid());
            }
        );
    }
    
    private boolean hasWebcamPermission(ServerPlayerEntity player) {
        // Add permission check logic here if needed
        // For now, all players can see webcams
        return true;
    }
    
    public static WebcamState getPlayerWebcamState(UUID playerUuid) {
        return playerWebcamStates.get(playerUuid);
    }
    
    public static class WebcamState {
        public final UUID uuid;
        public final boolean isVisible;
        public final long lastUpdate;
        
        public WebcamState(UUID uuid, boolean isVisible, long lastUpdate) {
            this.uuid = uuid;
            this.isVisible = isVisible;
            this.lastUpdate = lastUpdate;
        }
    }
}