import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import type { RootState } from "../../store/store";
import { useEffect, useState } from "react";
import { Box, Button, Card, Container, Icon, Modal, Textfield, Typography, useThemedIcon } from "../../ui";
import { RoomAPI, type GameRoom } from "../../api/RoomApi";

export default function Rooms() {
   const navigate = useNavigate();
   const { isAuthenticated } = useSelector((state: RootState) => state.auth);
   const [createModalOpen, setCreateModalOpen] = useState(false);
   const [newRoomName, setNewRoomName] = useState("");
   const [rooms, setRooms] = useState<GameRoom[]>([]);
   const { getIcon, getInverseIcon } = useThemedIcon();

   useEffect(() => {
      const fetchRooms = async () => {
         try {
            const response = await RoomAPI.getGameRooms();
            setRooms(response.data);
         } catch (err) {
            console.error('Failed to fetch rooms:', err);
         }
      };

      fetchRooms();
   }, []);

   const handleJoinRoom = (roomName: string) => {
      if (!isAuthenticated) {
         return;
      }
      navigate(`/room/game/${roomName}`);
   };

   const handleCreateRoom = () => {
      if (!newRoomName.trim()) {
         return;
      }

      setCreateModalOpen(false);
      setNewRoomName("");
      navigate(`/room/game/${newRoomName}`);
   };

   return (
      <>
         <Box style={{
            minHeight: "calc(100vh - 60px - 50px)",
            margin: "0 10rem",
            padding: "0 1rem",
            background: "var(--color-bg-glass)",
            backdropFilter: "blur(2px)",
            borderRadius: "var(--radius-md)",
            boxShadow: "var(--shadow-lg)"
         }}>
            <Container>
               <Box style={{
                  padding: "2rem 1rem 0 1rem",
                  marginBottom: "2rem",
                  display: "flex",
                  flexDirection: "row",
                  alignItems: "center",
                  justifyContent: "space-between"
               }}>
                  <Typography variant="h2" style={{ textAlign: "center" }}>
                     Rooms
                  </Typography>

                  <Button
                     variant="solid"
                     onClick={() => setCreateModalOpen(true)}
                     style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "8px"
                     }}
                  >
                     <Icon src={getInverseIcon("add")} alt="add" size={17} />
                     <Box style={{ textAlign: "center" }}>
                        <Typography variant="body" inverse style={{ fontSize: "16px", fontWeight: 500 }}>
                           Create Room
                        </Typography>
                     </Box>
                  </Button>
               </Box>

               <Box style={{ textAlign: "center" }}>
                  {rooms.length === 0 && (
                     <Typography>No rooms available. Try to create something!</Typography>
                  )}
               </Box>

               <Box style={{
                  paddingBottom: "1rem",
                  display: "grid",
                  gridTemplateColumns: "repeat(4, 1fr)",
                  columnGap: "16px",
                  rowGap: "3rem",
                  justifyItems: "center",
               }}>
                  {rooms.map((room) => (
                     <>
                        <Card
                           key={room.id}
                           style={{
                              width: "180px",
                              height: "180px",
                              textAlign: "center",
                              padding: "20px",
                              display: "flex",
                              flexDirection: "column",
                              gap: "10px",
                           }}
                        >
                           <Typography variant="body">{room.name}</Typography>
                           <Button variant="outline" onClick={() => handleJoinRoom(room.name)}>Join</Button>
                           {/* <Button variant="ghost" onClick={() => handleInfo(room)}>Info</Button> */}
                        </Card>

                        {rooms.length > 0 && (
                           <Card
                              onClick={() => setCreateModalOpen(true)}
                              style={{
                                 width: "180px",
                                 height: "180px",
                                 textAlign: "center",
                                 padding: "20px",
                                 display: "flex",
                                 alignItems: "center",
                                 justifyContent: "center",
                                 cursor: "pointer",
                              }}
                           >
                              <Icon src={getIcon("add")} alt="add" size={50} />
                           </Card>
                        )}
                     </>
                  ))}
               </Box>
            </Container>
         </Box>

         {/* <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title="Room Info">
            <Typography>{modalContent}</Typography>
         </Modal> */}

         <Modal isOpen={createModalOpen} onClose={() => setCreateModalOpen(false)} title="Create Room">
            <Box style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
               <Textfield value={newRoomName} onChange={setNewRoomName} />
               <Button variant="solid" onClick={handleCreateRoom}>Create</Button>
            </Box>
         </Modal>
      </>
   );
}