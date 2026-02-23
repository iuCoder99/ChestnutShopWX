package com.app.uni_app.infrastructure.redis.config;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.List;

public class YamlPropertySourceFactory extends DefaultPropertySourceFactory {
    @SuppressWarnings("NullableProblems")
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(resource.getResource().getFilename(), resource.getResource());
        if (!sources.isEmpty()) {
            return sources.get(0);
        }
        return super.createPropertySource(name, resource);
    }
}
