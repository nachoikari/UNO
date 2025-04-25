package Clases;

import Handlers.ClientHandler;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Isaac
 */
public class Player {

    private String name;
    private List<Card> hand;
    private ClientHandler conection;
    private int turn;

    public Player(String pName, ClientHandler pConection) {
        this.name = pName;
        this.conection = pConection;
        this.hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public void setHand(List<Card> hand) {
        this.hand = hand;
    }

    public ClientHandler getClientConnection() {
        return conection;
    }

    public void setHandler(ClientHandler handler) {
        this.conection = handler;
    }

    public void insertCard(Card card) {
        this.hand.add(card);
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public Card getCard(String color, String value) {
        for (Card card : hand) {
            if (card.getColor().equals(color)) {
                if (card.getValue().equals(value)) {
                    return card;
                }
            }
        }
        return null;
    }

    public void deleteCard(Card card) {
        this.hand.remove(card);
    }
}
