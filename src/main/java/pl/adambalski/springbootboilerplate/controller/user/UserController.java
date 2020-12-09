package pl.adambalski.springbootboilerplate.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.adambalski.springbootboilerplate.dto.SignUpUserDto;
import pl.adambalski.springbootboilerplate.model.User;
import pl.adambalski.springbootboilerplate.service.UserService;

import java.util.Optional;

/**
 * UserController <br><br>
 *
 * @see RestController
 * @author Adam Balski
 */
// TODO test
@RestController
public class UserController {
    UserService userService;

    @Autowired
    private void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/api/user/get-data")
    @PreAuthorize(value = "hasAnyRole('USER', 'ADMIN')")
    public User getOwnData() {
        Optional<User> userOptional = userService.getUserByLogin(getUsername());
        return userOptional.orElseThrow(IllegalStateException::new);
    }

    @DeleteMapping(value = "/api/user/delete-logged-user")
    @PreAuthorize(value = "hasAnyRole('USER', 'ADMIN')")
    public void deleteLoggedUser() {
        userService.deleteUser(getUsername());
    }

    @PutMapping(value = "/api/user/sign-up")
    @PreAuthorize(value = "permitAll()")
    public void signUp(@RequestBody SignUpUserDto signUpUserDto) {
        userService.addSignUpUserDto(signUpUserDto);
    }

    private String getUsername() {
        return getAuthentication().getName();
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
