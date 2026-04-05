package com.kecheng.market.comment.service;

import com.kecheng.market.comment.vo.CommentVo;
import java.util.List;

public interface CommentService {
    List<CommentVo> list(Long goodsId);
    CommentVo add(Long userId, Long goodsId, String content);
}
