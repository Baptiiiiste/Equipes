package fr.baptiiiiste.common.audio;

import lombok.Getter;

@Getter
public class AudioDatagram {

    private final AudioDatagramKind kind;
    private final String token;
    private final String senderId;
    private final String roomId;
    private final int sequence;
    private final long audioTimestamp;
    private final byte[] payload;

    private AudioDatagram(AudioDatagramKind kind,
                          String token,
                          String senderId,
                          String roomId,
                          int sequence,
                          long audioTimestamp,
                          byte[] payload) {
        this.kind = kind;
        this.token = token;
        this.senderId = senderId;
        this.roomId = roomId;
        this.sequence = sequence;
        this.audioTimestamp = audioTimestamp;
        this.payload = payload;
    }

    public static AudioDatagram register(String token, String senderId, String roomId) {
        return new AudioDatagram(AudioDatagramKind.REGISTER, token, senderId, roomId, 0, System.nanoTime(), new byte[0]);
    }

    public static AudioDatagram audioFrame(String token, String senderId, String roomId, int sequence, long audioTimestamp, byte[] payload) {
        return new AudioDatagram(AudioDatagramKind.AUDIO_FRAME, token, senderId, roomId, sequence, audioTimestamp, payload);
    }
}

