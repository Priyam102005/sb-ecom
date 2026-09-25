package com.ecommerce.Project.service;

import com.ecommerce.Project.exceptions.APIException;
import com.ecommerce.Project.exceptions.ResourceNotFoundException;
import com.ecommerce.Project.model.Category;
import com.ecommerce.Project.model.Product;
import com.ecommerce.Project.payload.ProductDTO;
import com.ecommerce.Project.payload.ProductResponse;
import com.ecommerce.Project.repositories.CategoryRepository;
import com.ecommerce.Project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        // CHECK IF PRODUCT ALREADY PRESENT OR NOT
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductNotPresent = true ;

        List<Product> products = category.getProducts();
        for(int i = 0 ; i < products.size() ; i++ ){
            if ( products.get(i).getProductName().equals(productDTO.getProductName())){
                isProductNotPresent = false;
                break ;
            }

        }

        if(isProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository
                    .save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }
        else {
            throw new APIException("Product already Exist !!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber  , pageSize , sortByAndOrder );
        Page<Product> pageProducts = productRepository.findAll(pageDetails);
        //PRODUCTS SIZE IS 0
        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if(products.isEmpty()){
            throw new APIException("No Products Exist !!");
        }


        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElement(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber  , pageSize , sortByAndOrder );
        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category ,pageDetails);
        //PRODUCTS SIZE IS 0
        List<Product> products = pageProducts.getContent();

        if(products.isEmpty() ){
            throw new APIException(category.getCategoryName() + " categor does not have any products ");
        }


        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();


        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElement(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());


        return productResponse;


    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber  , pageSize , sortByAndOrder );
        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);



        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if(products.isEmpty() ){
            throw new APIException("products not found with keyword: " + keyword);
        }


        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        return productResponse;


    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {

        //GET THE PRODUCT FROM DATABASE
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        Product product = modelMapper.map(productDTO, Product.class);


        // UPDATE THE PRODUCT INFO WITH ONE IN REQUEST BODY
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        //SAVE TO THE DATABASE

        Product savedProduct = productRepository.save(productFromDb);

        return modelMapper.map(savedProduct, ProductDTO.class);

    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId ", productId));

        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        //GET THE PRODUCT FROM DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //UPLOAD THR IMAGE TO SERVER
        //GET THE FILE NAME

        String fileName = fileService.uploadImage(path, image);


        //UPDATING THE FILE NAME TO THE PRODUCT
        productFromDb.setImage(fileName);

        //SAVE THE UPDATED PRODUCT

        Product updatedProduct = productRepository.save(productFromDb);

        //RETURN DTO AFTER MAPPING PRODUCT TO DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

}


