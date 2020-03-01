package gr.algo.AlgoMobileServer.config

import com.sun.jndi.toolkit.url.Uri
import gr.algo.AlgoMobileServer.context
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.sql.DataSource

//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import java.net.URL
import javax.annotation.PostConstruct

@Configuration
class DBConfig {

    @Value("\${algo.sqlserverurl}")
    lateinit var sqlServerUrl:String

    @Bean(name = arrayOf("sqlite"))
    @ConfigurationProperties(prefix = "spring.datasource")
    fun dataSource1(): DataSource {


        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("org.sqlite.JDBC")
        dataSourceBuilder.url("jdbc:sqlite:filestorage/algo.sqlite")
        return dataSourceBuilder.build()



    }


    @Bean(name = arrayOf("jdbcTemplate1"))
    fun jdbcTemplate1(@Qualifier("sqlite") ds:DataSource):JdbcTemplate {
        return JdbcTemplate(ds)
    }



    @Bean(name = arrayOf("mssql"))
    @ConfigurationProperties(prefix = "spring.second-datasource")

    fun dataSource2(): DataSource {
        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        dataSourceBuilder.url("$sqlServerUrl")

        return dataSourceBuilder.build()
    }


    @Bean(name = arrayOf("jdbcTemplate2"))
    fun jdbcTemplate2(@Qualifier("mssql") ds:DataSource):JdbcTemplate {
        return JdbcTemplate(ds)
    }



    @Bean(name = arrayOf("sqlite1"))
    @ConfigurationProperties(prefix = "spring.datasource")
    fun dataSource3(): DataSource {


        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("org.sqlite.JDBC")
        //dataSourceBuilder.url("jdbc:sqlite:filestorage/algo.sqlite.LATEST")
        dataSourceBuilder.url("jdbc:sqlite:filestorage/algo.sqlite")
        return dataSourceBuilder.build()



    }

    @Bean(name = arrayOf("jdbcTemplate3"))
    fun jdbcTemplate3(@Qualifier("sqlite1") ds:DataSource):JdbcTemplate {
        return JdbcTemplate(ds)
    }




}

