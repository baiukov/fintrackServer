package me.vse.fintrackserver.services;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import me.vse.fintrackserver.enums.ErrorMessages;
import me.vse.fintrackserver.mappers.CategoryMapper;
import me.vse.fintrackserver.model.Account;
import me.vse.fintrackserver.model.Asset;
import me.vse.fintrackserver.model.Category;
import me.vse.fintrackserver.model.User;
import me.vse.fintrackserver.model.dto.CategoryDto;
import me.vse.fintrackserver.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public List<Category> getAll(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        return user.getCategories();
    }

    @Transactional
    public Category add(CategoryDto categoryDto) {
        User user = entityManager.find(User.class, categoryDto.getUserId());
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        Category category = Category.builder()
                .name(categoryDto.getName())
                .icon(categoryDto.getIcon())
                .user(user)
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

        User user = entityManager.find(User.class, categoryDto.getUserId());
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        if (!user.getCategories().contains(category)) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }

        categoryMapper.updateCategoryMapperFromDto(categoryDto, category);
        categoryRepository.save(category);
        return category;
    }

    @Transactional
    public void delete(String id, String userId) {
        if (id == null) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }
        Category category = entityManager.find(Category.class, id);
        if (category == null) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }

        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new IllegalArgumentException(ErrorMessages.USER_DOESNT_EXIST.name());
        }

        if (!user.getCategories().contains(category)) {
            throw new IllegalArgumentException(ErrorMessages.CATEGORY_DOESNT_EXIST.name());
        }

        categoryRepository.delete(category);
    }
}
