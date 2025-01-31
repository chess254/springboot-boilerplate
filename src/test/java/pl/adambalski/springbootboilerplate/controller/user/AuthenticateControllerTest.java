package pl.adambalski.springbootboilerplate.controller.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.adambalski.springbootboilerplate.repository.AdminJpaRepository;
import pl.adambalski.springbootboilerplate.security.PasswordEncoderFactory;
import pl.adambalski.springbootboilerplate.security.SecurityConfiguration;
import pl.adambalski.springbootboilerplate.security.util.JwtUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@WebMvcTest
public class AuthenticateControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    ApplicationContext applicationContext;

    // ApplicationContext wants an AdminJpaRepository bean,
    // but jpa can't instantiate it with @WebMvcTest, so we
    // have to create a mock bean.
    @MockBean
    AdminJpaRepository adminJpaRepository;

    JwtUtil jwtUtil;

    @BeforeEach
    void init() {
        this.jwtUtil = new JwtUtil(SecurityConfiguration.KEY);
        mockUserDetailsService();
    }

    private void mockUserDetailsService() {
        String encodedPassword = new PasswordEncoderFactory().passwordEncoderBean().encode("password");

        UserDetails userDetails = new User("username", encodedPassword, List.of());
        UserDetailsService userDetailsService = new MockUserDetailsService(userDetails);

        AuthenticationController authenticationController = applicationContext.getBean(AuthenticationController.class);
        ReflectionTestUtils.setField(authenticationController, "userDetailsService", userDetailsService);
    }

    void testByRequestBody(String requestBody, ResultMatcher resultMatcher) {
        var requestBuilder = MockMvcRequestBuilders
                .post("/api/user/authenticate")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("utf-8");

        try {
            mvc.perform(requestBuilder)
                    .andExpect(resultMatcher)
                    .andDo(MockMvcResultHandlers.print());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testAuthenticateEndpointWithCorrectUser() {
        String requestBody = """
                        {
                            "username":"username",
                            "password":"password"
                        }
                """;
        testByRequestBody(requestBody, MockMvcResultMatchers.status().isOk());
    }

    @Test
    void testAuthenticateEndpointWithIncorrectUser() {
        String requestBody = """
                        {
                            "username":"incorrect",
                            "password":"password"
                        }
                """;
        testByRequestBody(requestBody, MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testAuthenticateEndpointWithCorrectUsernameButIncorrectPassword() {
        String requestBody = """
                        {
                            "username":"username",
                            "password":"incorrect"
                        }
                """;
        testByRequestBody(requestBody, MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void testAuthenticateEndpointWithoutUser() {
        String requestBody = "";
        testByRequestBody(requestBody, MockMvcResultMatchers.status().is(400));
    }

    @Test
    void testAuthenticateEndpointOnlyWithUsername() {
        String requestBody = """
                        {
                            "username":"invalid",
                        }
                """;
        testByRequestBody(requestBody, MockMvcResultMatchers.status().is(400));
    }

    @Test
    void testAuthenticateEndpointOnlyWithPassword() {
        String requestBody = """
                        {
                            "password":"password"
                        }
                """;
        testByRequestBody(requestBody, MockMvcResultMatchers.status().is(400));
    }

    // RequestBody: '{}'
    @Test
    void testAuthenticateEndpointWithEmptyJSON() {
        String requestBody = "{}";
        testByRequestBody(requestBody, MockMvcResultMatchers.status().is(400));
    }

    private static record MockUserDetailsService(UserDetails userDetails) implements UserDetailsService {

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            if ("username".equals(username)) {
                return userDetails;
            }
            throw new UsernameNotFoundException("UsernameNotFoundException");
        }
    }
}
