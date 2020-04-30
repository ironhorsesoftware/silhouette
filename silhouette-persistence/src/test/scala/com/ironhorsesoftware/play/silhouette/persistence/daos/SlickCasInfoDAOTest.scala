package com.ironhorsesoftware.play.silhouette.persistence.daos

import java.sql.Connection

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import com.ironhorsesoftware.play.silhouette.persistence.ConnectionUtils

class SlickCasInfoDAOSpec extends AnyFlatSpec {
  "SlickCasInfoDAOSpec" should "Should write and read new CasInfo" in {
    def conn : Connection = {
      val jdbcUrl = "jdbc:h2:mem:test"
      ConnectionUtils.getConnection(jdbcUrl)
    }
  }
}