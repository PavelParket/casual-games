import React, { useMemo, useState } from "react";
import { Box, Typography, Stack, Textfield } from "../../../ui";
import type { DeCoderGameHistory } from "../../../models/WsMessage";

interface DeCoderPanelProps {
  history: DeCoderGameHistory[];
}

export const DeCoderPanel = React.memo(({ history }: DeCoderPanelProps) => {
  const [searchQuery, setSearchQuery] = useState("");

  const items = useMemo(() => {
    let filtered = history;
    if (searchQuery) {
      filtered = history.filter((item) =>
        String(item.code).padStart(4, "0").includes(searchQuery),
      );
    }
    return [...filtered].reverse();
  }, [history, searchQuery]);

  return (
    <Box
      style={{
        display: "flex",
        flexDirection: "column",
        height: "100%",
        minHeight: 0,
      }}
    >
      <Stack gap="10px" style={{ marginBottom: "1rem", flexShrink: 0 }}>
        <Textfield
          value={searchQuery}
          onChange={(val) => setSearchQuery(val.replace(/\D/g, "").slice(0, 4))}
          placeholder="Search history (e.g. 1234)"
          style={{ width: "100%" }}
        />
      </Stack>

      <div
        style={{
          flex: 1,
          minHeight: 0,
          overflowY: "auto",
          background: "var(--color-bg-soft)",
          borderRadius: "var(--radius-md)",
          border: "1px solid var(--color-border)",
          padding: "10px",
          boxShadow: "inset 0 2px 4px rgba(0,0,0,0.05)",
          display: "flex",
          flexDirection: "column",
          gap: "8px",
        }}
      >
        {items.length === 0 ? (
          <Typography
            variant="body"
            style={{ textAlign: "center", opacity: 0.5, marginTop: "2rem" }}
          >
            {history.length === 0
              ? "No moves yet. Be the first!"
              : "No matches found"}
          </Typography>
        ) : (
          items.map((item, idx) => (
            <Box
              key={idx}
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                padding: "8px 12px",
                background: "var(--color-bg)",
                borderRadius: "var(--radius-sm)",
                border: "1px solid var(--color-border)",
              }}
            >
              <Typography
                variant="body"
                style={{
                  fontFamily: "monospace",
                  fontSize: "1.2rem",
                  letterSpacing: "2px",
                  fontWeight: "bold",
                }}
              >
                {String(item.code).padStart(4, "0")}
              </Typography>
              <Stack direction="row" gap="1rem">
                <Box
                  style={{ display: "flex", alignItems: "center", gap: "4px" }}
                >
                  <span title="Exact Match" style={{ fontSize: "1.2rem" }}>
                    Exact
                  </span>
                  <Typography
                    variant="body"
                    style={{
                      color: "var(--color-success, #2ecc71)",
                      fontWeight: "bold",
                    }}
                  >
                    {item.exactMatch}
                  </Typography>
                </Box>
                <Box
                  style={{ display: "flex", alignItems: "center", gap: "4px" }}
                >
                  <span title="Partial Match" style={{ fontSize: "1.2rem" }}>
                    Partial
                  </span>
                  <Typography
                    variant="body"
                    style={{
                      color: "var(--color-warning, #f1c40f)",
                      fontWeight: "bold",
                    }}
                  >
                    {item.partialMatch}
                  </Typography>
                </Box>
              </Stack>
            </Box>
          ))
        )}
      </div>
    </Box>
  );
});
