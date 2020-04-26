package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QuestionService {

    @Autowired
    private QuestionDao questionDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity upload(QuestionEntity questionEntity) throws AuthorizationFailedException {
        return questionDao.createQuestion(questionEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public QuestionEntity updateQuestion(QuestionEntity questionEntity) throws AuthorizationFailedException {
        return questionDao.updateQuestion(questionEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteQuestion(QuestionEntity questionEntity) throws AuthorizationFailedException {
        questionDao.deleteQuestion(questionEntity);
    }

    public QuestionEntity getQuestionByUuid(final String uuid) throws AuthorizationFailedException {
        return questionDao.getQuestionByUuid(uuid);
    }

    public List<QuestionEntity> getAllQuestionsByUser(final UserEntity user) throws AuthorizationFailedException {
        return questionDao.getAllQuestionsByUser(user);
    }

}