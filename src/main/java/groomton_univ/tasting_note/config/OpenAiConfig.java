package groomton_univ.tasting_note.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String openAiKey;

    @Bean



}
