package com.app.CashLedger.controller;

import com.app.CashLedger.dto.CategoryEditDto;
import com.app.CashLedger.dto.CategoryKeywordDto;
import com.app.CashLedger.dto.CategoryKeywordEditDto;
import com.app.CashLedger.models.Response;
import com.app.CashLedger.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static java.util.Map.of;

@RestController
@RequestMapping("/api/")
public class CategoryController {
    private final CategoryService categoryService;
    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @PostMapping("category/{id}")
    public ResponseEntity<Response> createCategory(@RequestBody CategoryEditDto category, @PathVariable("id") Integer userId) {
        return buildResponse("category", categoryService.createCategory(category, userId), "Category created", HttpStatus.CREATED);
    }

    @PutMapping("category/{id}")
    public ResponseEntity<Response> addKeywordsToCategory(@RequestBody CategoryEditDto category, @PathVariable("id") Integer categoryId) {
        return buildResponse("category", categoryService.addKeywordsToCategory(category, categoryId), "Category updated", HttpStatus.CREATED);
    }

    @DeleteMapping("category/{id}")
    public ResponseEntity<Response> deleteCategory(@PathVariable("id") Integer categoryId) {
        return buildResponse("category", categoryService.deleteCategory(categoryId), "Category deleted", HttpStatus.OK);
    }

    @GetMapping("category/{userId}")
    public ResponseEntity<Response> getUserCategories(
            @PathVariable("userId") Integer userId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "orderBy", required = false) String orderBy
    ) {
        return buildResponse("category", categoryService.getCategories(userId, name, orderBy), "Categories fetched", HttpStatus.OK);
    }
    @PutMapping("category/name/{id}")
    public ResponseEntity<Response> updateCategoryName(@RequestBody CategoryEditDto category, @PathVariable("id") Integer categoryId) {
        return buildResponse("category", categoryService.updateCategoryName(category, categoryId), "Category updated", HttpStatus.OK);
    }

    @GetMapping("category/details/{categoryId}")
    public ResponseEntity<Response> getCategory( @PathVariable("categoryId") Integer categoryId) {
        return buildResponse("category", categoryService.getCategory(categoryId), "Category fetched", HttpStatus.OK);
    }


    @PutMapping("category/keyword")
    public ResponseEntity<Response> updateCategoryKeyword(@RequestBody CategoryKeywordDto category) {
        return buildResponse("keyword", categoryService.updateCategoryKeyword(category), "Category keyword updated", HttpStatus.OK);
    }

    @DeleteMapping("category/keyword")
    public ResponseEntity<Response> deleteCategoryKeyword(@RequestBody CategoryKeywordEditDto keywordDetails) {
        return buildResponse("keyword", categoryService.deleteCategoryKeyword(keywordDetails), "Category keyword deleted", HttpStatus.OK);
    }

    private ResponseEntity<Response> buildResponse(String desc, Object data, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Response.builder()
                        .timestamp(LocalDateTime.now())
                        .data(data == null ? null : of(desc, data))
                        .message(message)
                        .status(status)
                        .statusCode(status.value())
                        .build());
    }
}
