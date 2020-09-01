package features.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomPhysicalNamingStrategy extends SpringPhysicalNamingStrategy {

    @Autowired
    private PhysicalNamingMappingsConfig config;

    private PhysicalNamingMappingsConfig.Table currentTable;

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return super.toPhysicalCatalogName(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return super.toPhysicalSchemaName(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (config.getEntities().containsKey(name.getText())) {
            currentTable = config.getEntities().get(name.getText());
            return new Identifier(currentTable.getTableName(), false);
        }
        Identifier newIdentifier = super.toPhysicalTableName(name, jdbcEnvironment);
        log.warn("No provided mapping for table {}, using default {}", name, newIdentifier);
        return newIdentifier;
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return super.toPhysicalSequenceName(name, jdbcEnvironment);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        if (currentTable != null && currentTable.getColumns().containsKey(name.getText())) {
            return new Identifier(currentTable.getColumns().get(name.getText()), false);
        }
        Identifier newIdentifier = super.toPhysicalColumnName(name, jdbcEnvironment);
        if (!"DTYPE".equals(name.getText())) {
            log.warn("No provided mapping for column {}, using default {}", name, newIdentifier);
        }
        return newIdentifier;
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