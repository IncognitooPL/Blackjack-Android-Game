package com.example.blackjack

import android.annotation.SuppressLint
import android.os.Bundle
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
    private val player: Player,
    private val dealer: Dealer
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
        binding.playerPoints.text = "POINTS: ${player.getHandValue().toString()}"
        binding.dealerPoints.text = "POINTS: ${dealer.getHandValue().toString()}"
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
//             PLAYER CLASS             //
//////////////////////////////////////////

class Player() {
    private val hand = mutableListOf<Card>()

    private fun addCard(card: Card) {
        hand.add(card)
    }

    fun resetHand() {
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

    fun hit(card: Card) {
        addCard(card)
    }

    fun stand() {

    }
}

//////////////////////////////////////////
//             DEALER CLASS             //
//////////////////////////////////////////

class Dealer() {
    private val hand = mutableListOf<Card>()

    fun addCard(card: Card) {
        hand.add(card)
    }

    fun resetHand() {
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

    fun hit(card: Card) {
        addCard(card)
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
    private lateinit var player: Player                             // Player object
    private lateinit var dealer: Dealer                             // Dealer object

    private var playerCardCount = 0                                 // Player card count
    private var dealerCardCount = 0                                 // Dealer card count

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

        cardDeck = CardDeck()
        player = Player()
        dealer = Dealer()

        table = Table(binding, player, dealer)


        val playerCard1 : Card = cardDeck.drawCard()                            // Draw a card for the player
        val dealerCard1 : Card = cardDeck.drawCard()                            // Draw a card for the dealer

        player.hit(playerCard1)                                                 // Player hits
        playerCardCount++                                                       // Increment player card count
        table.addCardToTable(playerCard1, "player", playerCardCount)        // Add the card to the table

        dealer.hit(dealerCard1)                                                 // Dealer hits
        dealerCardCount++                                                       // Increment dealer card count
        table.addCardToTable(dealerCard1, "dealer", dealerCardCount)        // Add the card to the table

        table.updateScores()                                                    // Update the scores



        val playerCard2 : Card = cardDeck.drawCard()
        val dealerCard2 : Card = cardDeck.drawCard()

        player.hit(playerCard2)
        playerCardCount++
        table.addCardToTable(playerCard2, "player", playerCardCount)

        dealer.hit(dealerCard2)
        dealerCardCount++
        table.addCardToTable(dealerCard2, "dealer", dealerCardCount)

        table.updateScores()

        val playerCard3 : Card = cardDeck.drawCard()
        val dealerCard3 : Card = cardDeck.drawCard()

        player.hit(playerCard3)
        playerCardCount++
        table.addCardToTable(playerCard3, "player", playerCardCount)

        dealer.hit(dealerCard3)
        dealerCardCount++
        table.addCardToTable(dealerCard3, "dealer", dealerCardCount)

        table.updateScores()

        val playerCard4 : Card = cardDeck.drawCard()
        val dealerCard4 : Card = cardDeck.drawCard()

        player.hit(playerCard4)
        playerCardCount++
        table.addCardToTable(playerCard4, "player", playerCardCount)

        dealer.hit(dealerCard4)
        dealerCardCount++
        table.addCardToTable(dealerCard4, "dealer", dealerCardCount)


        table.updateScores()
    }
}