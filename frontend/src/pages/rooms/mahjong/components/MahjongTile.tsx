import { motion } from "framer-motion";
import type { TileFace } from "../utils/MahjongTypes";
import { getTileAsset } from "../utils/MahjongGameUtils";
import { TILE_FRONT } from '../../../../assets/icons/mahjong-tiles-svg/index';

interface MahjongTileProps {
    face: TileFace;
    isFree: boolean;
    isSelected: boolean;
    onClick?: () => void;
}

export function MahjongTile({ face, isFree, isSelected, onClick }: MahjongTileProps) {
    const symbolSrc = getTileAsset(face);

    return (
        <motion.div
            className={`mahjong-tile ${isFree ? 'is-free' : 'is-blocked'} ${isSelected ? 'is-selected' : ''}`}
            onClick={isFree ? onClick : undefined}
            initial={false}
            animate={{ 
                x: isSelected ? 2 : 0, 
                y: isSelected ? -6 : 0,
                transition: { type: "spring", stiffness: 400, damping: 25 }
            }}
            whileHover={isFree && !isSelected ? { y: -6, x: 2 } : {}}
        >
            <div className="mahjong-tile-face">
                <img src={TILE_FRONT} className="mahjong-tile-bg" alt="tile base" draggable={false} />
                {symbolSrc && (
                    <img src={symbolSrc} className="mahjong-tile-symbol" alt="tile symbol" draggable={false} />
                )}
            </div>
        </motion.div>
    );
}
