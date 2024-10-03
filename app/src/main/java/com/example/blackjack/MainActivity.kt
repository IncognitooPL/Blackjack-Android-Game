package com.example.blackjack

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blackjack.Cards
import com.example.blackjack.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private  lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        enableEdgeToEdge()
        setContentView(view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        class Player() {
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

        val cardDeck = CardDeck()
        val player = Player()

        binding.btn.setOnClickListener {
            val card = cardDeck.drawCard()
            Log.d("BlackJack: ", "Selected card: ${card.name}")
            binding.img.setImageResource(card.drawable)
        }

        /*

        player.hit(cardDeck.drawCard())
        player.hit(cardDeck.drawCard())

        Log.d("BlackJack", "Player has cards: ${player.showHand()} with a value of ${player.getHandValue()}")

        if (player.hasBlackjack()) {
            Log.d("BlackJack", "Player has Blackjack!")
        } else if (player.isBusted()) {
            Log.d("BlackJack", "Player is busted!")
        }
        */
    }
}