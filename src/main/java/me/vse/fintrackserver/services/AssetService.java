package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssetService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

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
                .build();

        entityManager.persist(asset);
        return asset;
    }

    public Asset update(AssetDto assetDto) {

    }

}
