/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno;

/**
 *
 * @author Isaac
 */
public class WildCard extends Card {
    private String effect;

    public WildCard(String effect) {
        super("BLACK", "Wild"); 
        this.effect = effect; 
    }

    public String getEffect() {
        return effect;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String getDescripcion() {
        return effect + " (color elegido: " + color + ")";
    }
}
