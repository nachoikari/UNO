/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno;

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
            Socket socket = new Socket("localhost", 8000);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            Scanner scanner = new Scanner(System.in);

            System.out.print("Ingrese su nombre: ");
            String name = scanner.nextLine();
            out.writeUTF(name);
            out.flush();

            // Hilo para leer mensajes del servidor
            new Thread(() -> {
                try {
                    while (true) {
                        String serverMessage = in.readUTF();
                        System.out.println("📨 Servidor: " + serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Conexión cerrada.");
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
