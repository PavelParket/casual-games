import { useState, useCallback, useEffect } from "react";
import Cropper, { type Area } from "react-easy-crop";
import { Box, Button, Icon, Input, Modal, Stack, Typography, useThemedIcon } from "../../../ui";
import { processAvatarImages } from "../../../utils/CropUtils";

interface AvatarEditorModalProps {
    isOpen: boolean;
    imageSrc: string | null;
    onClose: () => void;
    onUpload: (files: { full: File; mini: File }) => Promise<void>;
    isLoading: boolean;
}

export function AvatarEditorModal({ isOpen, imageSrc, onClose, onUpload, isLoading }: AvatarEditorModalProps) {
    const [crop, setCrop] = useState({ x: 0, y: 0 });
    const [zoom, setZoom] = useState(1);
    const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null);
    const [localError, setLocalError] = useState<string | null>(null);

    const { getIcon } = useThemedIcon();

    useEffect(() => {
        if (!isOpen) {
            setLocalError(null);
        }
    }, [isOpen]);

    const onCropComplete = useCallback((_croppedArea: Area, croppedAreaPixels: Area) => {
        setCroppedAreaPixels(croppedAreaPixels);
    }, []);

    const handleSave = async () => {
        if (!imageSrc || !croppedAreaPixels) {
            setLocalError("Please select a valid image area.");
            return;
        }

        setLocalError(null);

        try {
            const croppedFiles = await processAvatarImages(imageSrc, croppedAreaPixels);
            await onUpload(croppedFiles);
        } catch (e) {
            console.error("Cropping failed", e);
            setLocalError("Failed to process image locally. Please try another file.");
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} disableOutsideClick title="Creating a Miniature">
            <Stack gap="1.5rem">
                {imageSrc ? (
                    <Box style={{ position: "relative", width: "100%", height: "300px", background: "#333", borderRadius: "var(--radius-md)", overflow: "hidden" }}>
                        <Cropper
                            image={imageSrc}
                            crop={crop}
                            zoom={zoom}
                            zoomSpeed={0.1}
                            aspect={1}
                            cropShape="round"
                            showGrid={false}
                            onCropChange={setCrop}
                            onCropComplete={onCropComplete}
                            onZoomChange={setZoom}
                        />
                    </Box>
                ) : (
                    <Typography variant="body">No image selected</Typography>
                )}

                {imageSrc && (
                    <Stack gap="0.5rem">
                        <Stack direction="row" justify="space-between">
                            <Typography variant="caption" style={{ opacity: 0.7 }}>Zoom</Typography>
                            <Typography variant="caption" style={{ opacity: 0.7 }}>{Math.round(zoom * 100)}%</Typography>
                        </Stack>
                        <Stack direction="row" gap="0.75rem" align="center">
                            <Button
                                variant="outline"
                                style={{ padding: "0.2rem 0 0 0", minWidth: "32px", height: "32px" }}
                                disabled={zoom <= 1}
                                onClick={() => setZoom(prev => Math.max(1, Number((prev - 0.01).toFixed(2))))}
                            >
                                <Icon src={getIcon("minus")} alt="minus" size={20} />
                            </Button>

                            <Input
                                type="range"
                                min={1}
                                max={3}
                                step={0.01}
                                value={zoom}
                                onChange={(e) => setZoom(Number(e.target.value))}
                                style={{
                                    width: "100%",
                                    cursor: "pointer",
                                    accentColor: "var(--color-primary)",
                                    padding: 0,
                                    border: "none",
                                    background: "transparent",
                                    boxShadow: "none"
                                }}
                            />

                            <Button
                                variant="outline"
                                style={{ padding: "0.2rem 0 0 0", minWidth: "32px", height: "32px" }}
                                disabled={zoom >= 3}
                                onClick={() => setZoom(prev => Math.min(3, Number((prev + 0.01).toFixed(2))))}
                            >
                                <Icon src={getIcon("add")} alt="add" size={20} />
                            </Button>
                        </Stack>
                    </Stack>
                )}

                {localError && (
                    <Typography variant="caption" style={{ color: "var(--color-expense-text)", textAlign: "center" }}>
                        {localError}
                    </Typography>
                )}

                <Stack direction="row" gap="1rem" justify="space-between" align="center">
                    <Typography variant="caption" style={{ opacity: 0.7 }}>Scroll to zoom, drag to move</Typography>
                    <Stack direction="row" gap="0.5rem">
                        <Button variant="outline" onClick={onClose} disabled={isLoading}>
                            Cancel
                        </Button>
                        <Button variant="solid" onClick={handleSave} disabled={isLoading || !imageSrc}>
                            {isLoading ? "Saving..." : "Save"}
                        </Button>
                    </Stack>
                </Stack>
            </Stack>
        </Modal>
    );
}
