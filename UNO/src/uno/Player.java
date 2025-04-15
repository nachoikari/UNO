package uno;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Isaac
 */
public class Player {
    private String name;
    private List<Card> hand;
    private Flow flow;
    private int turn;
    public Player(String pName, Flow pFlow,int pTurn){
        this.name = pName;
        this.flow = pFlow;
        this.hand = new ArrayList<>();
        this.turn = pTurn;
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

    public Flow getHandler() {
        return flow;
    }

    public void setHandler(Flow handler) {
        this.flow = handler;
    }
    public void insertCard(Card card){
        this.hand.add(card);
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
    public Card getCard(String color, String value){
        for (Card card : hand) {
            if (card.getColor().equals(color)) {
                if (card.getTipo().equals("Number")) {
                    int number = Integer.parseInt(value);
                    if (((NormalCard) card).getNumber() == number) {
                        return card;
                    }
                } else if (card.getTipo().equals("Action")) {
                    if ( ((SpecialCard) card).getEffect().equals(value)) {
                        return card;
                    }
                }
            } else if (card instanceof WildCard) {
                if (((WildCard) card).getEffect().equals(value)) {
                    return card;
                }
            }
        }
        return null;
    }
    
    public void  deleteCard(Card card){
        this.hand.remove(card);
    }
}
