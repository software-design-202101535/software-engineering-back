package com.example.edumanager.global.fcm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "fcm")
public class FcmProperties {

    private boolean enabled;
    private String credentialsPath;
}
