package casualgames.userservice.config;

import casualgames.userservice.domain.enums.AttachmentType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "attachments")
public class AttachmentsProperties {

    private Map<AttachmentType, AttachmentProperties> propertiesByType = new EnumMap<>(AttachmentType.class);

    @Data
    public static class AttachmentProperties {
        private String bucket;
        private String folder;
        private String publicBaseUrl;
        private String maxFileSize;
        private int maxDimensionPx;
        private String cacheControl;
        private List<String> allowedMimeTypes;
        private Map<String, VariantProperties> variants = new java.util.LinkedHashMap<>();
    }

    @Data
    public static class VariantProperties {
        private int size;
        private double quality;
    }
}
