package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.api.model.QuestionRequest;
import com.upgrad.quora.api.model.QuestionResponse;
import com.upgrad.quora.service.business.AuthenticationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private AuthenticationService authenticationService;

    /*
     * This endpoint is used to create a new question in the Quora Application.
     * input - questionRequest contain question content and
     *  authorization field containing authentication token generated from user sign-in
     *
     *  output - Success - QuestionResponse containing created question uuid
     *           Failure - Failure Code with message.
     */
    @RequestMapping(
            method = RequestMethod.POST,
            path = "/question/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(
            final QuestionRequest questionRequest,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UnsupportedEncodingException {

        // Call authenticationService with access_token field
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post a question");
        }

        //Create question
        final QuestionEntity questionEntity = new QuestionEntity();

        //setContent for question Entity from request
        questionEntity.setContent(questionRequest.getContent());

        //generate uuid for question
        questionEntity.setUuid(UUID.randomUUID().toString());

        //set user and time for new question
		questionEntity.setUser(userAuthTokenEntity.getUser());
        questionEntity.setDate(ZonedDateTime.now());

        //call questionService to create new question
        final QuestionEntity createQuestionEntity = questionService.upload(questionEntity);

        //create response for new question
        QuestionResponse questionResponse = new QuestionResponse().id(createQuestionEntity.getUuid()).status("Question SUCCESSFULLY REGISTERED");
        return new ResponseEntity<>(questionResponse, HttpStatus.CREATED);
    }

    /*
     * This endpoint is used get all question.
     * input - authorization field containing auth token generated from user sign-in
     *
     *  output - Success - QuestionDetailsResponse for all the questions
     *           Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/question/all",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UnsupportedEncodingException {

        // Call authenticationService with access_token field.
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
        }

        //to get list of all the question for a user
        final List<QuestionEntity> questionEntityList = questionService.getAllQuestionsByUser(userAuthTokenEntity.getUser());

        List<QuestionDetailsResponse> questionDetailsResponseList = new ArrayList<>();

        //prepare response with list of questions
        for(QuestionEntity questionEntity: questionEntityList) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(questionEntity.getUuid()).content(questionEntity.getContent());
            questionDetailsResponseList.add(questionDetailsResponse);

        }
        return new ResponseEntity<>(questionDetailsResponseList, HttpStatus.OK);
    }

    /*
     * This endpoint is used edit a question.
     * input - question uuid and authorization field containing auth token generated from user sign-in
     *
     *  output - Success - QuestionResponse containing edited question uuid
     *           Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            path = "/question/edit/{questionId}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> editQuestionContent(
            final QuestionRequest questionRequest,
            @PathVariable("questionId") final String questionUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException, UnsupportedEncodingException {

        // Call authenticationService with access_token field
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to edit the question");
        }

        //get a question by uuid
        final QuestionEntity questionEntity = questionService.getQuestionByUuid(questionUuid);

        //if given uuid doesn't exist
        if(questionEntity == null ) {
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

        if(  questionEntity.getUser() != userAuthTokenEntity.getUser() ) {
            throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
        }

        questionEntity.setContent(questionRequest.getContent());

        QuestionEntity updateQuestionEntity = questionService.updateQuestion(questionEntity);

        if( updateQuestionEntity == null ){
            throw new AuthorizationFailedException("OTHER-001","Database Error");
        }

        QuestionResponse questionResponse = new QuestionResponse().id(questionEntity.getUuid()).status("QUESTION EDITED");

        return new ResponseEntity<>(questionResponse, HttpStatus.OK);
    }

    /*
     * This endpoint is used delete a question.
     * input - question uuid and authorization field containing auth token generated from user sign-in
     *
     *  output - Success - QuestionResponse containing deleted question uuid
     *           Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            path = "/question/delete/{questionId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> deleteQuestion(
            @PathVariable("questionId") final String questionUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException, UnsupportedEncodingException {

        // Call authenticationService with access_token field
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to delete a question");
        }

        final QuestionEntity questionEntity = questionService.getQuestionByUuid(questionUuid);

        // given uuid doesn't exist
        if(questionEntity == null ) {
            throw new InvalidQuestionException("QUES-001","Entered question uuid does not exist");
        }

        if( questionEntity.getUser() != userAuthTokenEntity.getUser() && questionEntity.getUser().getRole().equals("nonadmin") ) {
            throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
        }

        questionService.deleteQuestion(questionEntity);

        QuestionResponse questionResponse = new QuestionResponse().id(questionEntity.getUuid()).status("QUESTION DELETED");

        return new ResponseEntity<>(questionResponse, HttpStatus.OK);
    }

    /*
     * This endpoint is used to get all the question of a user
     * input - user uuid and authorization field containing auth token generated from user sign-in
     *
     *  output - Success - QuestionDetailsResponse containing all questions for given user
     *           Failure - Failure Code  with message.
     */
    @RequestMapping(
            method = RequestMethod.GET,
            path = "question/all/{userId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(
            @PathVariable("userId") final String userUuid,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UserNotFoundException, UnsupportedEncodingException {

        // Call authenticationService with access token came in authorization field.
        UserAuthTokenEntity userAuthTokenEntity = authenticationService.authenticateByAccessToken(authorization);

        // Token exist but user logged out already or token expired
        if ( userAuthTokenEntity.getLogoutAt() != null ) {
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to get all questions");
        }

        UserEntity userEntity = authenticationService.getUserByUuid(userUuid);

        // if user doesnot exist
        if ( userEntity == null ) {
            throw new UserNotFoundException("USR-001","User with entered uuid whose question details are to be seen does not exist");
        }

        final List<QuestionEntity> questionEntityList = questionService.getAllQuestionsByUser(userEntity);

        List<QuestionDetailsResponse> questionDetailsResponseList = new ArrayList<>();

        for(QuestionEntity questionEntity: questionEntityList) {

            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(questionEntity.getUuid()).content(questionEntity.getContent());
            questionDetailsResponseList.add(questionDetailsResponse);

        }
        return new ResponseEntity<>(questionDetailsResponseList, HttpStatus.OK);
    }
}