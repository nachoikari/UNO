package uno;

import java.util.Collections;
import java.util.Stack;

/**
 *
 * @author Isaac
 */
public class GameHandler {
    private static Stack<Card> deck = new Stack<>();
    private static Stack<Card> discardStack = new Stack<>();
    private static int current_turn = 1;
    private static boolean reverse = false;
    private static Card top=null;
    public static void startGame(){
        createDeck();
        System.out.println("Iniciando el juego...");
        
    }
    public static boolean handlePlay(String playerName, String color, String value){
        System.out.println(playerName+" lanzo la carta: "+ color+" "+value);
        Player player = getPlayerByName(playerName);
        if(player.getTurn() != current_turn){
            System.out.println("No es el turno de " + playerName);
            return false;
        }
       
        Card played = player.getCard(color, value);
        if(played == null){
            System.out.println("El jugador no tiene esa carta.");
            return false;
        }
        if(!checkPlay(played, top)){
            System.out.println("No se puede jugar esa carta");
            return false;
        }
        
        if(played.getTipo().equals("Number")){
            
            numericCard((NormalCard) played, player);
        }
        if(played.getTipo().equals("Wild")){
            wildCard((WildCard) played, player);
        }
        if(played.getTipo().equals("Acction")){
            specialCard((SpecialCard) played, player);
        }
        
        if(player.getHand().isEmpty()){
            Flow.broadcast("WINNER:" + player.getName());
        }
        return true;
    }
    
    private static boolean checkPlay(Card played,Card top){
        // Comodines siempre son válidos
        if (played instanceof WildCard) {
           return true;
        }

        // Mismo color
        if (played.getColor().equals(top.getColor())) {
           return true;
        }

        // Mismo tipo o valor
        if (played.getTipo().equals(top.getTipo())) {
            if (played instanceof NormalCard && top instanceof NormalCard) {
                return ( (NormalCard) played).getNumber() == ( (NormalCard) top).getNumber();
            } else if (played instanceof SpecialCard && top instanceof SpecialCard) {
                return ((SpecialCard) played).getEffect().equals(((SpecialCard) top).getEffect());
            }
        }

       return false;
    } 
    private static void createDeck(){
        String colors[] = {"RED", "BLUE", "GREEN", "YELLOW"};
        
        for(String color: colors){
            deck.push(new NormalCard(color,0));
             for(int i=1; i <=9;i++){
                deck.push(new NormalCard(color,i));
                deck.push(new NormalCard(color,i));
            }
        }
        
        Collections.shuffle(deck);
        top = deck.pop();
        discardStack.push(top);
        
        for(String color: colors){
            deck.push(new SpecialCard(color,"REVERSE"));
            deck.push(new SpecialCard(color,"REVERSE"));
            deck.push(new SpecialCard(color,"SKIP"));
            deck.push(new SpecialCard(color,"SKIP"));
            deck.push(new SpecialCard(color,"+2"));
            deck.push(new SpecialCard(color,"+2"));
        }
        
        for (int i = 0; i < 4; i++) {
            deck.push(new WildCard("Wild"));
            deck.push(new WildCard("Wild +4"));
        }
        Collections.shuffle(deck);
        dealCards();
    }
    private static void dealCards(){
        for(Player player : Server.players){
            for(int i =1; i<=7;i++){
                player.insertCard(deck.pop());
            }
        }
    }
    public static Player getPlayerByName(String name){
        for(Player p : Server.players){
            if(p.getName().equals(name)){
                return p;
            }
        }
        return null;
    }
    private static void numericCard(NormalCard played, Player player){
        discardStack.push(played);
        player.deleteCard(played);
        nextTurn(1);
    }
    private static void wildCard(WildCard played, Player player){
        discardStack.push(played);
        player.deleteCard(played);
        nextTurn(1);
    }
    private static void specialCard(SpecialCard played, Player player){
        discardStack.push(played);
        player.deleteCard(played);
        if (played.getEffect().equals("REVERSE")) {
            reverse = !reverse;
            nextTurn(1);
        } else if (played.getEffect().equals("SKIP")) {
            nextTurn(2);
        } else if (played.getEffect().equals("+2")) {
            
            nextTurn(1);
        }
    }
    private static void nextTurn(int turnAdvance){
        int total = Server.players.size();
        if (!reverse) {
            current_turn = (current_turn + turnAdvance) % total;
        } else {
            current_turn = (current_turn - turnAdvance + total) % total;
        }
    }
    private static void notifyNextPlayer() {
        for (Player p : Server.players) {
            Flow f = (Flow) p.getHandler();
            if (p.getTurn() == current_turn) {
                f.sendMessage("YOUR_TURN");
            } else {
                f.sendMessage("WAIT");
            }
        }
    }
}
