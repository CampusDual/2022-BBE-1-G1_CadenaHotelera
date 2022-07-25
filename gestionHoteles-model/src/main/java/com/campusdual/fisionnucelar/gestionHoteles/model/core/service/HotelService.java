package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
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
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLStatement;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;

/**
 * This class builds the operations over the hotels table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
@Service("HotelService")
@Lazy
public class HotelService implements IHotelService {

	@Autowired
	private HotelDao hotelDao;
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	private Control control;
	private Validator dataValidator;
	private Logger log;

	public HotelService() {
		super();
		this.control = new Control();
		this.dataValidator=new Validator();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * Executes a generic query over the hotels table
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the hotels table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	public EntityResult hotelQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {

			searchResult = this.daoHelper.query(this.hotelDao, keyMap, attrList);

			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			log.error("unable to retrieve a hotel. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a hotel. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
			e.printStackTrace();
		}
		return searchResult;
	}

	
	
	
	

	
	/**
	 * It searchs hotels with a variable list of services especified by the user
	 * 
	 * @param keyMap the services required
	 * @param attrList the hotels fields to retrieve
	 * @return the hotels that have all the requested services
	 * 
	 * @exception	NoResultsException 		when there are no hotels with the requested services
	 * 
	 * @exception	EmptyRequestException	when it doesn't receives services to filter
	 * 
	 * @exception	ClassCastException		when it doesn't receives an integer array 
	 * 
	 * @throws OntimizeJEERuntimeException
	 */
	
	
	
	@Override
	public EntityResult hotelsbyservicesQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			if (keyMap.get("services") == null) {
				throw new EmptyRequestException("SERVICES_REQUIRED");
			}

			List<Integer> servicesRequired = (List<Integer>) keyMap.get("services");

			StringBuilder sqlSentence = new StringBuilder(" WHERE ID_HOTEL IN ");

			for (int i = 0; i < servicesRequired.size(); i++) {
				if (i == 0) {
					sqlSentence.append(
							"(SELECT SVH_HOTEL FROM SERVICES_HOTEL WHERE SVH_SERVICE=" + servicesRequired.get(i) + ")");
				} else {
					sqlSentence.append(" AND ID_HOTEL IN (SELECT SVH_HOTEL FROM SERVICES_HOTEL WHERE SVH_SERVICE="
							+ servicesRequired.get(i) + ")");
				}
			}
			keyMap.remove("services");

			searchResult = this.daoHelper.query(this.hotelDao, keyMap, attrList, "HOTELS_BY_SERVICES",
					new ISQLQueryAdapter() {
						@Override
						public SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao,
								Map<?, ?> keysValues, Map<?, ?> validKeysValues, List<?> attributes,
								List<?> validAttributes, List<?> sort, String queryId) {
							return new SQLStatement(sqlStatement.getSQLStatement().concat(sqlSentence.toString()),
									sqlStatement.getValues());
						}
					});

			control.checkResults(searchResult);
		} catch (NoResultsException | EmptyRequestException e) {
			log.error("unable to retrieve a hotel filtered by services. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (ClassCastException e) {
			log.error("unable to retrieve a hotel filtered by services. Request : {} {} ",keyMap,attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Adds a new register on the hotels table. We assume that we are receiving the
	 * correct fields
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @return The id of the new hotel and a message with the operation result
	 */
	@Override
	public EntityResult hotelInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (attrMap.get("htl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("htl_email").toString());
			}
			insertResult = this.daoHelper.insert(this.hotelDao, attrMap);
			if (insertResult.isEmpty())
				throw new AllFieldsRequiredException("FIELDS_REQUIRED");
			insertResult.setMessage("SUCESSFUL_INSERTION");

		} catch (InvalidEmailException e) {
			log.error("unable to insert an hotel. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to insert an hotel. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, "HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert an hotel. Request : {} ",attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (AllFieldsRequiredException e) {
			log.error("unable to insert an hotel. Request : {} ",attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the hotels table. We assume that we are
	 * receiving the correct fields
	 * 
	 * @since 27/06/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult hotelUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			checkIfHotelExists(keyMap);
			if (attrMap.get("htl_email") != null) {
				control.checkIfEmailIsValid(attrMap.get("htl_email").toString());
			}
			updateResult = this.daoHelper.update(this.hotelDao, attrMap, keyMap);
			updateResult.setMessage("SUCESSFUL_UPDATE");
		} catch (InvalidEmailException e) {
			log.error("unable to update an hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to update an hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, "HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS");
		} catch (RecordNotFoundException e) {
			log.error("unable to update an hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to update an hotel. Request : {} {} ",keyMap,attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	private boolean checkIfHotelExists(Map<String, Object> keyMap) {
		if (keyMap.isEmpty()) {
			throw new RecordNotFoundException("ID_HOTEL_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_hotel");
		EntityResult existingHotel = this.daoHelper.query(hotelDao, keyMap, attrList);
		if (existingHotel.isEmpty())
			throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");
		return existingHotel.isEmpty();

	}

	private EntityResult filterHotelsByServices(EntityResult result, Map<String, Object> keyMap)
			throws InvalidRequestException {
		SearchValue requiredServices;
		Map<String, Object> servicesFilter = new HashMap<>();

		List<Object> values = (List<Object>) keyMap.get("services");

//		values.add(2);
//		values.add(3);	
//		requiredServices= new SearchValue(SearchValue.IN, values);		
//		servicesFilter.put("svh_service", requiredServices);	
//		result=EntityResultTools.dofilter(result, servicesFilter);

		return result;
	}

}