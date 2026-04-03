package fr.baptiiiiste.client.audio;

import fr.baptiiiiste.client.models.Client;
import fr.baptiiiiste.common.audio.AudioDatagram;
import fr.baptiiiiste.common.audio.AudioDatagramCodec;
import fr.baptiiiiste.common.audio.AudioDatagramKind;
import fr.baptiiiiste.common.models.packets.AudioStartPacket;
import fr.baptiiiiste.common.models.packets.AudioStopPacket;
import fr.baptiiiiste.common.models.packets.AudioUdpAcceptPacket;
import fr.baptiiiiste.common.models.packets.AudioUdpOfferPacket;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioCallManager {

    private static final Logger logger = LoggerFactory.getLogger(AudioCallManager.class);

    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(16000f, 16, 1, true, false);
    private static final int FRAME_SIZE_BYTES = 640;

    private final Client client;
    private final String username;

    private final AtomicInteger sequenceGenerator = new AtomicInteger();
    private final BlockingQueue<byte[]> playbackQueue = new LinkedBlockingQueue<>(120);

    private volatile boolean running;
    @Getter
    private volatile String activeRoomId;
    private volatile String sessionToken;

    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int serverUdpPort;

    private Thread captureThread;
    private Thread receiveThread;
    private Thread playbackThread;

    private TargetDataLine captureLine;
    private SourceDataLine playbackLine;

    public AudioCallManager(Client client, String username) {
        this.client = client;
        this.username = username;
    }

    public synchronized void handleOffer(AudioUdpOfferPacket offerPacket) {
        if (offerPacket == null) {
            return;
        }

        try {
            if (running && Objects.equals(activeRoomId, offerPacket.getRoomId())) {
                return;
            }

            stopCall(activeRoomId, false);

            ensureUdpSocket();
            activeRoomId = offerPacket.getRoomId();
            sessionToken = offerPacket.getSessionToken();
            serverAddress = client.getSocket().getInetAddress();
            serverUdpPort = offerPacket.getUdpPort();

            client.sendPacket(new AudioUdpAcceptPacket(
                    System.currentTimeMillis(),
                    username,
                    offerPacket.getRoomId(),
                    udpSocket.getLocalPort(),
                    sessionToken
            ));

            sendRegisterDatagram();
            startAudioThreads();

            client.sendPacket(new AudioStartPacket(System.currentTimeMillis(), username, offerPacket.getRoomId()));
        } catch (Exception exception) {
            logger.error("[handleOffer] Could not start audio call", exception);
            stopCall(offerPacket.getRoomId(), false);
        }
    }

    public synchronized void stopCall(String roomId, boolean notifyServer) {
        if (!running && (activeRoomId == null || !Objects.equals(activeRoomId, roomId))) {
            return;
        }

        if (roomId != null && activeRoomId != null && !Objects.equals(roomId, activeRoomId)) {
            return;
        }

        String roomToClose = activeRoomId;
        running = false;

        if (notifyServer && roomToClose != null) {
            client.sendPacket(new AudioStopPacket(System.currentTimeMillis(), username, roomToClose));
        }

        closeCaptureLine();
        closePlaybackLine();
        playbackQueue.clear();

        if (captureThread != null) {
            captureThread.interrupt();
            captureThread = null;
        }
        if (receiveThread != null) {
            receiveThread.interrupt();
            receiveThread = null;
        }
        if (playbackThread != null) {
            playbackThread.interrupt();
            playbackThread = null;
        }

        activeRoomId = null;
        sessionToken = null;
    }

    public synchronized void shutdown() {
        stopCall(activeRoomId, false);

        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
    }

    private void ensureUdpSocket() throws Exception {
        if (udpSocket != null && !udpSocket.isClosed()) {
            return;
        }

        udpSocket = new DatagramSocket();
        udpSocket.setSoTimeout(500);
    }

    private void startAudioThreads() {
        running = true;
        sequenceGenerator.set(0);

        receiveThread = new Thread(this::receiveLoop, "audio-receive-loop");
        playbackThread = new Thread(this::playbackLoop, "audio-playback-loop");
        captureThread = new Thread(this::captureLoop, "audio-capture-loop");

        receiveThread.start();
        playbackThread.start();
        captureThread.start();
    }

    private void sendRegisterDatagram() throws Exception {
        AudioDatagram registerDatagram = AudioDatagram.register(sessionToken, username, activeRoomId);
        byte[] payload = AudioDatagramCodec.encode(registerDatagram);
        udpSocket.send(new DatagramPacket(payload, payload.length, serverAddress, serverUdpPort));
    }

    private void captureLoop() {
        try {
            openCaptureLine();
            byte[] frameBuffer = new byte[FRAME_SIZE_BYTES];

            while (running) {
                int offset = 0;
                while (offset < frameBuffer.length && running) {
                    int bytesRead = captureLine.read(frameBuffer, offset, frameBuffer.length - offset);
                    if (bytesRead <= 0) {
                        break;
                    }
                    offset += bytesRead;
                }

                if (offset < frameBuffer.length) {
                    continue;
                }

                sendAudioFrame(Arrays.copyOf(frameBuffer, frameBuffer.length));
            }
        } catch (Exception exception) {
            logger.warn("[captureLoop] Audio capture unavailable: {}", exception.getMessage());
        } finally {
            closeCaptureLine();
        }
    }

    private void receiveLoop() {
        byte[] buffer = new byte[65507];

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                AudioDatagram datagram = AudioDatagramCodec.decode(packet.getData(), packet.getLength());
                if (datagram == null || datagram.getKind() != AudioDatagramKind.AUDIO_FRAME) {
                    continue;
                }

                if (!Objects.equals(activeRoomId, datagram.getRoomId())) {
                    continue;
                }

                if (Objects.equals(username, datagram.getSenderId())) {
                    continue;
                }

                playbackQueue.offer(datagram.getPayload());
            } catch (java.net.SocketTimeoutException ignored) {
                // Periodic timeout allows the thread to observe the running flag.
            } catch (Exception exception) {
                if (running) {
                    logger.warn("[receiveLoop] {}", exception.getMessage());
                }
            }
        }
    }

    private void playbackLoop() {
        try {
            openPlaybackLine();

            while (running) {
                byte[] frame = playbackQueue.poll(300, TimeUnit.MILLISECONDS);
                if (frame == null) {
                    continue;
                }

                playbackLine.write(frame, 0, frame.length);
            }
        } catch (Exception exception) {
            logger.warn("[playbackLoop] Audio playback unavailable: {}", exception.getMessage());
        } finally {
            closePlaybackLine();
        }
    }

    private void sendAudioFrame(byte[] frame) throws Exception {
        AudioDatagram datagram = AudioDatagram.audioFrame(
                sessionToken,
                username,
                activeRoomId,
                sequenceGenerator.incrementAndGet(),
                System.nanoTime(),
                frame
        );

        byte[] payload = AudioDatagramCodec.encode(datagram);
        udpSocket.send(new DatagramPacket(payload, payload.length, serverAddress, serverUdpPort));
    }

    private void openCaptureLine() throws Exception {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
        captureLine = (TargetDataLine) AudioSystem.getLine(info);
        captureLine.open(AUDIO_FORMAT);
        captureLine.start();
    }

    private void openPlaybackLine() throws Exception {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, AUDIO_FORMAT);
        playbackLine = (SourceDataLine) AudioSystem.getLine(info);
        playbackLine.open(AUDIO_FORMAT);
        playbackLine.start();
    }

    private synchronized void closeCaptureLine() {
        TargetDataLine lineToClose = captureLine;
        captureLine = null;

        if (lineToClose == null) {
            return;
        }

        try {
            lineToClose.stop();
        } catch (Exception ignored) {
            // The line may already be stopped by another thread.
        }

        try {
            lineToClose.close();
        } catch (Exception ignored) {
            // Best-effort close during fast room switches.
        }
    }

    private synchronized void closePlaybackLine() {
        SourceDataLine lineToClose = playbackLine;
        playbackLine = null;

        if (lineToClose == null) {
            return;
        }

        try {
            lineToClose.stop();
        } catch (Exception ignored) {
            // The line may already be stopped by another thread.
        }

        try {
            lineToClose.close();
        } catch (Exception ignored) {
            // Best-effort close during fast room switches.
        }
    }
}

