/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.samples.assettransferdemo;

import static hu.bme.mit.ftsrg.chaincode.testutil.util.Serializer.serialize;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import hu.bme.mit.ftsrg.chaincode.testutil.ContractTesting;
import hu.bme.mit.ftsrg.chaincode.testutil.util.Serializer;
import hu.bme.mit.ftsrg.hypernate.context.HypernateContext;
import java.util.List;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
final class AssetTransferTest {

  private ContractTesting testing;
  private AssetTransfer contract;
  private HypernateContext ctx;
  @Mock private ChaincodeStub stub;

  @BeforeEach
  void setupContext() {
    testing = new ContractTesting(Serializer::serialize, obj -> serialize(obj).getBytes(UTF_8));
    testing.arrange(stub).identities.setTestAdminCreator();
    contract = new AssetTransfer();
    ctx = (HypernateContext) contract.createContext(stub);
  }

  @Test
  void should_throw_when_unknown_tx() {
    // Act
    Throwable thrown = catchThrowable(() -> contract.unknownTransaction(ctx));

    // Assert
    assertThat(thrown).isInstanceOf(RuntimeException.class);
    testing.assertThat(stub).state.readNTimes(0).updatedNTimes(0);
  }

  @Test
  void should_create_assets_when_init_ledger() {
    // Arrange
    var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
    var asset2 = new Asset("asset2", "red", 5, "Brad", 400);
    var asset3 = new Asset("asset3", "green", 10, "Jin Soo", 500);
    var asset4 = new Asset("asset4", "yellow", 10, "Max", 600);
    var asset5 = new Asset("asset5", "black", 15, "Adrian", 700);
    var asset6 = new Asset("asset6", "white", 15, "Michel", 700);
    var assets = List.of(asset1, asset2, asset3, asset4, asset5, asset6);

    var arrange = testing.arrange(stub);
    assets.forEach(
        asset ->
            arrange.setCompositeKey(
                asset, asset.getClass().getName().toUpperCase(), asset.getAssetID()));

    // Act
    contract.InitLedger(ctx);

    // Assert
    var stateAssertions = testing.assertThat(stub).state;
    assets.forEach(asset -> stateAssertions.updatedNTimes(asset, 1));
    stateAssertions.updatedNTimes(6);
  }

  @Nested
  final class CreateAssetTests {

    @Test
    void should_throw_when_asset_exists() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .byteStates
          .set(asset1);

      // Act
      Throwable thrown =
          catchThrowable(
              () ->
                  contract.CreateAsset(
                      ctx,
                      asset1.getAssetID(),
                      asset1.getColor(),
                      asset1.getSize(),
                      asset1.getOwner(),
                      asset1.getAppraisedValue()));

      // Assert
      assertThat(thrown).isInstanceOf(RuntimeException.class);
      testing.assertThat(stub).state.readNTimes(asset1, 1).readNTimes(1).updatedNTimes(0);
    }

    @Test
    void should_create_asset_when_it_does_not_exist() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .byteStates
          .setAsNonExistent(asset1);

      // Act
      final Asset returned =
          contract.CreateAsset(
              ctx,
              asset1.getAssetID(),
              asset1.getColor(),
              asset1.getSize(),
              asset1.getOwner(),
              asset1.getAppraisedValue());

