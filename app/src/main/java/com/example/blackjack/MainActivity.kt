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

    private fun addCardToLayout(card: Card, layout: LinearLayout, numberOfCards: Int, isDealer: Boolean) {
        val scale = binding.root.context.resources.displayMetrics.density
        val imageView = ImageView(binding.root.context).apply {
            setImageResource(card.drawable)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val margin30dp = (30 * scale + 0.5f).toInt()
                if (numberOfCards > 1) {
                    setMargins(
                        if (isDealer) -margin30dp else 0,
                        if (isDealer) -margin30dp else 0,
                        if (!isDealer) -margin30dp else 0,
                        if (!isDealer) -margin30dp else 0
                    )
                }
                height = (70 * scale + 0.5f).toInt()
            }
        }
        layout.addView(imageView, if (isDealer) -1 else 0)
    }

    @SuppressLint("SetTextI18n")
    fun updateScores() {
        binding.playerPoints.text = "POINTS: ${player.getHandValue()}"
        binding.dealerPoints.text = "POINTS: ${dealer.getHandValue()}"
    }

    fun addCardToTable(card: Card, table: String, numberOfCards: Int) {
        val layout = when (table) {
            "player" -> binding.playerCardsLayout
            "dealer" -> binding.dealerCardsLayout
            else -> throw IllegalArgumentException("Unknown table type: $table")
        }
        addCardToLayout(card, layout, numberOfCards, table == "dealer")
    }
}

//////////////////////////////////////////
//              SITE CLASS              //
//////////////////////////////////////////

class Site(private val site: String) {
    private val hand = mutableListOf<Card>()

    fun addCard(card: Card) {
        hand.add(card)
    }

    fun resetHand() {
        hand.clear()
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
    fun showHand(): String = hand.joinToString { it.name }

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

    @SuppressLint("MissingInflatedId")
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

        dealer.hit(cardDeck.drawCard(), table)
        dealer.hit(cardDeck.drawCard(), table)

        player.hit(cardDeck.drawCard(), table)
        player.hit(cardDeck.drawCard(), table)

        binding.decisionLayout.visibility = LinearLayout.VISIBLE
    }

    private fun dealerTurn() {
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
