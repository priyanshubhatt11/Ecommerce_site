package com.priyanshu.ecommerce.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.priyanshu.ecommerce.entity.Category;
import com.priyanshu.ecommerce.service.CategoryService;

@RestController
@RequestMapping("/api")
public class AdminRestController {

    @Autowired
    CategoryService categoryService;

    // ✅ FIXED: returns JSON response instead of broken redirect string
    @PostMapping("/save-category")
    public ResponseEntity<?> saveCategory(@RequestBody Category category) {

        Category saved = categoryService.saveCategory(category);

        if (saved != null) {
            return ResponseEntity.ok("Category saved successfully");
        } else {
            return ResponseEntity.internalServerError().body("Failed to save category");
        }
    }
}