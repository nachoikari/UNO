package Handlers;

import Clases.Card;
import Clases.Player;
import GUI.ServerGUI;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import Mains.Server;
import java.util.Collections;

/**
 *
 * @author Isaac
 */
public class ClientHandler extends Thread {

    private DataInputStream input;
    private DataOutputStream output;
    private Socket clientSocket = null;
    private String playerName;

    public ClientHandler(Socket pClientSocket, String pName) {
        this.clientSocket = pClientSocket;
        this.playerName = pName;
        try {
            this.input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            this.output = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("Error al inicializar flujos para " + playerName + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        sendMessage("Hola " + playerName);

        try {
            while (true) {
                String message = input.readUTF();
                System.out.println("Mensaje recibido de " + playerName + ": " + message);
                String parts[] = message.split(":");
                switch (parts[0]) {
                    case "PLAY" -> {
                        String color = (parts.length > 1) ? parts[1] : null;
                        String index = (parts.length > 2) ? parts[2] : null;
                        String chosenColor = (parts.length > 3) ? parts[3] : null;
                        
                        String result = GameHandler.handlePlay(playerName, color, index, chosenColor);
                        
                        if (!result.equals("OK")) {
                            sendMessage("Error: " + result);
                        }
                        
                        sendMessage("Carta actual: " + GameHandler.top.getDescripcion());
                        GameHandler.rewriteHand(playerName, this);
                    }
                    case "DRAW" -> {
                        GameHandler.handleDraw(playerName, this);
                        sendMessage("carta actual: "+ GameHandler.top.getDescripcion());
                        GameHandler.rewriteHand(playerName, this);
                    }
                    case "UNO" -> {
                        ClientHandler.broadcast("¡" + playerName + " ha dicho UNO!");
                        break;
                    }
                    case "EXIT" -> {
                    sendMessage("Saliendo del juego. ¡Hasta luego!");
                        input.close();
                        output.close();
                        clientSocket.close();

                        ClientHandler.broadcast(playerName + " ha salido del juego.");
                        Server.removePlayer(playerName); // quita del ArrayList

                        if (ServerGUI.instance != null) {
                            ServerGUI.instance.removePlayer(playerName); // quita de la GUI
                        }
                        return;
                    }
                    case "HELP" -> {
                        sendMessage("los comandos estan separados por ':'");
                        sendMessage("PLAY:charCarta:elValor:colorCambiar ---> Jugar una carta de la mano");
                        sendMessage("DRAW                                ---> Toma una carta de la pila");
                        sendMessage("UNO                                 ---> Notifica el UNO a toda la mesa. No hace nada xd");
                        sendMessage("EXIT                                ---> Cierra la conexion");
                        GameHandler.rewriteHand(playerName, this);
                    }
                    default -> {
                        sendMessage("Comando no reconocido.");
                        sendMessage("carta actual: "+ GameHandler.top.getDescripcion());
                        GameHandler.rewriteHand(playerName, this);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error recibiendo el mensaje " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            synchronized (output) {
                output.writeUTF(message);
                output.flush();
            }
        } catch (IOException e) {
            System.out.println("Error al enviar mensaje a " + playerName + ": " + e.getMessage());
        }
    }

    public static void broadcast(String message) {
        synchronized (Server.players) {
            for (Player player : Server.players) {
                ClientHandler connection = player.getClientConnection();
                try {
                    synchronized (connection.output) {
                        connection.output.writeUTF(message);
                        connection.output.flush();
                    }
                } catch (IOException ioe) {
                    System.out.println("Error broadcasting to " + player.getName() + ": " + ioe.getMessage());
                }
            }
        }
    }
}
