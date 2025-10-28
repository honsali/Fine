package app.fine.ui.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.fine.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddRoute(
    viewModel: AddExpenseViewModel,
    onEvent: (AddScreenEvent) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val appContext = remember(context) { context.applicationContext }
    val recognitionAvailable = remember {
        SpeechRecognizer.isRecognitionAvailable(appContext)
    }

    val speechRecognizer = remember(recognitionAvailable, appContext) {
        if (recognitionAvailable) SpeechRecognizer.createSpeechRecognizer(appContext) else null
    }

    val listeningState = remember { mutableStateOf(false) }
    val initialPermissionGranted = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    val hasPermissionState = rememberSaveable {
        mutableStateOf(initialPermissionGranted)
    }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    val currentStep = rememberUpdatedState(uiState.step)
    val currentContext = rememberUpdatedState(context)

    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    LaunchedEffect(recognitionAvailable) {
        if (!recognitionAvailable) {
            onEvent(AddScreenEvent.ShowMessage(context.getString(R.string.add_error_speech_unavailable)))
            viewModel.setRecordingActive(false)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionRequested = false
        hasPermissionState.value = granted
        if (!granted) {
            viewModel.setRecordingActive(false)
            onEvent(AddScreenEvent.ShowMessage(context.getString(R.string.add_permission_required)))
        } else if (uiState.isRecording) {
            // Re-trigger listening now that we have permission.
            viewModel.setRecordingActive(true)
        }
    }

    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    viewModel.onSpeechUpdate(currentStep.value, text)
                }
            }

            override fun onResults(results: Bundle?) {
                listeningState.value = false
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    viewModel.onSpeechUpdate(currentStep.value, text)
                }
                viewModel.setRecordingActive(false)
            }

            override fun onError(error: Int) {
                listeningState.value = false
                viewModel.setRecordingActive(false)
                val ctx = currentContext.value
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> ctx.getString(R.string.add_error_no_match)
                    SpeechRecognizer.ERROR_AUDIO -> ctx.getString(R.string.add_error_audio)
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> ctx.getString(R.string.add_permission_required)
                    SpeechRecognizer.ERROR_CLIENT,
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                    SpeechRecognizer.ERROR_SERVER -> ctx.getString(R.string.add_error_speech_generic)
                    else -> ctx.getString(R.string.add_error_speech_generic)
                }
                onEvent(AddScreenEvent.ShowMessage(message))
            }
        }
    }

    DisposableEffect(speechRecognizer, recognitionListener) {
        val recognizer = speechRecognizer
        if (recognizer != null) {
            recognizer.setRecognitionListener(recognitionListener)
        }
        onDispose {
            listeningState.value = false
            recognizer?.stopListening()
            recognizer?.cancel()
            recognizer?.destroy()
        }
    }

    LaunchedEffect(uiState.step, uiState.isRecording, hasPermissionState.value, recognitionAvailable) {
        if (!recognitionAvailable || speechRecognizer == null) {
            return@LaunchedEffect
        }
        if (!uiState.isRecording || uiState.step == CaptureStep.Initial) {
            if (listeningState.value) {
                speechRecognizer.stopListening()
                speechRecognizer.cancel()
                listeningState.value = false
            }
        } else {
            if (!hasPermissionState.value) {
                if (!permissionRequested) {
                    permissionRequested = true
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            } else if (!listeningState.value) {
                speechRecognizer.cancel()
                speechRecognizer.startListening(recognizerIntent)
                listeningState.value = true
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is AddExpenseEvent.ExpenseSaved -> {
                    onEvent(
                        AddScreenEvent.ShowMessage(
                            message = context.getString(R.string.add_message_saved)
                        )
                    )
                }

                is AddExpenseEvent.ShowError -> {
                    onEvent(AddScreenEvent.ShowMessage(event.message))
                }
            }
        }
    }

    AddScreen(
        state = uiState,
        onStart = viewModel::startCapture,
        onCancel = viewModel::cancel,
        onRepeat = viewModel::repeatStep,
        onContinue = viewModel::onContinue,
        onFinish = viewModel::onFinish,
        onMicToggle = viewModel::setRecordingActive,
        onTextChanged = viewModel::onTextChanged
    )
}
