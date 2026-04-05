package com.kecheng.market.wanted.vo;

import java.util.List;

public record WantedVo(Long id, String title, String budget, String category, String campus, String publisher,
                       String deadline, String intro, String description, List<String> tags, List<String> imageUrls,
                       String coverImageUrl, String coverStyle, Boolean contactVisible, String phone, String qq, String status) {
}