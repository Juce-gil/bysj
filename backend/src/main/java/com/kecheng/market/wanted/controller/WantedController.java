package com.kecheng.market.wanted.controller;

import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.AnonymousAccess;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import com.kecheng.market.wanted.dto.CreateWantedRequest;
import com.kecheng.market.wanted.service.WantedService;
import com.kecheng.market.wanted.vo.WantedVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wanted")
@RestController
@RequestMapping("/api/wanted")
@AnonymousAccess
public class WantedController {

    private final WantedService wantedService;

    public WantedController(WantedService wantedService) {
        this.wantedService = wantedService;
    }

    @Operation(summary = "Wanted list")
    @GetMapping
    public ApiResponse<List<WantedVo>> list() {
        return ApiResponse.success(wantedService.list(UserContext.getUserId()));
    }

    @Operation(summary = "Wanted detail")
    @GetMapping("/{id}")
    public ApiResponse<WantedVo> detail(@PathVariable Long id) {
        return ApiResponse.success(wantedService.detail(id, UserContext.getUserId() != null));
    }

    @Operation(summary = "Create wanted post")
    @PostMapping
    @RequireLogin
    public ApiResponse<WantedVo> create(@Valid @RequestBody CreateWantedRequest request) {
        return ApiResponse.success(wantedService.create(
                UserContext.getUserId(),
                request.title(),
                request.budget(),
                request.category(),
                request.campus(),
                request.deadline(),
                request.intro(),
                request.description(),
                request.tags(),
                request.imageUrls()
        ));
    }

    @Operation(summary = "Update wanted post")
    @PutMapping("/{id}")
    @RequireLogin
    public ApiResponse<WantedVo> update(@PathVariable Long id, @Valid @RequestBody CreateWantedRequest request) {
        return ApiResponse.success(wantedService.update(
                UserContext.getUserId(),
                id,
                request.title(),
                request.budget(),
                request.category(),
                request.campus(),
                request.deadline(),
                request.intro(),
                request.description(),
                request.tags(),
                request.imageUrls()
        ));
    }

    @Operation(summary = "Close wanted post")
    @PutMapping("/{id}/close")
    @RequireLogin
    public ApiResponse<WantedVo> close(@PathVariable Long id) {
        return ApiResponse.success(wantedService.close(UserContext.getUserId(), id));
    }

    @Operation(summary = "Reopen wanted post")
    @PutMapping("/{id}/reopen")
    @RequireLogin
    public ApiResponse<WantedVo> reopen(@PathVariable Long id) {
        return ApiResponse.success(wantedService.reopen(UserContext.getUserId(), id));
    }

    @Operation(summary = "Delete wanted post")
    @DeleteMapping("/{id}")
    @RequireLogin
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        wantedService.delete(UserContext.getUserId(), id);
        return ApiResponse.success(true);
    }
}