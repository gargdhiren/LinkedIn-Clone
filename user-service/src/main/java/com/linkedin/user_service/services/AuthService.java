package com.linkedin.user_service.services;

import com.linkedin.user_service.dto.LoginDto;
import com.linkedin.user_service.dto.SignUpDto;
import com.linkedin.user_service.dto.UserDto;
import com.linkedin.user_service.entity.User;
import com.linkedin.user_service.exception.BadRequestException;
import com.linkedin.user_service.exception.ResourceNotFoundException;
import com.linkedin.user_service.repository.UserRepository;
import com.linkedin.user_service.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;

    public UserDto signUp(SignUpDto signUpDto) {
        boolean userExits=userRepository.existsByEmail(signUpDto.getEmail());
        if(userExits){
            throw new BadRequestException("Email already exists");
        }
        User user = modelMapper.map(signUpDto, User.class);
        user.setPassword(PasswordUtil.hashPassword(signUpDto.getPassword()));
        System.out.println();

        return  modelMapper.map(userRepository.save(user), UserDto.class);
    }

    public String login(LoginDto loginDto) {
        User user= userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(()-> new ResourceNotFoundException("User not found with email: "+loginDto.getEmail()));
        boolean isPasswordMatch=PasswordUtil.checkPassword(loginDto.getPassword(),user.getPassword());
        if(!isPasswordMatch){
            throw new BadRequestException("Invalid password");
        }

        return jwtService.generateAccessToken(user);
    }
}
