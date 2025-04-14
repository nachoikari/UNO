package uno;

import java.util.Stack;

/**
 *
 * @author Isaac
 */
public class GameHandler {
    private static Stack<Card> deck = new Stack<>();
    private static Stack<Card> discard = new Stack<>();
    private static int current_turn = 0;
    private static boolean isReverse = false;
   
    
    public static void startGame(){
        System.out.println("Iniciando el juego...");
        
    }
    public static void handlePlay(String playerName, String color, String value){
        
    }
}
