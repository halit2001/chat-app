package com.chatapp.auth_service.controller;

import com.chatapp.auth_service.exceptions.UserNotFoundException;
import com.chatapp.auth_service.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chat_app.common_library.response.MemberDetailsResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Void> checkUserExistence(@PathVariable("userId") String userId) {
        userService.checkUserExistence(userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/details-by-ids")
    public ResponseEntity<List<MemberDetailsResponse>> getUsersDetailsByIds(@RequestBody List<String> userIds) {
        List<MemberDetailsResponse> userDetailsList = userService.createUserDetailsList(userIds);
        return ResponseEntity.status(HttpStatus.OK).body(userDetailsList);
    }
}
