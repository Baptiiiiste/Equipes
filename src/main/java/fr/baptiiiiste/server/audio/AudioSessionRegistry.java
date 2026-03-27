package fr.baptiiiiste.server.audio;

import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AudioSessionRegistry {

    private final Map<String, AudioSession> sessionsByClientAndRoom = new ConcurrentHashMap<>();
    private final Map<String, AudioSession> sessionsByToken = new ConcurrentHashMap<>();

    public AudioSession registerParticipant(String roomId, String clientId) {
        String key = key(roomId, clientId);
        AudioSession existingSession = sessionsByClientAndRoom.get(key);
        if (existingSession != null) {
            return existingSession;
        }

        String token = UUID.randomUUID().toString();
        AudioSession session = new AudioSession(roomId, clientId, token);
        sessionsByClientAndRoom.put(key, session);
        sessionsByToken.put(token, session);
        return session;
    }

    public void removeParticipant(String roomId, String clientId) {
        AudioSession removedSession = sessionsByClientAndRoom.remove(key(roomId, clientId));
        if (removedSession != null) {
            sessionsByToken.remove(removedSession.getToken());
        }
    }

    public boolean bindEndpoint(String token, String clientId, String roomId, InetSocketAddress endpoint) {
        AudioSession session = sessionsByToken.get(token);
        if (session == null || !session.matches(clientId, roomId)) {
            return false;
        }

        session.setEndpoint(endpoint);
        return true;
    }

    public boolean isAuthorized(String token, String clientId, String roomId) {
        AudioSession session = sessionsByToken.get(token);
        return session != null && session.matches(clientId, roomId);
    }

    public List<AudioSession> getParticipantsInRoom(String roomId) {
        List<AudioSession> sessions = new ArrayList<>();

        for (AudioSession session : sessionsByClientAndRoom.values()) {
            if (Objects.equals(roomId, session.getRoomId())) {
                sessions.add(session);
            }
        }

        return sessions;
    }

    private String key(String roomId, String clientId) {
        return roomId + "::" + clientId;
    }

    @Getter
    public static class AudioSession {

        private final String roomId;
        private final String clientId;
        private final String token;
        private volatile InetSocketAddress endpoint;

        public AudioSession(String roomId, String clientId, String token) {
            this.roomId = roomId;
            this.clientId = clientId;
            this.token = token;
        }

        public boolean matches(String expectedClientId, String expectedRoomId) {
            return Objects.equals(clientId, expectedClientId) && Objects.equals(roomId, expectedRoomId);
        }

        public void setEndpoint(InetSocketAddress endpoint) {
            this.endpoint = endpoint;
        }
    }
}

