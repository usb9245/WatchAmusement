package box.mon.amusement.watch

import box.mon.amusement.watch.theme.WatchAmusementTheme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cardList = CardDataList(this)

        setContent {
            WearAppCardList(cardList)
        }
    }
}


@Composable
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
                        onClick = {
                            // 카드 번호 랜덤 생성
                            val rnID = UUID.randomUUID().toString().replace("-", "").take(12).uppercase()
                            val rnName = rnID.takeLast(4)

                            cardList.addNewCard("Card $rnName", "02FE${rnID}")
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    ) {
                        Text("Add Card")
                    }
                },
            ) { contentPadding ->
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
                    cardListArray.forEachIndexed { index, card ->
                        item {
                            Button(
                                onClick = {
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