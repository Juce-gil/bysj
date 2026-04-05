package com.kecheng.market.wanted.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateWantedRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title length must be at most 100 characters")
        String title,

        @NotBlank(message = "Budget cannot be blank")
        @Size(max = 30, message = "Budget length must be at most 30 characters")
        String budget,

        @NotBlank(message = "Category cannot be blank")
        @Size(max = 30, message = "Category length must be at most 30 characters")
        String category,

        @NotBlank(message = "Campus cannot be blank")
        @Size(max = 50, message = "Campus length must be at most 50 characters")
        String campus,

        @NotBlank(message = "Deadline cannot be blank")
        String deadline,

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