package fr.baptiiiiste.server.persistence;

import fr.baptiiiiste.server.models.ChatMessage;

import java.util.List;

public interface ChatRepository {

    ChatMessage saveMessage(ChatMessage chatMessage);

    List<ChatMessage> findMessagesByRoomId(String roomId, int limit);
}

