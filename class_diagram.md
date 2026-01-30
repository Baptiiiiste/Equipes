classDiagram
%% Common package
class PacketType {
<<enum>>
TEXT
JOIN_ROOM
LEAVE_ROOM
}

    class PacketHandler {
        <<interface>>
        +handle(Packet packet)
    }
    
    class Packet {
        <<abstract>>
        -long timestamp
        -String senderId
        -String roomId
        -PacketType type
        +execute(PacketHandler handler)*
    }
    
    class TextPacket {
        -String message
        +execute(PacketHandler handler)
    }
    
    class JoinRoomPacket {
        +execute(PacketHandler handler)
    }
    
    class LeaveRoomPacket {
        +execute(PacketHandler handler)
    }
    
    %% Server package
    class Server {
        -int port
        -Map~String, Room~ rooms
        +start()
        +getRoom(String id) Room
        +createRoom(String id) Room
    }
    
    class Room {
        -String roomId
        -String roomName
        -List~ClientHandler~ clients
        -boolean isInMeeting
        +broadcast(Packet packet, ClientHandler sender)
        +addClient(ClientHandler client)
        +removeClient(ClientHandler client)
    }
    
    class ClientHandler {
        -Socket socket
        -Server server
        -Room currentRoom
        -ObjectOutputStream out
        -ObjectInputStream in
        -String clientId
        -boolean isRunning
        +run()
        +sendPacket(Packet packet)
        +joinRoom(String roomId)
        +leaveCurrentRoom()
        -closeConnection()
    }
    
    %% Client package
    class Client {
        -String host
        -int port
        -Socket socket
        -ObjectOutputStream out
        -ObjectInputStream in
        -PacketListener listener
        +connect()
        +sendPacket(Packet packet)
        +disconnect()
    }
    
    class PacketListener {
        -ObjectInputStream in
        -PacketHandler uiHandler
        -boolean isRunning
        +run()
        +stop()
    }
    
    %% Relations
    Packet <|-- TextPacket
    Packet <|-- JoinRoomPacket
    Packet <|-- LeaveRoomPacket
    
    Server "1" *-- "*" Room : manages
    ClientHandler "0..1" --> "1" Room : currentRoom
    Room "1" o-- "*" ClientHandler : clients
    
    ClientHandler ..|> PacketHandler : implements
    ClientHandler ..|> Runnable : implements
    PacketListener ..|> Runnable : implements
    
    Client "1" *-- "1" PacketListener : owns
    PacketListener --> PacketHandler : delegates