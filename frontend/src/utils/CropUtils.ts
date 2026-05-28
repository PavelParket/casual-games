import type { Area } from "react-easy-crop";

const MAX_FULL_DIMENSION = 1024;
const MAX_MINI_DIMENSION = 256;

const createImage = (url: string): Promise<HTMLImageElement> =>
    new Promise((resolve, reject) => {
        const image = new Image();
        image.addEventListener('load', () => resolve(image));
        image.addEventListener('error', (error) => reject(error));
        image.setAttribute('crossOrigin', 'anonymous');
        image.src = url;
    });

export const processAvatarImages = async (
    imageSrc: string,
    pixelCrop: Area
): Promise<{ full: File; mini: File }> => {
    const image = await createImage(imageSrc);

    const fullCanvas = document.createElement('canvas');
    let { width, height } = image;

    if (width > MAX_FULL_DIMENSION || height > MAX_FULL_DIMENSION) {
        const ratio = Math.min(MAX_FULL_DIMENSION / width, MAX_FULL_DIMENSION / height);
        width = Math.round(width * ratio);
        height = Math.round(height * ratio);
    }

    fullCanvas.width = width;
    fullCanvas.height = height;
    const fullCtx = fullCanvas.getContext('2d');
    if (!fullCtx) throw new Error('No 2d context for full image');

    fullCtx.drawImage(image, 0, 0, width, height);

    const fullFile = await new Promise<File>((resolve, reject) => {
        fullCanvas.toBlob((blob) => {
            if (!blob) return reject(new Error('Canvas is empty'));
            resolve(new File([blob], 'full.jpg', { type: 'image/jpeg' }));
        }, 'image/jpeg', 0.85);
    });

    const miniCanvas = document.createElement('canvas');
    miniCanvas.width = MAX_MINI_DIMENSION;
    miniCanvas.height = MAX_MINI_DIMENSION;
    const miniCtx = miniCanvas.getContext('2d');
    if (!miniCtx) throw new Error('No 2d context for mini image');

    miniCtx.drawImage(
        image,
        pixelCrop.x,
        pixelCrop.y,
        pixelCrop.width,
        pixelCrop.height,
        0,
        0,
        MAX_MINI_DIMENSION,
        MAX_MINI_DIMENSION
    );

    const miniFile = await new Promise<File>((resolve, reject) => {
        miniCanvas.toBlob((blob) => {
            if (!blob) return reject(new Error('Canvas is empty'));
            resolve(new File([blob], 'mini.jpg', { type: 'image/jpeg' }));
        }, 'image/jpeg', 0.85);
    });

    return { full: fullFile, mini: miniFile };
};
