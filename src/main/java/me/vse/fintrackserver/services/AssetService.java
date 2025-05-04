package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Setter;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.mappers.AssetMapper;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.repositories.AssetRepository;
import me.vse.fintrackserver.rest.requests.AssetAddRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.function.Predicate.not;

@Service
@AllArgsConstructor
public class AssetService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetMapper assetMapper;

    @Transactional
    public List<Asset> getAllByAccount(String accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }
        Account account = entityManager.find(Account.class, accountId);
        if (account == null) {
            throw new IllegalArgumentException(ErrorMessages.ACCOUNT_DOESNT_EXIST.name());
        }
        return account.getAssets();
    }

    @Transactional
    public List<Asset> getAll(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }
        User user = entityManager.find(User.class, userId);
        return user.getAccountUserRights()
                .stream()
                .filter(AccountUserRights::isOwner)
                .map(AccountUserRights::getAccount)
                .map(Account::getAssets)
                .flatMap(List::stream)
                .filter(asset -> asset.getEndDate().isAfter(LocalDate.now()))
                .filter(asset -> !asset.isRemoved())
                .toList();
    }

    @Transactional
    public Asset add(AssetAddRequest assetDto) {

        if (assetDto.getName() == null) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_ASSET.name());
        }

        Account relatedAcc = entityManager.find(Account.class, assetDto.getAccountId());

        if (relatedAcc == null) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_ASSET.name());
        }

        Asset asset = Asset.builder()
                .account(relatedAcc)
                .name(assetDto.getName())
                .acquisitionPrice(assetDto.getAcquisitionPrice())
                .depreciationPrice(assetDto.getDepreciationPrice())
                .startDate(assetDto.getStartDateStr())
                .endDate(assetDto.getEndDateStr())
                .icon(assetDto.getIcon())
                .build();

        entityManager.persist(asset);
        return asset;
    }

    @Transactional
    public Asset update(AssetDto assetDto) throws AuthenticationException {
        User user = entityManager.find(User.class, assetDto.getSenderId());
        if (user == null) {
            throw new AuthenticationException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        Asset asset = entityManager.find(Asset.class, assetDto.getId());

        if (asset == null) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_ASSET.name());
        }

        boolean doesUserHaveRights = user.getAccountUserRights().stream()
                .map(AccountUserRights::getAccount)
                .filter(not(Account::isRemoved))
                .map(Account::getAssets)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(not(Asset::isRemoved))
                .anyMatch(currentAsset -> currentAsset.equals(asset));

        if (!doesUserHaveRights) {
            throw new AuthenticationException(ErrorMessages.USER_DOESNT_HAVE_RIGHTS.name());
        }


        assetMapper.updateAssetFromDto(assetDto, asset);
        assetRepository.save(asset);
        return asset;
    }

    @Transactional
    public void delete(String id, String userId) throws AuthenticationException {
        Asset asset = entityManager.find(Asset.class, id);
        if (asset == null) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_ASSET.name());
        }

        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new AuthenticationException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        boolean doesUserHaveRights = user.getAccountUserRights().stream()
                .map(AccountUserRights::getAccount)
                .filter(not(Account::isRemoved))
                .map(Account::getAssets)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .filter(not(Asset::isRemoved))
                .anyMatch(currentAsset -> currentAsset.equals(asset));

        if (!doesUserHaveRights) {
            throw new AuthenticationException(ErrorMessages.USER_DOESNT_HAVE_RIGHTS.name());
        }

        asset.setRemoved(true);
        asset.setRemovedAt(LocalDateTime.now());
        if (asset.getEndDate() == null) {
            asset.setEndDate(LocalDate.now());
        }
        assetRepository.save(asset);
    }

    public Double getCurrentAssetPrice(Asset asset) {
        if (asset.isRemoved()) return 0.0;

        double acquisitionPrice = asset.getAcquisitionPrice();
        double depreciationPrice = asset.getDepreciationPrice();

        double assetUsageFullPrice = acquisitionPrice - depreciationPrice;
        LocalDate now = LocalDate.now();

        boolean isAssetDepreciatedByDate = ChronoUnit.DAYS.between(asset.getStartDate(), now) < 1;
        if (isAssetDepreciatedByDate) return depreciationPrice;

        long totalDaysOfUsage = ChronoUnit.DAYS.between(asset.getStartDate(), asset.getEndDate());
        if (totalDaysOfUsage == 0) return acquisitionPrice;

        double pricePerDateOfUsage = assetUsageFullPrice / totalDaysOfUsage;

        long daysBetweenStartAndNow = Duration.ofDays(DAYS.between(asset.getStartDate(), now)).toDays();
        return acquisitionPrice - (pricePerDateOfUsage * daysBetweenStartAndNow);
    }

}
