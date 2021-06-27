package id.psw.uiext

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import  androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.drawable.toBitmap
import id.psw.s40circle.R

class ValueButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private var mIsHovered : Boolean = false
    var mDrawable : Drawable? = null
    var mHoverBack : Drawable? = null
    var valueText : String? = null

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ValueButton, defStyleAttr,0)
        mDrawable = a.getDrawable(R.styleable.ValueButton_iconDrawableReference)
        mHoverBack = a.getDrawable(R.styleable.ValueButton_hoverDrawable)
        valueText = a.getString(R.styleable.ValueButton_valueContent) ?: ""
        a.recycle()
    }

    private val isNightMode get() = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    override fun onDraw(canvas: Canvas?) {
        if(canvas != null){
            onPaint(canvas)
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val dHeight = ((paint.textSize * 3) + 20f).toInt()

        val heightData = MeasureSpec.makeMeasureSpec(dHeight, MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpec, heightData)
    }

    private fun onPaint(ctx: Canvas) {

        if(mIsHovered || isHovered){
            val bgBitmap = mHoverBack?.toBitmap(width, height)
            if(bgBitmap != null){
                ctx.drawBitmap(bgBitmap,
                        Rect(0,0,bgBitmap.width, bgBitmap.height),
                        Rect(0,0,width,height),
                        paint)
            }
        }

        paint.color = if(isNightMode) Color.WHITE else Color.BLACK

        var textXOffset = 10f

        if(mDrawable != null){
            val iconSize = paint.textSize * 3f

            val mBitmap = mDrawable!!.toBitmap(iconSize.toInt(), iconSize.toInt())
            val bitmapTop = ((height/2f) - (iconSize/2f)).toInt()
            ctx.drawBitmap(mBitmap,
                    Rect(0,0,mBitmap.width, mBitmap.height),
                    Rect(10, bitmapTop, mBitmap.width, bitmapTop + mBitmap.height),
                    paint
            )

            textXOffset = iconSize + 20f
        }

        val center = height/2f

        if(text.isEmpty()) text = resources.getText(android.R.string.selectTextMode)

        var topTextPosition = center + (paint.textSize*0.5f)

        if(valueText?.isEmpty() == false){
            val defSize = paint.textSize
            paint.textSize = defSize/1.5f
            ctx.drawText(valueText ?: "", textXOffset, center + (paint.textSize * 1.1f), paint)
            paint.textSize = defSize
            topTextPosition = center - (paint.textSize * 0.1f)
        }
        ctx.drawText(text, 0, text.length, textXOffset, topTextPosition, paint)
    }
}