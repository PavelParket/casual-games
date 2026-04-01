import { CARD_BACK, CardImages } from './../../../../assets/icons/cards-svg/index';
import type { CardRank, CardSuit, DurakCard } from "../../../../models/Durak";

export function suitSymbol(suit: CardSuit): string {
    switch (suit) {
        case 'HEARTS': return '♥';
        case 'DIAMONDS': return '♠';
        case 'CLUBS': return '♦';
        case 'SPADES': return '♣';
    }
}

export function suitColor(suit: CardSuit): string {
    return suit === 'HEARTS' || suit === 'DIAMONDS' ? 'red' : 'black';
}

export function rankLabel(rank: CardRank): string {
    switch (rank) {
        case 'SIX': return '6';
        case 'SEVEN': return '7';
        case 'EIGHT': return '8';
        case 'NINE': return '9';
        case 'TEN': return '10';
        case 'JACK': return 'J';
        case 'QUEEN': return 'Q';
        case 'KING': return 'K';
        case 'ACE': return 'A';
    }
}

const RANK_ORDER: Record<CardRank, number> = {
    SIX: 0, SEVEN: 1, EIGHT: 2, NINE: 3, TEN: 4,
    JACK: 5, QUEEN: 6, KING: 7, ACE: 8,
};

export function sortCards(cards: DurakCard[], trumpSuit: CardSuit): DurakCard[] {
    return [...cards].sort((a, b) => {
        const aTrump = a.suit === trumpSuit ? 1 : 0;
        const bTrump = b.suit === trumpSuit ? 1 : 0;
        if (aTrump !== bTrump) return aTrump - bTrump;
        return RANK_ORDER[a.rank] - RANK_ORDER[b.rank];
    });
}

export function cardId(card: DurakCard): string {
    return `${card.rank}_${card.suit}`;
}

export function getCardImage(rank: CardRank, suit: CardSuit): string {
    return CardImages[rank][suit];
}

export { CARD_BACK };
