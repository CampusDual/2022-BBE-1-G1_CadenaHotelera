package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingExtraService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingExtraDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotEnoughExtrasException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This class listens the incoming requests related with the bookingExtra table
 * 
 * @since 20/07/2022
 * @version 1.0
 *
 */
@Service("BookingExtraService")
@Lazy
public class BookingExtraService implements IBookingExtraService{
	private Logger log;
	@Autowired
	private BookingExtraDao bookingExtraDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	@Autowired
	private IBookingExtraService bookingExtraService;

	private Control control;

	public BookingExtraService() {
		super();
		this.control = new Control();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	
	
	/**
	 * 
	 * Executes a generic query over the extraHotel table
	 * 
	 * @since 20/07/2022
	 * @param The filters and the fields of the query
	 * @exception NoResultsException     when there are not matching results on the
	 *                                   extraHotel table
	 * @exception BadSqlGrammarException when it receives an incorrect type in the
	 *                                   params
	 * @return The columns from the extraHotel table especified in the params and a
	 *         message with the operation result
	 * 
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult bookingextraQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.bookingExtraDao, keyMap, attrList);
			
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			log.error("unable to retrieve a booking extra .Request {} {} ",keyMap, attrList,e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a booking extra .Request {} {} ",keyMap, attrList,e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}
	
	
	

	/**
	 * 
	 * Adds a new register on the bookingExtra table.
	 * 
	 * @since 08/07/2022
	 * @param The fields of the new register
	 * @exception DataIntegrityViolationException when the params don't include the
	 *                                            not null fields
	 *                                            
	 * @exception EmptyRequestException           when the params are empty
	 * 
	 * @return The id of the new bookingExtra and a message with the operation result
	 */

	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult bookingextraInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			insertResult = this.daoHelper.insert(this.bookingExtraDao, attrMap);
			if (insertResult.isEmpty())
				throw new EmptyRequestException("FIELDS_REQUIRED");
			insertResult.setMessage("SUCESSFUL_INSERTION");
		}  catch (DataIntegrityViolationException e) {
			log.error("unable to save booking extra .Request {} ", attrMap,e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to save booking extra .Request {} ", attrMap,e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
		
	}

	
	/**
	 * Mark a variable quantity of extras associated with a concrete booking as enjoyed
	 * @since 26/07/2022
	 * @param the id of the bookingExtra and an integer as the quantity of extras to
	 *            mark as enjoyed
	 * 
	 * @return a confirmation message if the updates completes successfully or a
	 *         message indicating the error
	 * 
	 * @exception NotEnoughExtrasException when there aren't enough unenjoyed extras
	 *                                     to mark as enjoyed
	 * 
	 * @exception EmptyRequestException    when it doesn't receives the required
	 *                                     fields
	 * 
	 * @exception RecordNotFoundException  when it receives an unexisting
	 *                                     bookingExtra
	
	 * 
	 */
	
	@Override
	@Transactional
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult markextraenjoyedUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			
			checkIfExtraBookingExists(keyMap);

			if (attrMap.get("quantity") == null) {
				throw new EmptyRequestException("QUANTITY_FIELD_REQUIRED");
			}

			List<String> columnsExtraBooking = new ArrayList<>();
			columnsExtraBooking.add("bke_quantity");
			columnsExtraBooking.add("bke_enjoyed");

			searchResult = daoHelper.query(bookingExtraDao, keyMap, columnsExtraBooking);

			int bookingExtraQuantity = ((Integer) searchResult.getRecordValues(0).get("bke_quantity"));
			int bookingExtraEnjoyed = ((Integer) searchResult.getRecordValues(0).get("bke_enjoyed"));
			int pendingExtras = bookingExtraQuantity - bookingExtraEnjoyed;
			int extrasEnjoyed = (int) attrMap.get("quantity");
				

			if (pendingExtras - extrasEnjoyed < 0)
				throw new NotEnoughExtrasException("NOT_ENOUGH_PENDING_EXTRAS");

			Map<String, Object> bookingExtraUpdate = new HashMap<>();
			bookingExtraUpdate.put("bke_enjoyed", bookingExtraEnjoyed+extrasEnjoyed);
	
			daoHelper.update(bookingExtraDao, bookingExtraUpdate, keyMap);

			updateResult.setMessage("SUCCESSFULLY_UPDATED");

		} catch (RecordNotFoundException | EmptyRequestException | NotEnoughExtrasException e) {
			log.error("unable to mark an extra as enjoyed. Request : {} {}", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} 

		return updateResult;

	}

	
	private void checkIfExtraBookingExists(Map<String, Object> keyMap) {
		if (keyMap.isEmpty()) {
			throw new RecordNotFoundException("ID_BOOKING_EXTRA_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_booking_extra");
		EntityResult existingbookingExtra = daoHelper.query(bookingExtraDao, keyMap, attrList);
		if (existingbookingExtra.isEmpty())
			throw new RecordNotFoundException("BOOKING_EXTRA_DOESN'T_EXISTS");

	}

}
