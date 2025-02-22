package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.mappers.AssetMapper;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.repositories.AssetRepository;
import me.vse.fintrackserver.rest.requests.AssetAddRequest;
import org.apache.logging.log4j.util.Strings;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static me.vse.fintrackserver.ATest.randomString;
import static org.easymock.EasyMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    private EntityManager entityManager;
    private AssetService assetService;
    private AssetRepository assetRepository;
    private AssetMapper assetMapper;


    @BeforeEach
    public void setup() {
        entityManager = EasyMock.mock(EntityManager.class);
        assetRepository = EasyMock.mock(AssetRepository.class);
        assetMapper = EasyMock.mock(AssetMapper.class);
        assetService = new AssetService(entityManager, assetRepository, assetMapper);
    }

    private Stream<Arguments> getAddScenarios() {
        return Stream.of(
                Arguments.of(Asset.builder().account(new Account()).build(), ErrorMessages.INCORRECT_ASSET.name()),
                Arguments.of(Asset.builder().name("newAsset").build(),
                        ErrorMessages.INCORRECT_ASSET.name()),
                Arguments.of(Asset.builder()
                                .name("newAsset")
                                .account(Account.builder().id("accId").build())
                                .build(),
                        null
                ),
                Arguments.of(Asset.builder()
                                .name("newAsset")
                                .color(randomString(5))
                                .account(Account.builder().id("accId").build())
                                .build(),
                        null
                ),
                Arguments.of(Asset.builder()
                                .name("newAsset")
                                .color(randomString(5))
                                .acquisitionPrice(100.0)
                                .depreciationPrice(50.0)
                                .icon("newIcon")
                                .startDate(LocalDateTime.now().minusDays(30).toLocalDate())
                                .endDate(LocalDateTime.now().plusDays(60).toLocalDate())
                                .account(Account.builder().id("accId").build())
                                .build(),
                        null
                )
        );
    }

    @ParameterizedTest(name = "Test asset add. Given asset: {0}. Should save or return expetion: {1}")
    @MethodSource("getAddScenarios")
    public void addTest(Asset asset, String exception) {
        AssetAddRequest request = AssetAddRequest.builder()
                .accountId(asset.getAccount() == null ? null : asset.getAccount().getId())
                .name(asset.getName())
                .endDateStr(asset.getEndDate())
                .startDateStr(asset.getStartDate())
                .acquisitionPrice(asset.getAcquisitionPrice())
                .depreciationPrice(asset.getDepreciationPrice())
                .icon(asset.getIcon())
                .build();

        expect(entityManager.find(Account.class, request.getAccountId())).andReturn(asset.getAccount());
        entityManager.persist(anyObject(Account.class));
        replay(entityManager);

        if (exception != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> assetService.add(request));
            assertEquals(exception, thrown.getMessage());
        } else {
            assertEquals(asset, assetService.add(request));
            verify(entityManager);
        }
    }

    private Stream<Arguments> getUpdateScenarios() {
        return Stream.of(
                Arguments.of(new Asset(), ErrorMessages.USER_DOESNT_EXIST),
                Arguments.of(Asset.builder()
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .build())
                                .build(),
                        ErrorMessages.INCORRECT_ASSET
                ),
                Arguments.of(
                        Asset.builder()
                                .id("asseId")
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .isRemoved(true)
                                        .build())
                                .build(),
                        ErrorMessages.USER_DOESNT_HAVE_RIGHTS
                ),
                Arguments.of(
                        Asset.builder()
                                .id("assetId")
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .build())
                                .build(),
                        null
                ),
                Arguments.of(
                        Asset.builder()
                                .id("assetId")
                                .name("updatedAsset")
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .build())
                                .build(),
                        null
                ),
                Arguments.of(
                        Asset.builder()
                                .id("assetId")
                                .name("updatedAsset")
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .build())
                                .build(),
                        null
                ),
                Arguments.of(
                        Asset.builder()
                                .id("assetId")
                                .name("updatedAsset")
                                .acquisitionPrice(500.0)
                                .depreciationPrice(100.0)
                                .startDate(LocalDateTime.now().minusDays(60).toLocalDate())
                                .endDate(LocalDateTime.now().plusDays(120).toLocalDate())
                                .color("white")
                                .icon("car")
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .build())
                                .build(),
                        null
                ),
                Arguments.of(
                        Asset.builder()
                                .id("assetId")
                                .name("updatedAsset")
                                .acquisitionPrice(500.0)
                                .depreciationPrice(100.0)
                                .startDate(LocalDateTime.now().minusDays(60).toLocalDate())
                                .endDate(LocalDateTime.now().plusDays(120).toLocalDate())
                                .color("white")
                                .icon("car")
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .build())
                                .build(),
                        null
                ),
                Arguments.of(
                        Asset.builder()
                                .id("assetId")
                                .name("updatedAsset")
                                .acquisitionPrice(500.0)
                                .depreciationPrice(100.0)
                                .startDate(LocalDateTime.now().minusDays(60).toLocalDate())
                                .endDate(LocalDateTime.now().plusDays(120).toLocalDate())
                                .color("white")
                                .icon("car")
                                .account(Account.builder()
                                        .userRights(
                                                List.of(AccountUserRights.builder()
                                                        .user(User.builder().build())
                                                        .build()))
                                        .build())
                                .build(),
                        null
                )
        );
    }

    @ParameterizedTest(name = "Test asset update. Given expected asset: {0}. " +
            "Should return asset {0} or throw exception {1}")
    @MethodSource("getUpdateScenarios")
    public void updateTest(Asset expected, ErrorMessages exceptionMessage) throws AuthenticationException {
        User user = expected.getAccount() == null ? null : expected.getAccount().getUserRights().get(0).getUser();
        if (user != null) {
            user.setAccountUserRights(List.of(
                    AccountUserRights.builder()
                            .account(Account.builder()
                                    .assets(List.of(expected))
                                    .isRemoved(expected.getAccount().isRemoved())
                                    .build())
                            .user(user)
                            .build()));
        }

        AssetDto assetDto = AssetDto.builder()
                .senderId(user == null ? null : user.getId())
                .acquisitionPrice(expected.getAcquisitionPrice())
                .depreciationPrice(expected.getDepreciationPrice())
                .icon(expected.getIcon())
                .color(expected.getColor())
                .startDate(expected.getStartDate())
                .endDate(expected.getEndDate())
                .name(expected.getName())
                .build();

        expect(entityManager.find(User.class, assetDto.getSenderId())).andReturn(user);
        expect(entityManager.find(Asset.class, assetDto.getId())).andReturn(expected.getId() == null ? null : expected);
        assetMapper.updateAssetFromDto(assetDto, expected);
        expect(assetRepository.save(expected)).andReturn(expected);
        replay(entityManager, assetMapper, assetRepository);

        if (exceptionMessage != null) {
            if (ErrorMessages.INCORRECT_ASSET.equals(exceptionMessage)) {
                IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                        () -> assetService.update(assetDto));
                assertEquals(exceptionMessage.name(), thrown.getMessage());
            } else {
                AuthenticationException thrown = assertThrows(AuthenticationException.class,
                        () -> assetService.update(assetDto));
                assertEquals(exceptionMessage.name(), thrown.getMessage());
            }

        } else {
            assertEquals(expected, assetService.update(assetDto));
            verify(entityManager, assetMapper, assetRepository);
        }
    }

    private Stream<Arguments> getDeleteScenarios() {
        return Stream.of(
                Arguments.of(Asset.builder().build(), null, ErrorMessages.INCORRECT_ASSET),
                Arguments.of(Asset.builder().id("assetId").build(),
                        Asset.builder().id("assetId")
                                .isRemoved(true)
                                .removedAt(LocalDateTime.now())
                                .endDate(LocalDate.now())
                                .build(),
                        null
                ),
                Arguments.of(Asset.builder()
                                .id("assetId")
                                .isRemoved(false)
                                .build(),
                        Asset.builder().id("assetId")
                                .isRemoved(true)
                                .removedAt(LocalDateTime.now())
                                .endDate(LocalDate.now())
                                .build(),
                        null
                ),Arguments.of(Asset.builder().id("assetId")
                                .isRemoved(false)
                                .endDate(LocalDateTime.now().minusDays(3).toLocalDate())
                                .build(),
                        Asset.builder().id("assetId")
                                .isRemoved(true)
                                .removedAt(LocalDateTime.now())
                                .endDate(LocalDateTime.now().minusDays(3).toLocalDate())
                                .build(),
                        null
                )
        );
    }

    @ParameterizedTest(name = "Test asset delete. Given old asset {0}, expected asset: {1}. " +
            "Should return {1} or exception {2}")
    @MethodSource("getDeleteScenarios")
    public void deleteTest(Asset asset, Asset expected, ErrorMessages message) throws AuthenticationException {
        expect(entityManager.find(Asset.class, asset.getId())).andReturn(asset.getId() == null ? null : asset);
        expect(assetRepository.save(asset)).andReturn(expected);
        replay(entityManager, assetRepository);

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> assetService.delete(asset.getId(), "userId"));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            assetService.delete(asset.getId(), "userId");
            verify(entityManager, assetRepository);
        }
    }

    private Stream<Arguments> getCurrentAssetPriceScenarios() {
        return Stream.of(
                Arguments.of(true, 0, 0, null, null, 0),
                Arguments.of(true, 100, 0, null, null, 0),
                Arguments.of(true, 0, 100, null, null, 0),
                Arguments.of(true, 100, 100, null, null, 0),
                Arguments.of(true, 0, 0, LocalDate.now(), null, 0),
                Arguments.of(true, 100, 0, null, LocalDate.now(), 0),
                Arguments.of(true, 0, 100, LocalDate.now(), LocalDate.now(), 0),
                Arguments.of(false, 1000, 0,
                        LocalDate.now().minusDays(50), LocalDate.now().plusDays(50), 500),
                Arguments.of(false, 1000, 0,
                        LocalDate.now().minusDays(80), LocalDate.now().plusDays(20), 200),
                Arguments.of(false, 1000, 0,
                        LocalDate.now().minusDays(25), LocalDate.now().plusDays(75), 750),
                Arguments.of(false, 1000, 0,
                        LocalDate.now(), LocalDate.now().plusDays(100), 1000),
                Arguments.of(false, 1000, 0,
                        LocalDate.now().minusDays(100), LocalDate.now(), 0),
                Arguments.of(false, 1000, 100,
                        LocalDate.now().minusDays(50), LocalDate.now().plusDays(50), 550),
                Arguments.of(false, 1000, 100,
                        LocalDate.now().minusDays(80), LocalDate.now().plusDays(20), 280),
                Arguments.of(false, 1000, 100,
                        LocalDate.now().minusDays(25), LocalDate.now().plusDays(75), 775),
                Arguments.of(false, 1000, 100,
                        LocalDate.now(), LocalDate.now().plusDays(100), 1000),
                Arguments.of(false, 1000, 100,
                        LocalDate.now().minusDays(100), LocalDate.now(), 100)
        );
    }

    @ParameterizedTest(name = "Test get current asset price. Given is asset deprecated: {0}, acquisition price: {1}, " +
            "depreciation price: {2}, startDate: {3}, endDate: {4}. Should return {5}")
    @MethodSource("getCurrentAssetPriceScenarios")
    public void getCurrentAssetPriceTest(boolean isRemoved, double acquisitionPrice, double depreciationPrice,
                                         LocalDate startDate, LocalDate endDate, double expected) {
        Asset asset = Asset.builder()
                .isRemoved(isRemoved)
                .acquisitionPrice(acquisitionPrice)
                .depreciationPrice(depreciationPrice)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        assertEquals(expected, assetService.getCurrentAssetPrice(asset));
    }

    private Stream<Arguments> getAllAssetsScenarios() {
        return Stream.of(
                Arguments.of(new Account(), ErrorMessages.ACCOUNT_DOESNT_EXIST),
                Arguments.of(Account.builder().id("").build(), ErrorMessages.ACCOUNT_DOESNT_EXIST),
                Arguments.of(Account.builder()
                        .id("accId")
                        .assets(List.of(
                                Asset.builder().id("asset1").build()
                        ))
                        .build(),
                        null),
                Arguments.of(Account.builder()
                        .id("accId")
                        .assets(List.of(
                                Asset.builder().id("asset1").name(randomString(10)).build(),
                                Asset.builder().id("asset2").name(randomString(10)).build(),
                                Asset.builder().id("asset3").name(randomString(10)).build()
                        ))
                        .build(),
                        null),
                Arguments.of(Account.builder()
                        .id("accId")
                        .assets(List.of())
                        .build(),
                        null)
        );
    }

    @ParameterizedTest(name = "Test get all assets. Given account: {0}. " +
            "Should return all assets or throw exception message {1}")
    @MethodSource("getAllAssetsScenarios")
    public void getAllTest(Account account, ErrorMessages message) {
        String id = account.getId();
        List<Asset> assets = account.getAssets();

        expect(entityManager.find(Account.class, id)).andReturn(Strings.isBlank(id) ? null : account);
        replay(entityManager);

        if (message != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> assetService.getAll(id));
            assertEquals(message.name(), thrown.getMessage());
        } else {
            assertEquals(assets, assetService.getAll(id));
        }
    }
}
