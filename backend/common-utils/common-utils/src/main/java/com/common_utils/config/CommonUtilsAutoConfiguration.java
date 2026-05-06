package com.common_utils.config;

import com.common_utils.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        GlobalExceptionHandler.class
})
public class CommonUtilsAutoConfiguration {
}
