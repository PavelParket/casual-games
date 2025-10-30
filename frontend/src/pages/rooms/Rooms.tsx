import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import type { RootState } from "../../store/store";
import { useState } from "react";
import { Box, Button, Card, Container, Modal, Textfield, Typography } from "../../ui";

export default function Rooms() {
   const navigate = useNavigate();
   const { isAuthenticated } = useSelector((state: RootState) => state.auth);
   const [createModalOpen, setCreateModalOpen] = useState(false);
   const [newRoomName, setNewRoomName] = useState("");
   //const { getInverseIcon } = useThemedIcon();

   const handleJoinRoom = (roomName: string) => {
      if (!isAuthenticated) {
         navigate("/login");
         return;
      }
      navigate(`/room/game/${roomName}`);
   };

   const handleCreateRoom = () => {
      if (!newRoomName.trim()) {
         alert("Please enter a room name");
         return;
      }

      if (!isAuthenticated) {
         navigate("/login");
         return;
      }

      setCreateModalOpen(false);
      const roomName = newRoomName.trim();
      setNewRoomName("");
      navigate(`/room/game/${roomName}`);
   };

   const demoRooms = ["Room-1", "Room-2", "Room-3"];

   return (
      <>
         <Box style={{
            minHeight: "calc(100vh - 60px - 50px)",
            margin: "0 10rem",
            padding: "2rem 1rem",
            background: "var(--color-bg-glass)",
            backdropFilter: "blur(2px)",
            borderRadius: "var(--radius-md)",
            boxShadow: "var(--shadow-lg)"
         }}>
            <Container>
               <Box style={{
                  marginBottom: "3rem",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between"
               }}>
                  <Box>
                     <Typography variant="h2">Game Rooms</Typography>
                     <Typography variant="caption" style={{ opacity: 0.7, marginTop: "0.5rem", display: "block" }}>
                        Join or create a room to play Tic-Tac-Toe
                     </Typography>
                  </Box>

                  <Button
                     variant="solid"
                     onClick={() => setCreateModalOpen(true)}
                     style={{ display: "flex", alignItems: "center", gap: "8px" }}
                  >
                     <Typography variant="body" inverse style={{ fontSize: "16px", fontWeight: 500 }}>
                        Create Room
                     </Typography>
                  </Button>
               </Box>

               <Box style={{
                  display: "grid",
                  gridTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))",
                  gap: "1.5rem",
               }}>
                  {demoRooms.map((room) => (
                     <Card
                        key={room}
                        style={{
                           padding: "1.5rem",
                           display: "flex",
                           flexDirection: "column",
                           gap: "1rem",
                           cursor: "pointer"
                        }}
                     >
                        <Typography variant="h3">{room}</Typography>
                        <Typography variant="caption" style={{ opacity: 0.7 }}>
                           👥 0/2 players
                        </Typography>
                        <Button variant="solid" onClick={() => handleJoinRoom(room)}>
                           Join Room
                        </Button>
                     </Card>
                  ))}
               </Box>
            </Container>
         </Box>

         <Modal isOpen={createModalOpen} onClose={() => setCreateModalOpen(false)} title="Create New Room">
            <Box style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
               <Textfield
                  value={newRoomName}
                  onChange={setNewRoomName}
                  placeholder="Enter room name..."
                  rounded
               />
               <Box style={{ display: "flex", gap: "0.5rem" }}>
                  <Button
                     variant="outline"
                     onClick={() => {
                        setCreateModalOpen(false);
                        setNewRoomName("");
                     }}
                     style={{ flex: 1 }}
                  >
                     Cancel
                  </Button>
                  <Button
                     variant="solid"
                     onClick={handleCreateRoom}
                     disabled={!newRoomName.trim()}
                     style={{ flex: 1 }}
                  >
                     Create
                  </Button>
               </Box>
            </Box>
         </Modal>
      </>
   );
}