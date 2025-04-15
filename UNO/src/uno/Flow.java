package uno;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Isaac
 */
public class Flow extends Thread {
    
    private DataInputStream input;
    private DataOutputStream output;
    private Socket clientSocket = null;
    private String playerName;
    
    public Flow(Socket pClientSocket, String pName){
        this.clientSocket = pClientSocket;
        this.playerName = pName;
        try{
            this.input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            this.output = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
        }catch(IOException e){
            System.out.println("Error al inicializar flujos para " + playerName + ": " + e.getMessage());
        }
    }
    @Override
    public void run(){
       sendMessage("Hola "+ playerName);
       
       try{
           while(true){
               String message = input.readUTF();
               System.out.println("Mensaje recibido de " + playerName + ": " + message);
               String parts[] = message.split(":");
               switch(parts[0]){
                   case "PLAY":
                       String color = parts[1];
                       String value = parts[2];
                       //String playerName, String color, String value
                       GameHandler.handlePlay(playerName, color, value);
                       break;
                    case "DRAW":
                        
                        break;
                    case "UNO":
                        // GameHandler.handleUno(...)
                        break;
                    case "EXIT":
                        // cerrar conexión
                        break;
                    default:
                        sendMessage("Comando no reconocido.");
               }
           }
       } catch (IOException e) {
           System.out.println("Error recibiendo el mensaje "+ e.getMessage());
       }
    }
    public void sendMessage(String message){
        try{
            synchronized (output){
                output.writeUTF(message);
                output.flush();
            }
        }catch(IOException e){
            System.out.println("Error al enviar mensaje a " + playerName + ": " + e.getMessage());
        }
    }
    public static void broadcast(String message) {
        synchronized (Server.players) {
            Enumeration e = Server.players.elements();
            while (e.hasMoreElements()) {
                Player user = (Player) e.nextElement();
                Flow f = (Flow) user.getHandler();
                try {
                    synchronized (f.output) {
                        f.output.writeUTF(message);
                        f.output.flush();
                    }
                } catch (IOException ioe) {
                    System.out.println("Error broadcasting to " + user.getName() + ": " + ioe.getMessage());
                }
            }
        }
    }
}
