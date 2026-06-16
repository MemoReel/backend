package com.memoreel.backend.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.memoreel.backend.common.error.BusinessException;
import com.memoreel.backend.common.error.ErrorCode;
import com.memoreel.backend.common.web.DeviceIdArgumentResolver;
import com.memoreel.backend.entity.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User stubUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1024L);
        when(user.getNickname()).thenReturn("지은");
        when(user.getCreatedAt()).thenReturn(LocalDateTime.parse("2026-06-11T14:23:00"));
        return user;
    }

    @Test
    void POST_users_신규_기기는_201로_등록된_user를_반환한다() throws Exception {
        User user = stubUser();
        when(userService.register("device-1", "지은")).thenReturn(new RegisterResult(user, true));

        mockMvc.perform(post("/users")
                        .header(DeviceIdArgumentResolver.HEADER_NAME, "device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"지은\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.user.id").value(1024))
                .andExpect(jsonPath("$.data.user.nickname").value("지은"))
                .andExpect(jsonPath("$.data.user.created_at").exists());
    }

    @Test
    void POST_users_이미_등록된_기기는_200으로_기존_user를_반환한다() throws Exception {
        User user = stubUser();
        when(userService.register("device-1", "지은")).thenReturn(new RegisterResult(user, false));

        mockMvc.perform(post("/users")
                        .header(DeviceIdArgumentResolver.HEADER_NAME, "device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"지은\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.user.id").value(1024));
    }

    @Test
    void POST_users_닉네임을_생략해도_201로_가입된다() throws Exception {
        User user = stubUser();
        when(userService.register("device-1", null)).thenReturn(new RegisterResult(user, true));

        mockMvc.perform(post("/users")
                        .header(DeviceIdArgumentResolver.HEADER_NAME, "device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.user.id").value(1024));
    }

    @Test
    void POST_users_닉네임이_규칙에_맞지_않으면_400_VALIDATION_ERROR다() throws Exception {
        mockMvc.perform(post("/users")
                        .header(DeviceIdArgumentResolver.HEADER_NAME, "device-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verify(userService, never()).register(any(), any());
    }

    @Test
    void POST_users_X_Device_Id_헤더가_없으면_401이다() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"지은\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void GET_me_등록된_기기는_200으로_본인_정보를_반환한다() throws Exception {
        User user = stubUser();
        when(userService.getByDeviceId("device-1")).thenReturn(user);

        mockMvc.perform(get("/me")
                        .header(DeviceIdArgumentResolver.HEADER_NAME, "device-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.user.nickname").value("지은"));
    }

    @Test
    void GET_me_미등록_기기는_401_UNAUTHORIZED다() throws Exception {
        when(userService.getByDeviceId("unknown"))
                .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

        mockMvc.perform(get("/me")
                        .header(DeviceIdArgumentResolver.HEADER_NAME, "unknown"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }
}
