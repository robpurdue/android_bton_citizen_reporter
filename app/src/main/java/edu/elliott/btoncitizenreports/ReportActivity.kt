package edu.elliott.btoncitizenreports

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import edu.elliott.btoncitizenreports.databinding.ActivityReportBinding
import kotlin.random.Random

private const val REPORT_LATITUDE = "edu.elliott.btoncitizenreports.report_latitude"
private const val REPORT_LONGITUDE = "edu.elliott.btoncitizenreports.report_longitude"
private const val REPORT_ADDRESS = "edu.elliott.btoncitizenreports.report_address"
private const val TAG = "BTONEOC"

class ReportActivity : AppCompatActivity() {
    private var latitude: String? = ""
    private var longitude: String? = ""
    private var address: String? = ""
    private lateinit var binding: ActivityReportBinding
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        latitude = intent.getStringExtra(REPORT_LATITUDE)
        longitude = intent.getStringExtra(REPORT_LONGITUDE)
        address = intent.getStringExtra(REPORT_ADDRESS)

        // AWESOME BINDING TUTORIAL
        // https://www.raywenderlich.com/6430697-view-binding-tutorial-for-android-getting-started
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.reportTextView.text = makeReportDescription()

        // SET CLICK LISTENERS FOR BUTTONS
        binding.injury.setOnClickListener { writeToFirestore("injury") }
        binding.line.setOnClickListener { writeToFirestore("line") }
        binding.outage.setOnClickListener { writeToFirestore("outage") }
        binding.fire.setOnClickListener { writeToFirestore("fire") }
        binding.blocked.setOnClickListener { writeToFirestore("blocked") }

        Log.d(TAG, latitude!!)
        Log.d(TAG, longitude!!)
        Log.d(TAG, address!!)

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Firestore
        firestore = initFirestore()

    }

    private fun makeReportDescription() : String {
        var desc = "You are creating a report for the following location:\n\n"
        desc = desc + "Latitude: " + latitude + "\n"
        desc = desc + "Longitude: " + longitude + "\n"
        desc = desc + "Approximate Address: " + address + "\n\n"
        desc = desc + "If this is not correct, go back and tap a different location."
        return desc
    }

    fun initFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    fun writeToFirestore(reportType : String) {
        Log.d(TAG, "In writeToFirestore()")
        // Figure out what type of report we are making

        val description = when (reportType) {
            "injury" -> "Personal Injury"
            "line" -> "Downed Power Line"
            "outage" -> "Power Outage"
            "fire" -> "Fire"
            "blocked" -> "Blocked Roadway"
            else -> "Unknown"
        }

        val docData = hashMapOf(
            "description" to description,
            "icon" to reportType,
            "latitude" to latitude,
            "longitude" to longitude
        )
        firestore.collection("markers")
            .add(docData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }

    companion object {
        fun newIntent(packageContext: Context, latitude: String, longitude: String, addy: String?): Intent {
            return Intent(packageContext, ReportActivity::class.java).apply {
                putExtra(REPORT_LATITUDE, latitude)
                putExtra(REPORT_LONGITUDE, longitude)
                putExtra(REPORT_ADDRESS, addy)
            }
        }
    }
}