package id.psw.s40circle

import android.content.SharedPreferences
import android.graphics.*
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.os.Handler
import android.preference.PreferenceManager
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder


class NokLWService : WallpaperService() {

    /**
     * How particle is rendered
     */
    enum class ParticleDrawMode {
        /**
         *  Every particle is drawn individually, intersection have alpha-blending
         */
        Blended,

        /**
         * All particle is batched into one Path object then drawn in single call,
         * Path is Draw in Even Odd mode, intersections will not drawn)
         */
        BatchedEvenOdd,

        /**
         * All particle is batched into one Path object then drawn in single call,
         * Path is Draw in Non Zero Mode, intersection will be blended into one
         */
        BatchedNonZero
    }
    private class NokParticle()
    {
        var x = 0.0f
        var y = 0.0f
        init{
            reset(true)
        }

        fun reset(warmed:Boolean = false){
            this.x = if(warmed) Math.random().toFloat() else 0f
            this.y = ((Math.random().toFloat() % 0.6f) + 0.1f).coerceIn(0.1f, 0.7f)
        }

        fun move(deltaTime: Float, scale: Float){
            x += y * 0.1f * deltaTime * (1 + (8 * scale * 0.25f))
            if(x >= 1.1 || y <= -0.1) reset()
        }
    }

    private inner class NokWPEngine : WallpaperService.Engine() {
        private val handler = Handler()
        private val invalidator = Runnable { try{ invalidate() }catch(e:Exception){} }
        private val bg_paint = Paint()
        private val fg_paint = Paint()
        private var target_w = 1280f
        private var target_h = 720f
        private var visible = false
        private var particleCount = 32
        private var sBackAccent = Color.argb(0xFF, 0xFF, 0xFF, 0xFF)
        private var sTopAccent = Color.argb(0xFF, 0x00, 0x00, 0xFF)
        private var sBtmAccent = Color.argb(0xFF, 0x00, 0x99, 0xFF)
        private var sParticleColor = Color.argb(0x66, 0xFF, 0xFF, 0xFF)
        private var lastTime = 0L
        private var deltaTime = 1.0f
        private var speedScale = 1.0f
        private var updateSpeed = 33
        private var particles = arrayListOf<NokParticle>()
        private var drawMode = ParticleDrawMode.Blended

        init {
            handler.post(invalidator)

            loadPrefs()

            for(i in 0 .. particleCount){
                particles.add(NokParticle())
            }
        }

        fun loadPrefs(){
            val pref = PreferenceManager.getDefaultSharedPreferences(baseContext)
            particleCount = pref.getInt("particle_count", 32)
            speedScale = pref.getFloat("speed_scale", 1.0F)
            drawMode = when(pref.getInt("draw_mode", 0)){
                0 -> ParticleDrawMode.Blended
                1 -> ParticleDrawMode.BatchedEvenOdd
                2 -> ParticleDrawMode.BatchedNonZero
                else -> ParticleDrawMode.Blended
            }
            sParticleColor = pref.getInt("color_particle", sParticleColor)
            sTopAccent = pref.getInt("color_top", sTopAccent)
            sBackAccent = pref.getInt("color_back", sBackAccent)
            sBtmAccent = pref.getInt("color_bottom", sBtmAccent)
            updateSpeed = 1000 / pref.getInt("refresh_rate", 30)
        }

        fun regenBgGradient() {
            bg_paint.shader = LinearGradient(
                0f, 0f, 0f, target_h,
                intArrayOf(
                    Color.TRANSPARENT,
                    sTopAccent,
                    sBtmAccent,
                    sBtmAccent,
                    Color.TRANSPARENT
                ),
                floatArrayOf(0.0f, 0.1f, 0.3f, 0.8f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            this.visible = visible
            if(visible) {
                loadPrefs()
                handler.post(invalidator)
            }
            else handler.removeCallbacks(invalidator)
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            target_w = width * 1f
            target_h = height * 1f
            regenBgGradient()

            super.onSurfaceChanged(holder, format, width, height)
        }

        private val batchedPath = Path()
        private fun lerp(a:Float, b:Float, t:Float):Float = a + ((b - a) * t)

        private fun drawParticles(ctx:Canvas){
            batchedPath.reset()
            batchedPath.fillType = if(drawMode == ParticleDrawMode.BatchedEvenOdd) Path.FillType.EVEN_ODD else Path.FillType.WINDING
            particles.forEachIndexed { i, p ->
                val sweep = if(i % 2 == 0) 1 else -1
                val size = Math.min(target_w,target_h) * (0.8f * (1 + speedScale))
                val psize = p.y * p.y
                val pdsize = psize * size
                val x = lerp(-pdsize, target_w + pdsize, p.x)
                val y = target_h * lerp(0.2f, 1.0f, p.y)
                val w = size * psize
                val h = size / 2 * psize * psize

                if(drawMode == ParticleDrawMode.Blended){
                    ctx.drawArc(RectF(x,y,x+w,y+h), 0f,360f, true, fg_paint);
                }else{
                    batchedPath.addArc(RectF(x,y,x+w,y+h), 0f,360f * sweep);
                    batchedPath.close()
                }
            }

            if(drawMode != ParticleDrawMode.Blended){
                ctx.drawPath(batchedPath, fg_paint)
            }
        }

        private fun onDraw(ctx: Canvas){
            ctx.drawColor(sBackAccent)
            try{
                particles.forEach { it.move(deltaTime, speedScale) }
                ctx.drawPaint(bg_paint)
                fg_paint.color = sParticleColor
                drawParticles(ctx)
            } catch(e:Exception) {
                e.printStackTrace()
            }
        }

        private fun calcTime(){
            val currentTime = System.currentTimeMillis()
            val delta = currentTime - lastTime
            deltaTime = delta / 1000f
            lastTime = currentTime
        }

        private fun invalidate(){
            val holder = surfaceHolder
            var canvas : Canvas? = null
            try{
                canvas = holder.lockCanvas()
                if(canvas != null){
                    canvas.drawRGB(0, 0, 0)
                    calcTime()
                    this.target_w = canvas.width * 1f
                    this.target_h = canvas.height * 1f
                    onDraw(canvas)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
            finally {
                if(canvas != null) holder.unlockCanvasAndPost(canvas)
            }

            if(visible){
                handler.postDelayed(invalidator, updateSpeed * 1L)
            }
        }


    }

    override fun onCreateEngine(): Engine {
        return NokWPEngine()
    }

}