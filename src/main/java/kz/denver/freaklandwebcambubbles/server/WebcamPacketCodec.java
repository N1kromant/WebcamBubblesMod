package kz.denver.freaklandwebcambubbles.server;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

class WebcamPacketCodec implements PacketCodec<PacketByteBuf, WebcamBubble> {
    private static final Logger LOGGER = LoggerFactory.getLogger("WebcamPacketCodec");
    public static final WebcamPacketCodec PACKET_CODEC = new WebcamPacketCodec();
    
    @Override
    public WebcamBubble decode(PacketByteBuf buf) {
        try {
            UUID playerUUID = buf.readUuid();
            int width = buf.readInt();
            int height = buf.readInt();
            int frameBytes = buf.readInt();
            
            byte[] frame = new byte[frameBytes];
            buf.readBytes(frame);
            
            boolean isVisible = buf.readBoolean();
            
            WebcamBubble webcamBubble = new WebcamBubble(width, height, playerUUID);
            webcamBubble.setImage(frame);
            webcamBubble.setVisible(isVisible);
            
            return webcamBubble;
        } catch (Exception e) {
            LOGGER.error("[WB-Server] Error decoding webcam packet", e);
            throw e;
        }
    }
    
    @Override
    public void encode(PacketByteBuf buf, WebcamBubble value) {
        buf.writeUuid(value.uuid);
        buf.writeInt(value.width);
        buf.writeInt(value.height);
        buf.writeInt(value.image.length);
        buf.writeBytes(value.image);
        buf.writeBoolean(value.isVisible);
    }
}