package box.mon.amusement.watch

import box.mon.amusement.watch.theme.WatchAmusementTheme

import android.app.RemoteInput
import android.content.ComponentName
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.cardemulation.NfcFCardEmulation
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.input.RemoteInputIntentHelper.Companion.createActionRemoteInputIntent
import androidx.wear.input.RemoteInputIntentHelper.Companion.putRemoteInputsExtra
import java.util.UUID

class CardInfoPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cardIndex = intent.getIntExtra("cardIndex", -1)

        val cardList = CardDataList(this)

        setContent {
            val cardListArray by cardList
                .listFlow()
                .collectAsState(initial = emptyList())

            if (cardIndex in cardListArray.indices) {
                val showingCard = cardListArray[cardIndex]

                CardInfoPage(
                    name = showingCard.cardName,
                    idm = showingCard.cardIDm,
                    cardList = cardList,
                    index = cardIndex,
                    goHome = { finish() },
                    resume = { onResume() },
                    activity = this@CardInfoPageActivity
                )
            }
        }
    }
}

@Composable
fun CardInfoPage(
    name: String = "null",
    idm: String = "02FE000000000000",
    cardList: CardDataList,
    index: Int = 0,
    goHome: () -> Unit = {},
    resume: () -> Unit = {},
    activity: CardInfoPageActivity
) {
    val context = LocalContext.current
    var isRunningNFC by remember { mutableStateOf(false) }

    val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
    val isNfcAvailable: Boolean = (nfcAdapter != null)

    val felicaServiceInstance = if(isNfcAvailable) NfcFCardEmulation.getInstance(nfcAdapter) else null
    val serviceIntent = ComponentName(activity, FeliCaService::class.java)

    fun toastMessage(msg: String = "") {
        if(msg.isEmpty().not()) {
            val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    fun inputForm(msg: String = "", cmd: (String) -> Unit) {
        val remoteInputs: List<RemoteInput> = listOf(
            RemoteInput.Builder("inputForm")
                .setLabel(msg).build()
        )
        val intent: Intent = createActionRemoteInputIntent()
        putRemoteInputsExtra(intent, remoteInputs)

        val launcher = activity.activityResultRegistry.register(
            "remoteInput",
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val inputValue = RemoteInput
                .getResultsFromIntent(result.data)
                ?.getCharSequence("inputForm")
                .toString()

                cmd(inputValue)
        }

        launcher.launch(intent)
    }

    var isRequestedConfirm by remember { mutableStateOf(false) }
    var confirmMessage by remember { mutableStateOf("") }
    var confirmCommand by remember { mutableStateOf({ }) }

    @Composable
    fun confirmDialog() {
        Dialog (
            showDialog = true,
            onDismissRequest = { isRequestedConfirm = false }
        ) {
            Alert(
                title = { Text(confirmMessage) },
                negativeButton = {
                    Button(
                        onClick = { isRequestedConfirm = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("No")
                    }
                },
                positiveButton = {
                    Button(
                        onClick = {
                            isRequestedConfirm = false
                            confirmCommand()
                        },
                        colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Yes")
                    }
                }
            )
        }
    }

    fun requestConfirm(msg: String = "", cmd: () -> Unit) {
        confirmMessage = msg
        confirmCommand = cmd
        isRequestedConfirm = true
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        nfcAdapter?.resetDiscoveryTechnology(activity)
        isRunningNFC = false
    }

    WatchAmusementTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = {
                            nfcAdapter?.resetDiscoveryTechnology(activity)
                            goHome()
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("Back")
                    }
                },
            ) { contentPadding ->
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "WatchAmusement")
                        }
                    }
                    item {
                        ListHeader(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = name)
                        }
                    }

                    if(isNfcAvailable) {
                        if (isRunningNFC.not()) {
                            item {
                                Button(
                                    onClick = {
                                        isRunningNFC = true
                                        resume()
                                        // 교통카드 등 활성화된 상태더라도 FeliCa만 인식되게끔 하기
                                        nfcAdapter.setDiscoveryTechnology(
                                            activity,
                                            NfcAdapter.FLAG_READER_NFC_F,
                                            NfcAdapter.FLAG_LISTEN_NFC_PASSIVE_F
                                        )
                                        felicaServiceInstance?.setNfcid2ForService(serviceIntent, idm)
                                        felicaServiceInstance?.enableService(activity, serviceIntent)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .transformedHeight(this, transformationSpec),
                                    transformation = SurfaceTransformation(transformationSpec),
                                ) {
                                    Text("▶ Run HCE-F Card")
                                }
                            }
                        }
                        else {
                            item {
                                Button(
                                    onClick = {
                                        isRunningNFC = false
                                        nfcAdapter.resetDiscoveryTechnology(activity)
                                        felicaServiceInstance?.disableService(activity)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .transformedHeight(this, transformationSpec),
                                    transformation = SurfaceTransformation(transformationSpec),
                                ) {
                                    Text("■ Stop HCE-F Card")
                                }
                            }
                        }
                    }
                    else {
                        toastMessage("NFC Not Available")

                        // 버튼 위치에 빈 공간
                        item {
                            ListHeader(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                            ) {

                            }
                        }
                    }

                    // 중간 여백
                    item {
                        ListHeader(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {

                        }
                    }
                    item {
                        ListHeader(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {

                        }
                    }

                    item {
                        Button(
                            onClick = {
                                if(isRunningNFC) {
                                    toastMessage("Stop card first before change!")
                                }
                                else {
                                    inputForm("Input new card name here") { newCardName ->
                                        cardList.modifyCardInfo(index, cardName = newCardName)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("✎ Change Card Name")
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                if(isRunningNFC) {
                                    toastMessage("Stop card first before change!")
                                }
                                else {
                                    requestConfirm("Change Card Number?") {
                                        // 카드 번호 랜덤 생성
                                        val rnID = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
                                        cardList.modifyCardInfo(index, cardIDm = "02FE${rnID}")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("⟳ Change Card IDm")
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                if(isRunningNFC) {
                                    toastMessage("Stop card first before remove!")
                                }
                                else {
                                    requestConfirm("Remove Card?") {
                                        cardList.removeCard(index)
                                        goHome()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("☒ Remove Card")
                        }
                    }

                    item {
                        ListHeader(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "IDm: $idm")
                        }
                    }
                }
            }
        }

        if(isRequestedConfirm) {
            confirmDialog()
        }
    }
}