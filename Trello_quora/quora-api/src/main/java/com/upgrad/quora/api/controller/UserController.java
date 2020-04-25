package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SigninResponse;
import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.PasswordCryptographyProvider;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserBusinessService userBusinessService;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;


    @RequestMapping(
            method = RequestMethod.POST,
            path = "/signup",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signup(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {

        //create a new UserEntity Object
        UserEntity userEntity = new UserEntity();

        //create a new random unique uuid and set it to new User Entity
        userEntity.setUuid(UUID.randomUUID().toString());

        //Set All the field of new object from the Request
        userEntity.setFirstName(signupUserRequest.getFirstName());
        userEntity.setLastName(signupUserRequest.getLastName());
        userEntity.setUserName(signupUserRequest.getUserName());
        userEntity.setAboutMe(signupUserRequest.getAboutMe());
        userEntity.setDob(signupUserRequest.getDob());
        userEntity.setEmail(signupUserRequest.getEmailAddress());
        userEntity.setPassword(signupUserRequest.getPassword());
        userEntity.setCountry(signupUserRequest.getCountry());
        userEntity.setContactNumber(signupUserRequest.getContactNumber());

        // set Role to default nonadmin
        userEntity.setRole("nonadmin");

        //Call signupBusinessService to create a new user Entity
        final UserEntity createdUserEntity = userBusinessService.signup(userEntity);

        //create response with create user uuid
        SignupUserResponse userResponse = new SignupUserResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SignupUserResponse>(userResponse, HttpStatus.CREATED);

    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/signin",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SigninResponse> signin(
            @RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException {

        //split and extract authorization base 64 code string from "authorization" field
        String[] base64EncodedString = authorization.split("Basic ");

        //decode base64 string from a "authorization" field
        byte[] decodedArray = passwordCryptographyProvider.getBase64DecodedStringAsBytes(base64EncodedString[1]);

        String decodedString = new String(decodedArray);

        //decoded string contain user name and password separated by ":"
        String[] decodedUserNamePassword = decodedString.split(":");

        //call authenticationService service to generate user Auth Token for any further communication
        UserAuthTokenEntity userAuthToken = userBusinessService.authenticateByUserNamePassword(decodedUserNamePassword[0], decodedUserNamePassword[1]);

        //get userEntity from Auth Token
        UserEntity user = userAuthToken.getUser();

        //send response with user uuid and access token in HttpHeader
        SigninResponse signinResponse = new SigninResponse().id(user.getUuid()).message("SIGNED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("access_token", userAuthToken.getAccessToken());

        return new ResponseEntity<SigninResponse>(signinResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(
            method = RequestMethod.POST,
            path = "/signout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signout(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, SignOutRestrictedException {

        UserAuthTokenEntity userAuthTokenEntity = null;
        try {
            // Call authenticationService with access token came in authorization field.
            userAuthTokenEntity = userBusinessService.authenticateByAccessToken(authorization);
        } catch(Exception e){
            throw new AuthorizationFailedException("SGR-001","User is not Signed in");
        }

        // Token exist but user logged out already or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException("SGR-001","User is not Signed in");
        }

        //Set logout time
        userAuthTokenEntity.setLogoutAt(ZonedDateTime.now());

        //update userAuthTokenEntity with updated logout time.
        userBusinessService.updateUserAuthToken(userAuthTokenEntity);

        //create response with signed out user uuid
        SignoutResponse signoutResponse = new SignoutResponse().id(userAuthTokenEntity.getUser().getUuid()).message("SIGNED OUT SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<SignoutResponse>(signoutResponse, headers, HttpStatus.OK);
    }

}
