package casualgames.apigateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Api gateway API", version = "v1"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Value("${server-url}")
    private String url;

    /*@Bean
    public RouterFunction<ServerResponse> swaggerUiRedirect() {
        return RouterFunctions.route(
                RequestPredicates.GET("/swagger-ui.html"),
                request -> ServerResponse
                        .status(HttpStatus.FOUND)
                        .location(URI.create("/webjars/swagger-ui/index.html"))
                        .build()
        );
    }*/

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url(url));
    }
}
