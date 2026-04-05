package com.sujeet.projects.rate_limiter.configurations;

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Configuration
@Component
public class YamlPropertySourceFactory extends DefaultPropertySourceFactory {
    public YamlPropertySourceFactory() {
    }

    public PropertySource createPropertySource(String name, EncodedResource resource) throws IOException {
        if (resource == null) {
            return super.createPropertySource(name, resource);
        } else {
            List<PropertySource<?>> propertySourceList = (new YamlPropertySourceLoader()).load(resource.getResource().getFilename(), resource.getResource());
            return !propertySourceList.isEmpty() ? (PropertySource)propertySourceList.iterator().next() : super.createPropertySource(name, resource);
        }
    }
}
