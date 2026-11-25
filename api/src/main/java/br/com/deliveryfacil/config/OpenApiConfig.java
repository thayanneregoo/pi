package br.com.deliveryfacil.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI deliveryFacilOpenApi() {
        return new OpenAPI().info(new Info()
                .title("DeliveryFácil API")
                .version("1.0.0")
                .description("API REST de delivery próprio para restaurantes. "
                        + "Rotas restritas a funcionários exigem o cabeçalho X-Funcionario-Id."));
    }
}
