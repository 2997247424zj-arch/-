package com.example.musicshareplatformback.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.media")
public class MediaProperties {

    private String audioRoot = "../media/audio";

    public String getAudioRoot() {
        return audioRoot;
    }

    public void setAudioRoot(String audioRoot) {
        this.audioRoot = audioRoot;
    }
}
