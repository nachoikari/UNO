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
    
    public Player(String pName, Flow pFlow){
        this.name = pName;
        this.flow = pFlow;
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

    public Flow getHandler() {
        return flow;
    }

    public void setHandler(Flow handler) {
        this.flow = handler;
    }
    
}
