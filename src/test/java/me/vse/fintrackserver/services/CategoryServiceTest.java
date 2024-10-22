package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.mappers.AssetMapper;
import me.vse.fintrackserver.mappers.CategoryMapper;
import me.vse.fintrackserver.model.*;
import me.vse.fintrackserver.model.dto.AssetDto;
import me.vse.fintrackserver.model.dto.CategoryDto;
import me.vse.fintrackserver.repositories.AssetRepository;
import me.vse.fintrackserver.repositories.CategoryRepository;
import org.apache.logging.log4j.util.Strings;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
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
public class CategoryServiceTest {

    private EntityManager entityManager;
    private CategoryService categoryService;
    private CategoryRepository categoryRepository;
    private CategoryMapper categoryMapper;


    @BeforeEach
    public void setup() {
        entityManager = EasyMock.mock(EntityManager.class);
        categoryRepository = EasyMock.mock(CategoryRepository.class);
        categoryMapper = EasyMock.mock(CategoryMapper.class);
        categoryService = new CategoryService(entityManager, categoryMapper, categoryRepository);
    }

    @ParameterizedTest(name = "Test category add. Given category name: {0}, color: {1}, icon: {2}. " +
            "Should save category")
    @CsvSource(value = {
            "food | brown | food.svg",
            "car | red | auto.svg",
            "sport | white | ball.svg",
            "forTheFamily | orange | ",
            "health | ping | aidkit.svg",
            "hygiene |  | shampoo.png",
    }, delimiter = '|')
    public void addTest(String name, String color, String icon) {
        CategoryDto categoryDto = CategoryDto.builder()
                .color(color)
                .name(name)
                .icon(icon)
                .build();

        Category expectedCategory = Category.builder()
                .name(name)
                .color(color)
                .icon(icon)
                .build();
        entityManager.persist(expectedCategory);
        replay(entityManager);

        assertEquals(expectedCategory, categoryService.add(categoryDto));
        verify(entityManager);
    }

    private Stream<Arguments> getUpdateScenarios() {
        return Stream.of(
                Arguments.of(new Category(), ErrorMessages.CATEGORY_DOESNT_EXIST),
                Arguments.of(Category.builder().id("").build(), ErrorMessages.CATEGORY_DOESNT_EXIST),
                Arguments.of(Category.builder().id("categoryId").build(), null),
                Arguments.of(Category.builder()
                                .id("categoryId")
                                .name("newCategoryName")
                                .build(),
                null),
                Arguments.of(Category.builder()
                                .id("categoryId")
                                .name("newCategoryName")
                                .color("newColor")
                                .icon("otherIcon")
                                .build(),
                null)
        );
    }

    @ParameterizedTest(name = "Test category update. Given expected category: {0}. " +
            "Should return expected category or throw exception {1}")
    @MethodSource("getUpdateScenarios")
    public void updateTest(Category expected, ErrorMessages exceptionMessage) {
        CategoryDto categoryDto = CategoryDto.builder()
                .id(expected.getId())
                .icon(expected.getIcon())
                .name(expected.getName())
                .color(expected.getColor())
                .build();

        expect(entityManager.find(Category.class, categoryDto.getId()))
                .andReturn(Strings.isBlank(expected.getId()) ? null : expected);
        categoryMapper.updateCategoryMapperFromDto(categoryDto, expected);
        entityManager.persist(expected);
        replay(entityManager, categoryMapper);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> categoryService.update(categoryDto));
            assertEquals(exceptionMessage.name(), thrown.getMessage());
        } else {
            assertEquals(expected, categoryService.update(categoryDto));
        }
    }

    @ParameterizedTest(name = "Test asset delete. Given old asset {0}, expected asset: {1}. " +
            "Should return {1} or exception {2}")
    @MethodSource("getUpdateScenarios")
    public void deleteTest(Category expected, ErrorMessages exceptionMessage) {
        expect(entityManager.find(Category.class, expected.getId()))
                .andReturn(Strings.isBlank(expected.getId()) ? null : expected);
        categoryRepository.delete(expected);
        replay(entityManager, categoryRepository);

        if (exceptionMessage != null) {
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> categoryService.delete(expected.getId()));
            assertEquals(exceptionMessage.name(), thrown.getMessage());
        } else {
            categoryService.delete(expected.getId());
            verify(entityManager, categoryRepository);
        }
    }

    @Test
    public void getAllTest() {
        // todo user categories
    }
}
