package fr.baptiiiiste.client.screen;

import fr.baptiiiiste.client.models.Client;
import fr.baptiiiiste.common.models.packets.ScreenShareFramePacket;
import fr.baptiiiiste.common.models.packets.ScreenShareStartPacket;
import fr.baptiiiiste.common.models.packets.ScreenShareStopPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.Objects;

public class ScreenShareManager {

    private static final Logger logger = LoggerFactory.getLogger(ScreenShareManager.class);

    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 720;
    private static final int FRAME_INTERVAL_MS = 220;

    private final Client client;
    private final String username;

    private volatile boolean sharing;
    private volatile String activeRoomId;
    private Thread captureThread;

    public ScreenShareManager(Client client, String username) {
        this.client = client;
        this.username = username;
    }

    public void requestStart(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        client.sendPacket(new ScreenShareStartPacket(System.currentTimeMillis(), username, roomId));
    }

    public synchronized void onSharingStarted(String roomId, String sharerId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        if (!Objects.equals(username, sharerId)) {
            stopCapture(roomId);
            return;
        }

        if (sharing && Objects.equals(activeRoomId, roomId)) {
            return;
        }

        stopCapture(activeRoomId);

        activeRoomId = roomId;
        sharing = true;
        captureThread = new Thread(this::captureLoop, "screen-share-capture");
        captureThread.start();
    }

    public synchronized void stopSharing(String roomId, boolean notifyServer) {
        if (roomId != null && activeRoomId != null && !Objects.equals(roomId, activeRoomId)) {
            return;
        }

        String roomToClose = activeRoomId;
        stopCapture(roomId);

        if (notifyServer && roomToClose != null) {
            client.sendPacket(new ScreenShareStopPacket(System.currentTimeMillis(), username, roomToClose));
        }
    }

    public synchronized void onSharingStopped(String roomId, String sharerId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        if (Objects.equals(username, sharerId)) {
            stopCapture(roomId);
        }
    }

    public synchronized void shutdown() {
        stopCapture(activeRoomId);
    }

    public boolean isSharing(String roomId) {
        return sharing && Objects.equals(activeRoomId, roomId);
    }

    private synchronized void stopCapture(String roomId) {
        if (roomId != null && activeRoomId != null && !Objects.equals(roomId, activeRoomId)) {
            return;
        }

        sharing = false;
        activeRoomId = null;

        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
        }
    }

    private void captureLoop() {
        try {
            Robot robot = new Robot();

            while (sharing && activeRoomId != null) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle fullScreen = new Rectangle(screenSize);

                BufferedImage screenshot = robot.createScreenCapture(fullScreen);
                BufferedImage resized = resizeIfNeeded(screenshot);
                byte[] payload = encodeJpeg(resized, 0.65f);

                if (payload != null && payload.length > 0) {
                    client.sendPacket(new ScreenShareFramePacket(
                            System.currentTimeMillis(),
                            username,
                            activeRoomId,
                            payload,
                            resized.getWidth(),
                            resized.getHeight()
                    ));
                }

                Thread.sleep(FRAME_INTERVAL_MS);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            logger.warn("[captureLoop] Screen share capture stopped: {}", exception.getMessage());
        }
    }

    private BufferedImage resizeIfNeeded(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        if (width <= MAX_WIDTH && height <= MAX_HEIGHT) {
            return source;
        }

        double ratio = Math.min((double) MAX_WIDTH / width, (double) MAX_HEIGHT / height);
        int targetWidth = Math.max(1, (int) Math.round(width * ratio));
        int targetHeight = Math.max(1, (int) Math.round(height * ratio));

        Image scaled = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();

        return resized;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            return null;
        }

        ImageWriter writer = writers.next();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(imageOutput);

            ImageWriteParam params = writer.getDefaultWriteParam();
            if (params.canWriteCompressed()) {
                params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                params.setCompressionQuality(quality);
            }

            writer.write(null, new IIOImage(image, null, null), params);
            writer.dispose();
            return output.toByteArray();
        } catch (Exception exception) {
            writer.dispose();
            logger.warn("[encodeJpeg] Could not encode screenshot: {}", exception.getMessage());
            return null;
        }
    }
}

