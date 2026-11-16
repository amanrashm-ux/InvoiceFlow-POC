package com.invoiceflow.invoice;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
@Configuration
public class ReadDataSourceConfig {
 @Bean @Primary public JdbcTemplate writeJdbcTemplate(DataSource dataSource){return new JdbcTemplate(dataSource);}
 @Bean @Qualifier("readJdbcTemplate") public JdbcTemplate readJdbcTemplate(
   @Value("${app.datasource.read-url:}") String readUrl,
   @Value("${app.datasource.read-username:}") String readUser,
   @Value("${app.datasource.read-password:}") String readPassword, DataSource primary) {
   if(readUrl==null || readUrl.isBlank()) return new JdbcTemplate(primary);
   DriverManagerDataSource replica=new DriverManagerDataSource();
   replica.setDriverClassName("org.h2.Driver"); replica.setUrl(readUrl);
   replica.setUsername(readUser); replica.setPassword(readPassword);
   return new JdbcTemplate(replica);
 }
}
