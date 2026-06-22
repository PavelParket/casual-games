import { Box, Typography } from "../../../../ui";
import "../styles/TicTacToeRoom.css"

interface TicTacToeBoardProps {
    board: string[];
    isGame: boolean;
    gameAborted: boolean;
    winnerId?: string | null;
    mySymbol?: string;
    currentPlayerSymbol?: string;
    onMove: (index: number) => void;
}

export function TicTacToeBoard({
    board,
    isGame,
    gameAborted,
    winnerId,
    mySymbol,
    currentPlayerSymbol,
    onMove
}: TicTacToeBoardProps) {

    const isMyTurn = isGame && mySymbol === currentPlayerSymbol;

    return (
        <Box style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: "1.5rem"
        }}>
            {isGame && winnerId === undefined && !gameAborted ? (
                <Typography variant="h3" style={{ fontWeight: 700, height: "30px", display: "flex", alignItems: "center", gap: "6px" }}>
                    Turn: {currentPlayerSymbol}
                    {isMyTurn && (<span>(You)</span>)}
                </Typography>
            ) : (
                <Box style={{ height: "30px" }} />
            )}

            <Box style={{
                display: "grid",
                gridTemplateColumns: "repeat(3, 80px)",
                gridTemplateRows: "repeat(3, 80px)",
                justifyContent: "center",
                borderRadius: "var(--radius-lg)",
                overflow: "hidden",
                boxShadow: "var(--shadow-lg)",
            }}>
                {board.map((cell, index) => {
                    const canMove = isMyTurn && !cell && winnerId === undefined && !gameAborted;

                    const style: React.CSSProperties = {
                        borderRight: index % 3 !== 2 ? "2px solid var(--color-text)" : "none",
                        borderBottom: index < 6 ? "2px solid var(--color-text)" : "none",
                    };

                    let className = "ttt-cell";
                    if (canMove) {
                        className += " interactive";
                    } else if (!cell) {
                        className += " disabled-empty";
                    }

                    return (
                        <div
                            key={index}
                            className={className}
                            style={style}
                            onClick={() => canMove && onMove(index)}
                        >
                            {cell}
                        </div>
                    );
                })}
            </Box>
        </Box>
    );
}
