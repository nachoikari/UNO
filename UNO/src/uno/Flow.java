package uno;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;

/**
 *
 * @author Isaac
 */
public class Flow extends Thread {
    
    private DataInputStream read_flow;
    private DataOutputStream write_flow;
    private Socket clientSocket = null;
    private String playerName;
    
    public Flow(Socket pClientSocket, String pName){
        this.clientSocket = pClientSocket;
        this.playerName = pName;
        try {
            read_flow = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            write_flow = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error initializing I/O streams for " + playerName + ": " + e.getMessage());
        }
    }
    @Override
    public void run(){
        sendMessage("Welcome to UNO, " + playerName);
        while (true) {
            try {
                String message = read_flow.readUTF();

                if (message.startsWith("PLAY:")) {
                    handlePlayCommand(message);
                } else if (message.equals("DRAW")) {
                    //GameManager.handleDraw(playerName);
                } else if (message.equals("UNO")) {
                    //GameManager.handleUnoDeclaration(playerName);
                } else if (message.equals("EXIT")) {
                    //GameManager.removePlayer(playerName);
                    break;
                } else {
                    sendMessage("Unknown command.");
                }

            } catch (IOException e) {
                System.out.println("Connection lost with " + playerName);
                //GameManager.removePlayer(playerName);
                break;
            }
        }
    }
    private void handlePlayCommand(String command){
        String[] parts = command.split(":");
        if (parts.length < 3) {
            sendMessage("Invalid PLAY format. Use: PLAY:Color:Value");
            return;
        }
        String color = parts[1];
        String value = parts[2];
        GameHandler.handlePlay(playerName, color, value);
    }
    public void sendMessage(String mensaje) {
        try {
            synchronized (write_flow) {
                write_flow.writeUTF(mensaje);
                write_flow.flush();
            }
        } catch (IOException ioe) {
            System.out.println("Error sending message to " + playerName + ": " + ioe.getMessage());
        }
    }

    
    
    public void broadcast(String message) {
        synchronized (Server.players) {
            Enumeration e = Server.players.elements();
            while (e.hasMoreElements()) {
                Player user = (Player) e.nextElement();
                Flow f = (Flow) user.getHandler();
                try {
                    synchronized (f.write_flow) {
                        f.write_flow.writeUTF(message);
                        f.write_flow.flush();
                    }
                } catch (IOException ioe) {
                    System.out.println("Error broadcasting to " + user.getName() + ": " + ioe.getMessage());
                }
            }
        }
    }
}
