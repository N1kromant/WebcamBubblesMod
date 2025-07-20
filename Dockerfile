FROM eclipse-temurin:21-jre-alpine

# Install required packages
RUN apk add --no-cache wget bash

# Create minecraft directory
WORKDIR /minecraft

# Download Fabric server launcher
RUN wget https://meta.fabricmc.net/v2/versions/loader/1.21/0.15.11/1.0.1/server/jar -O fabric-server-launch.jar

# Copy the mod
COPY build/libs/freaklandwebcambubbles-server-*.jar mods/

# Create eula.txt
RUN echo "eula=true" > eula.txt

# Create server.properties with basic settings
RUN echo "server-port=25565" > server.properties && \
    echo "motd=FreakLand Webcam Test Server" >> server.properties && \
    echo "max-players=20" >> server.properties && \
    echo "online-mode=false" >> server.properties && \
    echo "view-distance=10" >> server.properties

# Expose Minecraft port
EXPOSE 25565

# Start the server
CMD ["java", "-Xmx2G", "-Xms1G", "-jar", "fabric-server-launch.jar", "nogui"]