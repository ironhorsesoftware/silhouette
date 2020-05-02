package com.ironhorsesoftware.play.silhouette.persistence.utils

import java.sql.{Time, Timestamp}

import scala.concurrent.duration.{Duration, FiniteDuration, MILLISECONDS}

import org.joda.time.{Instant, DateTime, DateTimeZone}

object DateTimeConverters {
  def timestampToDateTime(ts : Timestamp) = {
    Instant.ofEpochMilli(ts.getTime).toDateTime().withZone(DateTimeZone.UTC)
  }

  def dateTimeToTimestamp(dt : DateTime) = {
    new Timestamp(dt.toInstant.getMillis)
  }

  def millisToFiniteDuration(millis : Long) = {
    FiniteDuration(millis, MILLISECONDS)    
  }

  def durationToMillis(duration : Duration) = {
    duration.toMillis
  }
}