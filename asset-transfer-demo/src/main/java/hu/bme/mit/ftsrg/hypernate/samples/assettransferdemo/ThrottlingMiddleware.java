/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.samples.assettransferdemo;

import hu.bme.mit.ftsrg.hypernate.middleware.StubMiddleware;
import java.util.HashMap;
import java.util.Map;

public class ThrottlingMiddleware extends StubMiddleware {

  private final Map<String, byte[]> writes = new HashMap<>();

  @Override
  public void putState(String key, byte[] value) {
    writes.put(key, value);
  }

  @Override
  protected void onTransactionEnd() {
    writes.forEach(super::putState);
  }
}
