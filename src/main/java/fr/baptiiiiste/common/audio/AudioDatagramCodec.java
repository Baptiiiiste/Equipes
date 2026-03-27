package fr.baptiiiiste.common.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class AudioDatagramCodec {

    private static final int MAGIC = 0x45515550;
    private static final byte VERSION = 1;

    private AudioDatagramCodec() {
    }

    public static byte[] encode(AudioDatagram datagram) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (DataOutputStream output = new DataOutputStream(buffer)) {
            output.writeInt(MAGIC);
            output.writeByte(VERSION);
            output.writeByte(datagram.getKind().getCode());
            writeString(output, datagram.getToken());
            writeString(output, datagram.getSenderId());
            writeString(output, datagram.getRoomId());
            output.writeInt(datagram.getSequence());
            output.writeLong(datagram.getAudioTimestamp());

            byte[] payload = datagram.getPayload() == null ? new byte[0] : datagram.getPayload();
            output.writeInt(payload.length);
            output.write(payload);
            output.flush();
            return buffer.toByteArray();
        }
    }

    public static AudioDatagram decode(byte[] payload, int payloadLength) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(payload, 0, payloadLength))) {
            int magic = input.readInt();
            if (magic != MAGIC) {
                return null;
            }

            byte version = input.readByte();
            if (version != VERSION) {
                return null;
            }

            AudioDatagramKind kind = AudioDatagramKind.fromCode(input.readByte());
            if (kind == null) {
                return null;
            }

            String token = readString(input);
            String senderId = readString(input);
            String roomId = readString(input);
            int sequence = input.readInt();
            long timestamp = input.readLong();

            int dataLength = input.readInt();
            if (dataLength < 0 || dataLength > 65507) {
                return null;
            }

            byte[] audioPayload = new byte[dataLength];
            input.readFully(audioPayload);

            if (kind == AudioDatagramKind.REGISTER) {
                return AudioDatagram.register(token, senderId, roomId);
            }

            return AudioDatagram.audioFrame(token, senderId, roomId, sequence, timestamp, audioPayload);
        }
    }

    private static void writeString(DataOutputStream output, String value) throws IOException {
        byte[] data = value == null ? new byte[0] : value.getBytes(StandardCharsets.UTF_8);
        output.writeInt(data.length);
        output.write(data);
    }

    private static String readString(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length < 0 || length > 4096) {
            throw new IOException("Invalid string length in audio datagram");
        }

        byte[] data = new byte[length];
        input.readFully(data);
        return new String(data, StandardCharsets.UTF_8);
    }
}

