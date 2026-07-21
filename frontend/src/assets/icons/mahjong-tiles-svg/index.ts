// =====================
// Characters
// =====================
import oneCharacters from './one_characters.svg';
import twoCharacters from './two_characters.svg';
import threeCharacters from './three_characters.svg';
import fourCharacters from './four_characters.svg';
import fiveCharacters from './five_characters.svg';
import sixCharacters from './six_characters.svg';
import sevenCharacters from './seven_characters.svg';
import eightCharacters from './eight_characters.svg';
import nineCharacters from './nine_characters.svg';

// =====================
// Circles
// =====================
import oneCircles from './one_circles.svg';
import twoCircles from './two_circles.svg';
import threeCircles from './three_circles.svg';
import fourCircles from './four_circles.svg';
import fiveCircles from './five_circles.svg';
import sixCircles from './six_circles.svg';
import sevenCircles from './seven_circles.svg';
import eightCircles from './eight_circles.svg';
import nineCircles from './nine_circles.svg';

// =====================
// Bamboo
// =====================
import oneBamboo from './one_bamboo.svg';
import twoBamboo from './two_bamboo.svg';
import threeBamboo from './three_bamboo.svg';
import fourBamboo from './four_bamboo.svg';
import fiveBamboo from './five_bamboo.svg';
import sixBamboo from './six_bamboo.svg';
import sevenBamboo from './seven_bamboo.svg';
import eightBamboo from './eight_bamboo.svg';
import nineBamboo from './nine_bamboo.svg';

// =====================
// Winds
// =====================
import eastWind from "./east_wind.svg";
import southWind from "./south_wind.svg";
import westWind from "./west_wind.svg";
import northWind from "./north_wind.svg";

// =====================
// Dragons
// =====================
import greenDragon from './green_dragon.svg';
import redDragon from './red_dragon.svg';
import whiteDragon from './white_dragon.svg';

// =====================
// Flowers
// =====================
import plumFlower from "./plum_flower.svg";
import chrysanthemumFlower from "./chrysanthemum_flower.svg";
import orchidFlower from "./orchid_flower.svg";
import bambooFlower from "./bamboo_flower.svg";

// =====================
// Seasons
// =====================
import springSeason from "./spring_season.svg";
import summerSeason from "./summer_season.svg";
import autumnSeason from "./autumn_season.svg";
import winterSeason from "./winter_season.svg";

// =====================
// Tile front
// =====================
import tileFront from './tile_front.svg';

export const MahjongTileImages = {
    CHARACTERS: { ONE: oneCharacters, TWO: twoCharacters, THREE: threeCharacters, FOUR: fourCharacters, FIVE: fiveCharacters, SIX: sixCharacters, SEVEN: sevenCharacters, EIGHT: eightCharacters, NINE: nineCharacters },
    CIRCLES: { ONE: oneCircles, TWO: twoCircles, THREE: threeCircles, FOUR: fourCircles, FIVE: fiveCircles, SIX: sixCircles, SEVEN: sevenCircles, EIGHT: eightCircles, NINE: nineCircles },
    BAMBOO: { ONE: oneBamboo, TWO: twoBamboo, THREE: threeBamboo, FOUR: fourBamboo, FIVE: fiveBamboo, SIX: sixBamboo, SEVEN: sevenBamboo, EIGHT: eightBamboo, NINE: nineBamboo },
    DRAGON: { RED: redDragon, GREEN: greenDragon, WHITE: whiteDragon },
    WIND: { EAST: eastWind, SOUTH: southWind, WEST: westWind, NORTH: northWind },
    FLOWER: { PLUM: plumFlower, CHRYSANTHEMUM: chrysanthemumFlower, ORCHID: orchidFlower, BAMBOO: bambooFlower },
    SEASON: { SPRING: springSeason, SUMMER: summerSeason, AUTUMN: autumnSeason, WINTER: winterSeason },
} as const;

export const TILE_FRONT: string = tileFront;
