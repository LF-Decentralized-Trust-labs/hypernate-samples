/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.samples.assettransferdemo;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import hu.bme.mit.ftsrg.hypernate.context.HypernateContext;
import hu.bme.mit.ftsrg.hypernate.contract.HypernateContract;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;

@Contract(
    name = "basic",
    info =
        @Info(
            title = "Asset Transfer",
            description = "The hyperlegendary asset transfer slightly improved",
            version = "0.1.0",
            license =
                @License(
                    name = "Apache 2.0 License",
                    url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
            contact =
                @Contact(
                    email = "a.transfer@example.com",
                    name = "Adrian Transfer",
                    url = "https://hyperledger.example.com")))
@Default
public final class AssetTransfer implements HypernateContract {

  private final ObjectMapper mapper =
      JsonMapper.builder().enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build();

  private enum AssetTransferErrors {
    ASSET_NOT_FOUND,
    ASSET_ALREADY_EXISTS,
    ASSET_VALUE_TOO_LOW,
  }

  /**
   * Creates some initial assets on the ledger.
   *
   * @param ctx the transaction context
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void InitLedger(final HypernateContext ctx) {
    var registry = ctx.getRegistry();
    registry.tryCreate(new Asset("asset1", "blue", 5, "Tomoko", 300));
    registry.tryCreate(new Asset("asset2", "red", 5, "Brad", 400));
    registry.tryCreate(new Asset("asset3", "green", 10, "Jin Soo", 500));
    registry.tryCreate(new Asset("asset4", "yellow", 10, "Max", 600));
    registry.tryCreate(new Asset("asset5", "black", 15, "Adrian", 700));
    registry.tryCreate(new Asset("asset6", "white", 15, "Michel", 700));
  }

  /**
   * Creates a new asset on the ledger.
   *
   * @param ctx the transaction context
   * @param assetID the ID of the new asset
   * @param color the color of the new asset
   * @param size the size for the new asset
   * @param owner the owner of the new asset
   * @param appraisedValue the appraisedValue of the new asset
   * @return the created asset
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public Asset CreateAsset(
      final HypernateContext ctx,
      final String assetID,
      final String color,
      final int size,
      final String owner,
      final int appraisedValue) {
    var asset = new Asset(assetID, color, size, owner, appraisedValue);
    ctx.getRegistry().mustCreate(asset);
    return asset;
  }

  /**
   * Retrieves an asset with the specified ID from the ledger.
   *
   * @param ctx the transaction context
   * @param assetID the ID of the asset
   * @return the asset found on the ledger if there was one
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public Asset ReadAsset(final HypernateContext ctx, final String assetID) {
    return ctx.getRegistry().mustRead(Asset.class, assetID);
  }

  /**
   * Updates the properties of an asset on the ledger.
   *
   * @param ctx the transaction context
   * @param assetID the ID of the asset being updated
   * @param color the color of the asset being updated
   * @param size the size of the asset being updated
   * @param owner the owner of the asset being updated
   * @param appraisedValue the appraisedValue of the asset being updated
   * @return the transferred asset
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public Asset UpdateAsset(
      final HypernateContext ctx,
      final String assetID,
      final String color,
      final int size,
      final String owner,
      final int appraisedValue) {
    var asset =
        Asset.builder()
            .assetID(assetID)
            .color(color)
            .size(size)
            .owner(owner)
            .appraisedValue(appraisedValue)
            .build();
    ctx.getRegistry().mustUpdate(asset);
    return asset;
  }

  /**
   * Deletes asset on the ledger.
   *
   * @param ctx the transaction context
   * @param assetID the ID of the asset being deleted
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public void DeleteAsset(final HypernateContext ctx, final String assetID) {
    var asset = Asset.builder().assetID(assetID).build();
    ctx.getRegistry().mustDelete(asset);
  }

  /**
   * Checks the existence of the asset on the ledger
   *
   * @param ctx the transaction context
   * @param assetID the ID of the asset
   * @return boolean indicating the existence of the asset
   */
  @Transaction(intent = Transaction.TYPE.EVALUATE)
  public boolean AssetExists(final HypernateContext ctx, final String assetID) {
    return ctx.getRegistry().tryRead(Asset.class, assetID) != null;
  }

  /**
   * Changes the owner of a asset on the ledger.
   *
   * @param ctx the transaction context
   * @param assetID the ID of the asset being transferred
   * @param newOwner the new owner
   * @return the old owner
   */
  @Transaction(intent = Transaction.TYPE.SUBMIT)
  public String TransferAsset(
      final HypernateContext ctx, final String assetID, final String newOwner) {
    var registry = ctx.getRegistry();
    Asset oldAsset;
    oldAsset = registry.mustRead(Asset.class, assetID);

    Asset updatedAsset = oldAsset.withOwner(newOwner);
    registry.mustUpdate(updatedAsset);

    if (oldAsset.getAppraisedValue() < 100)
      throw new ChaincodeException(
          "Asset %s's value is too low: %d".formatted(assetID, oldAsset.getAppraisedValue()));

    return oldAsset.getOwner();
  }
}
