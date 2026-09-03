package com.ecommerce.Project.service;

import com.ecommerce.Project.model.Category;
import com.ecommerce.Project.payload.CategoryDTO;
import com.ecommerce.Project.payload.CategoryResponse;



public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber , Integer pagesize , String sortBy , String sortOrder );
    CategoryDTO createCategory(CategoryDTO categoryDTO);


   CategoryDTO deleteCategory(Long categoryId);

    CategoryDTO updatecategory(CategoryDTO categoryDTO, Long categoryId);
}
