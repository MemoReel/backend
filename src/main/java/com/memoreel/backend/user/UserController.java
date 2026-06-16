package com.memoreel.backend.user;

import com.memoreel.backend.common.response.ApiResponse;
import com.memoreel.backend.common.web.DeviceId;
import com.memoreel.backend.user.dto.UserData;
import com.memoreel.backend.user.dto.UserRegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 기기 최초 등록 (명세 §1-1). 신규는 201, 이미 등록된 기기는 200(멱등).
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserData>> register(
            @DeviceId String deviceId,
            @Valid @RequestBody UserRegisterRequest request) {
        RegisterResult result = userService.register(deviceId, request.nickname());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status)
                .body(ApiResponse.success(UserData.from(result.user())));
    }

    /**
     * X-Device-Id로 식별된 본인 정보 (명세 §7-1).
     */
    @GetMapping("/me")
    public ApiResponse<UserData> me(@DeviceId String deviceId) {
        return ApiResponse.success(UserData.from(userService.getByDeviceId(deviceId)));
    }
}
