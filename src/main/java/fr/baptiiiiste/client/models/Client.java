package fr.baptiiiiste.client.models;

import fr.baptiiiiste.common.interfaces.PacketHandler;
import fr.baptiiiiste.common.models.packets.Packet;
import fr.baptiiiiste.client.listeners.PacketListener;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Getter
public class Client {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private String host;
    private int port;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PacketListener listener;
    private Thread listenerThread;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect(PacketHandler uiHandler) throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        listener = new PacketListener(in, uiHandler);
        listenerThread = new Thread(listener);
        listenerThread.start();
    }

    public void sendPacket(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (Exception e) {
            logger.error("[sendPacket] " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (listener != null) {
                listener.stop();
            }
            if (listenerThread != null) {
                listenerThread.interrupt();
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            logger.error("[disconnect] " + e.getMessage());
        }
    }
}