package cz.svetice.widget

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ConfigActivity : AppCompatActivity() {

    private lateinit var settings: Settings
    private lateinit var fromBtn: Button
    private lateinit var toBtn: Button
    private val dayLabels = arrayOf("Po","Út","St","Čt","Pá","So","Ne")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)

        settings = SettingsRepository.load(this)

        // Postavit řádek tlačítek dnů
        val daysRow = findViewById<LinearLayout>(R.id.days_row)
        for (i in 0..6) {
            val b = Button(this).apply {
                text = dayLabels[i]
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                ).apply { setMargins(4, 0, 4, 0) }
                setOnClickListener {
                    settings.activeDays[i] = !settings.activeDays[i]
                    refreshDayButtons(daysRow)
                }
            }
            daysRow.addView(b)
        }
        refreshDayButtons(daysRow)

        fromBtn = findViewById(R.id.from_btn)
        toBtn = findViewById(R.id.to_btn)
        fromBtn.text = settings.fromHHmm
        toBtn.text = settings.toHHmm

        fromBtn.setOnClickListener { pickTime(settings.fromHHmm) { hh, mm ->
            settings = settings.copy(fromHHmm = "%02d:%02d".format(hh, mm))
            fromBtn.text = settings.fromHHmm
        }}
        toBtn.setOnClickListener { pickTime(settings.toHHmm) { hh, mm ->
            settings = settings.copy(toHHmm = "%02d:%02d".format(hh, mm))
            toBtn.text = settings.toHHmm
        }}

        findViewById<Button>(R.id.save_btn).setOnClickListener {
            SettingsRepository.save(this, settings)
            TrainUpdateScheduler.schedule(this)
            TrainWidgetProvider.updateAll(this)
            finish()
        }
    }

    private fun refreshDayButtons(row: LinearLayout) {
        for (i in 0..6) {
            val b = row.getChildAt(i) as Button
            if (settings.activeDays[i]) {
                b.setBackgroundColor(0xFF0F6E56.toInt())
                b.setTextColor(0xFFFFFFFF.toInt())
            } else {
                b.setBackgroundColor(0xFF333333.toInt())
                b.setTextColor(0xFFBBBBBB.toInt())
            }
        }
    }

    private fun pickTime(current: String, cb: (Int, Int) -> Unit) {
        val parts = current.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 6
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(this, { _, hh, mm -> cb(hh, mm) }, h, m, true).show()
    }

    // Settings je data class s arrayem -> potřebujeme copy helper
    private fun Settings.copy(
        activeDays: BooleanArray = this.activeDays,
        fromHHmm: String = this.fromHHmm,
        toHHmm: String = this.toHHmm
    ): Settings = Settings(activeDays, fromHHmm, toHHmm)
}
