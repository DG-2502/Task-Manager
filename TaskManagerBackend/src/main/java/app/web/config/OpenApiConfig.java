package app.web.config;

import app.web.dto.response.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        Schema<?> errorSchema = new Schema<>().$ref("#/components/schemas/ErrorResponse");
        Content errorContent = new Content().addMediaType("application/json",
                new MediaType().schema(errorSchema));

        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API")
                        .description("REST API for Task Management with JWT Authentication")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSchemas("ErrorResponse", ModelConverters.getInstance()
                                .read(ErrorResponse.class).get("ErrorResponse"))
                        .addResponses("BadRequest", new ApiResponse()
                                .description("Bad Request — Validation failed or malformed JSON")
                                .content(errorContent))
                        .addResponses("Unauthorized", new ApiResponse()
                                .description("Unauthorized — Missing or invalid JWT token")
                                .content(errorContent))
                        .addResponses("Forbidden", new ApiResponse()
                                .description("Forbidden — Insufficient permissions")
                                .content(errorContent))
                        .addResponses("NotFound", new ApiResponse()
                                .description("Not Found — Resource does not exist")
                                .content(errorContent))
                        .addResponses("Conflict", new ApiResponse()
                                .description("Conflict — Database constraint violation")
                                .content(errorContent))
                        .addResponses("InternalError", new ApiResponse()
                                .description("Internal Server Error")
                                .content(errorContent))
                );
    }
}
