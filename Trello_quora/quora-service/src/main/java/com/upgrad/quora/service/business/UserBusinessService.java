package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {

        UserEntity existedUser = userDao.getUserByUserName(userEntity.getUserName());
        if( existedUser != null ) {
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");
        }

        existedUser = userDao.getUserByEmail(userEntity.getEmail());
        if( existedUser != null ) {
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }

        String[] encryptedText = passwordCryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);

        try {
            return userDao.createUser(userEntity);
        } catch (Exception e) {
            throw new SignUpRestrictedException("SGR-003","Unknown database error while creating user");
        }

    }


}
