package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.Arrays;
import java.util.Date;
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

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.ISeasonService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.SeasonDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidDateException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class builds the operations over the seasons table
 * 
 * @since 05/08/2022
 * @version 1.0
 *
 */

@Service("SeasonService")
@Lazy
public class SeasonService implements ISeasonService {
	private Logger log;

	private Control control;
	private UserControl userControl;
	private Validator dataValidator;

	@Autowired
	private SeasonDao seasonDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	public SeasonService() {
		super();
		this.control = new Control();
		this.dataValidator = new Validator();
		this.log = LoggerFactory.getLogger(this.getClass());
		this.userControl = new UserControl();
	}

	/**
	 * 
	 * Executes a query over the seasons table, showing the seasons of a concrete
	 * hotel
	 * 
	 * @since 05/08/2022
	 * @param The hotel to filter the seasons
	 * 
	 * @exception NoResultsException     when there are no seasons in the specified
	 *                                   hotel
	 * @exception BadSqlGrammarException when it receives an incorrect param type
	 * @exception NotAuthorizedException when it receives a search without filter or
	 *                                   an hotel manager tries to search the
	 *                                   seasons from another hotel
	 * 
	 * @return The columns from the seasons table especified in the params and a
	 *         message with the operation result
	 */

	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult seasonQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			if (keyMap.get("ss_hotel") == null)
				throw new NotAuthorizedException("NOT_AUTHORIZED");
			searchResult = daoHelper.query(seasonDao, keyMap, attrList);
			control.checkResults(searchResult);
			userControl.controlAccess((int) keyMap.get("ss_hotel"));
		} catch (NotAuthorizedException | NoResultsException e) {
			log.error("unable to retrieve seasons by hotel. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve seasons by hotel. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the seasons table.
	 * 
	 * @since 05/08/2022
	 * @param The fields of the new register
	 * @return The id of the new hotel and a message with the operation result
	 * 
	 * @exception EmptyRequestException           when it doesn't receive any of the
	 *                                            register fields
	 * @exception DataIntegrityViolationException when it receives an unexisting
	 *                                            hotel
	 * @exception DuplicateKeyException           when it receives a season with a
	 *                                            already registered name
	 * 
	 * @exception BadSqlGrammarException          when it receives an incorrect type
	 * 
	 * @exception NotAuthorizedException          when a hotel manager try to insert
	 *                                            a season in a different hotel
	 * 
	 * @exception InvalidDateException			when the start date is after the end date or before the
	 * 											current date
	 * 
	 * 
	 * 
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult seasonInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			userControl.controlAccess((int) attrMap.get("ss_hotel"));
			checkDates(attrMap);
			checkCoincidentSeasons(attrMap);
			insertResult = daoHelper.insert(seasonDao, attrMap);
			insertResult.setMessage("SUCESSFULL_INSERTION");

		} catch (DuplicateKeyException e) {
			log.error("unable to insert a season. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, "SS_NAME_ALREADY_EXISTS");
		} catch (NotAuthorizedException | DataIntegrityViolationException | InvalidRequestException|InvalidDateException e) {
			log.error("unable to insert a season. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to insert an hotel. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a register on the seasons table.
	 * 
	 * @since 05/08/2022
	 * @param The fields to update
	 * @return A message with the result of the update
	 * 
	 * @exception EmptyRequestException           when it doesn't receive any of the
	 *                                            register fields
	 * @exception DuplicateKeyException           when it receives a season with a
	 *                                            already registered name
	 * 
	 * 
	 * @exception NotAuthorizedException          when an hotel manager tries to
	 *                                            update a season on another hotel
	 *                                            
	 * @exception RecordNotFoundException			when it receiveis an id_season that doesn't exists                                           
	 * 
	 * 
	 * 
	 */
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult seasonUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			if (keyMap.isEmpty()) {
				throw new EmptyRequestException("ID_SEASON_REQUIRED");
			}
			if(attrMap.get("ss_hotel")!=null) {
				throw new NotAuthorizedException("SS_HOTEL_CAN'T_BE_UPDATED");
			}
			
			dataValidator.checkIfMapIsEmpty(attrMap);
			searchResult = daoHelper.query(seasonDao, keyMap,
					Arrays.asList("ss_hotel", "ss_start_date", "ss_end_date"));
			if(searchResult.isEmpty()) {
				throw new RecordNotFoundException("SEASON_DON'T_EXISTS");
			}		
		
			int hotel = (int) searchResult.getRecordValues(0).get("ss_hotel");
			userControl.controlAccess(hotel);

			if (attrMap.get("ss_start_date") != null || attrMap.get("ss_end_date") != null) {
				if (attrMap.get("ss_start_date") == null) {
					attrMap.put("ss_start_date", searchResult.getRecordValues(0).get("ss_start_date"));
				}
				if (attrMap.get("ss_end_date") == null) {
					attrMap.put("ss_end_date", searchResult.getRecordValues(0).get("ss_end_date"));
				}
				attrMap.put("ss_hotel", (int) searchResult.getRecordValues(0).get("ss_hotel"));
				checkDates(attrMap);
				checkCoincidentSeasons(attrMap);
			}

			updateResult = this.daoHelper.update(seasonDao, attrMap, keyMap);
			updateResult.setMessage("SUCESSFUL_UPDATE");
		} catch (EmptyRequestException | NotAuthorizedException | InvalidRequestException|RecordNotFoundException e) {
			log.error("unable to update a season. Request : {} ", attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		}  catch (DuplicateKeyException e) {
			log.error("unable to update a season. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "SS_NAME_ALREADY_EXISTS");
		} 
		return updateResult;
	}

	/**
	 * 
	 * Deletes a register on the seasons table.
	 * 
	 * @since 05/08/2022
	 * @param The id of the season to delete
	 * @return A message with the result of the delete
	 * 
	 * @exception EmptyRequestException  when it doesn't receive the id_season
	 * 
	 * @exception NoResultsException     when it receives an unexisting season
	 * 
	 * 
	 * @exception BadSqlGrammarException when it receives an incorrect type
	 * 
	 * @exception NotAuthorizedException when an hotel manager tries to delete a
	 *                                   season on another hotel
	 * 
	 */

	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult seasonDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		EntityResult deleteResult = new EntityResultMapImpl();
		try {
			if (keyMap.isEmpty()) {
				throw new EmptyRequestException("ID_SEASON_REQUIRED");
			}
			deleteResult = daoHelper.query(seasonDao, keyMap, Arrays.asList("id_season", "ss_hotel"));
			control.checkResults(deleteResult);
			userControl.controlAccess((int) deleteResult.getRecordValues(0).get("ss_hotel"));			
			deleteResult = daoHelper.delete(seasonDao, keyMap);
			deleteResult.setMessage("SUCCESSFULL_DELETE");

		} catch (NotAuthorizedException e) {
			log.error("unable to delete a season. Request : {} {} ", keyMap, e);
			control.setErrorMessage(deleteResult, "NOT_AUTHORIZED");
		} catch (EmptyRequestException e) {
			log.error("unable to delete a season. Request : {} ", e);
			control.setMessageFromException(deleteResult, e.getMessage());
		} catch (NoResultsException e) {
			log.error("unable to delete a season. Request : {}s ", e);
			control.setMessageFromException(deleteResult, e.getMessage());
		}
		return deleteResult;
	}

	/**
	 * 
	 * Deletes all the seasons ended before the current date in an hotel
	 * 
	 * @since 05/08/2022
	 * @param The id of the season to delete
	 * @return A message with the result of the delete
	 * 
	 * @exception EmptyRequestException  when it doesn't receive the id_season
	 * 
	 * @exception NoResultsException     when it receives an unexisting season
	 * 
	 * 
	 * @exception BadSqlGrammarException when it receives an incorrect type
	 * 
	 * 
	 * 
	 */

	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult hotelseasonsDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		EntityResult deleteResult = new EntityResultMapImpl();
		try {
			if (!keyMap.containsKey("ss_hotel")) {
				throw new EmptyRequestException("SS_HOTEL_REQUIRED");
			}
			deleteResult = daoHelper.query(seasonDao, keyMap, Arrays.asList("id_season"), "OLD_SEASONS");
			
			control.checkResults(deleteResult);
			userControl.controlAccess((int) keyMap.get("ss_hotel"));
		
			Map<String, Object> deleteFilter = new HashMap<>();

			for (int i = 0; i < deleteResult.calculateRecordNumber(); i++) {
				deleteFilter.put("id_season", deleteResult.getRecordValues(i).get("id_season"));
				daoHelper.delete(seasonDao, deleteFilter);
			}
			deleteResult.setMessage("SUCCESSFULL_DELETE");

		} catch (NotAuthorizedException e) {
			log.error("unable to delete a season. Request : {} {} ", keyMap, e);
			control.setErrorMessage(deleteResult, "NOT_AUTHORIZED");
		} catch (EmptyRequestException e) {
			log.error("unable to delete a season. Request : {} ", e);
			control.setMessageFromException(deleteResult, e.getMessage());
		} catch (NoResultsException e) {
			log.error("unable to delete a season. Request : {}s ", e);
			control.setMessageFromException(deleteResult, e.getMessage());
		}
		return deleteResult;
	}

	private void checkCoincidentSeasons(Map<String, Object> attrMap) throws InvalidRequestException {
		Date newSeasonStart = (Date) attrMap.get("ss_start_date");
		Date newSeasonEnd = (Date) attrMap.get("ss_end_date");
		Integer hotel = (Integer) attrMap.get("ss_hotel");
		Map<String, Object> keyMap = new HashMap<>();

		keyMap.put("ss_hotel", attrMap.get("ss_hotel"));
		keyMap.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
				buildExpressionToSearchSeasons(newSeasonStart, newSeasonEnd));
		EntityResult result = daoHelper.query(seasonDao, keyMap, Arrays.asList("id_season"));

		if (!result.isEmpty()) {
			throw new InvalidRequestException("THERE_IS_ANOTHER_SEASON_ACTIVE_ON_THAT_DATES");
		}

	}

	/**
	 * Checks if the start date is before the end date and after the current date
	 * 
	 * @param startDate
	 * @param endDate
	 * @throws InvalidDateException Sends a message to the user when check_in or
	 *                              check_out fields are invalid
	 */
	private void checkDates(Map<String, Object> attrMap) throws InvalidDateException {
		Date startDate = (Date) attrMap.get("ss_start_date");
		Date endDate = (Date) attrMap.get("ss_end_date");

		if (endDate.before(startDate) || endDate.equals(startDate)) {
			throw new InvalidDateException("START_DATE_MUST_BE_BEFORE_END_DATE");
		}
		if (startDate.before(new Date(System.currentTimeMillis() - 86400000))) {
			throw new InvalidDateException("START_DATE_MUST_BE_EQUAL_OR_AFTER_CURRENT_DATE");
		}
	}

	/**
	 * 
	 * Builds a Basic expression to search the seasons on a concrete dates
	 * 
	 * @since 05/08/2022
	 * @param A start and a end date
	 * @return The Basic expression to search the seasons
	 */
	public BasicExpression buildExpressionToSearchSeasons(Date newSeasonStart, Date newSeasonEnd) {

		BasicField bdSeasonStart = new BasicField(SeasonDao.ATTR_START_DATE);
		BasicField bdSeasonEnd = new BasicField(SeasonDao.ATTR_END_DATE);

		BasicExpression b1 = new BasicExpression(bdSeasonStart, BasicOperator.LESS_EQUAL_OP, newSeasonStart);
		BasicExpression b2 = new BasicExpression(bdSeasonEnd, BasicOperator.MORE_EQUAL_OP, newSeasonStart);
		BasicExpression rule1 = new BasicExpression(b1, BasicOperator.AND_OP, b2);

		BasicExpression b3 = new BasicExpression(bdSeasonStart, BasicOperator.LESS_EQUAL_OP, newSeasonEnd);
		BasicExpression b4 = new BasicExpression(bdSeasonEnd, BasicOperator.MORE_EQUAL_OP, newSeasonEnd);
		BasicExpression rule2 = new BasicExpression(b3, BasicOperator.AND_OP, b4);

		BasicExpression b5 = new BasicExpression(bdSeasonStart, BasicOperator.MORE_EQUAL_OP, newSeasonStart);
		BasicExpression b6 = new BasicExpression(bdSeasonEnd, BasicOperator.LESS_EQUAL_OP, newSeasonEnd);
		BasicExpression rule3 = new BasicExpression(b5, BasicOperator.AND_OP, b6);

		BasicExpression rule1_2 = new BasicExpression(rule1, BasicOperator.OR_OP, rule2);

		return new BasicExpression(rule1_2, BasicOperator.OR_OP, rule3);
	}

}
