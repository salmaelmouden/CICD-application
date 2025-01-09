package controller;

import model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import service.UserService;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    @GetMapping("/profile")
    public Map<String, Object> getProfile(@AuthenticationPrincipal OAuth2User principal) {
        return principal.getAttributes(); // Returns GitHub user details
    }


    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/roles")
    public Set<String> getRoles(@AuthenticationPrincipal OAuth2User principal) {
        String username = principal.getAttribute("email");
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getRoles();
    }

    @PostMapping("/roles/add")
    public User addRole(@AuthenticationPrincipal OAuth2User principal, @RequestBody String role) {
        String username = principal.getAttribute("email");
        User user = userService.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    return newUser;
                });

        user.getRoles().add(role);
        return userService.saveUser(user);
    }
}
