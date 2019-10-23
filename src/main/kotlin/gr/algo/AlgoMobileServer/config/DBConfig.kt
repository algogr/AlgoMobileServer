package gr.algo.AlgoMobileServer.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import javax.sql.DataSource

//import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class DBConfig {
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
        dataSourceBuilder.url("jdbc:sqlserver://192.168.2.249:1433;databaseName=xLINENICEICE")
        return dataSourceBuilder.build()
        //return  DataSourceBuilder.create().build()
    }


    @Bean(name = arrayOf("jdbcTemplate2"))
    fun jdbcTemplate2(@Qualifier("mssql") ds:DataSource):JdbcTemplate {
        return JdbcTemplate(ds)
    }



}

