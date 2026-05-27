import type { Area } from "react-easy-crop";

const createImage = (url: string): Promise<HTMLImageElement> =>
    new Promise((resolve, reject) => {
        const image = new Image();
        image.addEventListener('load', () => resolve(image));
        image.addEventListener('error', (error) => reject(error));
        image.setAttribute('crossOrigin', 'anonymous');
        image.src = url;
    });

export const getCroppedImg = async (
    imageSrc: string,
    pixelCrop: Area
): Promise<{ full: File; mini: File }> => {
    const image = await createImage(imageSrc);
    const createSizedFile = (size: number, fileName: string): Promise<File> => {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        if (!ctx) {
            throw new Error('No 2d context');
        }

        canvas.width = size;
        canvas.height = size;

        ctx.drawImage(
            image,
            pixelCrop.x,
            pixelCrop.y,
            pixelCrop.width,
            pixelCrop.height,
            0,
            0,
            size,
            size
        );

        return new Promise((resolve, reject) => {
            canvas.toBlob((blob) => {
                if (!blob) {
                    reject(new Error('Canvas is empty'));
                    return;
                }
                resolve(new File([blob], fileName, { type: 'image/jpeg' }));
            }, 'image/jpeg', 0.95);
        });
    };
    const full = await createSizedFile(512, 'full.jpg');
    const mini = await createSizedFile(128, 'mini.jpg');

    return { full, mini };
};
