package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingDao;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class builds the operations over the bookings table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */

@Service("BookingService")
@Lazy
public class BookingService implements IBookingService {

	@Autowired
	private BookingDao bookingDao;
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	/**
	 * 
	 * Executes a query over the bookings table filtered by client
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the bookings table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	public EntityResult clientbookingsQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = this.daoHelper.query(this.bookingDao, keyMap, attrList, "CLIENT_BOOKINGS");
		if (searchResult.getCode() == EntityResult.OPERATION_WRONG) {
			searchResult.setMessage("ERROR_WHILE_SEARCHING");
		}
		if (searchResult.isEmpty())
			searchResult.setMessage("THERE ARE NOT ACTIVE BOOKINGS ASSOCIATED WITH THAT CLIENT");
		return searchResult;
	}

	
	/**
	 * 
	 * Executes a query over the bookings table filtered by client and showings only
	 * the active bookings
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the bookings table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	public EntityResult clientactivebookingsQuery(Map<String, Object> keyMap, List<String> attrList) {
		EntityResult searchResult = this.daoHelper.query(this.bookingDao, keyMap, attrList, "CLIENT_ACTIVE_BOOKINGS");
		if (searchResult.getCode() == EntityResult.OPERATION_WRONG) {
			searchResult.setMessage("ERROR_WHILE_SEARCHING");
		}
		if (searchResult.isEmpty())
			searchResult.setMessage("THERE ARE NOT BOOKINGS ASSOCIATED WITH THAT CLIENT");
		return searchResult;
	}
	
	
	
	/**
	 * 
	 * Executes a generic query over the bookings table. It shows all the historic
	 * bookings, even the discharged ones
	 * 
	 * @since 27/06/2022
	 * @param The filters and the fields of the query
	 * @return The columns from the bookings table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	public EntityResult bookingQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = this.daoHelper.query(this.bookingDao, keyMap, attrList);
		if (searchResult.getCode() == EntityResult.OPERATION_WRONG) {
			searchResult.setMessage("ERROR_WHILE_SEARCHING");
		}
		if (searchResult.isEmpty())
			searchResult.setMessage("THERE ARE NOT RESULTS");
		return searchResult;
	}

	/**
	 * 
	 * Executes a query over the bookings table listing only the available rooms on
	 * a concrete hotel in a date range. We assume that we are receiving the correct
	 * fields and the dates range has been previously checked.
	 * 
	 * @since 30/06/2022
	 * @param The id of the hotel, the check-in and the check-out
	 * @return The available rooms filtered by hotel, with a calculated price
	 * for the selected dates
	 */
	@Override
	public EntityResult availableroomsQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult resultsByHotel = new EntityResultMapImpl();
		try {
			if (keyMap.get("bk_check_in") != null && keyMap.get("bk_check_out") != null
					&& keyMap.get("id_hotel") != null) {
				resultsByHotel = searchAvailableRooms(keyMap, attrList);

			} else {
				resultsByHotel = this.daoHelper.query(this.bookingDao, keyMap, attrList);
				resultsByHotel.setCode(EntityResult.OPERATION_WRONG);
				resultsByHotel.setMessage("ID_HOTEL, CHECK IN AND CHECK OUT FIELDS NEEDED");
			}
		} catch (ParseException e) {
			e.printStackTrace();
			EntityResult res = new EntityResultMapImpl();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
		if (resultsByHotel != null && resultsByHotel.getCode() == EntityResult.OPERATION_WRONG) {
			resultsByHotel.setMessage("ERROR_WHILE_SEARCHING");
		}
		return resultsByHotel;
	}

	/**
	 * 
	 * Search available rooms on a concrete hotel in a date range. It checks if it's
	 * receiving the needed params and it filters the occupied rooms
	 * 
	 * @since 30/06/2022
	 * @param The id of the hotel, the check-in date and the check-out dates and the
	 *            columns to send back
	 * @return The available rooms in a concrete hotel on a date range
	 */
	private EntityResult searchAvailableRooms(Map<String, Object> keyMap, List<String> attrList) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
		EntityResult result;
		final String checkIn = "bk_check_in";
		final String checkOut = "bk_check_out";
		final String hotelId = "id_hotel";

		String requestCheckIn = (String) keyMap.get(checkIn);
		String requestCheckOut = (String) keyMap.get(checkOut);
		keyMap.remove(checkIn);
		keyMap.remove(checkOut);

		Date startDate = formatter.parse(requestCheckIn);
		Date endDate = formatter.parse(requestCheckOut);	
				
