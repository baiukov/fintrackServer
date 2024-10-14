package me.vse.fintrackserver.repositories;

import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, String> {
}
