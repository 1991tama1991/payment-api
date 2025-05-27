package com.tama.payment.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class OpenApiConfig {

    @Bean
    OpenAPI paymentConfigApi(@Value("${spring.application.name}") String applicationName,
                           @Value("${spring.application.api-version}") String apiVersion) {
        String apiTitle = String.format("%s API", applicationName);

        return new OpenAPI()
                .info(new Info().title(apiTitle).version(apiVersion));
    }

    @Bean
    OpenApiCustomizer openApiCustomizer(ObjectMapper objectMapper,
                                        @Value("${application.openapi-example-path-directory}")
                                        String openApiExamplePathDirectory) {
        return openApi -> customize(objectMapper, openApiExamplePathDirectory, openApi);
    }

    private void customize(ObjectMapper objectMapper, String openApiExamplePathDirectory, OpenAPI openApi) {
        filterEndpoints(openApi);
        provideExamplesForSchemas(objectMapper, openApi, openApiExamplePathDirectory);
    }

    private void filterEndpoints(OpenAPI openApi) {
        LinkedHashSet<Map.Entry<String, PathItem>> paths = organizePaths(openApi);

        openApi.getPaths().clear();

        paths.forEach(path -> openApi.getPaths().addPathItem(path.getKey(), path.getValue()));
    }

    private LinkedHashSet<Map.Entry<String, PathItem>> organizePaths(OpenAPI openApi) {
        return openApi.getPaths()
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByKey()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void provideExamplesForSchemas(ObjectMapper objectMapper,
                                           OpenAPI openApi,
                                           String openApiExamplePathDirectory) {
        openApi.getComponents().getSchemas()
                .values()
                .forEach(schema -> addExample(objectMapper, openApiExamplePathDirectory, schema));
    }

    private void addExample(ObjectMapper objectMapper, String openApiExamplePathDirectory, Schema<?> schema) {
        String schemaName = schema.getName();
        try {
            String exampleJson = readExampleJson(schemaName, openApiExamplePathDirectory);

            JsonNode node = objectMapper.readTree(exampleJson);

            schema.setExample(node);

        } catch (IOException | IllegalArgumentException e) {
            log.warn("Json example is missing for schema: {}", schemaName);
        }
    }

    private String readExampleJson(String schemaName, String openApiExamplePathDirectory) throws IOException {
        URL url = Resources.getResource(openApiExamplePathDirectory + schemaName + ".json");
        return Resources.toString(url, StandardCharsets.UTF_8);
    }
}
