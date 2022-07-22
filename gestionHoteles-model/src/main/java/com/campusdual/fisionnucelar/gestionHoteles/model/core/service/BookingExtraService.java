package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingExtraService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingExtraDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
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
	public EntityResult bookingextraQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.bookingExtraDao, keyMap, attrList);
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
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
	public EntityResult bookingextraInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			insertResult = this.daoHelper.insert(this.bookingExtraDao, attrMap);
			if (insertResult.isEmpty())
				throw new EmptyRequestException("FIELDS_REQUIRED");
			insertResult.setMessage("SUCESSFUL_INSERTION");
		}  catch (DataIntegrityViolationException e) {
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (EmptyRequestException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}


}
