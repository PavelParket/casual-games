// =====================
// Hearts
// =====================
import sixHearts from './six_hearts.svg';
import sevenHearts from './seven_hearts.svg';
import eightHearts from './eight_hearts.svg';
import nineHearts from './nine_hearts.svg';
import tenHearts from './ten_hearts.svg';
import jackHearts from './jack_hearts.svg';
import queenHearts from './queen_hearts.svg';
import kingHearts from './king_hearts.svg';
import aceHearts from './ace_hearts.svg';

// =====================
// Diamonds
// =====================
import sixDiamonds from './six_diamonds.svg';
import sevenDiamonds from './seven_diamonds.svg';
import eightDiamonds from './eight_diamonds.svg';
import nineDiamonds from './nine_diamonds.svg';
import tenDiamonds from './ten_diamonds.svg';
import jackDiamonds from './jack_diamonds.svg';
import queenDiamonds from './queen_diamonds.svg';
import kingDiamonds from './king_diamonds.svg';
import aceDiamonds from './ace_diamonds.svg';

// =====================
// Clubs
// =====================
import sixClubs from './six_clubs.svg';
import sevenClubs from './seven_clubs.svg';
import eightClubs from './eight_clubs.svg';
import nineClubs from './nine_clubs.svg';
import tenClubs from './ten_clubs.svg';
import jackClubs from './jack_clubs.svg';
import queenClubs from './queen_clubs.svg';
import kingClubs from './king_clubs.svg';
import aceClubs from './ace_clubs.svg';

// =====================
// Spades
// =====================
import sixSpades from './six_spades.svg';
import sevenSpades from './seven_spades.svg';
import eightSpades from './eight_spades.svg';
import nineSpades from './nine_spades.svg';
import tenSpades from './ten_spades.svg';
import jackSpades from './jack_spades.svg';
import queenSpades from './queen_spades.svg';
import kingSpades from './king_spades.svg';
import aceSpades from './ace_spades.svg';

// =====================
// Card back
// =====================
import cardBack from './card_back.svg';

export const CardImages = {
    SIX: { HEARTS: sixHearts, DIAMONDS: sixDiamonds, CLUBS: sixClubs, SPADES: sixSpades },
    SEVEN: { HEARTS: sevenHearts, DIAMONDS: sevenDiamonds, CLUBS: sevenClubs, SPADES: sevenSpades },
    EIGHT: { HEARTS: eightHearts, DIAMONDS: eightDiamonds, CLUBS: eightClubs, SPADES: eightSpades },
    NINE: { HEARTS: nineHearts, DIAMONDS: nineDiamonds, CLUBS: nineClubs, SPADES: nineSpades },
    TEN: { HEARTS: tenHearts, DIAMONDS: tenDiamonds, CLUBS: tenClubs, SPADES: tenSpades },
    JACK: { HEARTS: jackHearts, DIAMONDS: jackDiamonds, CLUBS: jackClubs, SPADES: jackSpades },
    QUEEN: { HEARTS: queenHearts, DIAMONDS: queenDiamonds, CLUBS: queenClubs, SPADES: queenSpades },
    KING: { HEARTS: kingHearts, DIAMONDS: kingDiamonds, CLUBS: kingClubs, SPADES: kingSpades },
    ACE: { HEARTS: aceHearts, DIAMONDS: aceDiamonds, CLUBS: aceClubs, SPADES: aceSpades },
} as const;

export const CARD_BACK: string = cardBack;
