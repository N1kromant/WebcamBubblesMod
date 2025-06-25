package kz.denver.freaklandwebcambubbles.server;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WebcamImagePayload(WebcamBubble video) implements CustomPayload {
    public static final Identifier PAYLOAD_ID = new Identifier("freaklandwebcambubbles", "video_frame");
    public static final CustomPayload.Id<WebcamImagePayload> ID = new CustomPayload.Id<>(PAYLOAD_ID);
    public static final PacketCodec<PacketByteBuf, WebcamImagePayload> CODEC = PacketCodec.of(
        WebcamPacketCodec.PACKET_CODEC,
        WebcamImagePayload::video,
        WebcamImagePayload::new
    );
    
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}