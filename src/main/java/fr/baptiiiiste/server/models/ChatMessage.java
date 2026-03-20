package fr.baptiiiiste.server.models;

public class ChatMessage {

    private Long id;
    private String roomId;
    private String senderId;
    private String message;
    private long sentAt;

    public ChatMessage() {
    }

    public ChatMessage(Long id, String roomId, String senderId, String message, long sentAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.message = message;
        this.sentAt = sentAt;
    }

    public ChatMessage(String roomId, String senderId, String message, long sentAt) {
        this(null, roomId, senderId, message, sentAt);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getSentAt() {
        return sentAt;
    }

    public void setSentAt(long sentAt) {
        this.sentAt = sentAt;
    }
}
