// MainActivity.kt, last updated: Tues 19th Nov 2024, 10:51 PT

@file:OptIn(ExperimentalMaterial3Api::class)

package net.annedawson.hush

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // remove system bars
        setContent {
            HushApp()
        }
    }
}


@Composable
fun HushApp() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Hush - soothe you soul...") })
        }
    ) { innerPadding ->
        HushPlayer(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun HushPlayer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    // val mediaPlayer = remember { MediaPlayer.create(context, R.raw.whitenoise) }
    var sliderPosition by remember { mutableStateOf(1f) }
    var isLooping by remember { mutableStateOf(true) }
    // var selectedAudioUri by remember { mutableStateOf<Uri?>(null) } // Store URI

    // Get the URI of whitenoise.mp3 from raw resources
    val defaultAudioUri = Uri.parse("android.resource://${context.packageName}/${R.raw.whitenoise}")

    // Initialize selectedAudioUri with the default URI
    var selectedAudioUri by remember { mutableStateOf<Uri?>(defaultAudioUri) }

    LaunchedEffect(selectedAudioUri) {
        if (selectedAudioUri != null) {
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer.create(context, selectedAudioUri).apply {
                isLooping = isLooping
                setVolume(sliderPosition, sliderPosition)
                // You can optionally call prepareAsync() here if needed
            }
        }
    }


    // Set looping based on the state
    mediaPlayer?.isLooping = isLooping

    // Code for background image
    // try this

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.hush_background),
            contentDescription = "Hush Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )



        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row( // Row for the switch
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Looping")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isLooping,
                    onCheckedChange = { isChecked ->
                        isLooping = isChecked
                        mediaPlayer?.isLooping = isChecked // Update looping
                    }
                )
            }

            LaunchedEffect(selectedAudioUri) {
                if (selectedAudioUri != null) {
                    mediaPlayer?.release() // Release any existing MediaPlayer

                    // Create and prepare the MediaPlayer for the selected URI
                    mediaPlayer = MediaPlayer.create(context, selectedAudioUri).apply {
                        isLooping = isLooping
                        setVolume(sliderPosition, sliderPosition)
                        // Prepare the MediaPlayer asynchronously (optional but recommended)
                        // prepareAsync()
                    }
                }
            }

            Button(
                onClick = {
                    if (mediaPlayer != null) {
                        if (isPlaying) {
                            mediaPlayer?.pause()
                        } else {
                            mediaPlayer?.start()
                        }
                        isPlaying = !isPlaying
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }


            Slider( // Volume slider
                value = sliderPosition,
                onValueChange = { newPosition ->
                    sliderPosition = newPosition
                    mediaPlayer?.setVolume(newPosition, newPosition) // Set volume
                },
                modifier = Modifier.padding(16.dp)
            )


            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    selectedAudioUri = result.data?.data
                }
            }

            Button(onClick = {
                if (!isPlaying) { // Check if a file is not already playing
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        type = "audio/*"
                        addCategory(Intent.CATEGORY_OPENABLE)
                    }
                    launcher.launch(intent)
                } else {
                    Toast.makeText(context, "An audio file is already playing.", Toast.LENGTH_SHORT)
                        .show()
                }
            }, modifier = Modifier.padding(16.dp)) {
                Text("Select Audio")
            }

        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}