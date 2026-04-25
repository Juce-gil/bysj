package com.kecheng.market.auth.service.impl;

import com.kecheng.market.auth.dto.LoginRequest;
import com.kecheng.market.auth.dto.RegisterRequest;
import com.kecheng.market.common.exception.BusinessException;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.security.util.JwtUtil;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private MarketStore marketStore;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    void registerRejectsMismatchedPasswordsBeforeCallingStore() {
        RegisterRequest request = new RegisterRequest("tester", "Tester", "secret123", "different123");

        assertThatThrownBy(() -> authService().register(request))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(marketStore);
    }

    @Test
    void loginRejectsWrongPasswordBeforeGeneratingToken() {
        when(marketStore.findUserByUsernameOrStudentNo("tester")).thenReturn(new MarketStore.UserData(
                99L,
                "tester",
                "correct-password",
                "Tester",
                "user",
                "School",
                "Slogan",
                "20239999",
                "13900000001",
                "",
                false,
                false,
                LocalDateTime.now()
        ));

        assertThatThrownBy(() -> authService().login(new LoginRequest("tester", "wrong-password")))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(jwtUtil);
    }

    private AuthServiceImpl authService() {
        @SuppressWarnings("unchecked")
        ObjectProvider<MarketStore> marketStoreProvider = mock(ObjectProvider.class);
        @SuppressWarnings("unchecked")
        ObjectProvider<MarketPersistenceService> persistenceProvider = mock(ObjectProvider.class);
        when(marketStoreProvider.getIfAvailable()).thenReturn(marketStore);
        return new AuthServiceImpl("memory", marketStoreProvider, persistenceProvider, jwtUtil);
    }
}
