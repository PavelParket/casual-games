package com.casualgames.userservice.exception;

import com.casualgames.commonutils.dto.ErrorResponse;
import com.casualgames.commonutils.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import static com.casualgames.userservice.config.ResourceMessageConstants.FILES_ARE_MISSING;
import static com.casualgames.userservice.config.ResourceMessageConstants.TOO_LARGE_UPLOADING_FILE;

@RestControllerAdvice
@Slf4j
public class ServiceExceptionHandler {

    @ExceptionHandler(InvalidAttachmentTypeException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ErrorResponse handleInvalidAttachmentType(InvalidAttachmentTypeException ex, HttpServletRequest request) {
        log.warn("Invalid attachment type: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(InvalidImageDimensionsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidDimensions(InvalidImageDimensionsException ex, HttpServletRequest request) {
        log.warn("Invalid image dimensions: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(CorruptedImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCorruptedImage(CorruptedImageException ex, HttpServletRequest request) {
        log.warn("Corrupted image: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ErrorResponse handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Upload size exceeded: {}", ex.getMessage());

        return ErrorResponse.of(ErrorCode.PAYLOAD_TOO_LARGE, TOO_LARGE_UPLOADING_FILE, request.getRequestURI(), null);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpServletRequest request) {
        log.warn("Missing request part: {}", ex.getRequestPartName());

        return ErrorResponse.of(ErrorCode.BAD_REQUEST, FILES_ARE_MISSING, request.getRequestURI(), null);
    }
}
