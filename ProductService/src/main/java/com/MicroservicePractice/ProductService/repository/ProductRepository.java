package com.MicroservicePractice.ProductService.repository;

import com.MicroservicePractice.ProductService.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {

}
