package fr.baptiiiiste.common.audio;

public enum AudioDatagramKind {
    REGISTER((byte) 1),
    AUDIO_FRAME((byte) 2);

    private final byte code;

    AudioDatagramKind(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static AudioDatagramKind fromCode(byte code) {
        for (AudioDatagramKind kind : values()) {
            if (kind.code == code) {
                return kind;
            }
        }
        return null;
    }
}

