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
    public static Stack<Card> deck = new Stack<>();
    public static Vector<Player> players = new Vector();
    public static int minPlayers = 4;
    public static boolean gameStarted = false;
    public static void main(String args[]){
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(8000);
            System.out.println("El servidor ha iniciado.... esperando usuarios");
            
        }catch(IOException e){
            System.out.println("Comunicacion rechazada: "+e);
        }
        while(true){
            try{
                Socket clientSocket = serverSocket.accept();
                DataInputStream input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                String name = input.readUTF();
                System.out.println("Conexion aceptada: "+name);
                
                Flow flow = new Flow(clientSocket, name);
                Player newPlayer = new Player(name, flow);
                players.add(newPlayer);
                flow.start();
                if(!gameStarted && players.size() == minPlayers && !players.isEmpty()){
                    startGame();
                }
            }catch(IOException e){
                System.out.println("Error en la conexion: "+ e);
            }
        }
    }
    private static void startGame(){
        gameStarted = true;
        GameHandler.startGame();
        
    }
}
