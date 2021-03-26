package edu.vanderbilt.starter.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private JdbcUserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Returns the information about the currently logged in user's
     * account.
     *
     * @param authentication
     * @return
     */
    @GetMapping
    public UserDetails getCurrentUser(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return userDetails;
    }

    private List<SimpleGrantedAuthority> roles(String...roles){
        return Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }

    /**
     * Adds a user account to the system using the provided
     * username nad password.
     *
     * @param username
     * @param password
     * @return
     */
    // The @PreAuthorize ensures that only admin users can access
    // this method.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public UserDetails addUser(String username, String password){

        User user = new User(
                username,
                // We always need to salt & hash the passwords before they are stored
                // in the database.
                passwordEncoder.encode(password),
                roles("USER"));

        userDetailsManager.createUser(user);

        // We remove the password before returning the
        // information for the newly created user
        return new User(user.getUsername(),null, user.getAuthorities());
    }

}
