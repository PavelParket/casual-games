import React, { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import type { AppDispatch, RootState } from "../../../store/store";
import { useWebSocket } from "../../../hooks/useWebSocket";
import { getBalance } from "../../../store/slices/UserSlice";
import { RoomAPI } from "../../../api/WsHubApi";

import { Box, Button, Card, Container, Typography, Toast, Stack, Divider, Grid, CooldownTimer, Modal } from "../../../ui";

import { validateRoomName, validateWSMessage, validateToastMessage } from "../../../utils/SecurityUtils";
import type { DeCoderMessage } from "../../../models/WsMessage";
import { DeCoderPanel } from "./DeCoderPanel";


export default function DeCoderRoom() {
   const dispatch = useDispatch<AppDispatch>();
   const navigate = useNavigate();

   const { user } = useSelector((state: RootState) => state.auth);
   const guid = user?.guid;

   const { roomName: rawRoomName, roomId } = useParams<{ roomName?: string, roomId?: string }>();
   const roomName = validateRoomName(rawRoomName ?? "");

   const [toast, setToast] = useState<{ text: string, type: 'success' | 'error' | 'info' } | null>(null);
   const [players, setPlayers] = useState<Record<string, string>>({});
   const playersRef = useRef(players);
   
   useEffect(() => { playersRef.current = players; }, [players]);

   const[gameActive, setGameActive] = useState<boolean>(false);
   const [gridData, setGridData] = useState<Uint8Array>(new Uint8Array(1250));
   const [gameOverModal, setGameOverModal] = useState<{ isOpen: boolean; isWin: boolean; winnerName?: string } | null>(null);

   const[digits, setDigits] = useState<string[]>(['', '', '', '']);
   const inputRefs = useRef<(HTMLInputElement | null)[]>([]);
   const [cooldown, setCooldown] = useState(0);

   const { isConnected, message, send } = useWebSocket<DeCoderMessage>(roomId, "DE_CODER");

   useEffect(() => {
      if (!roomName || !roomId) navigate("/rooms");
   },[roomName, roomId, navigate]);

   const fetchPlayers = useCallback(async () => {
      if (!roomId) return;
      try {
         const res = await RoomAPI.getUsernamesInRoom(roomId, "DE_CODER");
         setPlayers(res.data);
      } catch (e) {
         console.error("Failed to fetch players", e);
      }
   }, [roomId]);

   useEffect(() => { fetchPlayers(); }, [fetchPlayers]);

   useEffect(() => {
      if (cooldown <= 0) return;
      const timerId = setInterval(() => setCooldown(c => c - 1), 1000);
      return () => clearInterval(timerId);
   }, [cooldown]);

   const refreshUserBalance = useCallback(() => {
      if (guid) dispatch(getBalance(guid));
   },[dispatch, guid]);

   const showToast = (text: string, type: 'success' | 'error' | 'info' = 'info') => {
      setToast({ text: validateToastMessage(text), type });
   };

      const requestSync = useCallback(() => {
      if (isConnected) {
         send({ type: "SYSTEM", event: "STATE", roomId: roomId! });
      }
   }, [isConnected, send, roomId]);

   useEffect(() => {
      if (!isConnected) return;

      const syncInterval = setInterval(() => {
         console.debug("Auto-syncing game state...");
         requestSync();
      }, 120000);

      return () => clearInterval(syncInterval);
   },[isConnected, requestSync]);


   useEffect(() => {
      if (!isConnected || !message) return;

      const sanitized = validateWSMessage(message,[
         "type", "event", "message", "code", "player", "winner", "gameState", "isGameStarted"
      ]) as DeCoderMessage;

      switch (sanitized.event) {
         case "STATE":
            if (sanitized.gameState) {
               const binString = atob(sanitized.gameState);
               const bytes = new Uint8Array(1250);
               for (let i = 0; i < binString.length; i++) {
                  bytes[i] = binString.charCodeAt(i);
               }
               setGridData(bytes);
            } else {
               setGridData(new Uint8Array(1250));
            }
            
            if (sanitized.isGameStarted !== undefined) {
               setGameActive(sanitized.isGameStarted);
            }
            break;

         case "START":
            setGameActive(true);
            setGridData(new Uint8Array(1250));
            setDigits(['', '', '', '']);
            setCooldown(0);
            setGameOverModal(null);
            showToast("Game started! System generated a new code.", 'success');
            break;

         case "MOVE":
            if (sanitized.code !== undefined) {
               setGridData(prev => {
                  const newBytes = new Uint8Array(prev);
                  const byteIdx = Math.floor(sanitized.code! / 8);
                  const bitIdx = sanitized.code! % 8;
                  newBytes[byteIdx] |= (1 << bitIdx);
                  return newBytes;
               });

               const playerName = playersRef.current[sanitized.player!] || "Someone";
               
               if (sanitized.player !== guid) {
                   showToast(`${playerName} checked ${String(sanitized.code).padStart(4, '0')}`, 'info');
               } else {showToast(`${String(sanitized.code).padStart(4, '0')} does not match the winning code`, 'info');}

               if (sanitized.player === guid) refreshUserBalance();
            }
            break;

         case "WINNER": {
            console.log("11::",sanitized)
            setGameActive(false);
            const winnerName = playersRef.current[sanitized.winner!] || "Unknown Player";
            const isMe = sanitized.winner === guid;
            
            setGameOverModal({
               isOpen: true,
               isWin: isMe,
               winnerName: isMe ? "You" : winnerName
            });

            refreshUserBalance();
            break;
         }

         case "ERROR": {
            const msg = sanitized.message || "Error occurred";
                        
            if (msg.includes("already tried") || msg.includes("already checked")) {
               showToast("This code already tried", 'info');
               requestSync();
            } 
            else if (msg.includes("not started") || msg.includes("Game not found")) {
               setGameActive(false);
               showToast("Game session expired or not started.", 'error');
            }
            else if (msg.includes("already in progress")) {
               setGameActive(true);
               requestSync();
            }
            else if (msg.includes("Insufficient funds") || msg.includes("Transaction")) {
               showToast("Transaction failed: Insufficient funds!", 'error');
            }
            else {
               showToast(msg, 'error');
            }
            break;
         }

         case "JOIN":
         case "LEAVE":
            //showToast(sanitized.message || "", 'info');
            fetchPlayers();
            break;
      }
   }, [isConnected, message, guid, refreshUserBalance, fetchPlayers, requestSync]);

   const handleDigitChange = (index: number, val: string) => {
      const digit = val.replace(/\D/g, '').slice(-1);
      const newDigits =[...digits];
      newDigits[index] = digit;
      setDigits(newDigits);

      if (digit !== '' && index < 3) {
         inputRefs.current[index + 1]?.focus();
      }
   };

   const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Backspace' && digits[index] === '' && index > 0) {
         inputRefs.current[index - 1]?.focus();
      }
   };

   const handleStartGame = () => {
      if (!isConnected) return;
      send({ type: "SYSTEM", event: "START", roomId: roomId! });
   };

   const handleSendMove = () => {
      const codeStr = digits.join('');
      if (codeStr.length !== 4) return;
      if (cooldown > 0) return;

      send({ type: "SYSTEM", event: "MOVE", roomId: roomId!, code: parseInt(codeStr, 10) });
      setCooldown(5);
      setDigits(['', '', '', '']);
      inputRefs.current[0]?.focus();
   };


   return (
      <Box style={{
         minHeight: "calc(100vh - 60px - 50px)",
         margin: "0 10rem",
         padding: "0 1rem",
         background: "var(--color-bg-glass)",
         backdropFilter: "blur(2px)",
         borderRadius: "var(--radius-md)",
         boxShadow: "var(--shadow-lg)"
      }}>
         <Container maxWidth="1100px" style={{ display: "flex", flexDirection: "column", flex: 1, minHeight: 0 }}>
            
            <Box style={{ padding: "2rem 0" }}>
               <Typography variant="h2" style={{ textAlign: "center" }}>
                  {roomName}
               </Typography>
            </Box>

            <Card style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0, padding: "1.5rem" }}>
               <Grid columns="250px 1fr 300px" gap="2rem" style={{ height: "65vh", minHeight: "500px", alignItems: "stretch" }}>
                  
                  <Box style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0, borderRight: "1px solid var(--color-border)", paddingRight: "1rem" }}>
                     <Typography variant="h3" style={{ textAlign: "center", marginBottom: "1rem" }}>Players</Typography>
                     
                     <Box style={{ flex: 1, overflowY: "auto", minHeight: 0, display: "flex", flexDirection: "column", gap: "8px" }}>
                        {Object.entries(players).map(([id, name]) => (
                           <Typography 
                              key={id} 
                              variant="body" 
                              style={{ 
                                 fontWeight: id === guid ? "bold" : "normal",
                                 color: id === guid ? "var(--color-primary)" : "var(--color-text)",
                                 padding: "8px",
                                 background: id === guid ? "var(--color-bg-soft)" : "transparent",
                                 borderRadius: "var(--radius-sm)"
                              }}
                           >
                              {name} {id === guid && "(You)"}
                           </Typography>
                        ))}
                     </Box>

                     <Divider style={{ margin: "1rem 0" }} />
                     <Button variant="outline" onClick={() => navigate("/rooms")} style={{ width: "100%" }}>
                        Leave
                     </Button>
                  </Box>

                  <Box style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", minHeight: 0 }}>
                     {!gameActive ? (
                        <Box style={{ textAlign: "center" }}>
                           <Typography variant="h2" style={{ marginBottom: "1.5rem" }}>Game Not Started</Typography>
                           <Button variant="solid" onClick={handleStartGame} style={{ padding: "1rem 3rem", fontSize: "1.2rem" }}>
                              Start game
                           </Button>
                        </Box>
                     ) : (
                        <Stack align="center" gap="3rem">
                           
                           <Box style={{ display: "flex", gap: "15px" }}>
                              {digits.map((digit, index) => (
                                 <input
                                    key={index}
                                    ref={(el) => { inputRefs.current[index] = el; }} 
                                    type="text"
                                    inputMode="numeric"
                                    placeholder="0"
                                    value={digit}
                                    onChange={(e) => handleDigitChange(index, e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(index, e)}
                                    style={{
                                       width: "70px", height: "90px",
                                       fontSize: "3rem", textAlign: "center",
                                       borderRadius: "var(--radius-md)",
                                       border: "2px solid var(--color-border)",
                                       background: "var(--glass-surface)",
                                       color: "var(--color-text)",
                                       outline: "none",
                                       boxShadow: "var(--shadow-sm)",
                                       transition: "border-color 0.2s",
                                       caretColor: "transparent",
                                    }}
                                    onFocus={(e) => e.target.style.borderColor = "var(--color-primary)"}
                                    onBlur={(e) => e.target.style.borderColor = "var(--color-border)"}
                                 />
                              ))}
                           </Box>

                           <Button 
                                variant="solid" 
                                onClick={handleSendMove} 
                                disabled={cooldown > 0 || digits.join('').length !== 4}
                                style={{ 
                                    display: "inline-flex", alignItems: "center", justifyContent: "center", gap: "12px", 
                                    borderRadius: "40px", padding: "8px 16px 8px 24px", fontSize: "1.1rem", height: "56px", whiteSpace: "nowrap"
                                }}
                            >
                                <Typography variant="caption" style={{ fontWeight: "bold", fontSize: "inherit", color: "inherit" }}>Send</Typography>
                                <Typography variant="caption" style={{ opacity: 0.8, fontSize: "0.85rem", minWidth: "55px", color: "inherit" }}>10 CG Coins</Typography>
                                <Box style={{ width: "1px", height: "24px", background: "currentColor", opacity: 0.3, margin: "0 4px" }} />
                                <CooldownTimer timeLeft={cooldown} maxTime={5} />
                            </Button>
                        </Stack>
                     )}
                  </Box>

                  <Box style={{ display: "flex", flexDirection: "column", height: "100%", minHeight: 0, borderLeft: "1px solid var(--color-border)", paddingLeft: "1rem", overflow: "hidden" }}>
                     <Typography variant="h3" style={{ textAlign: "center", marginBottom: "1rem" }}>Code Terminal</Typography>
                     <Box style={{ flex: 1, minHeight: 0 }}>
                        <DeCoderPanel gridData={gridData} />
                     </Box>
                  </Box>

               </Grid>
            </Card>
         </Container>

         {gameOverModal && (
            <Modal
               isOpen={gameOverModal.isOpen}
               onClose={() => { }}
               title={gameOverModal.isWin ? "Victory!" : "System Hacked"}
            >
               <Box style={{ textAlign: "center", padding: "1rem 0" }}>
                  <Typography variant="h2" style={{ 
                     color: gameOverModal.isWin ? "var(--color-success)" : "var(--color-text)",
                     marginBottom: "1rem"
                  }}>
                     {gameOverModal.isWin ? "You Cracked the Code!" : `${gameOverModal.winnerName} won!`}
                  </Typography>
                  <Typography variant="body" style={{ marginBottom: "2rem", opacity: 0.8 }}>
                     {gameOverModal.isWin 
                        ? "Congratulations! The reward has been added to your balance."
                        : "Better luck next time. The code has been deciphered."}
                  </Typography>
                  <Button variant="solid" onClick={() => navigate("/rooms")} style={{ width: "100%", padding: "12px" }}>
                     Leave Room
                  </Button>
               </Box>
            </Modal>
         )}

         {toast && (
            <Toast message={toast.text} onClose={() => setToast(null)} />
         )}
      </Box>
   );
}