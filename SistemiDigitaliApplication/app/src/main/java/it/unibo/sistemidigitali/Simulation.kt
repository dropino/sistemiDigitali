package it.unibo.sistemidigitali

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tanh

object Simulation {

    /**
     * Matrice che riporta i valori di riferimento di vulnerabilità sismica e duttilità per ciascuna tipologia di monumento
     * */
    private val values = arrayOf(
        doubleArrayOf(0.296, 2.30),     //arch bridges
        doubleArrayOf(0.456, 2.30),     //castles
        doubleArrayOf(0.890, 3.00),     //churches
        doubleArrayOf(0.776, 2.30),     //towers
        doubleArrayOf(0.456, 2.30)      //triumphal arches
    )

    /**
     * Funzione per il calcolo dell'intensità macrosismica. Formula Faccioli-Cauzzi - input: raggio(km) e magnitudo
     */
    private fun calculateImm(mw: Double, r: Double): Double {
        return 1.0157 + 1.2566 * mw - 0.6547 * ln(sqrt(r.pow(2) + 4))
    }

    /**
     * Funzione per il calcolo del livello di danno
     */
    private fun calculateMeanDamage(v: Double, q: Double, imm: Double): Double {
        return 2.5 * (1.0 + tanh((imm + 6.25 * v - 13.1) / q))
    }
    /**
     * Funzione per il lancio della simulazione.
     * @param category : la categoria che risulta dalla predizione
     * @param mw : magnitudo - il valore è fornito da tastiera
     * @param r : raggio - il valore è fornito da tastiera
     * @return l'entità del danno
     * */
    fun runSimulation(category: String, mw: Double, r: Double): Double {
        val v: Double
        var q = 0.0
        if (category.trim() == "Arch_bridges") {
            v = values[0][0]
            q = values[0][1]
        } else if (category.trim() == "Castles") {
            v = values[1][0]
            q = values[1][1]
        } else if (category.trim() == "Churches") {
            v = values[2][0]
            q = values[2][1]
        } else if (category.trim() == "Towers") {
            v = values[3][0]
            q = values[3][1]
        } else if (category.trim() == "Triumphal_Arches") {
            v = values[4][0]
            q = values[4][1]
        } else {
            v = 0.5
            q = 2.30
        }
        val imm = calculateImm(mw, r)
        return calculateMeanDamage(v, q, imm)
    }

    fun printClass(classified: String?): String{
        var returnResult = ""
        when (classified) {
            "Arch_bridges" -> returnResult = "Arch bridges"
            "Castles" -> returnResult ="Castles"
            "Churches" -> returnResult ="Churches"
            "Towers" -> returnResult ="Towers"
            "Triumphal_Arches" -> returnResult ="Triumphal arches"
        }
        return returnResult
    }
}