		keyMap.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
				buildExpressionToSearchRooms(startDate, endDate));
		result = this.daoHelper.query(this.bookingDao, keyMap, attrList, "AVAILABLE_ROOMS");

		Map<String, Object> hotelFilter = new HashMap<>();
		hotelFilter.put(hotelId, keyMap.get(hotelId));
		return EntityResultTools.dofilter(result, hotelFilter);
	}


	/**
	 * 
	 * Builds a Basic expression to search the occupied rooms on a date range
	 * 
	 * @since 30/06/2022
	 * @param A check-in and a check-out date
	 * @return The Basic expression to search the occupied rooms
	 */
	private BasicExpression buildExpressionToSearchRooms(Date userCheckIn, Date userCheckOut) {

		BasicField bdCheckIn = new BasicField(BookingDao.ATTR_CHECK_IN);
		BasicField bdCheckOut = new BasicField(BookingDao.ATTR_CHECK_OUT);

		// Calcula que la fecha de entrada no esté incluida en una reserva existente
		BasicExpression b1 = new BasicExpression(bdCheckIn, BasicOperator.LESS_EQUAL_OP, userCheckIn);
		BasicExpression b2 = new BasicExpression(bdCheckOut, BasicOperator.MORE_OP, userCheckIn);
		BasicExpression rule1 = new BasicExpression(b1, BasicOperator.AND_OP, b2);

		// Calcula que la fecha de salida no esté incluida en una reserva existente
		BasicExpression b3 = new BasicExpression(bdCheckIn, BasicOperator.LESS_OP, userCheckOut);
		BasicExpression b4 = new BasicExpression(bdCheckOut, BasicOperator.MORE_EQUAL_OP, userCheckOut);
		BasicExpression rule2 = new BasicExpression(b3, BasicOperator.AND_OP, b4);

		// Calcula que la nueva reserva no incluya una reserva existente
		BasicExpression b5 = new BasicExpression(bdCheckIn, BasicOperator.MORE_EQUAL_OP, userCheckIn);
		BasicExpression b6 = new BasicExpression(bdCheckOut, BasicOperator.LESS_EQUAL_OP, userCheckOut);
		BasicExpression rule3 = new BasicExpression(b5, BasicOperator.AND_OP, b6);

		// Une las tres reglas anteriores para filtrar las reservas inválidas
		BasicExpression rule1_2 = new BasicExpression(rule1, BasicOperator.OR_OP, rule2);

		return new BasicExpression(rule1_2, BasicOperator.OR_OP, rule3);
	}

	/**
	 * 
	 * Adds a new register on the bookings table. We assume that we are receiving
	 * the correct fields and the dates range has been previously checked
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @return The id of the new register and a message with the operation result
	 */
	@Override
	public EntityResult bookingInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {	
		attrMap.put("bk_entry_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		attrMap.put("bk_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));		
		EntityResult insertResult = this.daoHelper.insert(this.bookingDao, attrMap);
		if (insertResult.getCode() !=EntityResult.OPERATION_SUCCESSFUL) {
			insertResult.setMessage("ERROR_WHILE_INSERTING");
		} else {
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		}
		return insertResult;
	}

	/**
	 * 
	 * Updates a existing register on the bookings table. We assume that we are
	 * receiving the correct fields and the dates range has been previously checked
	 * 
	 * @since 27/06/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult bookingUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		
		attrMap.put("bk_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));		
		EntityResult updateResult = this.daoHelper.update(this.bookingDao, attrMap, keyMap);
		if (updateResult.getCode() !=EntityResult.OPERATION_SUCCESSFUL) {
			updateResult.setMessage("ERROR_WHILE_UPDATING");
		} else {
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		}
		return updateResult;
	}

	/**
	 * 
	 * Deletes a existing register on the bookings table
	 * 
	 * @since 27/06/2022
	 * @param The id of the booking
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult bookingDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		Map<Object, Object> attrMap = new HashMap<>();
		attrMap.put("bk_leaving_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));	
		EntityResult deleteResult = new EntityResultMapImpl();		
		if (checkIfBookingExists(keyMap)) {
			deleteResult.setMessage("ERROR_BOOKING_NOT_FOUND");
			deleteResult.setCode(1);
		}
		else {				
			deleteResult=this.daoHelper.update(this.bookingDao, attrMap, keyMap);
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		}
		return deleteResult;
	}
	

	/**
	   * 
	   * Puts a leaving date on a client. If the client doesn't exists
	   * or has active reservations returns an error message
	   * 
	   * @since 05/07/2022
	   * @param The id of the client
	   * @return True if the client exists, false if it does't exists
	   */
	private boolean checkIfBookingExists(Map<String, Object> keyMap) {
		List<String> fields = new ArrayList<>();
		fields.add("id_booking");
		EntityResult existingBookings = daoHelper.query(bookingDao, keyMap, fields);	
		return existingBookings.isEmpty();
		
	}
	
	
	



}