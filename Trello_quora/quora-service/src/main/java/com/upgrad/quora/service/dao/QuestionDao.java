package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {


    @PersistenceContext
    private EntityManager entityManager;

    public QuestionEntity createQuestion(QuestionEntity questionEntity) {
        try {
            entityManager.persist(questionEntity);
            return questionEntity;
        } catch (Exception e) {
            return null;
        }
    }

    public QuestionEntity updateQuestion(QuestionEntity questionEntity) {
        try {
            entityManager.merge(questionEntity);
            return questionEntity;
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteQuestion(QuestionEntity questionEntity) {
        entityManager.remove(questionEntity);
    }

    public QuestionEntity getQuestionByUuid(final String uuid) {
        try {
            return entityManager.createNamedQuery("questionEntityByUuid", QuestionEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }


    public List<QuestionEntity> getAllQuestionsByUser(final UserEntity user) {
        try {
            return entityManager.createNamedQuery("questionsByUser", QuestionEntity.class).setParameter("user", user).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}