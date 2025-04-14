/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno;

/**
 *
 * @author Isaac
 */
public class NormalCard extends Card {
    private int number;
    public NormalCard(String color, int pNumber){
        super(color, "Number");
        this.number = pNumber;
    }
    public int getNumber(){
        return this.number;
    }
    
    @Override
    public String getDescripcion() {
        return color + " " + number;
    }
    
}
