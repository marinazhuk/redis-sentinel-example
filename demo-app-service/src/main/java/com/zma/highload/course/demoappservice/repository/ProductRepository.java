package com.zma.highload.course.demoappservice.repository;

import com.zma.highload.course.demoappservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {




}
