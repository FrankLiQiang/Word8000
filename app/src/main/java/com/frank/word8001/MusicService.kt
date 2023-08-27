package com.frank.word8001

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Binder
import android.os.IBinder

class MusicService : Service() {
    var mediaPlayer: MediaPlayer? = null
        private set
    private var mVisualizer: Visualizer? = null
    override fun onBind(intent: Intent): IBinder? {
        return MusicControl()
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
    }

    internal inner class MusicControl : Binder() {
        fun play(uri: Uri?) {
            try {
                if (mVisualizer != null) {
                    mVisualizer!!.setDataCaptureListener(null, 0, false, true)
                    mVisualizer = null
                }
                if (mediaPlayer != null) {
                    mediaPlayer!!.setOnPreparedListener(null)
                    mediaPlayer!!.stop()
                    mediaPlayer!!.reset()
                    mediaPlayer!!.release()
                    mediaPlayer = null
                }
                mediaPlayer = MediaPlayer()
                mediaPlayer!!.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                mediaPlayer!!.setDataSource(applicationContext, uri!!)
                val params = mediaPlayer!!.playbackParams
                mediaPlayer!!.playbackParams = params
                mediaPlayer!!.prepare()
                mediaPlayer!!.setOnErrorListener(null)
                mediaPlayer!!.setOnCompletionListener { mp: MediaPlayer ->
                    mp.seekTo(0) //循环播放
                    mp.start()
                }
                mediaPlayer!!.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun pausePlay() {
            mediaPlayer!!.pause()
        }

        fun continuePlay() {
            mediaPlayer!!.start()
        }

        fun seekTo(progress: Int) {
            mediaPlayer!!.seekTo(progress)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }
}