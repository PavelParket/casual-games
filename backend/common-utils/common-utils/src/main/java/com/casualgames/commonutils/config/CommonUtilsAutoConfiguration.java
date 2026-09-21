package com.casualgames.commonutils.config;

import com.casualgames.commonutils.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        GlobalExceptionHandler.class
})
public class CommonUtilsAutoConfiguration {
}
