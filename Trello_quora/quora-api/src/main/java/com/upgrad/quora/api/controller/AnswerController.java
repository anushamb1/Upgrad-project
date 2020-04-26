package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class AnswerController {

  @Autowired private AnswerService answerService;

  @Autowired private AuthenticationService authenticationService;

  @Autowired private QuestionService questionService;

    /*
     * This endpoint is used to create a new answer in the Quora Application.
     * input - answerRequest contain question id, answer content and
     *  authorization field containing auth token generated from user sign-in
     *
     *  output - Success - AnswerResponse containing created answer uuid
     *           Failure - Failure Code  with message.
     */

  @RequestMapping(
          method = RequestMethod.POST,
          path = "/question/{questionId}/answer/create",
          consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
          produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<AnswerResponse> addAnswer(
          final AnswerRequest answerRequest,
          @PathVariable("questionId") final String questionUuid,
          @RequestHeader("authorization") final String authorization)
          throws AuthorizationFailedException, UnsupportedEncodingException, InvalidQuestionException {

      final QuestionEntity questionEntity = questionService.getQuestionByUuid(questionUuid);

      // Checking whether the question exists in DB
      if ( questionEntity == null ) {
        throw new InvalidQuestionException("QUES-001","The question entered is invalid");
      }

      // Call authenticationService with access token came in authorization field.
      UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

      // Token exist but user logged out already or token expired
      if ( userAuthTokenEntity.getLogoutAt() != null ) {
        throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
      }

      //Create and new answer entity and set answer content for the question
      final AnswerEntity answerEntity = new AnswerEntity();
      answerEntity.setAns(answerRequest.getAnswer());
      answerEntity.setUuid(UUID.randomUUID().toString());
      answerEntity.setDate(ZonedDateTime.now());
      answerEntity.setUser(userAuthTokenEntity.getUser());
      answerEntity.setQuestion(questionEntity);

      final AnswerEntity createAnswerEntity = answerService.createAnswer(answerEntity);
      AnswerResponse questionResponse = new AnswerResponse().id(createAnswerEntity.getUuid()).status("Answer SUCCESSFULLY REGISTERED");
      return new ResponseEntity<AnswerResponse>(questionResponse, HttpStatus.CREATED);
  }

    /*
     * This endpoint is used to edit an existing answer.
     * input - Existing answer id which needs to be edited and authorization field containing auth token generated from user sign-in
     *
     *  output - Success - Answer Edit Response
     *           Failure - Failure Code  with message.
     */
  @RequestMapping(
          method = RequestMethod.PUT,
          path = "/answer/edit/{answerId}",
          consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
          produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> editAnswerContent(
          final String content,
          @PathVariable("answerId") final String answerUuid,
          @RequestHeader("authorization") final String authorization)
          throws AuthorizationFailedException, AnswerNotFoundException {

         // Call authenticationService with access token came in authorization field.
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist but user logged out already or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
          throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit an answer");
        }


        final AnswerEntity answerEntity = answerService.getAnswerByUuid(answerUuid);

        if( answerEntity == null ) {
          throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist'");
        }

        if( answerEntity.getUser() != userAuthTokenEntity.getUser() ) {
            throw new AuthorizationFailedException("ATHR-003","Only the answer owner can edit the answer");
        }

        //set content for the answer which needs to be edited in the answer entity
        answerEntity.setAns(content);
        AnswerEntity updatedAnswerEntity = answerService.updateAnswer(answerEntity);
        AnswerResponse answerDetailsResponse =
                new AnswerResponse().id(updatedAnswerEntity.getUuid()).status("ANSWER EDITED");

        return new ResponseEntity<AnswerResponse>(answerDetailsResponse, HttpStatus.OK);
  }

    /*
     * This endpoint is used to delete an existing answer
     * input - Answer id for the answer which needs to be deleted and authorization field containing auth token generated from user sign-in
     *
     *  output - Success - QuestionDetailsResponse for all the questions
     *           Failure - Failure Code  with message.
     */
  @RequestMapping(
          method = RequestMethod.DELETE,
          path = "/answer/delete/{answerId}",
          produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity deleteAnswer(
          @PathVariable("answerId") final String answerUuid,
          @RequestHeader("authorization") final String authorization)
          throws AuthorizationFailedException, AnswerNotFoundException {

      // Call authenticationService with access token came in authorization field.
      UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

      // Token exist but user logged out already or token expired
      if ( userAuthTokenEntity.getLogoutAt() != null ) {
          throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete an answer");
      }

      final AnswerEntity answerEntity = answerService.getAnswerByUuid(answerUuid);

      if( answerEntity == null ) {
          throw new AnswerNotFoundException("ANS-001","Entered answer uuid does not exist");
      }

      if( answerEntity.getUser() != userAuthTokenEntity.getUser() ) {
          throw new AuthorizationFailedException("ATHR-003","Only the answer owner or admin can delete the answer");
      }

      answerService.deleteAnswer(answerEntity);
      AnswerResponse answerDetailsResponse =
        new AnswerResponse().id(answerEntity.getUuid()).status("ANSWER DELETED");

      return ResponseEntity.status(HttpStatus.OK).body(answerDetailsResponse);
  }

  @RequestMapping(
          method = RequestMethod.GET,
          path = "answer/all/{questionId}",
          produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<List<AnswerEntity>> getAllAnswersToQuestion(
            @PathVariable("questionId") final String questionUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {

      // Call authenticationService with access token came in authorization field.
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

      // Token exist but user logged out already or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
          throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get the answers");
        }

        final QuestionEntity questionEntity = questionService.getQuestionByUuid(questionUuid);

        if(questionEntity == null ) {
          throw new InvalidQuestionException("QUES-001","The question with entered uuid whose details are to be seen does not exist");
        }

        final List<AnswerEntity> answerEntityList = answerService.getAllAnswersToQuestionId(questionEntity.getId());

        return ResponseEntity.status(HttpStatus.OK).body(answerEntityList);
  }
}
