package features.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix="mappings")
@Data
public class PhysicalNamingMappingsConfig {
    private Map<String, Table> entities;

    @Data
    static class Table {
        private String tableName;
        private Map<String,String> columns;
    }
}

/*

model:
    user:
        tableName:
        columns:
            a:
            b:
            c:
 =

 */