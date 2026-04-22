package casualgames.userservice.exception;

import com.common_utils.dto.ErrorResponse;
import com.common_utils.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static casualgames.userservice.config.ResourceMessageConstants.FILES_ARE_MISSING;
import static casualgames.userservice.config.ResourceMessageConstants.TOO_LARGE_UPLOADING_FILE;

@RestControllerAdvice
@Slf4j
public class ServiceExceptionHandler {

    @ExceptionHandler(InvalidAttachmentTypeException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse handleInvalidAttachmentType(InvalidAttachmentTypeException ex, HttpServletRequest request) {
        log.warn("Invalid attachment type: {}", ex.getMessage());
        return ErrorResponse.of(
                ErrorCode.BAD_REQUEST,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidImageDimensionsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidDimensions(InvalidImageDimensionsException ex, HttpServletRequest request) {
        log.warn("Invalid image dimensions: {}", ex.getMessage());
        return ErrorResponse.of(
                ErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(CorruptedImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCorruptedImage(CorruptedImageException ex, HttpServletRequest request) {
        log.warn("Corrupted image: {}", ex.getMessage());
        return ErrorResponse.of(
                ErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Upload size exceeded: {}", ex.getMessage());
        return ErrorResponse.of(
                ErrorCode.BAD_REQUEST,
                HttpStatus.PAYLOAD_TOO_LARGE,
                TOO_LARGE_UPLOADING_FILE,
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpServletRequest request) {
        log.warn("Missing request part: {}", ex.getRequestPartName());
        return ErrorResponse.of(
                ErrorCode.BAD_REQUEST,
                HttpStatus.BAD_REQUEST,
                FILES_ARE_MISSING,
                null,
                request.getRequestURI()
        );
    }
}
