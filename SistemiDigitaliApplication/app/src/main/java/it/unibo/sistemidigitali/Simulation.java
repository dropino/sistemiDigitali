package it.unibo.sistemidigitali;

public class Simulation {

     private double[][] values = {{
            0.296, 2.30},   //arch bridges
            {0.456, 2.30},  //castles
            {0.890, 3.00},  //churches
            {0.776, 2.30},  //towers
            {0.456, 2.30}}; //triumphal arches

    //calcolo dell'intensità macrosismica. Formula Faccioli-Cauzzi - input: raggio e magnitudo
    public double calculateImm(double mw, double r){
        double imm = 1.0157 + 1.2566 * mw - 0.6547 * (Math.log(Math.sqrt(Math.pow(r,2) + 4)));
        return imm;
    }

    //calcolo del livello di danno
    public double calculateMeanDamage(double v, double q, double imm){
        double damage = 2.5 * (1.0 + Math.tanh((imm + 6.25*v - 13.1)/q));
        return damage;
    }

    public void runSimulation(String category, double mw, double r){
        double v, q = 0;
        if(category.equals("archbridge")) {
            v = values[0][0];
            q = values[0][1];
        }
        else if(category.equals("castle")) {
            v = values[1][0];
            q = values[1][1];
        }
        else if(category.equals("church")) {
            v = values[2][0];
            q = values[2][1];
        }
        else if(category.equals("tower")) {
            v = values[3][0];
            q = values[3][1];
        }
        else if(category.equals("triumphalarch")) {
            v = values[4][0];
            q = values[4][1];
        }
        else {
            v = 0;
            q = 2.30;
        }
        double imm = calculateImm(mw, r);
        double damage = calculateMeanDamage(v, q, imm);

    }
}
