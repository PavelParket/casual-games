package com.security_starter.whitelist;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "spring.security.service-whitelist")
@Getter
@Setter
public class ServiceWhitelistProperties {

    private List<String> hosts = new ArrayList<>();

    private boolean enabled = false;
}
