package com.chatapp.auth_service.service;

import com.chatapp.auth_service.model.User;
import com.chatapp.auth_service.repository.UserRepository;
import com.chatapp.auth_service.security.JwtUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method loads a user by their username from the database. If the user is found,
     * it returns the user details encapsulated in a JwtUserDetails object. If the user is
     * not found, it throws a UsernameNotFoundException.
     *
     * @param username The username of the user to be loaded.
     * @return The user details encapsulated in a JwtUserDetails object.
     * @throws UsernameNotFoundException If no user is found with the provided username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()) return JwtUserDetails.create(user.get());
        throw new UsernameNotFoundException("User not found with username : " + username);
    }
}
