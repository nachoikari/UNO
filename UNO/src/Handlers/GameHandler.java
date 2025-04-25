package Handlers;

import Clases.Player;
import Clases.Card;
import java.util.Collections;
import java.util.Stack;
import Mains.Server;

/**
 *
 * @author Isaac 🔁
 */
public class GameHandler {

    private static Stack<Card> deck = new Stack<>();
    private static Stack<Card> discardStack = new Stack<>();
    private static int totalPlayers;
    private static int currentTurn;
    private static boolean isReverse;
    private static String actualColor;
    public static Card top;

    private static void init() {
        currentTurn = 1;
        totalPlayers = Server.players.size();
        isReverse = false;
        top = null;
        actualColor = null;
        assignTurns();
    }

    private static void assignTurns() {
        int turn = 1;
        for (int i = 0; i < Server.players.size(); i++) {
            Server.players.get(i).setTurn(turn);
            turn++;
        }
    }

    public static void startGame() {
        init();
        createDeck();
        dealCards();
        System.out.println("Iniciando el juego...");
        ClientHandler.broadcast("START_CARD: " + top.getDescripcion());
        notifyPlayersIsYourTurnToPlay();
        sendInitialHands();
    }

    private static boolean isMyturn(Player player) {
        System.out.println("Turno actual: " + currentTurn);
        System.out.println("Turno del jugador que solicita: " + player.getTurn());
        if (player.getTurn() != currentTurn) {
            System.out.println("No su turno");
            return false;
        }
        return true;
    }

    public static boolean handlePlay(String playerName, String color, String value, String chosenColor) {
        System.out.println(playerName + " lanza la carta: " + color + " " + value);

        Player player = getPlayerByName(playerName);
        if (!isMyturn(player)) {
            return false;
        }
        Card playedCard = player.getCard(color, value);

        if (playedCard == null) {
            System.out.println("El jugador no tiene esa carta.");
            return false;
        }

        if (!canPlayThisCard(playedCard, chosenColor)) {
            System.out.println("No se puede jugar esa carta");
            return false;
        }

        //La carta existe y se puede jugar
        int category = whichIsTheCardtype(playedCard); //1: Numerica, 2: Especial, 3: Comodin
        switch (category) {
            case 1:
                handleNumericCard(playedCard, player);
                break;
            case 2:
                handleSpecialCard(playedCard, player);
                break;
            case 3:
                handleWildCard(playedCard, player);
                actualColor = chosenColor;
                // Notificar a todos que el color cambió
                ClientHandler.broadcast("COLOR_CHANGED_TO: " + chosenColor);
                break;
            default:
                System.out.println("Mi loco, imposible :0 - GameHandler/handlePlay");
                break;
        }

        if (player.getHand().isEmpty()) {
            ClientHandler.broadcast("WINNER: " + player.getName());
        }
        return true;
    }

    private static int whichIsTheCardtype(Card card) {
        String values[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "SKIP", "REVERSE", "+2", "WILD", "+4"};
        int i;
        for (i = 0; i < 15; i++) {
            if (card.getValue().equals(values[i])) {
                break;
            }
        }
        if (i <= 9) {
            return 1; //1 para numeros
        }
        if (i <= 12) {
            return 2; //2 para especiales
        }
        return 3; // es comodin si o si
    }

    private static boolean canPlayThisCard(Card played, String chosenColor) {
        // Comodines siempre son válidos
        if (played.getColor().equals("N")) {
            String colors[] = {"R","A","V","Y"};
            for(String color : colors){
                if(chosenColor.equals(color)){
                    return true;
                }
            }
            
            return false;
        }
        // Mismo color
        if (played.getColor().equals(actualColor)) {
            return true;
        }
        //mismo indice
        if (played.getValue().equals(top.getValue())) {
            return true;
        }
        return false;
    }

    private static void createDeck() {
        String colors[] = {"R", "A", "V", "Y", "N"}; //Rojo, Azul, Verde, Yellow, Negro
        String values[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "SKIP", "REVERSE", "+2", "WILD", "+4"};

        for (int j = 0; j < 4; j++) {
            deck.push(new Card(colors[j], values[0]));
            for (int i = 1; i <= 9; i++) {
                deck.push(new Card(colors[j], values[i]));
                deck.push(new Card(colors[j], values[i]));
            }
        }

        Collections.shuffle(deck);
        top = deck.pop();
        actualColor = top.getColor();
        discardStack.push(top);

        for (int j = 0; j < 4; j++) {
            for (int i = 10; i <= 12; i++) {
                deck.push(new Card(colors[j], values[i]));
                deck.push(new Card(colors[j], values[i]));
            }
        }

        for (int i = 13; i <= 14; i++) {
            deck.push(new Card(colors[4], values[i]));
            deck.push(new Card(colors[4], values[i]));
        }
        Collections.shuffle(deck);
    }

    private static void dealCards() {
        for (Player player : Server.players) {
            for (int i = 1; i <= 7; i++) {
                player.insertCard(deck.pop());
            }
        }
    }

