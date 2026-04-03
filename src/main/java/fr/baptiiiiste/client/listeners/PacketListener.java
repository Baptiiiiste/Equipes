package fr.baptiiiiste.client.listeners;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;

public class PacketListener implements PacketHandler, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PacketListener.class);

    private ObjectInputStream in;
    private PacketHandler uiHandler;
    private boolean running = true;

    public PacketListener(ObjectInputStream in, PacketHandler uiHandler) {
        this.in = in;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Object obj = in.readObject();
                if (obj instanceof Packet packet) {
                    packet.execute(this);
                }
            }
        } catch (Exception e) {
            logger.error("[run] " + e.getMessage());

        }
    }

    @Override
    public void handle(TextPacket packet) {
        logger.info("[" + packet.getRoomId() + "] " + packet.getSenderId() + ": " + packet.getMessage());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(JoinRoomPacket packet) {
        logger.info("[" + packet.getRoomId() + "] User " + packet.getSenderId() + "joined the room");

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(LeaveRoomPacket packet) {
        logger.info("[" + packet.getRoomId() + "] User " + packet.getSenderId() + "left the room");

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(JoinMeetingPacket packet) {
        logger.info("[" + packet.getRoomId() + "] User " + packet.getSenderId() + "joined the meeting");

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(LeaveMeetingPacket packet) {
        logger.info("[" + packet.getRoomId() + "] User " + packet.getSenderId() + "left the meeting");

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(MeetingStartPacket packet) {
        logger.info("[" + packet.getRoomId() + "] Meeting started by " + packet.getSenderId());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(MeetingStopPacket packet) {
        logger.info("[" + packet.getRoomId() + "] Meeting stopped by " + packet.getSenderId());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(AudioUdpOfferPacket packet) {
        logger.info("[{}] Received audio UDP offer on port {}", packet.getRoomId(), packet.getUdpPort());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(AudioUdpAcceptPacket packet) {
        logger.info("[{}] Received audio UDP accept from {}", packet.getRoomId(), packet.getSenderId());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(AudioStartPacket packet) {
        logger.info("[{}] Audio stream started by {}", packet.getRoomId(), packet.getSenderId());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(AudioStopPacket packet) {
        logger.info("[{}] Audio stream stopped by {}", packet.getRoomId(), packet.getSenderId());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(ScreenShareStartPacket packet) {
        logger.info("[{}] Screen sharing started by {}", packet.getRoomId(), packet.getSenderId());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(ScreenShareStopPacket packet) {
        logger.info("[{}] Screen sharing stopped by {}", packet.getRoomId(), packet.getSenderId());

        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    @Override
    public void handle(ScreenShareFramePacket packet) {
        if (uiHandler != null) {
            uiHandler.handle(packet);
        }
    }

    public void stop() {
        running = false;
    }
}