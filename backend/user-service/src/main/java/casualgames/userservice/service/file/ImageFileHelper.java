package casualgames.userservice.service.file;

import casualgames.userservice.exception.CorruptedImageException;
import casualgames.userservice.exception.InvalidImageDimensionsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageFileHelper {

    public static final String VARIANT_FULL = "full";
    public static final String VARIANT_MINI = "mini";
    public static final String CONTENT_TYPE_JPEG = "image/jpeg";

    private final static String JPG_FORMAT = "jpg";
    private final static String IMAGE_FORMAT_SUFFIX = ".jpg";
    private final static String IMAGE_MINI_FORMAT_SUFFIX = "-mini.jpg";
    private final static String IMAGE_KEY_FORMAT = "%s%s/%s%s";
    private final static String IMAGE_URL_FORMAT = "%s/%s/%s";

    public void validateDimensions(byte[] content, int maxDimensionPx) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                throw new CorruptedImageException("Image cannot be decoded");
            }
            if (image.getWidth() > maxDimensionPx || image.getHeight() > maxDimensionPx) {
                throw new InvalidImageDimensionsException("Image dimensions exceed maximum allowed %dpx".formatted(maxDimensionPx));
            }
        } catch (IOException e) {
            throw new CorruptedImageException("Failed to read image: " + e.getMessage());
        }
    }

    public byte[] resizeToJpeg(byte[] content, int targetSize, double quality) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(content))
                    .crop(Positions.CENTER)
                    .size(targetSize, targetSize)
                    .outputFormat(JPG_FORMAT)
                    .outputQuality(quality)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new CorruptedImageException("Failed to process image: " + e.getMessage());
        }
    }

    public String buildFullKey(Long userId, UUID uuid, String folder) {
        return String.format(IMAGE_KEY_FORMAT, folder, userId, uuid, IMAGE_FORMAT_SUFFIX);
    }

    public String buildMiniKey(Long userId, UUID uuid, String folder) {
        return String.format(IMAGE_KEY_FORMAT, folder, userId, uuid, IMAGE_MINI_FORMAT_SUFFIX);
    }

    public String buildUrl(String publicBaseUrl, String bucket, String key) {
        return String.format(IMAGE_URL_FORMAT, publicBaseUrl, bucket, key);
    }
}
