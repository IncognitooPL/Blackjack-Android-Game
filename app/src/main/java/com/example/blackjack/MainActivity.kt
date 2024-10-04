
package com.example.blackjack
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blackjack.databinding.ActivityMainBinding
import kotlinx.coroutines.*

//////////////////////////////////////////
//           CARD DECK CLASS            //
//////////////////////////////////////////

class CardDeck {
    private val inUse = mutableSetOf<String>()
    private val card = Cards()

    private fun getRandomCardName(): String {
        val suits = arrayOf("Club", "Diamond", "Heart", "Spade")
        val values = arrayOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")
        return "${suits.random()}${values.random()}"
    }

    private fun createCardInstance(className: String): Card {
        return try {
            val clazz = Class.forName(className)
            val constructor = clazz.getDeclaredConstructor(card::class.java)
            constructor.newInstance(card) as Card
        } catch (e: Exception) {
            throw IllegalArgumentException("Card not found: $className", e)
        }
    }

    fun drawCard(): Card {
        var selectedCard: String
        do {
            selectedCard = getRandomCardName()
        } while (inUse.contains(selectedCard))
        inUse.add(selectedCard)
        return createCardInstance("com.example.blackjack.Cards\$${selectedCard.replaceFirstChar { it.uppercase() }}")
    }
}

//////////////////////////////////////////
//             TABLE CLASS              //
//////////////////////////////////////////

class Table(
    private val binding: ActivityMainBinding,
    private val player: Site,
    private val dealer: Site
) {
    private fun calculateLayoutParams(
        scale: Float, isDealer: Boolean, numberOfCards: Int
    ): LinearLayout.LayoutParams {
        val margin30dp = (30 * scale + 0.5f).toInt()
        val margin20dp = (20 * scale + 0.5f).toInt()
        val height70dp = (70 * scale + 0.5f).toInt()

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            height70dp
        ).apply {
            if (isDealer) {
                setMargins(
                    if (numberOfCards % 2 == 0) -margin20dp else 0,
                    if (numberOfCards > 1) -margin30dp else 0,
                    0,
                    0
                )
            } else {
                setMargins(
                    0,
                    0,
                    if (numberOfCards % 2 == 0) -margin20dp else 0,
                    if (numberOfCards > 1) -margin30dp else 0
                )
            }
        }
    }

    private fun addCardToLayout(
        card: Card?, layout: LinearLayout, numberOfCards: Int, isDealer: Boolean, isBackCard: Boolean = false
    ) {
        val scale = binding.root.context.resources.displayMetrics.density
        val layoutParams = calculateLayoutParams(scale, isDealer, numberOfCards)

        val imageView = ImageView(binding.root.context).apply {
            setImageResource(if (isBackCard) R.drawable.back_light else card?.drawable ?: 0)
            this.layoutParams = layoutParams
        }

        layout.addView(imageView, if (isDealer) -1 else 0)
    }

    fun addCardToTable(card: Card?, table: String, numberOfCards: Int, isBackCard: Boolean = false) {
        val layout = when (table) {
            "player" -> binding.playerCardsLayout
            "dealer" -> binding.dealerCardsLayout
            else -> throw IllegalArgumentException("Unknown table type: $table")
        }
        addCardToLayout(card, layout, numberOfCards, table == "dealer", isBackCard)
    }

    @SuppressLint("SetTextI18n")
    fun updateScores() {
        binding.playerPoints.text = "POINTS: ${player.getHandValue()}"
        binding.dealerPoints.text = "POINTS: ${dealer.getHandValue()}"
    }
}

//////////////////////////////////////////
//              SITE CLASS              //
//////////////////////////////////////////

class Site(private val site: String) {
    private val hand = mutableListOf<Card>()
    private var hiddenCard: Card? = null

    fun addCard(card: Card) {
        hand.add(card)
    }

    fun addHiddenCard(card: Card) {
        hiddenCard = card
    }

    fun revealHiddenCard(table: Table) {
        hiddenCard?.let {
            addCard(it)
            table.addCardToTable(it, site, hand.size)
            table.updateScores()
            hiddenCard = null
        }
    }

    fun resetHand() {
        hand.clear()
        hiddenCard = null
    }

    fun getHandValue(): Int {
        var total = hand.sumOf { it.value }
        var aces = hand.count { it.name.startsWith("A") }

        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }
        return total
    }

    fun isBusted(): Boolean = getHandValue() > 21
    fun hasBlackjack(): Boolean = getHandValue() == 21
    fun hit(card: Card, table: Table) {
        addCard(card)
        table.addCardToTable(card, site, hand.size)
        table.updateScores()
    }
}

//////////////////////////////////////////
//              MAIN CLASS              //
//////////////////////////////////////////

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cardDeck: CardDeck
    private lateinit var table: Table
    private lateinit var player: Site
    private lateinit var dealer: Site

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdgeDisplay()

        binding.startButton.setOnClickListener {
            binding.startLayout.visibility = LinearLayout.GONE
            startGame()
        }

        binding.hitButton.setOnClickListener {
            player.hit(cardDeck.drawCard(), table)
            if (player.isBusted()) {
                binding.decisionLayout.visibility = LinearLayout.GONE
                Log.d("Blackjack", "Player busted")
            }
        }

        binding.stayButton.setOnClickListener {
            binding.decisionLayout.visibility = LinearLayout.GONE
            dealerTurn()
        }
    }

    private fun startGame() {
        cardDeck = CardDeck()
        player = Site("player")
        dealer = Site("dealer")
        table = Table(binding, player, dealer)

        CoroutineScope(Dispatchers.Main).launch {
            dealer.hit(cardDeck.drawCard(), table)
            delay(2000)

            dealer.addHiddenCard(cardDeck.drawCard())
            table.addCardToTable(null, "dealer", 2, isBackCard = true)
            delay(2000)

            player.hit(cardDeck.drawCard(), table)
            delay(2000)
            player.hit(cardDeck.drawCard(), table)

            delay(2000)
            binding.decisionLayout.visibility = LinearLayout.VISIBLE
        }
    }

    private fun dealerTurn() {
        dealer.revealHiddenCard(table)
        while (dealer.getHandValue() < 17) {
            dealer.hit(cardDeck.drawCard(), table)
        }
        val result = when {
            dealer.isBusted() || player.getHandValue() > dealer.getHandValue() -> "Player wins"
            player.getHandValue() < dealer.getHandValue() -> "Dealer wins"
            else -> "It's a tie"
        }
        Log.d("Blackjack", result)
    }

    private fun setupEdgeToEdgeDisplay() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
