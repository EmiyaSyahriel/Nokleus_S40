package id.psw.uiext

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import id.psw.s40circle.R

class ColorPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), OnSeekBarChangeListener {

    private lateinit var s_r :SeekBar
    private lateinit var s_g :SeekBar
    private lateinit var s_b :SeekBar
    private lateinit var s_a :SeekBar
    private lateinit var l_r : TextView
    private lateinit var l_g :TextView
    private lateinit var l_b :TextView
    private lateinit var l_a :TextView
    private lateinit var preview : View

    init {
        inflate(context, R.layout.editor_color_picker, this)
        s_r = findViewById<SeekBar>(R.id.seek_r)
        s_g = findViewById<SeekBar>(R.id.seek_g)
        s_b = findViewById<SeekBar>(R.id.seek_b)
        s_a = findViewById<SeekBar>(R.id.seek_a)
        l_r = findViewById<TextView>(R.id.value_r)
        l_g = findViewById<TextView>(R.id.value_g)
        l_b = findViewById<TextView>(R.id.value_b)
        l_a = findViewById<TextView>(R.id.value_a)
        preview = findViewById<View>(R.id.color_preview)

        // Set a SeekBar change listener
        s_r.setOnSeekBarChangeListener(this)
        s_g.setOnSeekBarChangeListener(this)
        s_b.setOnSeekBarChangeListener(this)
        s_a.setOnSeekBarChangeListener(this)
    }

    var value : Int
    get()= Color.argb(s_a.progress, s_r.progress, s_g.progress, s_b.progress)
    set(v) {
        s_a.progress = Color.alpha(v)
        s_r.progress = Color.red(v)
        s_g.progress = Color.green(v)
        s_b.progress = Color.blue(v)
    }

    private fun updateValues(){
        l_r.text = s_r.progress.toString()
        l_g.text = s_g.progress.toString()
        l_b.text = s_b.progress.toString()
        l_a.text = s_a.progress.toString()
        preview.setBackgroundColor(value)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        updateValues()
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}