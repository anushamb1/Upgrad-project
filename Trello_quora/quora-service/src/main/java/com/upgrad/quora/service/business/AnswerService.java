package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.UserAuthDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnswerService {

  @Autowired private AnswerDao answerDao;

  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity createAnswer(AnswerEntity answerEntity) throws AuthorizationFailedException {
    return answerDao.createAnswer(answerEntity);
  }

  public List<AnswerEntity> getAllAnswersToQuestionId(final long questionId) {
    return answerDao.getAllAnswersToQuestionId(questionId);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public AnswerEntity updateAnswer(AnswerEntity answerEntity) throws AuthorizationFailedException {
    return answerDao.updateAnswer(answerEntity);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void deleteAnswer(AnswerEntity answerEntity) throws AuthorizationFailedException {
    answerDao.deleteAnswer(answerEntity);
  }

  public AnswerEntity getAnswerByUuid(final String uuid) throws AuthorizationFailedException {
    return answerDao.getAnswerByUuid(uuid);
  }
}
