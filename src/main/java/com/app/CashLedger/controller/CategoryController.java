package com.app.CashLedger.controller;

import com.app.CashLedger.dto.CategoryEditDto;
import com.app.CashLedger.dto.CategoryKeywordDto;
import com.app.CashLedger.dto.CategoryKeywordEditDto;
import com.app.CashLedger.dto.MultipleCategoriesReportDto;
import com.app.CashLedger.models.Response;
import com.app.CashLedger.services.CategoryService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
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
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "orderBy", required = false) String orderBy
    ) {
        return buildResponse("category", categoryService.getCategories(userId, categoryId, name, orderBy), "Categories fetched", HttpStatus.OK);
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

    @DeleteMapping("category/keyword/{categoryId}/{keywordId}")
    public ResponseEntity<Response> deleteCategoryKeyword(@PathVariable("categoryId") Integer categoryId, @PathVariable("keywordId") Integer keywordId) {
        return buildResponse("keyword", categoryService.deleteCategoryKeyword(categoryId, keywordId), "Category keyword deleted", HttpStatus.OK);
    }

    @PostMapping("category/report")
    public ResponseEntity<byte[]> generateReportForMultipleCategories(@RequestBody MultipleCategoriesReportDto multipleCategoriesReportDto) throws JRException, ParseException {
        ByteArrayOutputStream reportStream = categoryService.generateMultipleCategoriesReport(multipleCategoriesReportDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        return new ResponseEntity<>(reportStream.toByteArray(), headers, HttpStatus.OK);
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
