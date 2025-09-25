package com.polariss.shosu.app

import android.content.Context
import android.media.MediaPlayer
import com.polariss.shosu.R

class SoundUtils(private val context: Context) {

    private var holdSound: MediaPlayer? = null
    private var finishSound: MediaPlayer? = null

    /** 播放按住循环音效 */
    fun playHoldSound() {
        try {
            if (holdSound == null) {
                holdSound = MediaPlayer.create(context, R.raw.processing)
            }
            holdSound?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 停止并释放按住音效 */
    fun stopHoldSound() {
        try {
            holdSound?.let {
                it.stop()
                it.release()
            }
            holdSound = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 播放完成音效（只播放一次） */
    fun playFinishSound() {
        try {
            if (finishSound == null) {
                finishSound = MediaPlayer.create(context, R.raw.finished) // res/raw/finish.wav
                finishSound?.isLooping = false
            }
            finishSound?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 释放所有音效资源 */
    fun release() {
        stopHoldSound()
        finishSound?.release()
        finishSound = null
    }
}
