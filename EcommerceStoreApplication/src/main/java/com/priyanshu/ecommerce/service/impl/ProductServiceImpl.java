package com.priyanshu.ecommerce.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.priyanshu.ecommerce.entity.Product;
import com.priyanshu.ecommerce.repository.ProductRepository;
import com.priyanshu.ecommerce.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j  // ✅ proper logging instead of System.out.println
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Boolean deleteProduct(long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            productRepository.deleteById(product.get().getId());
            return true;
        }
        return false;
    }

    // ✅ FIXED: was always returning Optional.empty()
    @Override
    public Optional<Product> findById(long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product getProductById(long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product updateProductById(Product product, MultipartFile file) {

        Product oldProduct = productRepository.findById(product.getId()).orElse(null);

        if (oldProduct == null) {
            log.warn("Product not found for update, id: {}", product.getId());
            return null;
        }

        // update text fields
        oldProduct.setProductTitle(product.getProductTitle());
        oldProduct.setProductDescription(product.getProductDescription());
        oldProduct.setProductCategory(product.getProductCategory());
        oldProduct.setProductStock(product.getProductStock());
        oldProduct.setIsActive(product.getIsActive());
        oldProduct.setDiscount(product.getDiscount());

        // update price and discount
        if (product.getProductPrice() != null) {
            oldProduct.setProductPrice(product.getProductPrice());

            double price = product.getProductPrice();
            double discount = product.getDiscount();
            double discountPrice = price - (price * (discount / 100.0));
            oldProduct.setDiscountPrice(discountPrice);
        } else {
            oldProduct.setProductPrice(0.0);
            oldProduct.setDiscountPrice(0.0);
        }

        // ✅ FIXED: image is now actually saved to disk, not just the name in DB
        if (file != null && !file.isEmpty()) {
            try {
                // ✅ sanitize filename using UUID to prevent path traversal attacks
                String originalFilename = file.getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String safeFilename = UUID.randomUUID().toString() + extension;

                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(
                        saveFile.getAbsolutePath()
                                + File.separator
                                + "product_image"
                                + File.separator
                                + safeFilename
                );

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                log.info("Product image saved at: {}", path);

                oldProduct.setProductImage(safeFilename); // ✅ save the safe name, not original
            } catch (Exception e) {
                log.error("Failed to save product image: {}", e.getMessage());
            }
        }
        // if no new file uploaded, keep the existing image — no change needed

        return productRepository.save(oldProduct);
    }

    @Override
    public List<Product> findAllActiveProducts(String category) {
        if (ObjectUtils.isEmpty(category)) {
            return productRepository.findByIsActiveTrue();
        }
        // ✅ FIXED: now filters by isActive too, inactive products won't show in category view
        return productRepository.findByProductCategoryAndIsActiveTrue(category);
    }
}