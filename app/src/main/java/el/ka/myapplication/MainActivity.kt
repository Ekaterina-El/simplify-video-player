package el.ka.myapplication

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
    SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {

    companion object {
        const val SECOND = 1000
        const val GET_VIDEO = 123
    }

    private lateinit var selectedVideoUri: Uri
    private var mediaPlayer = MediaPlayer()
    private val MediaPlayer.totalTime: Int
        get() = this.duration / SECOND
    private val MediaPlayer.currentTime: Int
        get() = this.currentPosition / SECOND

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var changedFromUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView.holder.addCallback(this)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
        seekBar.setOnSeekBarChangeListener(this)

        playButton.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                playButton.setImageResource(android.R.drawable.ic_media_play)
                mediaPlayer.pause()
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_pause)
                mediaPlayer.start()
            }
        }

        chooseVideo.setOnClickListener {
            videoView.holder.removeCallback(this)
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT

            startActivityForResult(intent, GET_VIDEO)
            chooseVideo.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GET_VIDEO && resultCode == Activity.RESULT_OK && data != null) {
            selectedVideoUri = data.data!!
            videoView.holder.addCallback(this)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        mediaPlayer.release()
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        mediaPlayer.apply {
            setDataSource(
                this@MainActivity,
//                Uri.parse("android.resource://$packageName/raw/test_video")
                selectedVideoUri
            )
            setDisplay(surfaceHolder)
            prepareAsync()
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
//        mediaPlayer.apply {
//            setDataSource(
//                this@MainActivity,
////                Uri.parse("android.resource://$packageName/raw/test_video")
//                selectedVideoUri
//            )
//            setDisplay(p0)
//            prepareAsync()
//        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {}

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        initializeSeekBar()
        updateSeekBar()
    }

    override fun onCompletion(p0: MediaPlayer?) {
        playButton.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun updateSeekBar() {
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentTime
            text_current_time.text = timeToString(mediaPlayer.currentTime)
            handler.postDelayed(runnable, SECOND.toLong())
        }
        handler.postDelayed(runnable, SECOND.toLong())
    }

    private fun initializeSeekBar() {
        text_total_time.text = timeToString(mediaPlayer.totalTime)
        seekBar.max = mediaPlayer.totalTime
        text_current_time.text = "00:00"
    }

    private fun timeToString(seconds: Int): String {
        val s1 = seconds % 3600
        val m = s1 / 60
        val s = s1 - m * 60
        return String.format("%02d:%02d", m, s)
    }

    // Seek bar - start
    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            mediaPlayer.seekTo(progress * SECOND)
        }
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {}

    override fun onStopTrackingTouch(p0: SeekBar?) {}
    // Seek bar - end
}