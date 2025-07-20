package ru.n1kromant.webcambubbles.server;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class WebcamBubblesServer implements ModInitializer {
    public static final String MOD_ID = "webcambubblesserver";
    public static final String MOD_NAME = "Webcam Bubbles Server";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[WBS] {} is initializing!", MOD_NAME);
        
        // Регистрация пакетов
        PayloadTypeRegistry.playC2S().register(WebcamImagePayload.ID, WebcamImagePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WebcamImagePayload.ID, WebcamImagePayload.CODEC);
        
        // Обработчик получения изображения от клиента
        ServerPlayNetworking.registerGlobalReceiver(WebcamImagePayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            
            // Логирование для отладки
            LOGGER.debug("[WBS] Received webcam image from {} (UUID: {}, size: {} bytes, visible: {})", 
                sender.getName().getString(),
                payload.playerUuid(),
                payload.imageData().length,
                payload.isVisible());
            
            // Отправляем изображение всем игрокам в радиусе 100 блоков, кроме отправителя
            for (ServerPlayerEntity player : PlayerLookup.around(sender.getServerWorld(), sender.getPos(), 100.0)) {
                if (!player.getUuid().equals(sender.getUuid())) {
                    ServerPlayNetworking.send(player, payload);
                    LOGGER.debug("[WBS] Forwarding webcam to {}", player.getName().getString());
                }
            }
        });
        
        LOGGER.info("[WBS] {} initialized successfully!", MOD_NAME);
    }
    
    // Payload для передачи изображения с веб-камеры
    public record WebcamImagePayload(
        UUID playerUuid,
        int width,
        int height,
        byte[] imageData,
        boolean isVisible
    ) implements CustomPayload {
        
        // ВАЖНО: Используем точно такой же идентификатор, как в клиенте!
        // Клиент использует: "freaklandwebcambubbles:video_frame"
        public static final CustomPayload.Id<WebcamImagePayload> ID = 
            new CustomPayload.Id<>(Identifier.of("freaklandwebcambubbles", "video_frame"));
        
        // Создаем кастомный кодек, который совместим с форматом клиента
        public static final PacketCodec<RegistryByteBuf, WebcamImagePayload> CODEC = 
            PacketCodec.of(
                (payload, buf) -> write(buf, payload),
                WebcamImagePayload::read
            );
        
        public static void write(RegistryByteBuf buf, WebcamImagePayload payload) {
            // Когда сервер отправляет клиенту, клиент будет читать по байту
            // Поэтому записываем по байту для совместимости с клиентским decode
            buf.writeUuid(payload.playerUuid);
            buf.writeInt(payload.width);
            buf.writeInt(payload.height);
            buf.writeInt(payload.imageData.length);
            
            // Записываем по байту, чтобы клиент мог прочитать по байту
            for (byte b : payload.imageData) {
                buf.writeByte(b);
            }
            
            buf.writeBoolean(payload.isVisible);
        }
        
        public static WebcamImagePayload read(RegistryByteBuf buf) {
            // Когда сервер получает от клиента, клиент отправил через writeBytes
            // Поэтому читаем через readBytes
            UUID playerUuid = buf.readUuid();
            int width = buf.readInt();
            int height = buf.readInt();
            int dataLength = buf.readInt();
            byte[] imageData = new byte[dataLength];
            
            // Клиент отправляет через writeBytes, поэтому читаем через readBytes  
            buf.readBytes(imageData);
            
            boolean isVisible = buf.readBoolean();
            
            return new WebcamImagePayload(playerUuid, width, height, imageData, isVisible);
        }
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}