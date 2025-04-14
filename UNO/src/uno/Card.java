/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno;

/**
 *
 * @author Isaac
 */
public abstract class Card {
    protected String color;
    
    protected String type;
    public Card(String color, String pType) {
        this.color = color;
        this.type = pType;
    }

    public String getColor() {
        return color;
    }

    public String getTipo() {
        return type;
    }

    public abstract String getDescripcion();
}
