package me.vse.fintrackserver.mappers;

import me.vse.fintrackserver.FintrackServerApplication;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.model.dto.CategoryDto;
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
public class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    private Stream<Arguments> getMapperScenarios() {
        return Stream.of(
                Arguments.of(
                        CategoryDto.builder().name("food").build(),
                        Category.builder().id("catId").build(),
                        Category.builder().id("catId").name("food").build()
                ),
                Arguments.of(
                        CategoryDto.builder().build(),
                        Category.builder().id("catId").name("food").build(),
                        Category.builder().id("catId").name("food").build()
                ),
                Arguments.of(
                        CategoryDto.builder().name("groceries").build(),
                        Category.builder().id("catId").name("food").build(),
                        Category.builder().id("catId").name("groceries").build()
                ),
                Arguments.of(
                        CategoryDto.builder()
                                .name("food")
                                .color("brown")
                                .icon("bread.svg")
                                .build(),
                        Category.builder().id("catId").build(),
                        Category.builder().id("catId")
                                .name("food")
                                .color("brown")
                                .icon("bread.svg")
                                .build()
                ),
                Arguments.of(
                        CategoryDto.builder()
                                .name("groceries")
                                .color("yellow")
                                .icon("milk.svg")
                                .build(),
                    Category.builder().id("assetId")
                                .name("food")
                                .color("brown")
                                .icon("bread.svg")
                                .build(),
                        Category.builder().id("assetId")
                                .name("groceries")
                                .color("yellow")
                                .icon("milk.svg")
                                .build()
                )
        );
    }


    @ParameterizedTest(name = "Category mapper test. Given dto: {0}, oldCategory: {1}. " +
            "Should return category {2}")
    @MethodSource("getMapperScenarios")
    public void updateCategoryMapperFromDtoTest(CategoryDto dto, Category oldCategory, Category newCategory) {
        categoryMapper.updateCategoryMapperFromDto(dto, oldCategory);
        assertEquals(newCategory, oldCategory);
    }
}
