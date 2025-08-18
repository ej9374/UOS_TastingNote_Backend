package groomton_univ.tasting_note.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class VertexAiConfig {

    @Value("${gcp.vertex.project-id}")
    private String projectId;

    @Value("${gcp.vertex.location}")
    private String location;

    @Value("${gcp.vertex.model}")
    private String model;

    @Bean(destroyMethod = "close")
    public VertexAI vertexAI(){
        return new VertexAI(projectId, location);
    }
    @Bean
    public GenerativeModel generativeModel(VertexAI vertexAI) {
        return new GenerativeModel(model, vertexAI);
    }
}
