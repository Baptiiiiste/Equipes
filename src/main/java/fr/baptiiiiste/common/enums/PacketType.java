package fr.baptiiiiste.common.enums;

public enum PacketType {

    // Rooms
    TEXT,
    JOIN_ROOM,
    LEAVE_ROOM,

    // Meetings
    JOIN_MEETING,
    LEAVE_MEETING,
    MEETING_STARTED,
    MEETING_STOPPED,

    // Audio
    AUDIO_UDP_OFFER,
    AUDIO_UDP_ACCEPT,
    AUDIO_START,
    AUDIO_STOP,

    // Screen sharing
    SCREEN_SHARE_START,
    SCREEN_SHARE_STOP,
    SCREEN_SHARE_FRAME


}
