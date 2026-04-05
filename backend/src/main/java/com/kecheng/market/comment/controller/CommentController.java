package com.kecheng.market.comment.controller;

import com.kecheng.market.comment.dto.CreateCommentRequest;
import com.kecheng.market.comment.service.CommentService;
import com.kecheng.market.comment.vo.CommentVo;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.AnonymousAccess;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论模块")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "商品评论列表")
    @GetMapping
    @AnonymousAccess
    public ApiResponse<List<CommentVo>> list(@RequestParam Long goodsId) {
        return ApiResponse.success(commentService.list(goodsId));
    }

    @Operation(summary = "新增评论")
    @PostMapping
    @RequireLogin
    public ApiResponse<CommentVo> add(@Valid @RequestBody CreateCommentRequest request) {
        return ApiResponse.success("评论成功", commentService.add(UserContext.getUserId(), request.goodsId(), request.content()));
    }
}
