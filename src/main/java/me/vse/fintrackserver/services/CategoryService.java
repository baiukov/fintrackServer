package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.mappers.CategoryMapper;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.dto.CategoryDto;
import me.vse.fintrackserver.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public Category add(CategoryDto categoryDto) {
        Category category = Category.builder()
                .name(categoryDto.getName())
                .color(categoryDto.getColor())
                .icon(categoryDto.getIcon())
                .build();
        entityManager.persist(category);
        return category;
    }

    @Transactional
    public Category update(CategoryDto categoryDto) {
        String id = categoryDto.getId();
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }
        Category category = entityManager.find(Category.class, id);
        if (category == null) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }
        categoryMapper.updateCategoryMapperFromDto(categoryDto, category);
        entityManager.persist(category);
        return category;
    }

    @Transactional
    public void delete(String id) {
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }
        Category category = entityManager.find(Category.class, id);
        if (category == null) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }
        categoryRepository.delete(category);
    }
}