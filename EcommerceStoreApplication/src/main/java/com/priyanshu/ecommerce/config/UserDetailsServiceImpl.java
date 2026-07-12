package com.priyanshu.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;  // ✅ Added

import com.priyanshu.ecommerce.entity.User;
import com.priyanshu.ecommerce.repository.UserRepository;

@Service  // ✅ Added — makes Spring manage this bean and @Autowired works inside it
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found for: " + username);
        }
        return new CustomUser(user);
    }
}