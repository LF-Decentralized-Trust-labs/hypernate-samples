/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.samples.assettransferdemo;

import hu.bme.mit.ftsrg.hypernate.middleware.StubMiddleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMiddleware extends StubMiddleware {

  private final Logger logger = LoggerFactory.getLogger(LoggingMiddleware.class);

  @Override
  public byte[] getState(String key) {
    logger.warn("Getting state for {}", key);
    return super.getState(key);
  }

  @Override
  public void putState(String key, byte[] value) {
    logger.warn("Putting state {}: {} bytes", key, value.length);
    super.putState(key, value);
  }
}
