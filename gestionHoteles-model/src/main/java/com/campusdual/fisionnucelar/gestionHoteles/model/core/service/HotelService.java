package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.SloppyMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.annotation.Secured;
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
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.ApiKey;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.GooglePlaces;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.Place;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.PlacesResult;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLStatement;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
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
		this.dataValidator = new Validator();
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
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult hotelQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.hotelDao, keyMap, attrList);
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			log.error("unable to retrieve a hotel. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a hotel. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
			e.printStackTrace();
		}
		return searchResult;
	}

	/**
	 * It searchs hotels with a variable list of services especified by the user
	 * 
	 * @param keyMap   the services required
	 * @param attrList the hotels fields to retrieve
	 * @return the hotels that have all the requested services
	 * 
	 * @exception NoResultsException    when there are no hotels with the requested
	 *                                  services
	 * 
	 * @exception EmptyRequestException when it doesn't receives services to filter
	 * 
	 * @exception ClassCastException    when it doesn't receives an integer array
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
							"(SELECT SVH_HOTEL FROM SERVICES_HOTEL WHERE SVH_SERVICE=" + servicesRequired.get(i) + " AND SVH_ACTIVE=1)");
				} else {
					sqlSentence.append(" AND ID_HOTEL IN (SELECT SVH_HOTEL FROM SERVICES_HOTEL WHERE SVH_SERVICE="
							+ servicesRequired.get(i) + " AND SVH_ACTIVE=1)");
				}
			}
			keyMap.remove("services");

			searchResult = daoHelper.query(hotelDao, keyMap, attrList, "", new ISQLQueryAdapter() {
				@Override
				public SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao, Map<?, ?> keysValues,
						Map<?, ?> validKeysValues, List<?> attributes, List<?> validAttributes, List<?> sort,
						String queryId) {
					return new SQLStatement(sqlStatement.getSQLStatement().concat(sqlSentence.toString()),
							sqlStatement.getValues());
				}
			});

			control.checkResults(searchResult);
		} catch (NoResultsException | EmptyRequestException e) {
			log.error("unable to retrieve a hotel filtered by services. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (ClassCastException e) {
			log.error("unable to retrieve a hotel filtered by services. Request : {} {} ", keyMap, attrList, e);
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
	@Secured({ PermissionsProviderSecured.SECURED })
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
			log.error("unable to insert an hotel. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to insert an hotel. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert an hotel. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (AllFieldsRequiredException e) {
			log.error("unable to insert an hotel. Request : {} ", attrMap, e);
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
	@Secured({ PermissionsProviderSecured.SECURED })
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
			log.error("unable to update an hotel. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to update an hotel. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS");
		} catch (RecordNotFoundException e) {
			log.error("unable to update an hotel. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to update an hotel. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	/**
	 * It searchs hotels in an concrete area, using a latitude, a longitude and a
	 * radius
	 * 
	 * @param keyMap the longitude, the latitude and the radius to the search
	 * @return the hotels in the requested area
	 * 
	 * @exception NoResultsException    when there are no hotels in the selected
	 *                                  area
	 * 
	 * @exception EmptyRequestException when it doesn't receives latitude, longitude
	 *                                  or the radius
	 * 
	 * @exception ClassCastException    when it receives an incorrect type
	 * 
	 * @throws OntimizeJEERuntimeException
	 */
	@Override
	public EntityResult searchbylocationQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {

		EntityResult hotelResult = daoHelper.query(hotelDao, new HashMap<>(),
				Arrays.asList("id_hotel", "htl_name", "htl_phone", "htl_email", "htl_latitude", "htl_longitude"));
		EntityResult searchResult = new EntityResultMapImpl();
		double distance;

		try {

			if (keyMap.get("latitude") == null || keyMap.get("longitude") == null || keyMap.get("radius") == null) {
				throw new EmptyRequestException("LONGITUDE_LATITUDE_AND_RADIUS_REQUIRED");
			}

			SloppyMath x = new SloppyMath();
			Double latitude = (Double) keyMap.get("latitude");
			Double longitude = (Double) keyMap.get("longitude");
			Integer radius = (Integer) keyMap.get("radius") * 1000;

			for (int i = 0; i < hotelResult.calculateRecordNumber(); i++) {
				Map<String, Object> record = hotelResult.getRecordValues(i);
				BigDecimal hotelLatitude = (BigDecimal) hotelResult.getRecordValues(i).get("htl_latitude");
				BigDecimal hotelLongitude = (BigDecimal) hotelResult.getRecordValues(i).get("htl_longitude");
				distance = x.haversinMeters(latitude, longitude, hotelLatitude.doubleValue(),
						hotelLongitude.doubleValue());
				if (distance < radius) {
					record.put("distance", Math.round(distance / 1000));
					searchResult.addRecord(record);
				}
			}
			if (searchResult.isEmpty()) {
				control.setErrorMessage(searchResult, "NO_HOTELS_IN_REQUESTED_AREA");
			}
		} catch (ClassCastException e) {
			log.error("unable to search by location. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_TYPE");
		} catch (EmptyRequestException e) {
			log.error("unable to search by location. Request : {} {} ", keyMap, attrList, e);
			control.setMessageFromException(searchResult, e.getMessage());
		}
		return searchResult;
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

	

	/**
	 * It searchs hotels in an area using a city as reference. It recover
	 * the longitude and latitude from google maps api. If can receive just the
	 * location or also the region
	 * 
	 * @param keyMap the location and the radius to the search. Optional: the region of the location
	 * @return the hotels in the requested area
	 * 
	 * @exception EmptyRequestException when it doesn't receives the location or the radius
	 * 
	 * @exception ClassCastException    when it receives an incorrect type
	 * 
	 * @exception RecordNotFoundException		when it doesn't found any city or region with that matches the location param
	 * 
	 * @exception InvalidRequestException	when it founds more than one location	
	 * 
	 * @throws OntimizeJEERuntimeException
	 */
	
	
	@Override
	public EntityResult searchbycityQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {

		EntityResult queryResult = new EntityResultMapImpl();
		try {
			if (keyMap.get("location") == null || keyMap.get("radius") == null) {
				throw new EmptyRequestException("LOCATION_AND_RADIUS_REQUIRED");
			}
			
			int counter=0;
			String placeName = (String) keyMap.get("location");
					
			if(keyMap.get("region")!=null) {
				placeName = placeName + " "+ (String)keyMap.get("region");
			}
			
			GooglePlaces googleSearch = new GooglePlaces(ApiKey.KEY_GOOGLE_PLACES);
			keyMap.remove("location");
			PlacesResult result = googleSearch.searchText(placeName);
			
			for (Place place : result) {
				if (place.getTypes().contains("political")) {
					counter++;
					keyMap.put("latitude", Double.valueOf(place.getGeometry().getLocation().getLat()));
					keyMap.put("longitude", Double.valueOf(place.getGeometry().getLocation().getLng()));
				}
			}		
			if (counter==1) {
				queryResult = searchbylocationQuery(keyMap, new ArrayList<>());
			} else if(counter==0 && keyMap.get("region")==null){
				throw new RecordNotFoundException("NO_RESULTS_TRY_ADDING_REGION");
			}else if (counter==0) {
				throw new RecordNotFoundException("NO_RESULTS");
			}else {
				throw new InvalidRequestException("TOO_MANY_COINCIDENCES_TRY_TO_ADD_REGION_OR_COUNTRY");
			}
		} catch (RecordNotFoundException | EmptyRequestException|InvalidRequestException e) {
			log.error("unable to search by city. Request : {} {} ", keyMap,attrList, e);
			control.setErrorMessage(queryResult, e.getMessage());
		} catch (ClassCastException e) {
			log.error("unable to search by city. Request : {} {} ", keyMap,attrList, e);
			control.setErrorMessage(queryResult, "INVALID_TYPE");
		} 
		return queryResult;
	}

}