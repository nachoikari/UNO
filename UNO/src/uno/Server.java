package uno;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;
import java.util.Vector;

/**
 *
 * @author Isaac
 */
public class Server {
    public static Vector<Player> players = new Vector();
    public static int minPlayers = 2;
    public static boolean gameStarted = false;
    private static int turn = 0;
    public static void main(String args[]){

        ServerUI.instance = new ServerUI();
        ServerUI.instance.setVisible(true);

        new Thread(() -> startServer()).start();
    }

    private static void startServer(){
        try (ServerSocket serverSocket = new ServerSocket(8000)) {
            ServerUI.instance.log("Servidor iniciado en el puerto 8000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                DataInputStream input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                String name = input.readUTF();

                ServerUI.instance.log("Conexión aceptada: " + name);
                turn++;
                Flow flow = new Flow(clientSocket, name);
                Player newPlayer = new Player(name, flow,turn);
                players.add(newPlayer);

                ServerUI.instance.addPlayer(name); 
                flow.start(); 
            }
        } catch (IOException e) {
            ServerUI.instance.log("Error en la conexión: " + e.getMessage());
        }
    }

    public static int getTurn() {
        return turn;
    }

    public static void setTurn(int turn) {
        Server.turn = turn;
    }
    
}
