package id.itborneo.musicplayer

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.playBtn
import kotlinx.android.synthetic.main.activity_play.*


class PlayActivity : AppCompatActivity() {

    private val TAG = "PlayActivity"

    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private var position = 0

    var allFiles = mutableListOf<Data>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
    }

    private fun initializePlayer() {

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this)

//        mediaDataSourceFactory = DefaultDataSourceFactory(
//            this, Util.getUserAgent(
//                this,
//                "mediaPlayerSample"
//            )
//        )
//
//        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
//            Uri.parse(STREAM_URL)
//        )


        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "exo-demo"))

        val concatenatingMediaSource = ConcatenatingMediaSource()

        //Ensure to populate the allFiles array.
        allFiles.add(Data("1 Demon slayer 1", STREAM_URL))
        allFiles.add(Data("2 Digimon", STREAM_URL2))
        allFiles.add(Data("3 Kimi no nawa", STREAM_URL3))
        allFiles.add(Data("4 Flash", STREAM_URL4))


        updateUI()

        //Ensure to populate the allFiles array.
        for (i in 0 until allFiles.size) {
//            val currentFile = File(allFiles.get(i))

            val mediaSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
//                    Uri.parse("file://" + currentFile.absolutePath)
                    Uri.parse(allFiles[i].url)
                )
            concatenatingMediaSource.addMediaSource(mediaSource)
        }


        simpleExoPlayer.prepare(concatenatingMediaSource, false, false)
//        simpleExoPlayer.playWhenReady = true

//        simpleExoPlayer
        startPlayer()

//        playerView.setShutterBackgroundColor(Color.TRANSPARENT)
//        playerView.player = simpleExoPlayer
//        playerView.requestFocus()


        initProgress()
        progressListener()

        buttonListener()

    }

    private fun buttonListener() {
        pauseBtn.setOnClickListener {
            pausePlayer()

        }
        playBtn.setOnClickListener {
            startPlayer()

        }
        nextBtn.setOnClickListener {
            nextPlayer()
        }

        prevBtn.setOnClickListener {
            prevPlayer()
        }

        selectSoundBtn.setOnClickListener {
            playSelected(3-1)
        }
    }

    private fun initProgress() {
        progressBar.max = 100

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                if (p0 == null) return
                val progress: Long = p0.progress.toLong()
                val seekTo: Long = (progress * simpleExoPlayer.duration) / 100
                simpleExoPlayer.seekTo(seekTo)
                Log.d(TAG, "seekTo: $seekTo dari ${p0.progress} and ${simpleExoPlayer.duration}")


            }

        })


    }

    private fun progressListener() {
        simpleExoPlayer.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                Log.d(TAG, "onPlaybackParametersChanged: ")
            }

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
                Log.d(TAG, "onTracksChanged: ")
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Log.d(TAG, "onPlayerError: ")
            }

            /** 4 playbackState exists */
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                updateProgressBar()
                progressBar.visibility = View.VISIBLE


                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_BUFFERING")
                    }
                    Player.STATE_READY -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_READY")
                    }
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_IDLE")
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged - STATE_ENDED")
                    }
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.d(TAG, "onLoadingChanged: ")
            }

            override fun onPositionDiscontinuity(reason: Int) {

                tvTitle.text = allFiles[position].soundName


                val latestWindowIndex: Int = simpleExoPlayer.currentWindowIndex
                if (latestWindowIndex != position) {
                    // item selected in playlist has changed, handle here
                    position = latestWindowIndex
                    // ...

                }
                updateUI()

                Log.d(TAG, "onPositionDiscontinuity: ")
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                Log.d(TAG, "onRepeatModeChanged: ")
                Toast.makeText(baseContext, "repeat mode changed", Toast.LENGTH_SHORT).show()
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                Log.d(TAG, "onTimelineChanged: ${timeline.toString()}")
            }
        })
    }

    private fun updateUI() {
        tvTitle.text = allFiles[position].soundName

    }

    private fun releasePlayer() {

        simpleExoPlayer.release()
    }

    public override fun onStart() {
        super.onStart()

        if (Util.SDK_INT > 23) initializePlayer()
    }

    public override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23) initializePlayer()
    }

    public override fun onPause() {
        super.onPause()

        if (Util.SDK_INT <= 23) releasePlayer()
    }

    public override fun onStop() {
        super.onStop()

        if (Util.SDK_INT > 23) releasePlayer()
    }

    companion object {
        const val STREAM_URL = "http://192.168.43.46/project_baca_suara/demonslayer.mp3"
        const val STREAM_URL2 = "http://192.168.43.46/project_baca_suara/1.mp3"
        const val STREAM_URL3 = "http://192.168.43.46/project_baca_suara/2.mp3"
        const val STREAM_URL4 = "http://192.168.43.46/project_baca_suara/3.mp3"

    }

    private fun pausePlayer() {

        simpleExoPlayer.playWhenReady = false
        simpleExoPlayer.playbackState
    }

    private fun startPlayer() {
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.playbackState
    }

    private fun nextPlayer() {
        simpleExoPlayer.next()
        simpleExoPlayer.playbackState
    }

    private fun prevPlayer() {
        simpleExoPlayer.previous()
        simpleExoPlayer.playbackState
    }

    private fun playSelected(index: Int) {
        simpleExoPlayer.seekTo(index, 0.toLong())
    }

    private val dragging = false
    private val handler = Handler()

    private fun updateProgressBar() {

        val duration: Long = simpleExoPlayer.duration
        val position: Long = simpleExoPlayer.currentPosition
        if (!dragging) {
            progressBar.progress = progressBarValue(position, duration)
        }
        val bufferedPosition: Long = simpleExoPlayer.bufferedPosition
        progressBar.secondaryProgress = progressBarValue(bufferedPosition, duration)
        // Remove scheduled updates.
        handler.removeCallbacks(updateProgressAction)
        // Schedule an update if necessary.
        val playbackState = simpleExoPlayer.playbackState
        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            var delayMs: Long
            if (simpleExoPlayer.playWhenReady && playbackState == Player.STATE_READY) {
                delayMs = 1000 - position % 1000
                if (delayMs < 200) {
                    delayMs += 1000
                }
            } else {
                delayMs = 1000
            }
            handler.postDelayed(updateProgressAction, delayMs)
        }
    }

    private fun progressBarValue(position: Long, duration: Long): Int {

        val percentage: Double = (position.toDouble() / duration) * 100
        Log.d(
            TAG,
            "progressBarValue $position dan $duration dari ${position / duration} and percentage is $percentage"
        )



        return percentage.toInt()

    }

    private val updateProgressAction = Runnable { updateProgressBar() }
}