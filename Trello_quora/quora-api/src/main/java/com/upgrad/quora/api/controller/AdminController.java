package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /*
     * This endpoint is delete user.
     * input - User  uuid and authorization field containing auth token generated from user sign-in
     *
     *  output - Success - UserDeleteResponse containing deleted User uuid
     *           Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            path = "/admin/user/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> deleteUser(@RequestHeader("authorization") final String accessToken, @PathVariable("userId") String userId) throws AuthorizationFailedException, UserNotFoundException {

        //Call AdminService deleteUser to delete, If accessToken was valid
        UserEntity userEntity = adminService.deleteUser(userId, accessToken);

        //Prepare UserDeleteResponse object to returns
        UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(userEntity.getUuid()).status("USER SUCCESSFULLY DELETED");

        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
    }
}
