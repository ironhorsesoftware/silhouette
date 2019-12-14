package com.ironhorsesoftware.play.silhouette.api.util

import java.time.LocalDateTime

/**
 * A trait which provides a mockable implementation for a {@link LocalDateTime} instance.
 */
trait Clock {

  /**
   * Gets the current {@link LocalDateTime}.
   *
   * @return the current <code>LocalDateTime</code>.
   */
  def now : LocalDateTime
}

/**
 * Creates a clock implementation.
 */
object Clock {

  /**
   * Constructs a Clock implementation.
   *
   * @return A Clock implementation.
   */
  def apply() = new Clock {
    def now = LocalDateTime.now
  }
}