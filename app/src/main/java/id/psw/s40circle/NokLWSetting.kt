package id.psw.s40circle

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.preference.PreferenceManager
import id.psw.uiext.ColorPickerView
import id.psw.uiext.ValueButton

class NokLWSetting : AppCompatActivity() {

    private lateinit var prefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Nokleus Setting"

        prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)

        setContentView(R.layout.settings_activity)

        addCallbacks()
        updateValues()
    }

    private fun addCallbacks() {
        findViewById<ValueButton>(R.id.btn_speedscale).setOnClickListener { openSpeedSetting() }
        findViewById<ValueButton>(R.id.btn_fps).setOnClickListener { openFPSSetting() }
        findViewById<ValueButton>(R.id.btn_particlecount).setOnClickListener { openParticleCountSetting() }
        findViewById<ValueButton>(R.id.btn_uppercolor).setOnClickListener { openColorASetting() }
        findViewById<ValueButton>(R.id.btn_bottomcolor).setOnClickListener { openColorBSetting() }
        findViewById<ValueButton>(R.id.btn_backgroundColor).setOnClickListener { openColorBackgroundSetting() }
        findViewById<ValueButton>(R.id.btn_particleColor).setOnClickListener { openColorParticleSetting() }
        findViewById<ValueButton>(R.id.btn_drawmode).setOnClickListener { openModeSelector() }
    }

    private fun getModeName(mode:Int):String{
        return when(mode){
            0 -> getString(R.string.mode_blended)
            1 -> getString(R.string.mode_batched_even)
            2 -> getString(R.string.mode_batched_zero)
            else -> getString(R.string.mode_unknown)
        }
    }

    private fun openParticleCountSetting() {
        val k = createIntEdit("particle_count", 32)
        createSettingDialog(k, R.string.setting_sys_count){
            pref -> pref.putInt("particle_count", k.text.toString().toInt())
        }
    }

    private fun openModeSelector(){
        val k = LinearLayout(this)
        val name ="draw_mode"

        val dlg = createSettingDialog(k, R.string.setting_sys_mode){ }
        val btn1 = Button(this)
        val btn2 = Button(this)
        val btn3 = Button(this)

        btn1.text = getModeName(0)
        btn2.text = getModeName(1)
        btn3.text = getModeName(2)

        btn1.setOnClickListener {
            prefs.edit().putInt(name, 0).apply()
            dlg.dismiss()
            updateValues()
        }
        btn2.setOnClickListener {
            prefs.edit().putInt(name, 1).apply()
            dlg.dismiss()
            updateValues()
        }
        btn3.setOnClickListener {
            prefs.edit().putInt(name, 2).apply()
            dlg.dismiss()
            updateValues()
        }

        k.addView(btn1)
        k.addView(btn2)
        k.addView(btn3)
    }

    private fun openSpeedSetting(){
        val countEditText = createFloatEdit("speed_scale", 1.0F)
        createSettingDialog(countEditText, R.string.setting_sys_speed)
        { pref ->
            pref.putFloat("speed_scale", countEditText.text.toString().toFloat())
        }
    }
    private fun openFPSSetting(){
        val countEditText = createIntEdit("refresh_rate", 30)
        createSettingDialog(countEditText, R.string.setting_sys_fps)
        { pref ->
            pref.putInt("refresh_rate", countEditText.text.toString().toInt().coerceAtLeast(15))
        }
    }
    private fun openColorASetting(){
        val countEditText = createColorEdit("color_top", Color.BLUE)
        createSettingDialog(countEditText, R.string.setting_color_a)
        { pref ->
            pref.putInt("color_top", countEditText.value)
        }
    }
    private fun openColorBSetting(){
        val editor = createColorEdit("color_bottom", Color.BLUE)
        createSettingDialog(editor, R.string.setting_color_b)
        { pref ->
            pref.putInt("color_bottom", editor.value)
        }
    }
    private fun openColorBackgroundSetting(){
        val editor = createColorEdit("color_back", Color.BLUE)
        createSettingDialog(editor, R.string.setting_color_bg)
        { pref ->
            pref.putInt("color_back", editor.value)
        }
    }
    private fun openColorParticleSetting(){
        val editor = createColorEdit("color_particle", Color.BLUE)
        createSettingDialog(editor, R.string.setting_color_p)
        { pref ->
            pref.putInt("color_particle", editor.value)
        }
    }

    private fun createColorEdit(key:String, @ColorInt defVal: Int):ColorPickerView{
        val picker = ColorPickerView(this)
        picker.value = prefs.getInt(key, defVal)
        return picker
    }

    private fun createFloatEdit(key:String, defVal : Float) : EditText {
        val countEditText = EditText(this)
        countEditText.inputType = EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_FLAG_DECIMAL or EditorInfo.TYPE_NUMBER_FLAG_SIGNED
        countEditText.setText(prefs.getFloat(key, defVal).toString())
        return countEditText
    }

    private fun createIntEdit(key:String, defVal : Int) : EditText {
        val countEditText = EditText(this)
        countEditText.inputType = EditorInfo.TYPE_CLASS_NUMBER
        countEditText.setText(prefs.getInt(key, defVal).toString())
        return countEditText
    }

    private fun createSettingDialog(view: View, @StringRes title:Int, source : (SharedPreferences.Editor) -> Unit) : AlertDialog{

        return AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { d, _ ->
                val pref = prefs.edit()
                source.invoke(pref)
                pref.apply()
                d.dismiss()
                updateValues()
            }
            .setNegativeButton(android.R.string.cancel) { d, _ ->
                d.cancel()
            }
            .show()
    }

    private fun updateValues() {
        findViewById<ValueButton>(R.id.btn_speedscale).valueText = prefs.getFloat("speed_scale", 1.0F).toString()
        findViewById<ValueButton>(R.id.btn_fps).valueText = prefs.getInt("refresh_rate", 30).toString()
        findViewById<ValueButton>(R.id.btn_uppercolor).valueText = String.format("#%08X", prefs.getInt("color_top", Color.BLUE))
        findViewById<ValueButton>(R.id.btn_bottomcolor).valueText =  String.format("#%08X",prefs.getInt("color_bottom", Color.CYAN))
        findViewById<ValueButton>(R.id.btn_backgroundColor).valueText =  String.format("#%08X",prefs.getInt("color_back", Color.BLUE))
        findViewById<ValueButton>(R.id.btn_particleColor).valueText =  String.format("#%08X",prefs.getInt("color_particle", Color.WHITE))
        findViewById<ValueButton>(R.id.btn_particlecount).valueText = prefs.getInt("particle_count", 32).toString()
        findViewById<ValueButton>(R.id.btn_drawmode).valueText = getModeName(prefs.getInt("draw_mode", 1))
    }
}