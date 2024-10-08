package com.example.blackjack

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private val scale by lazy { binding.root.context.resources.displayMetrics.density }
    private val cardHeight = (70 * scale + 0.5f).toInt()
    private val margin30dp = (30 * scale + 0.5f).toInt()
    private val margin20dp = (20 * scale + 0.5f).toInt()

    private fun calculateLayoutParams(isDealer: Boolean, numberOfCards: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            cardHeight
        ).apply {
            val horizontalMargin = if (numberOfCards % 2 == 0) -margin20dp else 0
            val verticalMargin = if (numberOfCards > 1) -margin30dp else 0

            if (isDealer) setMargins(horizontalMargin, verticalMargin, 0, 0)
            else setMargins(0, 0, horizontalMargin, verticalMargin)
        }
    }

    private fun createCardImageView(card: Card?, isBackCard: Boolean): ImageView {
        return ImageView(binding.root.context).apply {
            setImageResource(if (isBackCard) R.drawable.back_light else card?.drawable ?: 0)
        }
    }

    private fun addCardToLayout(
        card: Card?, layout: LinearLayout, numberOfCards: Int, isDealer: Boolean, isBackCard: Boolean = false
    ) {
        val layoutParams = calculateLayoutParams(isDealer, numberOfCards)
        val imageView = createCardImageView(card, isBackCard).apply {
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
//             SITE CLASS               //
//////////////////////////////////////////

class Site(private val site: String, private val binding: ActivityMainBinding) {
    private val hand = mutableListOf<Card>()
    private var hiddenCard: Card? = null

    private fun addCard(card: Card) {
        hand.add(card)
    }

    fun addHiddenCard(card: Card) {
        hiddenCard = card
    }

    fun revealHiddenCard(table: Table) {
        hiddenCard?.let {
            binding.dealerCardsLayout.removeViewAt(1)

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

    fun isBusted() = getHandValue() > 21

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

        binding.hitButton.setOnClickListener { handlePlayerHit() }
        binding.stayButton.setOnClickListener { handleDealerTurn() }
        binding.resetButton.setOnClickListener { resetGame() }
    }

    private fun startGame() {
        cardDeck = CardDeck()
        player = Site("player", binding)
        dealer = Site("dealer", binding)
        table = Table(binding, player, dealer)

        binding.endLayout.visibility = LinearLayout.GONE
        binding.dealerCardsLayout.removeAllViews()
        binding.playerCardsLayout.removeAllViews()

        CoroutineScope(Dispatchers.Main).launch {
            dealer.hit(cardDeck.drawCard(), table)
            delay(2000)

            player.hit(cardDeck.drawCard(), table)
            delay(2000)

            dealer.addHiddenCard(cardDeck.drawCard())
            table.addCardToTable(null, "dealer", 2, isBackCard = true)
            delay(2000)

            player.hit(cardDeck.drawCard(), table)
            delay(2000)

            binding.decisionLayout.visibility = LinearLayout.VISIBLE
        }
    }

    private fun handlePlayerHit() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.decisionLayout.visibility = LinearLayout.GONE
            delay(2000)
            player.hit(cardDeck.drawCard(), table)
            if (player.isBusted()) {
                delay(2000)
                endGame("Player busted! Dealer wins!")
            } else {
                delay(2000)
                binding.decisionLayout.visibility = LinearLayout.VISIBLE
            }
        }
    }

    private fun handleDealerTurn() {
        binding.decisionLayout.visibility = LinearLayout.GONE
        dealerTurn()
    }

    private fun dealerTurn() {
        CoroutineScope(Dispatchers.Main).launch {
            dealer.revealHiddenCard(table)
            delay(2000)
            while (dealer.getHandValue() < 17) {
                dealer.hit(cardDeck.drawCard(), table)
                delay(2000)
            }
            val result = when {
                dealer.isBusted() -> "Dealer busted! Player wins!"
                player.getHandValue() > dealer.getHandValue() -> "Player wins!"
                player.getHandValue() < dealer.getHandValue() -> "Dealer wins!"
                else -> "It's a tie!"
            }
            delay(2000)
            endGame(result)
        }
    }

    private fun endGame(result: String) {
        binding.endText.text = result

        val color = when (result) {
            "Player wins!" -> ContextCompat.getColor(this, R.color.gold)
            "Dealer wins!" -> ContextCompat.getColor(this, R.color.red)
            "It's a tie!" -> ContextCompat.getColor(this, R.color.white)
            "Player busted! Dealer wins!" -> ContextCompat.getColor(this, R.color.red)
            "Dealer busted! Player wins!" -> ContextCompat.getColor(this, R.color.gold)
            else -> ContextCompat.getColor(this, R.color.white)
        }

        binding.endText.setTextColor(color)
        binding.endLayout.visibility = LinearLayout.VISIBLE
        Log.d("Blackjack", result)
    }




    private fun resetGame() {
        player.resetHand()
        dealer.resetHand()
        binding.startLayout.visibility = LinearLayout.VISIBLE
        binding.endLayout.visibility = LinearLayout.GONE
    }

    private fun setupEdgeToEdgeDisplay() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

