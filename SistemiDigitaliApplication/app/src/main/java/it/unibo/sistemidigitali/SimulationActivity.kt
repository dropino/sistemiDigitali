package it.unibo.sistemidigitali

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class SimulationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulate)
        val classification : TextView = findViewById(R.id.classification)
        val imageClass : String
        val classified = intent.getStringExtra("classified")
        imageClass = if(classified.isNullOrEmpty()){
            "Something went wrong. Please, try again."
        }
        else {
            "Class: " + Simulation.printClass(classified.trim())
        }
        classification.text = imageClass
        //Valori di distanza e magnitudo letti da tastiera
        val distance : EditText = findViewById(R.id.editDistance)
        val magnitude : EditText = findViewById(R.id.editMagnitude)
        val simulate : Button = findViewById(R.id.runSimulation)
        val conclusion : TextView = findViewById(R.id.conclusion)
        var text : String
        conclusion.visibility= View.INVISIBLE
        //Lancio della simulazione
        simulate.setOnClickListener{
            val r : String = distance.text.toString()
            val mw : String = magnitude.text.toString()
            text = if(r.isEmpty() || mw.isEmpty()){
                "Distance and magnitude cannot be null"
            }else {
                //calcolo e rappresentazione del livello di danno
                val damage : Double = Simulation.runSimulation(classified.toString(), mw.toDouble(), r.toDouble())
                "Damage Level: ${damage.roundToInt()}\n" + printDamage(damage.roundToInt())
            }
            conclusion.text = text
            conclusion.visibility = View.VISIBLE
            }
    }

    private fun printDamage(damage: Int): String {
        return when(damage){
            0 -> "No damage to people and structures"
            1 -> "Slight damage, cracking of non-structural elements"
            2 -> "Moderate damage, with major damage to non-structural elements (e.g. dry walls) and minor to load bearing ones"
            3 -> "Heavy damage, significant damage to load bearing elements"
            4 -> "Very heavy damage, partial structural collapse"
            else -> "Destruction, full collapse"
        }
    }

}