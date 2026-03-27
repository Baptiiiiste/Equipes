package fr.baptiiiiste.client.ui.panels;

import fr.baptiiiiste.client.models.ClientRoom;
import fr.baptiiiiste.client.ui.core.SessionManager;
import fr.baptiiiiste.common.models.packets.JoinRoomPacket;
import fr.baptiiiiste.common.models.packets.JoinMeetingPacket;
import fr.baptiiiiste.common.models.packets.LeaveMeetingPacket;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainContentPanel extends JPanel {
    private final MeetingControlsPanel meetingControlsPanel;
    private final JPanel chatCenterPanel;
    private final JPanel meetingCenterPanel;

    @Getter
    private final ChatPanel chatPanel;

    @Getter
    private final ConnectedUsersSidebarPanel connectedUsersSidebarPanel;

    private final Set<String> roomsWithActiveMeeting = new HashSet<>();
    private final Set<String> roomsWhereCurrentUserIsInMeeting = new HashSet<>();
    private final Map<String, Set<String>> meetingParticipantsByRoom = new HashMap<>();

    private final CardLayout centerCardLayout;
    private final JPanel centerPanel;
    private final JPanel rightPanel;

    private static final String CENTER_EMPTY = "CENTER_EMPTY";
    private static final String CENTER_CHAT = "CENTER_CHAT";
    private static final String CENTER_MEETING_EMPTY = "CENTER_MEETING_EMPTY";

    public MainContentPanel() {
        setLayout(new BorderLayout());

        centerCardLayout = new CardLayout();
        centerPanel = new JPanel(centerCardLayout);
        rightPanel = new JPanel(new BorderLayout());
        meetingControlsPanel = new MeetingControlsPanel();
        meetingControlsPanel.setOnPrimaryAction(this::handleMeetingPrimaryAction);
        meetingControlsPanel.setOnLeaveAction(this::handleMeetingLeaveAction);

        // Empty panel
        JPanel emptyCenterPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Choose a room", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        emptyCenterPanel.add(titleLabel, BorderLayout.CENTER);

        chatPanel = new ChatPanel();
        chatCenterPanel = new JPanel(new BorderLayout());
        chatCenterPanel.add(chatPanel, BorderLayout.CENTER);

        // Meeting center panel (intentionally empty for now)
        meetingCenterPanel = new JPanel();

        connectedUsersSidebarPanel = new ConnectedUsersSidebarPanel();

        centerPanel.add(emptyCenterPanel, CENTER_EMPTY);
        centerPanel.add(chatCenterPanel, CENTER_CHAT);
        centerPanel.add(meetingCenterPanel, CENTER_MEETING_EMPTY);

        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        setNoRoomLayout();
    }

    public void updateDisplay() {
        ClientRoom selectedRoom = SessionManager.getSelectedRoom();

        if (selectedRoom != null) {
            connectedUsersSidebarPanel.clearUsers();

            JoinRoomPacket joinPacket = new JoinRoomPacket(
                    System.currentTimeMillis(),
                    SessionManager.getUsername(),
                    selectedRoom.getRoomId()
            );
            SessionManager.getClient().sendPacket(joinPacket);

            chatPanel.setCurrentRoom(selectedRoom);
            applyRoomLayout(selectedRoom.getRoomId());
            refreshMeetingControls(selectedRoom.getRoomId());
        } else {
            setNoRoomLayout();
        }

        revalidate();
        repaint();
    }

    public void onMeetingStarted(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        roomsWithActiveMeeting.add(roomId);
        meetingParticipantsByRoom.putIfAbsent(roomId, new HashSet<>());
        applyRoomLayoutIfSelected(roomId);
    }

    public void onMeetingStopped(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        roomsWithActiveMeeting.remove(roomId);
        roomsWhereCurrentUserIsInMeeting.remove(roomId);
        meetingParticipantsByRoom.remove(roomId);
        applyRoomLayoutIfSelected(roomId);
    }

    public void onMeetingParticipantJoined(String roomId, String userId) {
        if (roomId == null || roomId.isBlank() || userId == null || userId.isBlank()) {
            return;
        }

        Set<String> participants = meetingParticipantsByRoom.computeIfAbsent(roomId, ignored -> new HashSet<>());
        participants.add(userId);
        roomsWithActiveMeeting.add(roomId);

        if (SessionManager.getUsername() != null && SessionManager.getUsername().equals(userId)) {
            roomsWhereCurrentUserIsInMeeting.add(roomId);
        }

        applyRoomLayoutIfSelected(roomId);
    }

    public void onMeetingParticipantLeft(String roomId, String userId) {
        if (roomId == null || roomId.isBlank() || userId == null || userId.isBlank()) {
            return;
        }

        Set<String> participants = meetingParticipantsByRoom.computeIfAbsent(roomId, ignored -> new HashSet<>());
        participants.remove(userId);

        if (SessionManager.getUsername() != null && SessionManager.getUsername().equals(userId)) {
            roomsWhereCurrentUserIsInMeeting.remove(roomId);
        }

        if (participants.isEmpty()) {
            roomsWithActiveMeeting.remove(roomId);
        }

        applyRoomLayoutIfSelected(roomId);
    }

    private void applyRoomLayoutIfSelected(String roomId) {
        ClientRoom selectedRoom = SessionManager.getSelectedRoom();
        if (selectedRoom == null || !roomId.equals(selectedRoom.getRoomId())) {
            return;
        }

        applyRoomLayout(roomId);
        refreshMeetingControls(roomId);
        revalidate();
        repaint();
    }

    private void applyRoomLayout(String roomId) {
        if (roomsWhereCurrentUserIsInMeeting.contains(roomId)) {
            setMeetingLayout();
            return;
        }

        setChatWithUsersLayout();
    }

    private void setNoRoomLayout() {
        meetingControlsPanel.setRoomSelected(false);
        chatPanel.setMeetingStyle(false);
        chatCenterPanel.remove(meetingControlsPanel);
        meetingCenterPanel.removeAll();
        centerCardLayout.show(centerPanel, CENTER_EMPTY);
        rightPanel.removeAll();
        rightPanel.setVisible(false);
    }

    private void setChatWithUsersLayout() {
        meetingControlsPanel.setRoomSelected(true);
        chatPanel.setMeetingStyle(false);
        chatCenterPanel.removeAll();
        chatCenterPanel.add(meetingControlsPanel, BorderLayout.NORTH);
        chatCenterPanel.add(chatPanel, BorderLayout.CENTER);

        centerCardLayout.show(centerPanel, CENTER_CHAT);
        rightPanel.removeAll();
        rightPanel.add(connectedUsersSidebarPanel, BorderLayout.CENTER);
        rightPanel.setVisible(true);
    }

    private void setMeetingLayout() {
        meetingControlsPanel.setRoomSelected(true);
        chatPanel.setMeetingStyle(true);
        meetingCenterPanel.removeAll();
        meetingCenterPanel.setLayout(new BorderLayout());

        JPanel controlsBand = new JPanel(new BorderLayout());
        controlsBand.setBorder(BorderFactory.createEmptyBorder(24, 24, 12, 24));
        controlsBand.add(meetingControlsPanel, BorderLayout.CENTER);
        meetingCenterPanel.add(controlsBand, BorderLayout.NORTH);

        JPanel screenShareContainer = new JPanel(new GridBagLayout());
        screenShareContainer.setBorder(BorderFactory.createEmptyBorder(12, 24, 24, 24));

        JPanel screenSharePlaceholder = new JPanel();
        screenSharePlaceholder.setPreferredSize(new Dimension(640, 360));
        screenSharePlaceholder.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 2));
        screenSharePlaceholder.setOpaque(false);

        screenShareContainer.add(screenSharePlaceholder);
        meetingCenterPanel.add(screenShareContainer, BorderLayout.CENTER);

        centerCardLayout.show(centerPanel, CENTER_MEETING_EMPTY);

        JPanel meetingRightPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        meetingRightPanel.setPreferredSize(new Dimension(250, 0));
        meetingRightPanel.add(connectedUsersSidebarPanel);
        meetingRightPanel.add(chatPanel);

        rightPanel.removeAll();
        rightPanel.add(meetingRightPanel, BorderLayout.CENTER);
        rightPanel.setVisible(true);
    }

    private void refreshMeetingControls(String roomId) {
        boolean isMeetingActive = roomsWithActiveMeeting.contains(roomId);
        boolean currentUserInMeeting = roomsWhereCurrentUserIsInMeeting.contains(roomId);
        int participantCount = meetingParticipantsByRoom.getOrDefault(roomId, Collections.emptySet()).size();
        meetingControlsPanel.updateState(isMeetingActive, currentUserInMeeting, participantCount);
    }

    private void handleMeetingPrimaryAction() {
        ClientRoom selectedRoom = SessionManager.getSelectedRoom();
        if (selectedRoom == null) {
            return;
        }

        String roomId = selectedRoom.getRoomId();
        if (roomsWhereCurrentUserIsInMeeting.contains(roomId)) {
            return;
        }

        String username = SessionManager.getUsername();
        if (username != null && !username.isBlank()) {
            onMeetingParticipantJoined(roomId, username);
        }

        SessionManager.getClient().sendPacket(new JoinMeetingPacket(
                System.currentTimeMillis(),
                SessionManager.getUsername(),
                roomId
        ));
    }

    private void handleMeetingLeaveAction() {
        ClientRoom selectedRoom = SessionManager.getSelectedRoom();
        if (selectedRoom == null) {
            return;
        }

        String roomId = selectedRoom.getRoomId();
        if (!roomsWhereCurrentUserIsInMeeting.contains(roomId)) {
            return;
        }

        String username = SessionManager.getUsername();
        if (username != null && !username.isBlank()) {
            onMeetingParticipantLeft(roomId, username);
        }

        SessionManager.getClient().sendPacket(new LeaveMeetingPacket(
                System.currentTimeMillis(),
                SessionManager.getUsername(),
                roomId
        ));
    }
}