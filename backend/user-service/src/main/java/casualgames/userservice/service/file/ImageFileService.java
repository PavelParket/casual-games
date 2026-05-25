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

    private final S3Service s3Service;

    private final AttachmentsProperties attachmentsProperties;

    private final AttachmentTypeValidator attachmentTypeValidator;

    private final ImageFileHelper imageFileHelper;

    public User upload(User user, MultipartFile file) {
        AttachmentsProperties.AttachmentProperties props = attachmentsProperties.getByType().get(AttachmentType.PROFILE_PICTURE);
        AttachmentsProperties.VariantProperties fullVariant = props.getVariants().get(ImageFileHelper.VARIANT_FULL);
        AttachmentsProperties.VariantProperties miniVariant = props.getVariants().get(ImageFileHelper.VARIANT_MINI);

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Failed to read uploaded file");
        }

        attachmentTypeValidator.validate(content, props.getAllowedMimeTypes());
        imageFileHelper.validateDimensions(content, props.getMaxDimensionPx());

        byte[] fullJpeg = imageFileHelper.resizeToJpeg(content, fullVariant.getSize(), fullVariant.getQuality());
        byte[] miniJpeg = imageFileHelper.resizeToJpeg(content, miniVariant.getSize(), miniVariant.getQuality());

        UUID pictureUuid = UUID.randomUUID();
        String fullKey = imageFileHelper.buildFullKey(user.getId(), pictureUuid, props.getFolder());
        String miniKey = imageFileHelper.buildMiniKey(user.getId(), pictureUuid, props.getFolder());

        s3Service.upload(props.getBucket(), fullKey, fullJpeg, ImageFileHelper.CONTENT_TYPE_JPEG, props.getCacheControl(), null);
        s3Service.upload(props.getBucket(), miniKey, miniJpeg, ImageFileHelper.CONTENT_TYPE_JPEG, props.getCacheControl(), null);

        String fullUrl = imageFileHelper.buildUrl(props.getPublicBaseUrl(), props.getBucket(), fullKey);
        String miniUrl = imageFileHelper.buildUrl(props.getPublicBaseUrl(), props.getBucket(), miniKey);

        deleteOldImages(user, props.getBucket());

        user.setLinkProfilePicture(fullUrl);
        user.setLinkProfilePictureMini(miniUrl);

        return user;
    }

    private void deleteOldImages(User user, String bucket) {
        String oldFull = user.getLinkProfilePicture();
        String oldMini = user.getLinkProfilePictureMini();

        if (oldFull == null && oldMini == null) {
            return;
        }

        List<String> keysToDelete = new java.util.ArrayList<>();

        if (oldFull != null) {
            keysToDelete.add(extractKey(oldFull));
        }

        if (oldMini != null) {
            keysToDelete.add(extractKey(oldMini));
        }

        try {
            s3Service.deleteAll(bucket, keysToDelete);
        } catch (S3OperationException e) {
            log.error("Failed to delete old profile pictures for user id={}, keys={}. Orphaned objects remain.", user.getId(), keysToDelete, e);
        }
    }

    public User delete(User user) {
        AttachmentsProperties.AttachmentProperties props = attachmentsProperties.getByType().get(AttachmentType.PROFILE_PICTURE);

        deleteOldImages(user, props.getBucket());

        user.setLinkProfilePicture(null);
        user.setLinkProfilePictureMini(null);

        return user;
    }

    private String extractKey(String url) {
        // URL format: {publicBaseUrl}/{bucket}/{key}
        // key starts after the third slash segment
        try {
            int bucketSlash = url.indexOf(URL_SLASH, url.indexOf(PROTOCOL_SEPARATOR) + PROTOCOL_SEPARATOR_LENGTH);
            return url.substring(url.indexOf(URL_SLASH, bucketSlash + NEXT_CHAR_OFFSET) + NEXT_CHAR_OFFSET);
        } catch (Exception e) {
            log.warn("Failed to extract S3 key from URL: {}", url);
            return url;
        }
    }
}
