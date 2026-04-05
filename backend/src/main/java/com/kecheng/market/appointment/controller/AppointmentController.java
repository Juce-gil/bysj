package com.kecheng.market.appointment.controller;

import com.kecheng.market.appointment.dto.CreateAppointmentRequest;
import com.kecheng.market.appointment.service.AppointmentService;
import com.kecheng.market.appointment.vo.AppointmentVo;
import com.kecheng.market.common.ApiResponse;
import com.kecheng.market.security.annotation.RequireLogin;
import com.kecheng.market.security.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "预约模块")
@RestController
@RequestMapping("/api/appointments")
@RequireLogin
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "我的预约列表")
    @GetMapping("/my")
    public ApiResponse<List<AppointmentVo>> myList() {
        return ApiResponse.success(appointmentService.myList(UserContext.getUserId()));
    }

    @Operation(summary = "提交预约")
    @PostMapping
    public ApiResponse<AppointmentVo> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ApiResponse.success("预约已提交",
                appointmentService.create(UserContext.getUserId(), request.goodsId(), request.intendedTime(), request.intendedLocation(), request.remark()));
    }

    @Operation(summary = "取消预约")
    @PutMapping("/{id}/cancel")
    public ApiResponse<AppointmentVo> cancel(@PathVariable Long id) {
        return ApiResponse.success("预约已取消", appointmentService.cancel(UserContext.getUserId(), id));
    }
}