    public static Player getPlayerByName(String name) {
        for (Player p : Server.players) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    private static void handleNumericCard(Card played, Player player) {
        discardStack.push(played);
        player.deleteCard(played);
        top = played;
        actualColor = played.getColor();
        ClientHandler.broadcast(player.getName() + " lanza: " + played.getColor() + ":" + played.getValue());
        nextTurn(1);
    }

    private static void handleWildCard(Card played, Player player) {
        discardStack.push(played);
        player.deleteCard(played);
        top = played;
        // Si es WILD +4, que el siguiente jugador robe
        if (played.getValue().equals("+4")) {
            int next;
            if (isReverse) {
                if (currentTurn == 1) {
                    next = totalPlayers - 1;
                } else {
                    next = currentTurn - 2;
                }
            } else {
                if (currentTurn == totalPlayers) {
                    next = 1;
                } else {
                    next = currentTurn;
                }
            }
            Player nextPlayer = Server.players.get(next);
            drawCards(nextPlayer, 4);
            nextTurn(2);
            ClientHandler.broadcast(player.getName() + " lanza: " + played.getValue()+ " a "+ nextPlayer.getName());
            return;
        }
        ClientHandler.broadcast(player.getName() + " lanza: " + played.getValue());
        nextTurn(1);
    }

    private static void handleSpecialCard(Card played, Player player) {
        discardStack.push(played);
        player.deleteCard(played);
        top = played;
        actualColor = played.getColor();

        if (played.getValue().equals("REVERSE")) {
            ClientHandler.broadcast(player.getName() + " lanza: " + played.getColor() + ":" + played.getValue());
            isReverse = !isReverse;
            nextTurn(1);
            return;
        }
        if (played.getValue().equals("SKIP")) {
            ClientHandler.broadcast(player.getName() + " lanza: " + played.getColor() + ":" + played.getValue());
            nextTurn(2);
        } else if (played.getValue().equals("+2")) {
            int next;
            if (isReverse) {
                if (currentTurn == 1) {
                    next = totalPlayers - 1;
                } else {
                    next = currentTurn - 2;
                }
            } else {
                if (currentTurn == totalPlayers) {
                    next = 1;
                } else {
                    next = currentTurn;
                }
            }
            Player nextPlayer = Server.players.get(next);
            drawCards(nextPlayer, 2);
            ClientHandler.broadcast(player.getName() + " lanza: " + played.getColor() + ":" + played.getValue()+ " a "+ nextPlayer.getName());
            nextTurn(2);
        }

    }

    private static void nextTurn(int turnAdvance) {
        if (!isReverse) {
            for (int i = 0; i < turnAdvance; i++) {
                if (currentTurn == totalPlayers) {
                    currentTurn = 1;
                } else {
                    currentTurn++;
                }
            }
        } else {
            if (currentTurn == 1) {
                currentTurn = totalPlayers;
            } else {
                currentTurn--;
            }
        }
        notifyPlayersIsYourTurnToPlay();
    }

    private static void notifyPlayersIsYourTurnToPlay() {
        for (int i = 0; i < totalPlayers; i++) {
            Player actual = Server.players.get(i);
            ClientHandler playerConnection = actual.getClientConnection();
            if (i + 1 == currentTurn) {
                System.out.println("Turno asignado a: " + actual.getName());
                playerConnection.sendMessage("Es tu turno");
            } else {
                playerConnection.sendMessage("Aun no es tu turno");
            }
        }
    }

    private static void drawCards(Player player, int quantityCards) {
        for (int i = 1; i <= quantityCards; i++) {
            player.insertCard(deck.pop());
        }
    }

    public static void handleDraw(String playerName, ClientHandler connection) {
        Player player = getPlayerByName(playerName);
        if (player == null) {
            return;
        }
        if (!isMyturn(player)) {
            return;
        }

        Card card = deck.pop();
        player.insertCard(card);
        String mensaje = "DREW: " + card.getDescripcion();
        connection.sendMessage(mensaje);
    }

    public static void rewriteHand(String playerName, ClientHandler connection) {
        Player player = getPlayerByName(playerName);
        StringBuilder handMessage = new StringBuilder("HAND: ");

        for (Card card : player.getHand()) {
            handMessage.append(card.getDescripcion());
            handMessage.append(", ");
        }
        handMessage.setLength(handMessage.length() - 2);
        connection.sendMessage(handMessage.toString());
    }

    private static void sendInitialHands() {
        for (Player player : Server.players) {
            ClientHandler playerConnection = player.getClientConnection();
            StringBuilder handMessage = new StringBuilder("HAND: ");

            for (Card card : player.getHand()) {
                handMessage.append(card.getDescripcion());
                handMessage.append(", ");
            }
            handMessage.setLength(handMessage.length() - 2);
            playerConnection.sendMessage(handMessage.toString());
            playerConnection.sendMessage(" ");
            playerConnection.sendMessage("ESCRIBE 'HELP' PARA VER LOS COMANDOS DISPONIBLES");
        }
    }
}
