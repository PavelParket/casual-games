package casualgames.userservice.service.file;

import casualgames.userservice.config.AttachmentsProperties;
import casualgames.userservice.domain.entity.User;
import casualgames.userservice.domain.enums.AttachmentType;
import casualgames.userservice.validator.AttachmentTypeValidator;
import com.common_utils.exception.BadRequestException;
import com.file_management_starter.exception.S3OperationException;
import com.file_management_starter.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageFileService {

    private static final String PROTOCOL_SEPARATOR = "//";
    private static final int PROTOCOL_SEPARATOR_LENGTH = PROTOCOL_SEPARATOR.length();
    private static final char URL_SLASH = '/';
    private static final int NEXT_CHAR_OFFSET = 1;
    private static final int ONE_INT = 1;
    private static final int MINUS_ONE_INT = -1;

    private final S3Service s3Service;

    private final AttachmentsProperties attachmentsProperties;

    private final AttachmentTypeValidator attachmentTypeValidator;

    private final ImageFileHelper imageFileHelper;

    public User upload(User user, MultipartFile fullFile, MultipartFile miniFile) {
        AttachmentsProperties.AttachmentProperties props = attachmentsProperties.getByType().get(AttachmentType.PROFILE_PICTURE);
        AttachmentsProperties.VariantProperties fullProps = props.getVariants().get(ImageFileHelper.VARIANT_FULL);
        AttachmentsProperties.VariantProperties miniProps = props.getVariants().get(ImageFileHelper.VARIANT_MINI);

        imageFileHelper.validateSize(fullFile, fullProps.getMaxFileSize());
        imageFileHelper.validateSize(miniFile, miniProps.getMaxFileSize());

        byte[] fullBytes = readBytes(fullFile);
        byte[] miniBytes = readBytes(miniFile);

        attachmentTypeValidator.validate(fullBytes, props.getAllowedMimeTypes());
        imageFileHelper.validateDimensions(fullBytes, fullProps.getMaxDimensionPx());

        attachmentTypeValidator.validate(miniBytes, props.getAllowedMimeTypes());
        imageFileHelper.validateDimensions(miniBytes, miniProps.getMaxDimensionPx());

        String fullKey = imageFileHelper.buildFullKey(user.getId(), UUID.randomUUID(), props.getFolder());
        String miniKey = imageFileHelper.buildMiniKey(user.getId(), UUID.randomUUID(), props.getFolder());

        s3Service.upload(props.getBucket(), fullKey, fullBytes, ImageFileHelper.CONTENT_TYPE_JPEG, props.getCacheControl(), null);
        s3Service.upload(props.getBucket(), miniKey, miniBytes, ImageFileHelper.CONTENT_TYPE_JPEG, props.getCacheControl(), null);

        user.setLinkProfilePicture(imageFileHelper.buildUrl(props.getPublicBaseUrl(), props.getBucket(), fullKey));
        user.setLinkProfilePictureMini(imageFileHelper.buildUrl(props.getPublicBaseUrl(), props.getBucket(), miniKey));

        return user;
    }

    public void deleteOldImages(String bucket, String oldFullUrl, String oldMiniUrl) {
        if (oldFullUrl == null && oldMiniUrl == null) {
            return;
        }

        List<String> keysToDelete = new ArrayList<>();

        String fullKey = extractKey(oldFullUrl);
        if (fullKey != null) {
            keysToDelete.add(fullKey);
        }

        String miniKey = extractKey(oldMiniUrl);
        if (miniKey != null) {
            keysToDelete.add(miniKey);
        }

        if (keysToDelete.isEmpty()) {
            return;
        }

        try {
            s3Service.deleteAll(bucket, keysToDelete);
        } catch (S3OperationException e) {
            log.error("Failed to delete old profile pictures, keys={}. Orphaned objects remain.", keysToDelete, e);
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file");
        }
    }

    private String extractKey(String url) {
        if (url == null) {
            return null;
        }

        try {
            int protocolEnd = url.indexOf(PROTOCOL_SEPARATOR);

            if (protocolEnd == MINUS_ONE_INT) {
                return null;
            }

            int firstSlash = url.indexOf(URL_SLASH, protocolEnd + PROTOCOL_SEPARATOR_LENGTH);

            if (firstSlash == MINUS_ONE_INT) {
                return null;
            }

            int secondSlash = url.indexOf(URL_SLASH, firstSlash + NEXT_CHAR_OFFSET);

            if (secondSlash == MINUS_ONE_INT) {
                return null;
            }

            String key = url.substring(secondSlash + ONE_INT);

            return key.isEmpty() ? null : key;
        } catch (Exception e) {
            log.warn("Failed to extract S3 key from URL: {}", url);
            return null;
        }
    }
}
