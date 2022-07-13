package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.math.BigDecimal;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IClientService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IExtraHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidDateException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.OccupiedRoomException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
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

	private Control control;

	@Autowired
	private IRoomService roomService;

	@Autowired
	private IClientService clientService;

	@Autowired
	private IExtraHotelService extraHotelService;

	public BookingService() {
		super();
		this.control = new Control();
	}

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
		return searchBookingsByClient(keyMap, attrList, false);
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
		return searchBookingsByClient(keyMap, attrList, true);
	}

	/**
	 * 
	 * Executes a query over the bookings table filtered by client. It returns the
	 * active bookings or all of them depending of the boolean param
	 * 
	 * @since 27/06/2022
	 * @param The filters, the fields of the query and a boolean indicating the
	 *            bookings we want to search
	 * @return The columns from the bookings table especified in the params and a
	 *         message with the operation result
	 */
	private EntityResult searchBookingsByClient(Map<String, Object> keyMap, List<String> attrList, boolean onlyActive) {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = onlyActive ? daoHelper.query(this.bookingDao, keyMap, attrList, "CLIENT_ACTIVE_BOOKINGS")
					: daoHelper.query(this.bookingDao, keyMap, attrList, "CLIENT_BOOKINGS");

			searchResult = this.daoHelper.query(this.bookingDao, keyMap, attrList, "CLIENT_ACTIVE_BOOKINGS");

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
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = daoHelper.query(bookingDao, keyMap, attrList);
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
	 * Executes a query over the bookings table listing only the available rooms on
	 * a concrete hotel in a date range. We assume that we are receiving the correct
	 * fields and the dates range has been previously checked.
	 * 
	 * @since 30/06/2022
	 * @param The id of the hotel, the check-in and the check-out
	 * @return The available rooms filtered by hotel, with a calculated price for
	 *         the selected dates
	 */
	@Override
	public EntityResult availableroomsQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult resultsByHotel = new EntityResultMapImpl();
		try {
			checkAvailableRoomsFields(keyMap);
			resultsByHotel = searchAvailableRooms(keyMap, attrList);
		} catch (AllFieldsRequiredException | InvalidDateException e) {
			control.setErrorMessage(resultsByHotel, e.getMessage());
		} catch (ParseException e) {
			control.setErrorMessage(resultsByHotel, e.getMessage());
		} catch (BadSqlGrammarException e) {
			control.setErrorMessage(resultsByHotel, "INCORRECT_REQUEST");
		}

		return resultsByHotel;
	}

	private void checkAvailableRoomsFields(Map<String, Object> keyMap) {
		if (keyMap.get("bk_check_in") == null || keyMap.get("bk_check_out") == null || keyMap.get("id_hotel") == null) {
			throw new AllFieldsRequiredException("CHECK_IN_CHECK_OUT_AND HOTEL_NEEDED");
		}

	}

	/**
	 * 
	 * Searchs the rooms with the checkout on the current date on a concrete hotel
	 * 
	 * @since 12/07/2022
	 * @param The id of the hotel
	 * @return The rooms with the chek-out on the current date filtered by hotel
	 */
	public EntityResult todaycheckoutQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult result = new EntityResultMapImpl();
		try {
			checkIfHotel(keyMap);
			result = daoHelper.query(bookingDao, keyMap, attrList, "TODAY_CHECKOUTS");
			control.checkResults(result);
		} catch (RecordNotFoundException e) {
			control.setErrorMessage(result, e.getMessage());
		} catch (EmptyRequestException e) {
			control.setErrorMessage(result, e.getMessage());
		} catch (NoResultsException e) {
			control.setErrorMessage(result, e.getMessage());
		} catch (BadSqlGrammarException e) {
			control.setErrorMessage(result, "INCORRECT_REQUEST");
		}
		return result;
	}

	private void checkIfHotel(Map<String, Object> keyMap) {
		if (keyMap.get("rm_hotel") == null) {
			throw new RecordNotFoundException("RM_HOTEL_NEEDED");
		}

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
		final String roomType = "id_room_type";

		String requestCheckIn = (String) keyMap.get(checkIn);
		String requestCheckOut = (String) keyMap.get(checkOut);
		keyMap.remove(checkIn);
		keyMap.remove(checkOut);

		Date startDate = formatter.parse(requestCheckIn);
		Date endDate = formatter.parse(requestCheckOut);

		checkDates(startDate, endDate);

		keyMap.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
				buildExpressionToSearchRooms(startDate, endDate));
		result = this.daoHelper.query(this.bookingDao, keyMap, attrList, "AVAILABLE_ROOMS");

		Map<String, Object> hotelFilter = new HashMap<>();
		hotelFilter.put(hotelId, keyMap.get(hotelId));

		if (keyMap.get(roomType) != null)
			hotelFilter.put(roomType, keyMap.get(roomType));

		return EntityResultTools.dofilter(result, hotelFilter);
	}

	private void checkDates(Date startDate, Date endDate) throws InvalidDateException {
		if (endDate.before(startDate) || endDate.equals(startDate)) {
			throw new InvalidDateException("CHECK_IN_MUST_BE_BEFORE_CHECK_OUT");
		}
		if (startDate.before(new Date(System.currentTimeMillis() - 86400000))) {
			throw new InvalidDateException("CHECK_IN_MUST_BE_EQUAL_OR_AFTER_CURRENT_DATE");
		}
	}

	private void checkDisponibility(Map<String, Object> attrMap) throws ParseException, InvalidDateException {
		Map<String, Object> filter = new HashMap<>();
		filter.put("bk_room", attrMap.get("bk_room"));
		EntityResult result;

		Date startDate = (Date) attrMap.get("bk_check_in");
		Date endDate = (Date) attrMap.get("bk_check_out");
		checkDates(startDate, endDate);

		List<String> columns = new ArrayList<>();
		columns.add("bk_room");

		filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
				buildExpressionToSearchRooms(startDate, endDate));

		result = this.daoHelper.query(this.bookingDao, filter, columns, "CHECK_ROOM_DISPONIBILITY");

		if (!result.isEmpty()) {
			throw new OccupiedRoomException("OCCUPIED_ROOM");
		}

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
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (attrMap.containsKey("bk_client")) {
				checkIfClientExists(attrMap);
				checkIfClientIsActive(attrMap);
			}
			if (attrMap.containsKey("bk_room"))
				checkIfRoomExists(attrMap);
			checkDisponibility(attrMap);
			insertResult = this.daoHelper.insert(this.bookingDao, attrMap);

			if (insertResult.isEmpty()) {
				throw new AllFieldsRequiredException(null);
			}
		} catch (DuplicateKeyException e) {
			control.setErrorMessage(insertResult, "ROOM_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			control.setErrorMessage(insertResult, "ROOM_CLIENT_CHECK_IN_AND_CHECK_OUT_REQUIRED");
		} catch (AllFieldsRequiredException | RecordNotFoundException | OccupiedRoomException | ParseException
				| InvalidDateException e) {
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	@Override
	public EntityResult bookingextraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfBookingExists(keyMap);
			checkDataUpdateExtraPrice(attrMap);
			checkIfExtraHotelExists(attrMap);
			updateResult = daoHelper.update(bookingDao, calculateExtraPrice(attrMap, keyMap), keyMap);
		} catch (RecordNotFoundException | EmptyRequestException e) {
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (ClassCastException e) {
			control.setErrorMessage(updateResult, "INCORRECT_REQUEST");
		}

		return updateResult;

	}

	private void checkDataUpdateExtraPrice(Map<String, Object> attrMap) {
		if (attrMap.get("id_extras_hotel") == null || attrMap.get("quantity") == null) {
			throw new EmptyRequestException("ID_EXTRAS_AND_QUANTITY_REQUIRED");
		}
	}

	public Map<String, Object> calculateExtraPrice(Map<String, Object> attrMap, Map<String, Object> keyMap) {
		BigDecimal unitExtraPrice, bookingExtraPrice, updatedExtraPrice, quantity, totalExtraPrice;
		EntityResult extraPriceResult = new EntityResultMapImpl();

		EntityResult bookingExtrasPriceResult = new EntityResultMapImpl();
		List<String> columnsBooking = new ArrayList<>();
		columnsBooking.add("bk_extras_price");
		bookingExtrasPriceResult = daoHelper.query(bookingDao, keyMap, columnsBooking);
		bookingExtraPrice = (BigDecimal) bookingExtrasPriceResult.getRecordValues(0).get("bk_extras_price");

		Map<String, Object> filter = new HashMap<>();
		filter.put("id_extras_hotel", attrMap.get("id_extras_hotel"));
		List<String> columns = new ArrayList<>();
		columns.add("exh_price");

		extraPriceResult = extraHotelService.extrahotelQuery(filter, columns);
		unitExtraPrice = (BigDecimal) extraPriceResult.getRecordValues(0).get("exh_price");

		quantity = new BigDecimal((int) attrMap.get("quantity"));
		totalExtraPrice = unitExtraPrice.multiply(quantity);
		updatedExtraPrice = bookingExtraPrice.add(totalExtraPrice);

		Map<String, Object> finalPrice = new HashMap<>();
		finalPrice.put("bk_extras_price", updatedExtraPrice);
		return finalPrice;

	}

	/**
	 * 
	 * 
	 * TO DO, NOT FUNCIONAL
	 * 
	 * @since 27/06/2022
	 * @param The fields to be updated
	 * @return A message with the operation result
	 */
	@Override
	public EntityResult bookingUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {

		attrMap.put("bk_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));

		checkIfDataIsEmpty(attrMap);

		EntityResult updateResult = this.daoHelper.update(this.bookingDao, attrMap, keyMap);
		if (updateResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
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
		} else {
			deleteResult = this.daoHelper.update(this.bookingDao, attrMap, keyMap);
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		}
		return deleteResult;
	}

	/**
	 * 
	 * Puts a leaving date on a client. If the client doesn't exists or has active
	 * reservations returns an error message
	 * 
	 * @since 05/07/2022
	 * @param The id of the client
	 * @return True if the client exists, false if it does't exists
	 */
	private boolean checkIfBookingExists(Map<String, Object> keyMap) {
		if (keyMap.isEmpty()) {
			throw new RecordNotFoundException("ID_BOOKING_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_booking");
		EntityResult existingBooking = this.daoHelper.query(bookingDao, keyMap, attrList);
		if (existingBooking.isEmpty())
			throw new RecordNotFoundException("BOOKING_DOESN'T_EXISTS");
		return existingBooking.isEmpty();

	}

	private boolean checkIfExtraHotelExists(Map<String, Object> keyMap) {

		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("id_extras_hotel", keyMap.get("id_extras_hotel"));

		List<String> attrList = new ArrayList<>();
		attrList.add("id_extras_hotel");

		EntityResult existingExtraHotel = extraHotelService.extrahotelQuery(attrMap, attrList);

		if (existingExtraHotel.isEmpty())
			throw new RecordNotFoundException("EXTRA_HOTEL_DOESN'T_EXISTS");
		return existingExtraHotel.isEmpty();

	}

	private boolean checkIfClientExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_client");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_client", attrMap.get("bk_client"));
		EntityResult existingClient = clientService.clientQuery(keyMap, attrList);
		if (existingClient.isEmpty())
			throw new RecordNotFoundException("CLIENT_DOESN'T_EXISTS");
		return existingClient.isEmpty();
	}

	private boolean checkIfClientIsActive(Map<String, Object> keyMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_client");
		attrList.add("cl_leaving_date");
		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("id_client", keyMap.get("bk_client"));
		EntityResult activeClient = clientService.clientQuery(attrMap, attrList);

		if (activeClient.getRecordValues(0).get("cl_leaving_date") != null) {
			throw new RecordNotFoundException("CLIENT_IS_NOT_ACTIVE");
		}
		return true;
	}

	private boolean checkIfRoomExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_room", attrMap.get("bk_room"));
		EntityResult existingRoom = roomService.roomQuery(keyMap, attrList);
		if (existingRoom.isEmpty())
			throw new RecordNotFoundException("ROOM_DOESN'T_EXISTS");
		return existingRoom.isEmpty();
	}

	private void checkIfDataIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.get("bk_check_in") == null && attrMap.get("bk_room") == null && attrMap.get("bk_room") == null
				&& attrMap.get("bk_client") == null && attrMap.get("bk_price") == null
				&& attrMap.get("bk_entry_date") == null && attrMap.get("bk_last_update") == null
				&& attrMap.get("bk_leaving_date") == null && attrMap.get("bk_extras_price") == null) {
			throw new EmptyRequestException("EMPTY_REQUEST");
		}
	}

	private void checkHotelIsEmpty(Map<String, Object> attrMap) {
		if (attrMap.get("rm_hotel") == null) {
			throw new EmptyRequestException("HOTEL_NEEDED");
		}
	}

	private void checkIfEmpty(EntityResult result) {
		if (result.isEmpty()) {
			throw new RecordNotFoundException("WITHOUT_RESULTS");
		}

	}

}