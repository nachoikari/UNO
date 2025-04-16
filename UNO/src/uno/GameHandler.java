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
    private static int current_turn = 0;
    private static boolean reverse = false;
    private static Card top=null;
    private static String actualColor;
    public static void startGame(){
        createDeck();
        System.out.println("Iniciando el juego...");
        Flow.broadcast("START_CARD:" + top.getDescripcion());
        notifyNextPlayer();
        sendInitialHands();
    }
    public static boolean handlePlay(String playerName, String color, String value, String chosenColor){
        System.out.println(playerName+" lanzo la carta: "+ color+" "+value);
        Player player = getPlayerByName(playerName);
        
        System.out.println("Turno actual: " + current_turn);
        
        System.out.println("Turno del jugador: " + player.getTurn());
        
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
        
        if (played.getTipo().equals("Number")) {
            
            numericCard((NormalCard) played, player);
            
            actualColor = played.getColor();
        } else if (played.getTipo().equals("Wild")) {
            
            ((WildCard) played).setColor(chosenColor);
            
            actualColor = chosenColor;
            
            wildCard((WildCard) played, player);
        } else if (played.getTipo().equals("Acction")) {
            
            specialCard((SpecialCard) played, player);
            
            actualColor = played.getColor();
        }
        
        if(player.getHand().isEmpty()){
            Flow.broadcast("WINNER:" + player.getName());
        }
        return true;
    }
    //Esta linea me detono, la hizo gpeto totalmente
    
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
                
                return ( (SpecialCard) played).getEffect().equals( ( (SpecialCard) top).getEffect() );
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
        actualColor = top.getColor();
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
            deck.push(new WildCard("WILD"));
            deck.push(new WildCard("WILD +4"));
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
        Flow.broadcast("PLAYED:" + player.getName() + ":" + played.getColor() + ":" + played.getNumber());
        nextTurn(1);
    }
    private static void wildCard(WildCard played, Player player){
        discardStack.push(played);
        player.deleteCard(played);
       // Notificar a todos que el color cambió
        Flow.broadcast("COLOR_CHANGED_TO:" + played.getColor());

        // Si es WILD +4, que el siguiente jugador robe
        if (played.getEffect().equals("WILD +4")) {
            int total = Server.players.size();
            int next = -1;
            if(reverse){
                next = (current_turn - 1 + total) % total;
            }else{
                next = (current_turn + 1) % total;
            }
            Player nextPlayer = Server.players.get(next);
            drawCards(nextPlayer, 4);
        }
        Flow.broadcast("PLAYED:" + player.getName() + ":" + played.getColor() + ":" + played.getEffect());
        Flow.broadcast("COLOR_CHANGED_TO:" + played.getColor());
        nextTurn(1);
    }
    private static void specialCard(SpecialCard played, Player player){
        discardStack.push(played);
        player.deleteCard(played);

        if (played.getEffect().equals("REVERSE")) {
            Flow.broadcast("PLAYED:" + player.getName() + ":" + played.getColor() + ":" + played.getEffect());
            reverse = !reverse;
            nextTurn(1);
        } else if (played.getEffect().equals("SKIP")) {
            Flow.broadcast("PLAYED:" + player.getName() + ":" + played.getColor() + ":" + played.getEffect());
            nextTurn(2);
        } else if (played.getEffect().equals("+2")) {
            int total = Server.players.size();
            int next = -1;
            if(reverse){
                next = (current_turn - 1 + total) % total;
            }else{
                next = (current_turn + 1) % total;
            }
            Player nextPlayer = Server.players.get(next);
            drawCards(nextPlayer, 2);
            Flow.broadcast("PLAYED:" + player.getName() + ":" + played.getColor() + ":" + played.getEffect());
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
        notifyNextPlayer();
    }
    private static void notifyNextPlayer() {
        for (Player p : Server.players) {
            Flow f = (Flow) p.getHandler();
            if (p.getTurn() == current_turn) {
                System.out.println("🔁 Turno asignado a: " + p.getName());
                f.sendMessage("YOUR_TURN");
            } else {
                f.sendMessage("WAIT");
            }
        }
    }
    private static void drawCards(Player player, int quantityCards){
        for(int i =1; i <= quantityCards; i++){
            player.insertCard(deck.pop());
        }
    }
    public static void handleDraw(String playerName, Flow flow){
        Player player = getPlayerByName(playerName);
        if (player == null) {
            return;
        }
        
        Card card = deck.pop();
        player.insertCard(card);
            String mensaje = "DREW:" + card.getColor() + ":";

        if (card instanceof NormalCard) {
            mensaje += ((NormalCard) card).getNumber();
        } else if (card instanceof SpecialCard) {
            mensaje += ((SpecialCard) card).getEffect();
        } else if (card instanceof WildCard) {
            mensaje += ((WildCard) card).getEffect();
        }

        flow.sendMessage(mensaje);
    }
    private static void sendInitialHands() {
        for (Player player : Server.players) {
            Flow f = (Flow) player.getHandler();
            StringBuilder handMessage = new StringBuilder("HAND:");

            for (Card card : player.getHand()) {
                handMessage.append(card.getColor()).append(":");

                if (card instanceof NormalCard) {
                    handMessage.append(((NormalCard) card).getNumber());
                } else if (card instanceof SpecialCard) {
                    handMessage.append(((SpecialCard) card).getEffect());
                } else if (card instanceof WildCard) {
                    handMessage.append(((WildCard) card).getEffect());
                }

                handMessage.append(",");
            }

            // Eliminar la última coma
            if (handMessage.length() > 5) {
                handMessage.setLength(handMessage.length() - 1);
            }

            f.sendMessage(handMessage.toString());
        }
    }
}
