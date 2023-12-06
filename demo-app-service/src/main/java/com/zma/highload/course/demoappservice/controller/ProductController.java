package com.zma.highload.course.demoappservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zma.highload.course.demoappservice.model.Product;
import com.zma.highload.course.demoappservice.repository.CacheClientWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CacheClientWrapper cacheClient;

    public ProductController(CacheClientWrapper cacheClient) {
        this.cacheClient = cacheClient;
    }

    @GetMapping("/{id}")
    public Product getId(@PathVariable String id) throws JsonProcessingException {
        Product product = cacheClient.fetchWithProbabilisticEarlyExpiration(id);

        return product;
    }

    @GetMapping("/putTestKeys")
    public void getAll(@RequestParam int quantity) throws JsonProcessingException {
        Product testProduct = new Product("test", "long test category");
        for (int i = 0; i < quantity; i++) {
            cacheClient.saveProduct(String.valueOf(i), testProduct);
        }
    }
}
