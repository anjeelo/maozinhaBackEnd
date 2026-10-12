package br.com.maozinha.category;

import br.com.maozinha.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository
    ) {
        this.categoryRepository = Objects.requireNonNull(
                categoryRepository,
                "categoryRepository não pode ser nulo"
        );
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Category findById(
            Long categoryId
    ) {
        Objects.requireNonNull(
                categoryId,
                "categoryId não pode ser nulo"
        );

        return categoryRepository
                .findById(categoryId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Categoria não encontrada."
                        )
                );
    }
}