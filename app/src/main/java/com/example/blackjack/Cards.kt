package com.example.blackjack

interface Card {
    val name: String
    val value: Int
    val drawable: Int
}

class Cards(){
    inner class Club2() : Card{
        override val name = "Two of Clubs"
        override val value = 2
        override val drawable = R.drawable.clubs2
    }
    inner class Club3() : Card{
        override val name = "Three of Clubs"
        override val value = 3
        override val drawable = R.drawable.clubs3
    }
    inner class Club4() : Card{
        override val name = "Four of Clubs"
        override val value = 4
        override val drawable = R.drawable.clubs4
    }
    inner class Club5() : Card{
        override val name = "Five of Clubs"
        override val value = 5
        override val drawable = R.drawable.clubs5
    }
    inner class Club6() : Card{
        override val name = "Six of Clubs"
        override val value = 6
        override val drawable = R.drawable.clubs6
    }
    inner class Club7() : Card{
        override val name = "Seven of Clubs"
        override val value = 7
        override val drawable = R.drawable.clubs7
    }
    inner class Club8() : Card{
        override val name = "Eight of Clubs"
        override val value = 8
        override val drawable = R.drawable.clubs8
    }
    inner class Club9() : Card{
        override val name = "Nine of Clubs"
        override val value = 9
        override val drawable = R.drawable.clubs9
    }
    inner class Club10() : Card{
        override val name = "Ten of Clubs"
        override val value = 10
        override val drawable = R.drawable.clubs10
    }
    inner class ClubJ() : Card{
        override val name = "Jack of Clubs"
        override val value = 10
        override val drawable = R.drawable.clubsj
    }
    inner class ClubQ() : Card{
        override val name = "Queen of Clubs"
        override val value = 10
        override val drawable = R.drawable.clubsq
    }
    inner class ClubK() : Card{
        override val name = "King of Clubs"
        override val value = 10
        override val drawable = R.drawable.clubsk
    }
    inner class ClubA() : Card{
        override val name = "Ace of Clubs"
        override val value = 10
        override val drawable = R.drawable.clubsa
    }
    inner class Diamond2() : Card{
        override val name = "Two of Diamonds"
        override val value = 2
        override val drawable = R.drawable.diamonds2
    }
    inner class Diamond3() : Card{
        override val name = "Three of Diamonds"
        override val value = 3
        override val drawable = R.drawable.diamonds3
    }
    inner class Diamond4() : Card{
        override val name = "Four of Diamonds"
        override val value = 4
        override val drawable = R.drawable.diamonds4
    }
    inner class Diamond5() : Card{
        override val name = "Five of Diamonds"
        override val value = 5
        override val drawable = R.drawable.diamonds5
    }
    inner class Diamond6() : Card{
        override val name = "Six of Diamonds"
        override val value = 6
        override val drawable = R.drawable.diamonds6
    }
    inner class Diamond7() : Card{
        override val name = "Seven of Diamonds"
        override val value = 7
        override val drawable = R.drawable.diamonds7
    }
    inner class Diamond8() : Card{
        override val name = "Eight of Diamonds"
        override val value = 8
        override val drawable = R.drawable.diamonds8
    }
    inner class Diamond9() : Card{
        override val name = "Nine of Diamonds"
        override val value = 9
        override val drawable = R.drawable.diamonds9
    }
    inner class Diamond10() : Card{
        override val name = "Ten of Diamonds"
        override val value = 10
        override val drawable = R.drawable.diamonds10
    }
    inner class DiamondJ() : Card{
        override val name = "Jack of Diamonds"
        override val value = 10
        override val drawable = R.drawable.diamondsj
    }
    inner class DiamondQ() : Card{
        override val name = "Queen of Diamonds"
        override val value = 10
        override val drawable = R.drawable.diamondsq
    }
    inner class DiamondK() : Card{
        override val name = "King of Diamonds"
        override val value = 10
        override val drawable = R.drawable.diamondsk
    }
    inner class DiamondA() : Card{
        override val name = "Ace of Diamonds"
        override val value = 10
        override val drawable = R.drawable.diamondsa
    }
    inner class Heart2() : Card{
        override val name = "Two of Hearts"
        override val value = 2
        override val drawable = R.drawable.hearts2
    }
    inner class Heart3() : Card{
        override val name = "Three of Hearts"
        override val value = 3
        override val drawable = R.drawable.hearts3
    }
    inner class Heart4() : Card{
        override val name = "Four of Hearts"
        override val value = 4
        override val drawable = R.drawable.hearts4
    }
    inner class Heart5() : Card{
        override val name = "Five of Hearts"
        override val value = 5
        override val drawable = R.drawable.hearts5
    }
    inner class Heart6() : Card{
        override val name = "Six of Hearts"
        override val value = 6
        override val drawable = R.drawable.hearts6
    }
    inner class Heart7() : Card{
        override val name = "Seven of Hearts"
        override val value = 7
        override val drawable = R.drawable.hearts7
    }
    inner class Heart8() : Card{
        override val name = "Eight of Hearts"
        override val value = 8
        override val drawable = R.drawable.hearts8
    }
    inner class Heart9() : Card{
        override val name = "Nine of Hearts"
        override val value = 9
        override val drawable = R.drawable.hearts9
    }
    inner class Heart10() : Card{
        override val name = "Ten of Hearts"
        override val value = 10
        override val drawable = R.drawable.hearts10
    }
    inner class HeartJ() : Card{
        override val name = "Jack of Hearts"
        override val value = 10
        override val drawable = R.drawable.heartsj
    }
    inner class HeartQ() : Card{
        override val name = "Queen of Hearts"
        override val value = 10
        override val drawable = R.drawable.heartsq
    }
    inner class HeartK() : Card{
        override val name = "King of Hearts"
        override val value = 10
        override val drawable = R.drawable.heartsk
    }
    inner class HeartA() : Card{
        override val name = "Ace of Hearts"
        override val value = 10
        override val drawable = R.drawable.heartsa
    }
    inner class Spade2() : Card{
        override val name = "Two of Spades"
        override val value = 2
        override val drawable = R.drawable.spades2
    }
    inner class Spade3() : Card{
        override val name = "Three of Spades"
        override val value = 3
        override val drawable = R.drawable.spades3
    }
    inner class Spade4() : Card{
        override val name = "Four of Spades"
        override val value = 4
        override val drawable = R.drawable.spades4
    }
    inner class Spade5() : Card{
        override val name = "Five of Spades"
        override val value = 5
        override val drawable = R.drawable.spades5
    }
    inner class Spade6() : Card{
        override val name = "Six of Spades"
        override val value = 6
        override val drawable = R.drawable.spades6
    }
    inner class Spade7() : Card{
        override val name = "Seven of Spades"
        override val value = 7
        override val drawable = R.drawable.spades7
    }
    inner class Spade8() : Card{
        override val name = "Eight of Spades"
        override val value = 8
        override val drawable = R.drawable.spades8
    }
    inner class Spade9() : Card{
        override val name = "Nine of Spades"
        override val value = 9
        override val drawable = R.drawable.spades9
    }
    inner class Spade10() : Card{
        override val name = "Ten of Spades"
        override val value = 10
        override val drawable = R.drawable.spades10
    }
    inner class SpadeJ() : Card{
        override val name = "Jack of Spades"
        override val value = 10
        override val drawable = R.drawable.spadesj
    }
    inner class SpadeQ() : Card{
        override val name = "Queen of Spades"
        override val value = 10
        override val drawable = R.drawable.spadesq
    }
    inner class SpadeK() : Card{
        override val name = "King of Spades"
        override val value = 10
        override val drawable = R.drawable.spadesk
    }
    inner class SpadeA() : Card{
        override val name = "Ace of Spades"
        override val value = 10
        override val drawable = R.drawable.spadesa
    }
}