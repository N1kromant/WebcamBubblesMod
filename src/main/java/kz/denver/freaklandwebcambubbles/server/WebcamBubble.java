package kz.denver.freaklandwebcambubbles.server;

import java.util.UUID;

public class WebcamBubble {
    public UUID uuid;
    public byte[] image;
    public int width;
    public int height;
    public boolean isVisible;
    
    public WebcamBubble(int width, int height, UUID uuid) {
        this.width = width;
        this.height = height;
        this.uuid = uuid;
    }
    
    public void setImage(byte[] image) {
        this.image = image;
    }
    
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}