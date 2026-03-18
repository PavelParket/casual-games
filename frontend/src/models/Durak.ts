export type CardSuit = 'HEARTS' | 'DIAMONDS' | 'CLUBS' | 'SPADES';

export type CardRank = | 'SIX' | 'SEVEN' | 'EIGHT' | 'NINE' | 'TEN' | 'JACK' | 'QUEEN' | 'KING' | 'ACE';

export type DurakPhase = | 'ATTACKING' | 'DEFENDING' | 'THROWING_MORE' | 'PICKING_UP' | 'BOUT_END' | 'GAME_OVER';

export type DurakAction = 'PLAY_CARD' | 'TAKE_CARDS' | 'PASS';

export interface DurakCard {
    rank: CardRank;
    suit: CardSuit;
}

export interface DurakTablePair {
    attackCard: DurakCard;
    defendCard: DurakCard | null;
}
