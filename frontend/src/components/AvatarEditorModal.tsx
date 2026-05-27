import { useState, useCallback } from "react";
import Cropper, { type Area } from "react-easy-crop";
import { Box, Button, Modal, Stack, Typography } from "../ui";
import { getCroppedImg } from "../utils/CropUtils";

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

    const onCropComplete = useCallback((_croppedArea: Area, croppedAreaPixels: Area) => {
        setCroppedAreaPixels(croppedAreaPixels);
    }, []);

    const handleSave = async () => {
        if (!imageSrc || !croppedAreaPixels) return;
        try {
            const croppedFiles = await getCroppedImg(imageSrc, croppedAreaPixels);
            await onUpload(croppedFiles);
        } catch (e) {
            console.error("Cropping failed", e);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Edit Profile Picture">
            <Stack gap="1.5rem">
                {imageSrc ? (
                    <Box style={{ position: "relative", width: "100%", height: "300px", background: "#333", borderRadius: "var(--radius-md)", overflow: "hidden" }}>
                        <Cropper
                            image={imageSrc}
                            crop={crop}
                            zoom={zoom}
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
                        <input
                            type="range"
                            min={1}
                            max={3}
                            step={0.01}
                            value={zoom}
                            onChange={(e) => setZoom(Number(e.target.value))}
                            style={{
                                width: "100%",
                                cursor: "pointer",
                                accentColor: "var(--color-primary)"
                            }}
                        />
                    </Stack>
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
