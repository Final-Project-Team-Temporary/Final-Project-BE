package com.example.whiplash;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            loadEnv();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void loadEnv() throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(".env.test")) {
            props.load(fis);
        }
        props.forEach((key, value) -> {
            System.setProperty(key.toString(), value.toString());
        });
    }
}
