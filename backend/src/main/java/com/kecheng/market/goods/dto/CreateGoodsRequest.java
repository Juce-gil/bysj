package com.kecheng.market.goods.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record CreateGoodsRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title length must be at most 100 characters")
        String title,

        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @DecimalMin(value = "0.00", message = "Original price must be greater than or equal to 0")
        BigDecimal originalPrice,

        @NotBlank(message = "Category cannot be blank")
        @Size(max = 30, message = "Category length must be at most 30 characters")
        String category,

        @NotBlank(message = "Campus cannot be blank")
        @Size(max = 50, message = "Campus length must be at most 50 characters")
        String campus,

        @NotBlank(message = "Condition cannot be blank")
        @Size(max = 30, message = "Condition length must be at most 30 characters")
        String condition,

        @NotBlank(message = "Intro cannot be blank")
        @Size(max = 120, message = "Intro length must be at most 120 characters")
        String intro,

        @NotBlank(message = "Description cannot be blank")
        @Size(max = 1000, message = "Description length must be at most 1000 characters")
        String description,

        @Size(max = 8, message = "Tags size must be at most 8")
        List<String> tags,

        @Size(max = 6, message = "Image count must be at most 6")
        List<String> imageUrls
) {
}