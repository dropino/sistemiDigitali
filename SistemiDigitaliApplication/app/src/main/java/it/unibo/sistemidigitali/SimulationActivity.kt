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

        var classification : TextView = findViewById(R.id.classification)
        val classified : String? = intent.getStringExtra("classified")
        val image_class : String? = "Class" + Simulation.printClass(classified)
        classification.text = image_class

        val distance : EditText = findViewById(R.id.editDistance)
        val magnitude : EditText = findViewById(R.id.editMagnitude)

        val simulate : Button = findViewById(R.id.runSimulation)
        val conclusion : TextView = findViewById(R.id.conclusion)
        var text : String
        conclusion.visibility= View.INVISIBLE
        simulate.setOnClickListener{
            val r : String? = distance.text.toString()
            val mw : String? = magnitude.text.toString()
            text = if(r.isNullOrEmpty() || mw.isNullOrEmpty()){
                "Distance and magnitude cannot be null"
            }else {
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