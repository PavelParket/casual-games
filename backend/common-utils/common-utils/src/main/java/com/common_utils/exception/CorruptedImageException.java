package casualgames.userservice.exception;

import com.common_utils.exception.AbstractException;

public class CorruptedImageException extends AbstractException {

    public CorruptedImageException(String message) {
        super(message);
    }
}
