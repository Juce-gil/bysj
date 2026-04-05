package com.kecheng.market.wanted.service;

import com.kecheng.market.wanted.vo.WantedVo;
import java.util.List;

public interface WantedService {
    List<WantedVo> list(Long userId);

    List<WantedVo> myWanted(Long userId);

    WantedVo detail(Long id, boolean loggedIn);

    WantedVo create(Long userId, String title, String budget, String category, String campus, String deadline,
                    String intro, String description, List<String> tags, List<String> imageUrls);

    WantedVo update(Long userId, Long wantedId, String title, String budget, String category, String campus, String deadline,
                    String intro, String description, List<String> tags, List<String> imageUrls);

    WantedVo close(Long userId, Long wantedId);

    WantedVo reopen(Long userId, Long wantedId);

    void delete(Long userId, Long wantedId);
}