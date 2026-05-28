package casualgames.userservice.config;

import casualgames.userservice.domain.enums.AttachmentType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "attachments")
public class AttachmentsProperties {

    private Map<AttachmentType, AttachmentProperties> byType = new EnumMap<>(AttachmentType.class);

    @Data
    public static class AttachmentProperties {
        private String bucket;
        private String folder;
        private String publicBaseUrl;
        private String cacheControl;
        private List<String> allowedMimeTypes;
        private Map<String, VariantProperties> variants = new java.util.LinkedHashMap<>();
    }

    @Data
    public static class VariantProperties {
        private DataSize maxFileSize;
        private int maxDimensionPx;
    }
}
