package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.dto.AssetDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes = FintrackServerApplication.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AssetMapperTest {

    @Autowired
    private AssetMapper assetMapper;

    private Stream<Arguments> getMapperScenarios() {
        return Stream.of(
                Arguments.of(
                    AssetDto.builder().name("car").build(),
                    Asset.builder().id("assetId").build(),
                    Asset.builder().id("assetId").name("car").build()
                ),
                Arguments.of(
                    AssetDto.builder().build(),
                    Asset.builder().id("assetId").name("car").build(),
                    Asset.builder().id("assetId").name("car").build()
                ),
                Arguments.of(
                    AssetDto.builder().name("car").build(),
                    Asset.builder().id("assetId").name("vehicle").build(),
                    Asset.builder().id("assetId").name("car").build()
                ),
                Arguments.of(
                    AssetDto.builder()
                            .name("car")
                            .icon("car.svg")
                            .color("red")
                            .acquisitionPrice(10000.0)
                            .depreciationPrice(1000.0)
                            .startDate(LocalDate.of(2024, 8, 1))
                            .endDate(LocalDate.of(2030, 8, 1))
                            .build(),
                    Asset.builder().id("assetId").build(),
                    Asset.builder().id("assetId")
                            .name("car")
                            .icon("car.svg")
                            .color("red")
                            .acquisitionPrice(10000.0)
                            .depreciationPrice(1000.0)
                            .startDate(LocalDate.of(2024, 8, 1))
                            .endDate(LocalDate.of(2030, 8, 1))
                            .build()
                ),
                Arguments.of(
                    AssetDto.builder()
                            .name("car")
                            .icon("car.svg")
                            .color("red")
                            .acquisitionPrice(10000.0)
                            .depreciationPrice(1000.0)
                            .startDate(LocalDate.of(2024, 8, 1))
                            .endDate(LocalDate.of(2030, 8, 1))
                            .build(),
                    Asset.builder().id("assetId")
                            .name("vehicle")
                            .icon("plane.svg")
                            .color("white")
                            .acquisitionPrice(5000.0)
                            .depreciationPrice(500.0)
                            .startDate(LocalDate.of(2023, 6, 1))
                            .endDate(LocalDate.of(2029, 6, 1))
                            .build(),
                    Asset.builder().id("assetId")
                            .name("car")
                            .icon("car.svg")
                            .color("red")
                            .acquisitionPrice(10000.0)
                            .depreciationPrice(1000.0)
                            .startDate(LocalDate.of(2024, 8, 1))
                            .endDate(LocalDate.of(2030, 8, 1))
                            .build()
                )
        );
    }


    @ParameterizedTest(name = "Asset mapper test. Given dto: {0}, oldAsset: {1}. Should return asset {2}")
    @MethodSource("getMapperScenarios")
    public void updateAssetFromDtoTest(AssetDto dto, Asset oldAsset, Asset newAsset) {
        assetMapper.updateAssetFromDto(dto, oldAsset);
        assertEquals(newAsset, oldAsset);

    }
}
