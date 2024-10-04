package com.example.blackjack

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blackjack.databinding.ActivityMainBinding

//////////////////////////////////////////
//           CARD DECK CLASS            //
//////////////////////////////////////////

class CardDeck {
    private val inUse = HashSet<String>()
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
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Card not found: $className", e)
        }
    }

    fun drawCard(): Card {
        var selectedCard: String

        do {
            selectedCard = getRandomCardName()
        } while (inUse.contains(selectedCard))

        inUse.add(selectedCard)

        val className = "com.example.blackjack.Cards\$${selectedCard.replaceFirstChar { it.uppercase() }}"

        return createCardInstance(className)
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

    private fun addCardToLayout(
        card: Card,
        layout: LinearLayout,
        numberOfCards: Int,
        isDealer: Boolean
    ) {
        val imageView = ImageView(binding.root.context).apply {
            setImageResource(card.drawable)
            layoutParams = createLayoutParams(numberOfCards, isDealer)
        }

        layout.addView(imageView, if (isDealer) -1 else 0)
    }

    private fun createLayoutParams(numberOfCards: Int, isDealer: Boolean): LinearLayout.LayoutParams {
        val scale = binding.root.context.resources.displayMetrics.density
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            val margin30dp = (30 * scale + 0.5f).toInt()

            if (numberOfCards > 1) {
                val leftMargin = if (numberOfCards % 2 == 0) -margin30dp else 0
                val rightMargin = if (numberOfCards % 2 == 0) -margin30dp else 0

                val topMargin = if (isDealer) -margin30dp else 0
                val bottomMargin = if (!isDealer) -margin30dp else 0

                setMargins(if(isDealer) leftMargin else 0, topMargin, if(!isDealer) rightMargin else 0, bottomMargin)
            }

            height = (70 * scale + 0.5f).toInt()
        }
        return params
    }


    private fun clearTable(table: String) {
        val layout = when (table) {
            "player" -> binding.playerCardsLayout
            "dealer" -> binding.dealerCardsLayout
            else -> throw IllegalArgumentException("Unknown table type: $table")
        }
        layout.removeAllViews()
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

    private fun addCard(card: Card) {
        hand.add(card)
    }

    fun resetHand(hand: MutableList<Card>) {
        hand.clear()
    }

    fun getHandValue(): Int {
        var total = 0
        var aces = 0

        for (card in hand) {
            total += card.value
            if (card.name.startsWith("A")) aces++
        }

        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }

        return total
    }

    fun isBusted(): Boolean {
        return getHandValue() > 21
    }

    fun hasBlackjack(): Boolean {
        return getHandValue() == 21
    }

    fun showHand(): String {
        return hand.joinToString { it.name }
    }

    fun hit(card: Card, table: Table) {
        Thread.sleep(5000)
        addCard(card)
        table.addCardToTable(card, site, hand.count())
        table.updateScores()
    }

    fun stand() {

    }
}


//////////////////////////////////////////
//              MAIN CLASS              //
//////////////////////////////////////////

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private  lateinit var binding: ActivityMainBinding              // View binding
    private lateinit var cardDeck: CardDeck                         // Card deck object
    private lateinit var table: Table                               // Table object
    private lateinit var player: Site                               // Player object
    private lateinit var dealer: Site                               // Dealer object

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)                          // Call the superclass onCreate

        binding = ActivityMainBinding.inflate(layoutInflater)       // Inflate the layout
        val view = binding.root                                     // Get the root view

        enableEdgeToEdge()                                          // Enable edge-to-edge display
        setContentView(view)                                        // Set the content view

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fun StartGame() {
            cardDeck = CardDeck()
            player = Site("player")
            dealer = Site("dealer")
            table = Table(binding, player, dealer)

            dealer.hit(cardDeck.drawCard(), table)                        // Dealer draws a card
            dealer.hit(cardDeck.drawCard(), table)                        // Dealer draws a card

            player.hit(cardDeck.drawCard(), table)                        // Player draws a card
            player.hit(cardDeck.drawCard(), table)                        // Player draws a card

            binding.decisionLayout.visibility = LinearLayout.VISIBLE
        }

        binding.startButton.setOnClickListener {
            binding.startLayout.visibility = LinearLayout.GONE
            StartGame()
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
            while (dealer.getHandValue() < 17) {
                dealer.hit(cardDeck.drawCard(), table)
            }
            if (dealer.isBusted() || player.getHandValue() > dealer.getHandValue()) {
                Log.d("Blackjack", "Player wins")
            } else if (player.getHandValue() < dealer.getHandValue()) {
                Log.d("Blackjack", "Dealer wins")
            } else {
                Log.d("Blackjack", "It's a tie")
            }
        }
    }
}