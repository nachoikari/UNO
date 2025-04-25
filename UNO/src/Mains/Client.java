package Mains;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Isaac
 */
public class Client {
    public static void main(String[] args) {
        try {
            //Establecer conexion al server
            Socket socket = new Socket("localhost", 8000);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            Scanner scanner = new Scanner(System.in);
            //registro del nombre Usuario
            System.out.print("Ingrese su nombre: ");
            String name = scanner.nextLine();
            //Recibir el nombre y comunicarlo
            out.writeUTF(name);
            out.flush();

            // Hilo para leer mensajes del servidor
            new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = in.readUTF();
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("La conexión con el servidor ha sido cerrada.");
                }
            }).start();

            // Hilo principal: enviar mensajes al servidor
            while (true) {
                String command = scanner.nextLine();
                out.writeUTF(command);
                out.flush();
            }

        } catch (IOException e) {
            System.out.println("Error conectando al servidor: " + e.getMessage());
        }
    }
}
