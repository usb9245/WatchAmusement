/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package box.mon.amusement.watch.presentation

import box.mon.amusement.watch.R
import box.mon.amusement.watch.presentation.theme.WatchAmusementTheme
import box.mon.amusement.watch.presentation.CardDataList
import box.mon.amusement.watch.presentation.CardInfoPageActivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import android.content.Intent
import java.util.Random
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cardList = CardDataList(this)

        // 카드 추가 테스트
        //cardList.addNewCard("Test1", "02FE000000000000", "0018000000000000")
        /*
        lifecycleScope.launch {
            cardList.addNewCardToList(
                "Test1",
                "02FE000000000000",
                "0018000000000000"
            )
        }
        */

        val cardListArray = cardList.getCardList()

        setContent {
            //WearAppCardList(cardListArray)
            WearAppCardList(cardList)

            /*
            CardInfoPage(
                name = "Test Card 001",
                idm = "02FE000000000000",
                pmm = "0018000000000000",
                cardList = cardList,
                index = 1
            )
            */
        }
    }
}


@Composable
//fun WearAppCardList(cardListArray : List<CardInfo>) {
fun WearAppCardList(cardList : CardDataList) {
    val context = LocalContext.current
    val cardListArray by cardList
        .listFlow()
        .collectAsState(initial = emptyList())

    WatchAmusementTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("More")
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
                            //Text(text = stringResource(R.string.hello_world, greetingName))
                            Text(text = "WatchAmusement")
                        }
                    }

                    item {
                        Button(
                            onClick = {
                                // 카드 번호 랜덤 생성 (나중에 modify 페이지 만들면 거기로 넘기기)
                                val rnID = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
                                val rnPM = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
                                val rnName = rnID.takeLast(4)

                                cardList.addNewCard("Test Card ${rnName}", "02FE${rnID}", "0018${rnPM}")
                            },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Add Card")
                        }
                    }

                    cardListArray.forEachIndexed { index, card ->
                        item {
                            Button(
                                onClick = {
                                    /*
                                    // 디버깅용 - 카드 IDm/PMm 표시
                                    val text = "${card.cardIDm} / ${card.cardPMm}"
                                    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                                    toast.show();
                                    */

                                    val cardInfoPageIntent =
                                        Intent(context, CardInfoPageActivity::class.java).apply {
                                            putExtra("cardIndex", index)
                                        }

                                    context.startActivity(cardInfoPageIntent)
                                },
                                modifier = Modifier.fillMaxWidth()
                                    .transformedHeight(this, transformationSpec),
                                transformation = SurfaceTransformation(transformationSpec),
                            ) {
                                Text(card.cardName)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    WatchAmusementTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
                edgeButton = {
                    EdgeButton(
                        onClick = { /*TODO*/ },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("More")
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
                            //Text(text = stringResource(R.string.hello_world, greetingName))
                            Text(text = "WatchAmusement")
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Button A")
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Button B")
                        }
                    }
                    item {
                        Button(
                            onClick = { /*TODO*/ },
                            modifier = Modifier.fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text("Button C")
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp("Preview Android")
}