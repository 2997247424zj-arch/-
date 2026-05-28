package com.example.ecommerceback.user.service;

import com.example.ecommerceback.user.entity.Address;
import com.example.ecommerceback.user.entity.User;
import com.example.ecommerceback.user.mapper.AddressMapper;
import com.example.ecommerceback.user.mapper.UserMapper;
import com.example.ecommerceback.utils.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private UserService userService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("123456");
        user.setEmail("test@example.com");

        when(userMapper.selectByUsername("testuser")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        Result<User> result = userService.register(user);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNull(result.getData().getPassword());
        assertNotEquals("123456", user.getPassword());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    void testRegister_UsernameExists() {
        User user = new User();
        user.setUsername("existinguser");
        user.setPassword("123456");

        when(userMapper.selectByUsername("existinguser")).thenReturn(new User());

        Result<User> result = userService.register(user);

        assertEquals(400, result.getCode());
    }

    @Test
    void testLogin_SuccessWithLegacyPlainPassword() {
        String username = "testuser";
        String password = "123456";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(username);
        mockUser.setPassword(password);

        when(userMapper.selectByUsername(username)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        Result<User> result = userService.login(username, password);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNull(result.getData().getPassword());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testLogin_WrongPassword() {
        String username = "testuser";
        String password = "wrongpassword";

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername(username);
        mockUser.setPassword(passwordEncoder.encode("123456"));

        when(userMapper.selectByUsername(username)).thenReturn(mockUser);

        Result<User> result = userService.login(username, password);

        assertEquals(401, result.getCode());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void testLogin_UserNotFound() {
        when(userMapper.selectByUsername("nonexistent")).thenReturn(null);

        Result<User> result = userService.login("nonexistent", "123456");

        assertEquals(401, result.getCode());
    }

    @Test
    void testChangePassword_SuccessWithBcryptPassword() {
        User user = new User();
        user.setId(1L);
        user.setPassword(passwordEncoder.encode("123456"));

        when(userMapper.selectById(1L)).thenReturn(user);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        Result<String> result = userService.changePassword(1L, "123456", "654321");

        assertEquals(200, result.getCode());
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    void testSetDefaultAddress_Success() {
        Address address = new Address();
        address.setId(10L);
        address.setUserId(1L);
        address.setIsDefault(0);

        when(addressMapper.selectById(10L)).thenReturn(address);
        when(addressMapper.updateById(any(Address.class))).thenReturn(1);

        Result<String> result = userService.setDefaultAddress(1L, 10L);

        assertEquals(200, result.getCode());
        verify(addressMapper, times(1)).updateDefaultByUserId(1L, 0);
        verify(addressMapper, times(1)).updateById(any(Address.class));
    }
}
