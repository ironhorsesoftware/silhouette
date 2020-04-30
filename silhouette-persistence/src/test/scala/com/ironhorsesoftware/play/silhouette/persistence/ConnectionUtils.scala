package com.ironhorsesoftware.play.silhouette.persistence

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import javax.sql.DataSource
import java.sql.Connection

// Courtesy of https://blog.knoldus.com/integration-testing-with-h2/
object ConnectionUtils {
  def getDataSource(jdbcUrl: String): DataSource = {
    val config: HikariConfig = new HikariConfig()
    config.setJdbcUrl(jdbcUrl)
    config.setUsername("dummy")
    config.setPassword("dummy")
    new HikariDataSource(config)
  }

  def getConnection(jdbcUrl: String, autocommit: Boolean = true): Connection = {
    val datasource: DataSource = getDataSource(jdbcUrl)
    val conn = datasource.getConnection()
    conn.setAutoCommit(autocommit)
    conn
  }

}