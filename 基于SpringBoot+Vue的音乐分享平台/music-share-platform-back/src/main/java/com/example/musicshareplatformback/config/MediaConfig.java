package com.example.musicshareplatformback.config;

import com.example.musicshareplatformback.media.MediaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MediaProperties.class)
public class MediaConfig {
}
