package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.AnswerEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDao {


    @PersistenceContext
    private EntityManager entityManager;

    public AnswerEntity createAnswer(AnswerEntity answerEntity) {
        System.out.println("UserDao..." + answerEntity);
        entityManager.persist(answerEntity);
        try {
            return answerEntity;
        } catch (Exception e) {
            return null;
        }
    }

    public AnswerEntity getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", AnswerEntity.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public AnswerEntity getAnswerById(final long id) {
        try {
            return entityManager.createNamedQuery("AnswerEntityById", AnswerEntity.class).setParameter("id", id).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    public AnswerEntity getAnswerByUuid(final String uuid) {
        try {
            return entityManager.createNamedQuery("AnswerEntityByUuid", AnswerEntity.class).setParameter("uuid", uuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<AnswerEntity> getAllAnswersToQuestionId(final long questionId) {
        try {
            return entityManager.createNamedQuery("AnswerEntityById", AnswerEntity.class).setParameter("id", questionId).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
    public AnswerEntity updateAnswer(final AnswerEntity answerEntity) {
        return entityManager.merge(answerEntity);
    }

    public void deleteAnswer(final AnswerEntity answerEntity) {
         entityManager.remove(answerEntity);
    }

}