package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IReviewService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingHistDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ReviewDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidDateException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class listens the incoming requests related with the reviews table
 * 
 * @since 19/08/2022
 * @version 1.0
 *
 */
@Service("ReviewService")
@Lazy
public class ReviewService implements IReviewService {

	@Autowired
	private ReviewDao reviewDao;

	@Autowired
	private BookingHistDao bookingHistDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	private Logger log;
	private Control control;

	private UserControl userControl;
	Validator dataValidator;

	public ReviewService() {
		this.control = new Control();
		this.dataValidator = new Validator();
		this.userControl = new UserControl();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * Executes a query over the reviews table, showing the reviews of a concrete
	 * hotel or client
	 * 
	 * @since 19/08/2022
	 * @param The hotel to filter or the client to filter the reviews
	 * 
	 * @exception NoResultsException      when there are reviews associated with the
	 *                                    specified hotel or client
	 * @exception BadSqlGrammarException  when it receives an incorrect param type
	 * @exception InvalidRequestException when it doesn't receive an hotel or a
	 *                                    client to filter the search
	 * 
	 * @return The columns from the reviews table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	public EntityResult reviewQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			if (!keyMap.containsKey("rv_hotel") && !keyMap.containsKey("rv_client")) {
				throw new InvalidRequestException("RV_HOTEL_OR_RV_CLIENT_REQUIRED");
			}
			searchResult = daoHelper.query(reviewDao, keyMap, attrList, "REVIEWS_DATA");
			control.checkResults(searchResult);
		} catch (NoResultsException | InvalidRequestException e) {
			log.error("unable to retrieve reviews. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve reviews. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the reviews table.
	 * 
	 * @since 19/08/2022
	 * @param The fields of the new register
	 * @return The id of the new review and a message with the operation result
	 * 
	 * @exception EmptyRequestException           when it doesn't receive any of the
	 *                                            register fields
	 * @exception DataIntegrityViolationException when it receives an unexisting
	 *                                            hotel or client, or when receives a too long comment
	 * @exception DuplicateKeyException           when the client already have a
	 *                                            review associated with the hotel
	 * 
	 * @exception BadSqlGrammarException          when it receives an incorrect type
	 * 
	 * @exception ClassCastException              when it receives an incorrect type
	 * 
	 * 
	 * @exception InvalidDateException            when the start date is after the
	 *                                            end date or before the current
	 *                                            date
	 * 
	 * 
	 * 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult reviewInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			if (attrMap.containsKey("rv_response")) {
				throw new InvalidRequestException("CLIENTS_CAN'T_RESPONSE_THEIR_OWN_RATINGS");
			}
			checkValidRating(attrMap);
			checkValidClient(attrMap);
			attrMap.put("rv_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
			insertResult = this.daoHelper.insert(reviewDao, attrMap);
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		} catch (InvalidRequestException | EmptyRequestException e) {
			log.error("unable to insert a review. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a review. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "THIS_CLIENT_HAS_ALREADY_RATED_THIS_HOTEL");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a review. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (ClassCastException | BadSqlGrammarException e) {
			log.error("unable to insert a review. Request : {} {} ", attrMap, e);
			control.setErrorMessage(insertResult, "INVALID_TYPE");
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a register on the reviews table adding a response from the hotel
	 * manager
	 * 
	 * @since 19/08/2022
	 * @param The id_review to update and the fields to be updated
	 * @return A message with the result of the update
	 * 
	 * @exception EmptyRequestException           when it doesn't receive any of the
	 *                                            register fields
	 * @exception InvalidRequestException         when it tries to update any field
	 *                                            except the rv_response or an
	 *                                            review that doesn't exists
	 * 
	 * 
	 * @exception NotAuthorizedException          when an hotel manager tries to
	 *                                            update a review on another hotel
	 * 
	 * @exception RecordNotFoundException         when it receiveis an id_season
	 *                                            that doesn't exists
	 * 
	 * @exception DataIntegrityViolationException when it receives a too response
	 *                                            too long
	 * 
	 * 
	 * 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult reviewresponseUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult searchResult = new EntityResultMapImpl();
		try {

			dataValidator.checkIfMapIsEmpty(attrMap);
			searchResult = checkIfReviewExists(keyMap);
			userControl.controlAccess((int) searchResult.getRecordValues(0).get("rv_hotel"));

			if (attrMap.containsKey("rv_hotel") || attrMap.containsKey("rv_client") || attrMap.containsKey("rv_rating")
					|| attrMap.containsKey("rv_comment")) {
				throw new InvalidRequestException("MANAGERS_CAN_ONLYPUBLISH_REPONSES");
			}
			updateResult = daoHelper.update(reviewDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_RESPONSE");
		} catch (EmptyRequestException | InvalidRequestException e) {
			log.error("unable to update a review. Request : {} ", attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}  catch (NotAuthorizedException e) {
			log.error("unable to update a review. Request : {} ", attrMap, e);
			control.setErrorMessage(updateResult, "MANAGERS_CAN_ONLY_RESPONSE_REVIEWS_FROM_THEIR_HOTEL");
		}catch (DataIntegrityViolationException e) {
			log.error("unable to update a review. Request : {} ", attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		}catch (BadSqlGrammarException e) {
			log.error("unable to update a review. Request : {} ", e);
			control.setErrorMessage(updateResult, "INVALID_TYPE");
		}

		return updateResult;

	}

	/**
	 * 
	 * Deletes a register on the reviews table.
	 * 
	 * @since 19/08/2022
	 * @param The id of the review to delete
	 * @return A message with the result of the delete
	 * 
	 * @exception EmptyRequestException   when it doesn't receive the id_review
	 * 
	 * @exception InvalidRequestException when it tries to delete an unexisting
	 *                                    review
	 * 
	 * 
	 * @exception BadSqlGrammarException  when it receives an incorrect type
	 * 
	 * @exception NotAuthorizedException  when an client tries to delete a review
	 *                                    from another
	 * 
	 * @
	 * 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult reviewDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		EntityResult deleteResult = new EntityResultMapImpl();
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = checkIfReviewExists(keyMap);
			userControl.controlAccessClient((int) searchResult.getRecordValues(0).get("rv_client"));
			deleteResult = daoHelper.delete(reviewDao, keyMap);
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		} catch (EmptyRequestException | InvalidRequestException e) {
			log.error("unable to delete a review. Request : {} ", e);
			control.setErrorMessage(deleteResult, e.getMessage());
		}  catch (BadSqlGrammarException e) {
			log.error("unable to delete a review. Request : {} ", e);
			control.setErrorMessage(deleteResult, "INVALID_TYPE");
		} catch (NotAuthorizedException e) {
			log.error("unable to delete a review. Request : {} ", e);
			control.setErrorMessage(deleteResult, "CLIENTS_CAN'T_ONLY_DELETE_THEIR_OWN_REVIEWS");
		}

		return deleteResult;
	}

	private EntityResult checkIfReviewExists(Map<String, Object> keyMap) throws InvalidRequestException {
		EntityResult searchResult = new EntityResultMapImpl();
		if (!keyMap.containsKey("id_review")) {
			throw new InvalidRequestException("ID_REVIEW_REQUIRED");
		} else {
			searchResult = daoHelper.query(reviewDao, keyMap, Arrays.asList("id_review", "rv_hotel", "rv_client"));
			if (searchResult.isEmpty())
				throw new InvalidRequestException("ID_REVIEW_DOESN'T_EXISTS");
		}
		return searchResult;

	}

	private void checkValidClient(Map<String, Object> attrMap) throws InvalidRequestException {
		if (attrMap.containsKey("rv_client") && attrMap.containsKey("rv_hotel")) {
			HashMap<String, Object> bookingFilter = new HashMap<>();
			bookingFilter.put("bk_client", attrMap.get("rv_client"));
			bookingFilter.put("id_hotel", attrMap.get("rv_hotel"));
			EntityResult bookingResult = daoHelper.query(bookingHistDao, bookingFilter, Arrays.asList("id_booking"),
					"HISTORIC_BOOKING_WITH_HOTEL");
			if (bookingResult.isEmpty()) {
				throw new InvalidRequestException("THAT_CLIENT_DOESN'T_HAVE_COMPLETED_BOOKINGS_ON_THAT_HOTEL");
			}

		}

	}

	private void checkValidRating(Map<String, Object> attrMap) throws InvalidRequestException {
		if (attrMap.containsKey("rv_rating")) {
			int rating = (int) attrMap.get("rv_rating");
			if (rating > 5 || rating < 1) {
				throw new InvalidRequestException("RATING_MUST_BE_BETWEEN_1_AND_5");
			}
		}

	}
}
