package com.priyanshu.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.priyanshu.ecommerce.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsActiveTrue();

    // ✅ OLD: findByProductCategory(String category) — showed inactive products too
    // ✅ NEW: also filters by isActive
    List<Product> findByProductCategoryAndIsActiveTrue(String category);
}