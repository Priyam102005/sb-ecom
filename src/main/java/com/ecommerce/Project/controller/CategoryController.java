package com.ecommerce.Project.controller;

import com.ecommerce.Project.model.Category;
import com.ecommerce.Project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;


    //@GetMapping("/api/public/categories")
    @RequestMapping(value = "/api/public/categories" , method = RequestMethod.GET)
    public ResponseEntity<List<Category>> getAllCategories(){
        List<Category> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories , HttpStatus.OK);

    }

    @PostMapping("/api/public/categories")
    public ResponseEntity<String> createCategory(@RequestBody Category category){
        categoryService.createCategory(category);
        return new ResponseEntity<>("Category added successfully" ,  HttpStatus.CREATED);
    }

    @DeleteMapping("/api/admin/categories/{categoryId}")
    public ResponseEntity deleteCategory (@PathVariable Long categoryId){
        try {
            String status = categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>(status , HttpStatus.OK);
        } catch (ResponseStatusException e){
            return new ResponseEntity<>(e.getReason() , e.getStatusCode());
        }
    }
    @PutMapping("/api/public/categories/{categoryId}")
    public ResponseEntity<String> updateCategory(@RequestBody Category category ,
                                                 @PathVariable Long categoryId){
        try{
            Category savedCategory = categoryService.updatecategory(category , categoryId);
            return new ResponseEntity<>("Category with category id : " + categoryId , HttpStatus.OK);

        } catch (ResponseStatusException e){
            return new  ResponseEntity<>(e.getReason() , e.getStatusCode());
        }
    }

}
