package box.mon.amusement.watch.presentation

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.lifecycle.lifecycleScope
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

@Serializable
data class CardInfo(
    val cardName: String,
    val cardIDm: String,
    val cardPMm: String
)

@Serializable data class CardList(val cardListArray: List<CardInfo>)
object CardListSerializer : Serializer<CardList> {

    override val defaultValue: CardList = CardList(cardListArray = emptyList())

    override suspend fun readFrom(input: InputStream): CardList =
        try {
            Json.decodeFromString(CardList.serializer(), input.readBytes().decodeToString())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Card List Data", serialization)
        }

    override suspend fun writeTo(t: CardList, output: OutputStream) {
        output.write(Json.encodeToString(CardList.serializer(), t).encodeToByteArray())
    }
}

val Context.dataStore: DataStore<CardList> by
    dataStore(
        fileName = "cardlist.json",
        serializer = CardListSerializer,
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    )

class CardDataList(private val context: Context) {
    fun listFlow(): Flow<List<CardInfo>> =
        context.dataStore.data.map { cardList -> cardList.cardListArray }

    suspend fun getCardListFromFlow() : List<CardInfo> {
        val cardList = listFlow().first()
        return cardList
    }

    public fun getCardList() : List<CardInfo>  {
        var list = emptyList<CardInfo>()
        CoroutineScope(Dispatchers.IO).launch {
            list = getCardListFromFlow()
        }
        return list
    }

    suspend fun addNewCardToList(name: String, idm : String, pmm : String) {
        val newCard = CardInfo(
            cardName = name,
            cardIDm = idm,
            cardPMm = pmm
        )

        context.dataStore.updateData { cardList ->
            cardList.copy(cardListArray = cardList.cardListArray + newCard)
        }
    }

    public fun addNewCard(name: String, idm : String, pmm : String) {
        CoroutineScope(Dispatchers.IO).launch {
            addNewCardToList(name, idm, pmm)
        }
    }

    suspend fun removeCardToList(index : Int) {
        context.dataStore.updateData { cardList ->
            if (index >= cardList.cardListArray.size) {
                cardList
            } else {
                cardList.copy(
                    cardListArray = cardList.cardListArray.toMutableList().apply {
                        removeAt(index)
                    }
                )
            }
        }
    }

    public fun removeCard(index : Int) {
        CoroutineScope(Dispatchers.IO).launch {
            removeCardToList(index)
        }
    }
}