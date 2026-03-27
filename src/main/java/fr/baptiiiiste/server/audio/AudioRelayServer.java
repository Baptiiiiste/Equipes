package fr.baptiiiiste.server.audio;

import fr.baptiiiiste.common.audio.AudioDatagram;
import fr.baptiiiiste.common.audio.AudioDatagramCodec;
import fr.baptiiiiste.common.audio.AudioDatagramKind;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;

public class AudioRelayServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AudioRelayServer.class);
    private static final int MAX_DATAGRAM_SIZE = 65507;

    private final int port;
    private final AudioSessionRegistry sessionRegistry;

    @Getter
    private volatile boolean running;

    private DatagramSocket socket;

    public AudioRelayServer(int port, AudioSessionRegistry sessionRegistry) {
        this.port = port;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(port);
            running = true;
            logger.info("[run] Audio UDP relay started on port {}", port);

            byte[] buffer = new byte[MAX_DATAGRAM_SIZE];
            while (running) {
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(incomingPacket);
                handlePacket(incomingPacket);
            }
        } catch (Exception exception) {
            if (running) {
                logger.error("[run] Audio UDP relay stopped unexpectedly", exception);
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void handlePacket(DatagramPacket incomingPacket) {
        try {
            AudioDatagram datagram = AudioDatagramCodec.decode(incomingPacket.getData(), incomingPacket.getLength());
            if (datagram == null) {
                return;
            }

            InetSocketAddress sourceEndpoint = new InetSocketAddress(incomingPacket.getAddress(), incomingPacket.getPort());
            if (!sessionRegistry.bindEndpoint(datagram.getToken(), datagram.getSenderId(), datagram.getRoomId(), sourceEndpoint)) {
                return;
            }

            if (datagram.getKind() == AudioDatagramKind.REGISTER) {
                return;
            }

            relayAudioFrame(datagram);
        } catch (Exception exception) {
            logger.warn("[handlePacket] Failed to process UDP packet: {}", exception.getMessage());
        }
    }

    private void relayAudioFrame(AudioDatagram datagram) throws IOException {
        List<AudioSessionRegistry.AudioSession> participants = sessionRegistry.getParticipantsInRoom(datagram.getRoomId());

        for (AudioSessionRegistry.AudioSession participant : participants) {
            if (participant.getClientId().equals(datagram.getSenderId())) {
                continue;
            }

            InetSocketAddress endpoint = participant.getEndpoint();
            if (endpoint == null) {
                continue;
            }

            AudioDatagram forwardedDatagram = AudioDatagram.audioFrame(
                    "",
                    datagram.getSenderId(),
                    datagram.getRoomId(),
                    datagram.getSequence(),
                    datagram.getAudioTimestamp(),
                    datagram.getPayload()
            );
            byte[] encodedPayload = AudioDatagramCodec.encode(forwardedDatagram);
            DatagramPacket outgoingPacket = new DatagramPacket(encodedPayload, encodedPayload.length, endpoint.getAddress(), endpoint.getPort());
            socket.send(outgoingPacket);
        }
    }
}

