package casualgames.userservice.validator;

import casualgames.userservice.exception.InvalidAttachmentTypeException;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.util.List;

import static casualgames.userservice.config.ResourceMessageConstants.INVALID_FILE_TYPE;
import static casualgames.userservice.config.ResourceMessageConstants.UNSUPPORTED_FILE_TYPE;

@Component
@RequiredArgsConstructor
public class AttachmentTypeValidator {

    private final Tika tika = new Tika();

    public String validate(byte[] content, List<String> allowedMimeTypes) {
        String detected;
        try {
            detected = tika.detect(content);
        } catch (Exception e) {
            throw new InvalidAttachmentTypeException(INVALID_FILE_TYPE);
        }

        if (!allowedMimeTypes.contains(detected)) {
            throw new InvalidAttachmentTypeException(String.format(UNSUPPORTED_FILE_TYPE, detected, allowedMimeTypes));
        }

        return detected;
    }
}
