package Mains;

import GUI.ServerGUI;
import Clases.Player;
import Handlers.ClientHandler;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Joseph
 * La clase Server se encarga de la gestión de las conexiones de los jugadores
 * y del servidor que maneja el juego.
 */
public class Server {

    public static Vector<Player> players = new Vector();
    public static ServerSocket serverSocket = null;
    public static int MINPLAYERS = 2;
    public static int MAXPLAYERS = 3;
    public static boolean gameStarted = false;
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAXPLAYERS + 3);

    public static void main(String args[]) throws IOException {
        ServerGUI.instance = new ServerGUI();
        ServerGUI.instance.setBounds(150, 150, 600, 300);
        ServerGUI.instance.setTitle("UNO: Lobby");
        ServerGUI.instance.setVisible(true);
        try {
            serverSocket = new ServerSocket(8000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (serverSocket != null) {
            ServerGUI.instance.log("Servidor iniciado en el puerto 8000");
            while (true) {
                listenPlayerPetitions();
                //cuando le dan al boton, sale aca
            }

        } else {
            ServerGUI.instance.log("Error en la conexión");
        }
    }

    private static void listenPlayerPetitions() {
        try {
            while (!gameStarted) {
                if (players.size() < MAXPLAYERS) {
                    Socket clientSocket = serverSocket.accept();
                    if (!gameStarted) {
                        threadPool.submit(() -> handleNewPlayer(clientSocket));
                    }else{
                        clientSocket.close();
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    private static void handleNewPlayer(Socket clientSocket) {
        try {
            DataInputStream input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            clientSocket.setSoTimeout(60000);
            String name = input.readUTF();
            clientSocket.setSoTimeout(0);
            synchronized (players) {
                if (players.size() < MAXPLAYERS) {
                    ClientHandler client = new ClientHandler(clientSocket, name);
                    Player newPlayer = new Player(name, client);
                    players.add(newPlayer);
                    ServerGUI.instance.addPlayer(name);
                    client.start();
                } else {
                    clientSocket.close();
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Un jugador no envió su nombre a tiempo.");
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        } catch (IOException e) {
            System.out.println("Error al manejar al jugador: " + e.getMessage());
        }
    }
    
        public static void removePlayer(String name) {
        synchronized (players) {
            players.removeIf(p -> p.getName().equals(name));
        }
    }


}
