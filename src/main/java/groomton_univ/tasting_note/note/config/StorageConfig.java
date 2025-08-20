package groomton_univ.tasting_note.note.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class StorageConfig {

    @Bean
    public Storage storage(
            @Value("${gcp.storage.project-id}") String projectId,
            @Value("${gcp.storage.file.name}") String keyFileOnClasspath
) throws IOException {
        ClassPathResource resource = new ClassPathResource(keyFileOnClasspath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build().getService();
    }
}
