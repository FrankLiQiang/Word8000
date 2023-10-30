package com.frank.word8001

import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frank.word8001.ui.theme.Black
import com.frank.word8001.ui.theme.Word8001Theme
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess


private var thisTimer: Timer = Timer()
private var thisTask: TimerTask? = null
var playTime by mutableStateOf("")
var isPort by mutableStateOf(true)
private val listTime = ArrayList<Int>()
var progress by mutableStateOf(0f)
var maxProgress = 0f
var isPlay by mutableStateOf(true)
var isNext by mutableStateOf(false)
var isFirstTime by mutableStateOf(true)
lateinit var mediaPlayer: MediaPlayer

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isPort = false
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            isPort = true
        }
        setContent {
//            var playTime by rememberSaveable {mutableStateOf("")}
            val launcher = rememberLauncherForActivityResult(contract = GetContent()) { result ->
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    if (result != null) {
                        listTime.clear()
                        isFirstTime = false
                        thisTask = object : TimerTask() {
                            override fun run() {
                                try {
                                    maxProgress = mediaPlayer.duration.toFloat()
                                    progress = mediaPlayer.currentPosition.toFloat()
                                    if (isPort) {
                                        playTime = "${getTime(mediaPlayer.currentPosition)}"
                                    } else {
                                        playTime = "${getTime(mediaPlayer.currentPosition)} / ${
                                            getTime(mediaPlayer.duration)
                                        }"
                                    }
                                    if (listTime.size == 2) {
                                        if (mediaPlayer.currentPosition > listTime[1]) {
                                            val time: Int = listTime[0]
                                            mediaPlayer.seekTo(time)
                                        }
                                    }
                                } catch (_: Exception) {
                                }
                            }
                        }
                        thisTimer.scheduleAtFixedRate(thisTask, 0, 20)
                        setDataSource(applicationContext, result)
                        setOnCompletionListener(OnCompletionListener { mp: MediaPlayer ->
                            mp.seekTo(0) //循环播放
                            mp.start()
                        })
                        prepare()
                        start()
                    }
                }
            }
            Word8001Theme {
                val play = {
                    isPlay = !mediaPlayer.isPlaying
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.start()
                    }
                }
                MainUI(
                    { launcher.launch("audio/*") },
                    { play() },
                    {
                        listTime.clear()
                        mediaPlayer.seekTo(0)
                        if (!mediaPlayer.isPlaying) play()
                    },
                    {
                        if (listTime.size < 2) {
                            listTime.add(mediaPlayer.currentPosition)
                        }
                        if (listTime.size == 2) {
                            isNext = false
                            val time: Int = listTime[0]
                            mediaPlayer.seekTo(time)
                        }
                        if (!mediaPlayer.isPlaying) play()
                    },
                    {
                        if (listTime.size == 2) {
                            isNext = true
                            listTime.removeAt(0)
                            val time: Int = listTime[0]
                            mediaPlayer.seekTo(time)
                        }
                        if (!mediaPlayer.isPlaying) play()
                    },
                    {
                        if (!isFirstTime) mediaPlayer.seekTo(it.toInt())
                    },
                )
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏

        } else {//竖屏

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        if (!isFirstTime)
//        {
//            MediaPlayer().stop()
//            MediaPlayer().release()
//        }
        exitProcess(0)
    }
}

@Composable
fun MainUI(
    openFile: () -> Unit,
    pause: () -> Unit,
    replay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    doSlider: (Float) -> Unit,
) {

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(Modifier.background(Black), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .height(60.dp)
                    .padding(3.dp)
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f, true)
                        .fillMaxHeight()
                        .padding(3.dp),
                    onClick = openFile
                ) {
                    Text("Choose MP3 File")
                }
                if (!isFirstTime) {
                    Text(
                        text = playTime,
                        style = TextStyle(
                            fontSize = 30.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .weight(1f, true)
                            .align(alignment = CenterVertically)
                            .padding(3.dp)
                    )
                    Button(
                        modifier = Modifier
                            .weight(1f, true)
                            .fillMaxHeight()
                            .padding(3.dp),
                        onClick = replay
                    ) {
                        Text("Replay")
                    }
                }
            }
            if (isPort) {
                if (isFirstTime) {
                    Row(modifier = Modifier.weight(6.1f, true)) {}
                } else {
                    Row(modifier = Modifier.weight(0.1f, true)) {
                    }
                    Slider(
                        value = progress,
                        onValueChange = doSlider,
                        valueRange = 0f..maxProgress,
                        modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                    )
                    Row(modifier = Modifier.weight(1.1f, true)) {
                    }
                    TabItem(
                        if (isPlay) R.drawable.outline_pause_circle_outline_24 else R.drawable.outline_play_circle_outline_24,
                        "Start",
                        Color.LightGray,
                        Modifier
                            .weight(2f, true)
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .weight(2f, true)
                            .clickable { pause() }
                    )
                    Row(
                        Modifier
                            .background(Color.White)
                            .height(1.dp)
                            .fillMaxWidth()) {}
                    Row(
                        modifier = Modifier
                            .weight(1.1f, true)
                            .fillMaxHeight()
                    ) {
                        TabItem(
                            R.drawable.outline_skip_previous_24, "Start",
                            Color.LightGray,
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .weight(1f, true)
                                .clickable { previous() }
                        )
                        Row(
                            Modifier
                                .background(Color.White)
                                .width(1.dp)
                                .fillMaxHeight()) {}
                        TabItem(
                            R.drawable.outline_skip_next_24, "End",
                            Color.LightGray,
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .weight(1f, true)
                                .clickable { next() }
                                //.background(if (isNext) Color(0xFFFFB6C5FF) else Color.Black)
                                .background(if (isNext) Color.Blue else Color.Black)
                        )
                    }
                }
            } else {
                if (!isFirstTime) {
                    Row(
                        modifier = Modifier
                            .weight(1.1f, true)
                            .fillMaxHeight()
                    ) {
                        TabItem(
                            R.drawable.outline_skip_previous_24, "Start",
                            Color.LightGray,
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .weight(1f, true)
                                .clickable { previous() }
                        )
                        TabItem(
                            if (isPlay) R.drawable.outline_pause_circle_outline_24 else R.drawable.outline_play_circle_outline_24,
                            "Start",
                            Color.LightGray,
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .weight(1f, true)
                                .clickable { pause() }
                        )
                        TabItem(
                            R.drawable.outline_skip_next_24, "End",
                            Color.LightGray,
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .weight(1f, true)
                                .clickable { next() }
                                .background(if (isNext) Color.Blue else Color.Black)
                        )
                    }
                    Slider(
                        value = progress,
                        onValueChange = doSlider,
                        valueRange = 0f..maxProgress,
                        modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TabItem(@DrawableRes iconId: Int, title: String, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painterResource(iconId),
            title,
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .weight(1f, true),
            tint = tint
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    isFirstTime = false
    Word8001Theme {
        MainUI({}, {}, {}, {}, {}, {})
    }
}

fun getTime(allTime: Int): String {
    val a = allTime % 1000
    val b = (allTime - a) / 1000
    val c = b % 60
    val d = (b - c) / 60
    val e = d % 60
    val f = (d - e) / 60
    return "$f:$e:$c"
}