      // Assert
      assertThat(returned).isEqualTo(asset1);
      testing.assertThat(stub).state.updatedNTimes(asset1, 1).updatedNTimes(1);
    }
  }

  @Nested
  final class ReadAssetTests {

    @Test
    void should_return_existing_asset() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .byteStates
          .set(asset1);

      // Act
      final Asset returned = contract.ReadAsset(ctx, asset1.getAssetID());

      // Assert
      assertThat(returned).isEqualTo(asset1);
      testing.assertThat(stub).state.readNTimes(asset1, 1).readNTimes(1).updatedNTimes(0);
    }

    @Test
    void should_throw_when_asset_not_found() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .byteStates
          .setAsNonExistent(asset1);

      // Act
      Throwable thrown = catchThrowable(() -> contract.ReadAsset(ctx, asset1.getAssetID()));

      // Assert
      assertThat(thrown).isInstanceOf(RuntimeException.class);
      testing.assertThat(stub).state.readNTimes(asset1, 1).readNTimes(1).updatedNTimes(0);
    }
  }

  @Nested
  final class UpdateAssetTests {

    @Test
    void should_update_asset_when_it_exists() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      var asset1Updated = new Asset("asset1", "pink", 45, "Arturo", 600);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .setCompositeKey(
              asset1Updated,
              asset1Updated.getClass().getName().toUpperCase(),
              asset1Updated.getAssetID())
          .byteStates
          .set(asset1);

      // Act
      final Asset returned =
          contract.UpdateAsset(
              ctx,
              asset1.getAssetID(),
              asset1Updated.getColor(),
              asset1Updated.getSize(),
              asset1Updated.getOwner(),
              asset1Updated.getAppraisedValue());

      // Assert
      assertThat(returned).isEqualTo(asset1Updated);
      testing.assertThat(stub).state.updatedNTimes(asset1Updated, 1).updatedNTimes(1);
    }

    @Test
    void should_throw_when_asset_not_found() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      var asset1Updated = new Asset("asset1", "pink", 45, "Arturo", 600);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .setCompositeKey(
              asset1Updated,
              asset1Updated.getClass().getName().toUpperCase(),
              asset1Updated.getAssetID())
          .byteStates
          .setAsNonExistent(asset1);

      // Act
      Throwable thrown =
          catchThrowable(
              () ->
                  contract.UpdateAsset(
                      ctx,
                      asset1.getAssetID(),
                      asset1Updated.getColor(),
                      asset1Updated.getSize(),
                      asset1Updated.getOwner(),
                      asset1Updated.getAppraisedValue()));

      // Assert
      assertThat(thrown).isInstanceOf(RuntimeException.class);
      testing
          .assertThat(stub)
          .state
          .readNTimes(asset1, 1)
          .readNTimes(1)
          .updatedNTimes(asset1Updated, 0)
          .updatedNTimes(0);
    }
  }

  @Nested
  final class DeleteAssetTests {

    @Test
    void should_throw_when_asset_not_found() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .byteStates
          .setAsNonExistent(asset1);

      // Act
      Throwable thrown = catchThrowable(() -> contract.DeleteAsset(ctx, asset1.getAssetID()));

      // Assert
      assertThat(thrown).isInstanceOf(RuntimeException.class);
      testing
          .assertThat(stub)
          .state
          .readNTimes(asset1, 1)
          .readNTimes(1)
          .updatedNTimes(asset1, 0)
          .updatedNTimes(0);
    }

    @Test
    void should_delete_asset_when_it_exists() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .byteStates
          .set(asset1);

      // Act
      contract.DeleteAsset(ctx, asset1.getAssetID());

      // Assert
      testing.assertThat(stub).state.deletedNTimes(asset1, 1);
    }
  }

  @Nested
  final class TransferAssetTests {

    @Test
    void should_update_owner_when_asset_exists() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      var asset1Updated = new Asset("asset1", "blue", 5, "Dr Evil", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .setCompositeKey(
              asset1Updated,
              asset1Updated.getClass().getName().toUpperCase(),
              asset1Updated.getAssetID())
          .byteStates
          .set(asset1);

      // Act
      String returned = contract.TransferAsset(ctx, asset1.getAssetID(), asset1Updated.getOwner());

      // Assert
      assertThat(returned).isEqualTo(asset1.getOwner());
      testing.assertThat(stub).state.updatedNTimes(asset1Updated, 1).updatedNTimes(1);
    }

    @Test
    void should_throw_when_asset_not_found() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 300);
      var asset1Updated = new Asset("asset1", "blue", 5, "Dr Evil", 300);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .setCompositeKey(
              asset1Updated,
              asset1Updated.getClass().getName().toUpperCase(),
              asset1Updated.getAssetID())
          .byteStates
          .setAsNonExistent(asset1);

      // Act
      Throwable thrown =
          catchThrowable(
              () -> contract.TransferAsset(ctx, asset1.getAssetID(), asset1Updated.getOwner()));

      // Assert
      assertThat(thrown).isInstanceOf(RuntimeException.class);
      testing
          .assertThat(stub)
          .state
          .readNTimes(asset1, 1)
          .readNTimes(1)
          .updatedNTimes(asset1, 0)
          .updatedNTimes(0);
    }

    @Test
    void should_throw_when_asset_value_too_low() {
      // Arrange
      var asset1 = new Asset("asset1", "blue", 5, "Tomoko", 50);
      var asset1Updated = new Asset("asset1", "blue", 5, "Dr Evil", 50);
      testing
          .arrange(stub)
          .setCompositeKey(asset1, asset1.getClass().getName().toUpperCase(), asset1.getAssetID())
          .setCompositeKey(
              asset1Updated,
              asset1Updated.getClass().getName().toUpperCase(),
              asset1Updated.getAssetID())
          .byteStates
          .set(asset1);

      // Act
      Throwable thrown =
          catchThrowable(
              () -> contract.TransferAsset(ctx, asset1.getAssetID(), asset1Updated.getOwner()));

      // Assert
      assertThat(thrown).isInstanceOf(RuntimeException.class);
      testing.assertThat(stub).state.updatedNTimes(asset1, 0).updatedNTimes(0);
    }
  }
}
