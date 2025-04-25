package Clases;

/**
 *
 * @author Joseph
 */

public class Card {
    private String color;
    private String value;

    public Card(String color, String value) {
        this.color = color;
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public String getValue() {
        return value;
    }
    
    public String getDescripcion() {
        return color+":"+value;
    }
}
