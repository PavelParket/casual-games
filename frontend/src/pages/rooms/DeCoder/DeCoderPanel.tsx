import React, { useMemo, useState } from "react";
import { Box, Typography, Stack, Button, ComboBox, Textfield } from "../../../ui";

export type StateFilter = "ALL" | "UNUSED" | "USED";
export type SearchMode = "SEQUENCE" | "ANY_ORDER" | "MASK";

interface DeCoderPanelProps {
   gridData: Uint8Array;
}
//todo: Решить куда определить этот компонент,
//в общую папку с остальными он не лезет,
//потому что очень специфичный, только для этой игры
export const DeCoderPanel = React.memo(({ gridData }: DeCoderPanelProps) => {
   const [stateFilter, setStateFilter] = useState<StateFilter>("ALL");
   const[searchMode, setSearchMode] = useState<SearchMode>("SEQUENCE");
   const [searchQuery, setSearchQuery] = useState("");

   const getSearchPlaceholder = () => {
      if (searchMode === "SEQUENCE") return "1234";
      if (searchMode === "ANY_ORDER") return "in any order";
      if (searchMode === "MASK") return "1*2*";
      return "Search...";
   };

   const items = useMemo(() => {
      const arr =[];
      const query = searchQuery.trim();

      let maskRegex: RegExp | null = null;
      if (searchMode === "MASK" && query) {
         try {
            const escaped = query.replace(/[.+?^${}()|[\]\\]/g, '\\$&');
            maskRegex = new RegExp(escaped.replace(/\*/g, '\\d'));
         } catch (e) {
            console.error("", e)
            maskRegex = null;
         }
      }

      for (let i = 0; i < 10000; i++) {
         const byteIdx = Math.floor(i / 8);
         const bitIdx = i % 8;
         const isUsed = ((gridData[byteIdx] >> bitIdx) & 1) === 1;

         if (stateFilter === "USED" && !isUsed) continue;
         if (stateFilter === "UNUSED" && isUsed) continue;

         const codeStr = String(i).padStart(4, '0');
         let matchesPattern = true;

         if (query) {
            if (searchMode === "SEQUENCE") {
               matchesPattern = codeStr.includes(query);
            } 
            else if (searchMode === "ANY_ORDER") {
               const codeChars = codeStr.split('');
               for (const char of query) {
                  const idx = codeChars.indexOf(char);
                  if (idx !== -1) {
                     codeChars.splice(idx, 1);
                  } else {
                     matchesPattern = false;
                     break;
                  }
               }
            } 
            else if (searchMode === "MASK") {
               matchesPattern = maskRegex ? maskRegex.test(codeStr) : false;
            }
         }

         if (!matchesPattern) continue;

         arr.push(
            //todo: Для компонента Box не корректно работает прокрутка: валивается из границ
            <div
               key={i}
               style={{
                  height: "28px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  color: isUsed ? "var(--color-primary)" : "var(--color-text)",
                  textDecoration: isUsed ? "line-through" : "none",
                  background: isUsed ? "var(--color-bg-glass)" : "transparent",
                  opacity: isUsed ? 0.4 : 1,
                  fontFamily: "monospace",
                  fontSize: "1.1rem",
                  letterSpacing: "4px",
                  borderRadius: "4px"
               }}
            >
               {codeStr}
            </div>
         );
      }
      return arr;
   },[gridData, stateFilter, searchMode, searchQuery]);

   return (
      <Box style={{ 
         display: "flex", 
         flexDirection: "column", 
         height: "100%", 
         minHeight: 0
      }}>               
         
         <Stack gap="10px" style={{ marginBottom: "1rem", flexShrink: 0 }}>
            <Box style={{ display: "flex", gap: "5px", padding: "4px", borderRadius: "var(--radius-sm)" }}>
               {(["ALL", "UNUSED", "USED"] as StateFilter[]).map(state => (
                  <Button 
                     key={state}
                     variant={stateFilter === state ? "solid" : "ghost"}
                     onClick={() => setStateFilter(state)}
                     style={{ flex: 1, padding: "4px 8px", fontSize: "0.8rem", minHeight: "28px" }}
                  >
                     {state === "UNUSED" ? "Free" : state === "USED" ? "Tried" : "All"}
                  </Button>
               ))}
            </Box>

            <Box style={{ display: "flex", gap: "8px", alignItems: "center" }}>
               <Box style={{ flexShrink: 0, width: "150px" }}>
                  <ComboBox 
                     options={[
                        { value: "SEQUENCE", label: "Sequence" },
                        { value: "ANY_ORDER", label: "Any Digit" },
                        { value: "MASK", label: "Mask (*)" }
                     ]}
                     value={searchMode}
                     onValueChange={(val) => setSearchMode(val as SearchMode)}
                  />
               </Box>
               <Box style={{ width: "90px", flexShrink: 0 }}>
                  <Textfield 
                    value={searchQuery}
                    onChange={(val) => setSearchQuery(val.replace(/[^0-9*]/g, '').slice(0, 4))}
                    placeholder={getSearchPlaceholder()}
                    style={{ width: "125px", flexShrink: 0}}
                />
                </Box>
            </Box>
         </Stack>

         <div style={{
            flex: 1,
            minHeight: 0,
            overflowY: "auto",
            background: "var(--color-bg-soft)", 
            borderRadius: "var(--radius-md)", 
            border: "1px solid var(--color-border)",
            padding: "10px",
            boxShadow: "inset 0 2px 4px rgba(0,0,0,0.05)",
            
            display: "grid",
            gridTemplateColumns: "repeat(auto-fill, minmax(70px, 1fr))", 
            gap: "4px",
            alignContent: "start"
         }}>
            {items.length === 0 ? (
               <Typography variant="body" style={{ gridColumn: "1 / -1", textAlign: "center", opacity: 0.5, marginTop: "2rem" }}>
                  No codes match filters
               </Typography>
            ) : (
               items
            )}
         </div>
      </Box>
   );
});