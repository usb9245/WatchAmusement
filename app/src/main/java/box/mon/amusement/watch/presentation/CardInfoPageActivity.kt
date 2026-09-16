package box.mon.amusement.watch.presentation

import box.mon.amusement.watch.presentation.theme.WatchAmusementTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import kotlin.system.exitProcess

class CardInfoPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cardIndex = intent.getIntExtra("cardIndex", -1)

        val cardList = CardDataList(this)
        //val cardListArray = cardList.getCardList()

        setContent {
            val cardListArray by cardList
                .listFlow()
                .collectAsState(initial = emptyList())

            if (cardIndex in cardListArray.indices) {

                val showingCard = cardListArray[cardIndex]

                CardInfoPage(
                    name = showingCard.cardName,
                    idm = showingCard.cardIDm,
                    pmm = showingCard.cardPMm,
                    cardList = cardList,
                    index = cardIndex,
                    goHome = { finish() }
                )
            }
        }
    }
}

@Composable
fun CardInfoPage(
    name: String = "null",
    idm: String = "02FE000000000000",
    pmm: String = "0118000000000000",
    cardList: CardDataList,
    index: Int = 0,
    goHome: () -> Unit = {}
) {
    val context = LocalContext.current

    WatchAmusementTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { goHome() },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("Back")
                    }
                },
            ) { contentPadding -> // ScreenScaffold provides default padding; adjust as needed
                TransformingLazyColumn(contentPadding = contentPadding, state = listState) {
                    item {
                        ListHeader(
                            modifier =
                                Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "WatchAmusement")
                        }
                    }
                    item {
                        ListHeader(
                            modifier =
                                Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = name)
                        }
                    }

                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("▶ Run HCE-F Card")
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("✎ Modify Card")
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                cardList.removeCard(index)
                                goHome()
                            },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("☒ Remove Card")
                        }
                    }

                    item {
                        ListHeader(
                            modifier =
                                Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "IDm: ${idm}")
                        }
                    }

                    item {
                        ListHeader(
                            modifier =
                                Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(text = "PMm: ${pmm}")
                        }
                    }
                }
            }
        }
    }
}