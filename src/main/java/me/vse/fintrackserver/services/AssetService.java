package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.mappers.AssetMapper;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.AccountUserRights;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.repositories.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
public class AssetService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetMapper assetMapper;

    @Transactional
    public Asset add(AssetDto assetDto) {

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
                .color(assetDto.getColor())
                .acquisitionPrice(assetDto.getAcquisitionPrice())
                .startDate(assetDto.getStartDateStr())
                .endDate(assetDto.getEndDateStr())
                .color(assetDto.getColor())
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
    public void delete(String id) {
        Asset asset = entityManager.find(Asset.class, id);
        if (asset == null) {
            throw new IllegalArgumentException(ErrorMessages.INCORRECT_ASSET.name());
        }

        asset.setRemoved(true);
        asset.setRemovedAt(LocalDateTime.now());
        if (asset.getEndDate() == null) {
            asset.setEndDate(LocalDate.now());
        }
    }

}