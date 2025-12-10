/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.samples.assettransferdemo;

import hu.bme.mit.ftsrg.hypernate.annotations.AttributeInfo;
import hu.bme.mit.ftsrg.hypernate.annotations.PrimaryKey;
import lombok.Builder;
import lombok.Value;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@Builder
@DataType
@FieldNameConstants
@Jacksonized
@PrimaryKey(@AttributeInfo(name = Asset.Fields.assetID))
@Value
@With
public class Asset {

  @Property String assetID;
  @Property String color;
  @Property int size;
  @Property String owner;
  @Property int appraisedValue;

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
        + "@"
        + Integer.toHexString(hashCode())
        + " [assetID="
        + assetID
        + ", color="
        + color
        + ", size="
        + size
        + ", owner="
        + owner
        + ", appraisedValue="
        + appraisedValue
        + "]";
  }
}
