package springkong.talki_spring.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = {
                @Server(
                        url = "http://43.201.182.246:8080",
                        description = "Talki API Server"
                )
        }
)
public class OpenApiConfig {
}
