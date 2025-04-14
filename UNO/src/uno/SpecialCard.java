/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno;

/**
 *
 * @author Isaac
 */
public class SpecialCard extends Card {
    private String effect;
    public SpecialCard(String pColor, String pEffect){
        super(pColor, "Action");
        this.effect = pEffect;
    }
    public String getEffect(){
        return this.effect;
    }
    @Override
    public String getDescripcion() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
