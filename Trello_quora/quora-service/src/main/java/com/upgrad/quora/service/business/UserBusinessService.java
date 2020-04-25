package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserAuthDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthenticationFailedException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthDao userAuthDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Autowired
    private PasswordCryptographyProvider CryptographyProvider;

    /*
    * Signup Method insert UserEntity Object into User tables.
    * */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {

        //Check whether already exists in User Table using UserName
        UserEntity existedUser = userDao.getUserByUserName(userEntity.getUserName());
        if( existedUser != null ) {
            // Throw SignUpRestrictedException exception when user with Username already exists.
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");
        }

        //Check Whether already exists in user table using User email
        existedUser = userDao.getUserByEmail(userEntity.getEmail());
        if( existedUser != null ) {
            // Throw SignUpRestrictedException exception when user with email already exists.
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }

        // Encrypt password
        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);

        try {
            // Insert user record
            return userDao.createUser(userEntity);
        } catch (Exception e) {
            // Throw SignUpRestrictedException exception when error occurred while inserting user.
            throw new SignUpRestrictedException("SGR-003","Unknown database error while creating user");
        }

    }

    /*
      Authentication logic check username and password in User Table and generate Token.
    */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity authenticateByUserNamePassword(final String username, final String password) throws AuthenticationFailedException {
        // Check Whether user exists or not, using username.
        UserEntity userEntity = userDao.getUserByUserName(username);
        if (userEntity == null) {
            // Throw AuthenticationFailedException exception when user doesn't exists.
            throw new AuthenticationFailedException("ATH-001", "This username does not exist");
        }
        //Encrypt password
        final String encryptedPassword = CryptographyProvider.encrypt(password, userEntity.getSalt());
        if (encryptedPassword.equals(userEntity.getPassword())) {
            // Get JWT provider using Encrypted Password
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            UserAuthTokenEntity userAuthTokenEntity = new UserAuthTokenEntity();
            userAuthTokenEntity.setUser(userEntity);
            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            // Generate Token using JWT provider and User UUID
            userAuthTokenEntity.setAccessToken(jwtTokenProvider.generateToken(userEntity.getUuid(), now, expiresAt));
            userAuthTokenEntity.setLoginAt(now);
            userAuthTokenEntity.setUuid(userEntity.getUuid());
            userAuthTokenEntity.setExpiresAt(expiresAt);

            //Insert UserAuth object into UserAuthToken Table
            userAuthDao.createAuthToken(userAuthTokenEntity);

            return userAuthTokenEntity;
        } else {
            // Throw AuthorizationFailedException exception if password was wrong.
            throw new AuthenticationFailedException("ATH-002", "Password failed");
        }
    }

    public UserAuthTokenEntity authenticateByAccessToken(final String accessToken) throws AuthorizationFailedException {
       //Get UserAuthToken Record using Token
        UserAuthTokenEntity userAuthTokenEntity = userAuthDao.getUserAuthTokenEntityByAccessToken(accessToken);
        if( userAuthTokenEntity != null ) {
            return userAuthTokenEntity;
        } else {
            // Throw AuthorizationFailedException exception if token present in UserAuthToken table
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthTokenEntity updateUserAuthToken(final UserAuthTokenEntity userAuthTokenEntity) {
        //Update UserAuthToken record.
        return userAuthDao.updateUserAuthToken(userAuthTokenEntity);
    }

}
