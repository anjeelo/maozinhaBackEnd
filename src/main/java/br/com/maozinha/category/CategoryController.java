package br.com.maozinha.category;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(
            CategoryService categoryService
    ) {
        this.categoryService = Objects.requireNonNull(
                categoryService,
                "categoryService não pode ser nulo"
        );
    }

    @GetMapping("/api/categories")
    public ResponseEntity<List<Category>> findAll() {

        return ResponseEntity.ok(
                categoryService.findAll()
        );
    }

    @GetMapping("/api/categories/{id}")
    public ResponseEntity<Category> findById(
            @PathVariable("id") Long categoryId
    ) {

        return ResponseEntity.ok(
                categoryService.findById(
                        categoryId
                )
        );
    }
}