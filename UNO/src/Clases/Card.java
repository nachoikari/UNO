package Clases;

/**
 *
 * @author Joseph
 * 
 * Representa una carta del juego, con un color y un valor.
 * Esta clase proporciona métodos para acceder a las propiedades de la carta,
 * así como para obtener una descripción de la carta.
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
