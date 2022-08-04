package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.math.BigDecimal;


import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingExtraDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ClientDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraHotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidDateException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotEnoughExtrasException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.OccupiedRoomException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLStatement;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.gui.SearchValue;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger log;
	private Control control;

	@Autowired
	private RoomDao roomDao;

	@Autowired
	private BookingExtraDao bookingExtraDao;

	@Autowired
	private HotelDao hotelDao;

	@Autowired
	private ClientDao clientDao;

	@Autowired
	private ExtraHotelDao extraHotelDao;
	
	private UserControl userControl;

	Validator validator;
	Validator dataValidator;

	public BookingService() {
		super();
		this.control = new Control();
		this.validator = new Validator();
		this.dataValidator = new Validator();
		this.userControl=new UserControl();
		this.log = LoggerFactory.getLogger(this.getClass());
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
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult clientbookingsQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			userControl.controlAccessClient((int) keyMap.get("bk_client"));
			searchResult = searchBookingsByClient(keyMap, attrList, false);
		} catch (NotAuthorizedException e) {
			log.error("unable to retrieve bookings by client. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		}

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
	@Secured({ PermissionsProviderSecured.SECURED })
	@Override
	public EntityResult clientactivebookingsQuery(Map<String, Object> keyMap, List<String> attrList) {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			userControl.controlAccessClient((int) keyMap.get("bk_client"));
			searchResult = searchBookingsByClient(keyMap, attrList, true);
		} catch (NotAuthorizedException e) {
			log.error("unable to retrieve bookings by client. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		}
		return searchResult;
	}

	/**
	 * 
	 * Executes a query over the bookings table filtered by client. It returns the
	 * active bookings or all of them depending of the boolean param
	 * 
	 * @since 27/06/2022
	 * @param The filters, the fields of the query and a boolean indicating the
	 *            bookings we want to search
	 * @exception NoResultsException     Sends a message to the user when the list
	 *                                   of results is empty
	 * @exception BadSqlGrammarException sends a message to the user when he send a
	 *                                   string in a numeric field
	 * @return The columns from the bookings table especified in the params and a
	 *         message with the operation result
	 */

	private EntityResult searchBookingsByClient(Map<String, Object> keyMap, List<String> attrList, boolean onlyActive) {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = onlyActive ? daoHelper.query(this.bookingDao, keyMap, attrList, "CLIENT_ACTIVE_BOOKINGS")
					: daoHelper.query(this.bookingDao, keyMap, attrList, "CLIENT_BOOKINGS");

			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			log.error("unable to retrieve bookings by client. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve bookings by client. Request : {} {}", keyMap, attrList, e);
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
	 * @exception NoResultsException     Sends a message to the user when the list
	 *                                   of results is empty
	 * @exception BadSqlGrammarException sends a message to the user when he send a
	 *                                   string in a numeric field
	 * @return The columns from the bookings table especified in the params and a
	 *         message with the operation result
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult bookingQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {

			checkIfHotel(keyMap);
			userControl.controlAccess((int) keyMap.get("rm_hotel"));
			searchResult = daoHelper.query(bookingDao, keyMap, attrList);
			control.checkResults(searchResult);
		} catch (NoResultsException | NotAuthorizedException e) {
			log.error("unable to retrieve bookings. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve bookings. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
		}
		return searchResult;
	}

	/**
	 * 
	 * Executes a query over the bookings table listing only the available rooms on
	 * a concrete hotel in a date range.
	 * 
	 * @since 30/06/2022
	 * @param The id of the hotel, the check-in and the check-out
	 * 
	 * @exception AllFieldsRequiredException sends a message to the user when he is
	 *                                       not providing all the fields required
	 *                                       to execute the query
	 * @exception ParseException             sends a message to the user when the
	 *                                       method searchAvailableRooms is not
	 *                                       capable of parsing check_in or
	 *                                       check_out dates
	 * @exception BadSqlGrammarException     sends a message to the user when he
	 *                                       send a string in a numeric field
	 * 
	 * @exception InvalidDateException       when the check in date is after the
	 *                                       check out date or the dates are before
	 *                                       the current date
	 * 
	 * @exception InvalidRequestException    when the minprice is higher thant the
	 *                                       maxprice
	 * 
	 * @exception RecordNotFoundException    when it receives an unexisting hotel
	 * 
	 * @exception ClassCastException         when it receives a String instead of an
	 *                                       Numeric value
	 * 
	 * @return The available rooms filtered by hotel, with a calculated price for
	 *         the selected dates
	 */
	@Override
	public EntityResult availableroomsQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult resultsByHotel = new EntityResultMapImpl();
		try {
			checkAvailableRoomsFields(keyMap);
			checkIfHotelExists(keyMap);
			resultsByHotel = searchAvailableRooms(keyMap, attrList);

		} catch (AllFieldsRequiredException | InvalidDateException | RecordNotFoundException | InvalidRequestException
				| ParseException e) {
			log.error("unable to retrieve available rooms. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(resultsByHotel, e.getMessage());
		} catch (BadSqlGrammarException | ClassCastException e) {
			log.error("unable to retrieve available rooms. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(resultsByHotel, "INCORRECT_REQUEST");
		}
		return resultsByHotel;
	}

	/**
	 * Checks if the user is providing all the fields required to execute a query to
	 * obtain all available rooms in a given date
	 * 
	 * @exception AllFieldsRequiredException sends a message to the user when he is
	 *                                       not providing the neccesary fields to
	 *                                       execute the query
	 * @param keyMap the fields to be checked if are present or not
	 */

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
	 * @exception RecordNotFoundException shends a message to the user when he
	 *                                    shends a hotel id that doesn´t exists
	 * @exception BadSqlGrammarException  sends a message to the user when he send a
	 *                                    string in a numeric field
	 * @exception EmptyRequestException   sends a message to the user when he try to
	 *                                    execute an empty request
	 * @return The rooms with the chek-out on the current date filtered by hotel
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult todaycheckoutQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult result = new EntityResultMapImpl();
		try {

			checkIfHotel(keyMap);
			userControl.controlAccess((int) keyMap.get("rm_hotel"));
			result = daoHelper.query(bookingDao, keyMap, attrList, "TODAY_CHECKOUTS");
			control.checkResults(result);
		} catch (RecordNotFoundException | EmptyRequestException | NoResultsException | NotAuthorizedException e) {
			log.error("unable to retrieve today checkouts. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(result, e.getMessage());
		} catch (BadSqlGrammarException | ClassCastException e) {
			log.error("unable to retrieve today checkouts. Request : {} {}", keyMap, attrList, e);
			control.setErrorMessage(result, "INCORRECT_REQUEST");
		}
		return result;
	}

	/**
	 * Checks if rm_hotel exists in a request
	 * 
	 * @param keyMap
	 * @exception RecordNotFoundException if rm_hotel not exists in the user request
	 */
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
	 *            columns to send back to the user
	 * @return The available rooms in a concrete hotel on a date range
	 */

	private EntityResult searchAvailableRooms(Map<String, Object> keyMap, List<String> attrList)
			throws ParseException, ClassCastException, InvalidRequestException {
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

		Map<String, Object> keyMap2 = new HashMap<>();

		keyMap2.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
				buildExpressionToSearchRooms(startDate, endDate));

		long diff = endDate.getTime() - startDate.getTime();
		TimeUnit time = TimeUnit.DAYS;
		long days = time.convert(diff, TimeUnit.MILLISECONDS);

		result = this.daoHelper.query(this.bookingDao, keyMap2, attrList, "AVAILABLE_ROOMS", new ISQLQueryAdapter() {
			@Override
			public SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao, Map<?, ?> keysValues,
					Map<?, ?> validKeysValues, List<?> attributes, List<?> validAttributes, List<?> sort,
					String queryId) {
				return new SQLStatement(sqlStatement.getSQLStatement().replaceAll("#days#", Long.toString(days)),
						sqlStatement.getValues());
			}
		});

		result = filterBookingByPrice(result, keyMap);

		Map<String, Object> hotelFilter = new HashMap<>();
		hotelFilter.put(hotelId, keyMap.get(hotelId));

		if (keyMap.get(roomType) != null)
			hotelFilter.put(roomType, keyMap.get(roomType));

		result = EntityResultTools.dofilter(result, hotelFilter);

		return result;

	}

	/**
	 * It filters the available rooms using a maxprice, a minprice or both
	 * introduced by the user
	 * 
	 * @param result the available rooms
	 * @param keyMap the prices to filter
	 * @return the available rooms filtered by price
	 * @throws InvalidRequestException when the min price is higher than the max
	 *                                 price
	 */
	private EntityResult filterBookingByPrice(EntityResult result, Map<String, Object> keyMap)
			throws InvalidRequestException {
		SearchValue maxPrice;
		SearchValue minPrice;
		Map<String, Object> priceFilter = new HashMap<>();

		if ((keyMap.get("max_price") != null) && (keyMap.get("min_price") != null)) {
			if ((Integer) keyMap.get("max_price") < (Integer) keyMap.get("min_price"))
				throw new InvalidRequestException("MAXPRICE_MUST_BE_HIGHER_THAN_MINPRICE");
		}
		if (keyMap.get("max_price") != null) {
			maxPrice = new SearchValue(SearchValue.LESS, new BigDecimal((Integer) keyMap.get("max_price")));
			priceFilter.put("price", maxPrice);
			result = EntityResultTools.dofilter(result, priceFilter);
			priceFilter.remove("price");
		}

		if (keyMap.get("min_price") != null) {
			minPrice = new SearchValue(SearchValue.MORE, new BigDecimal((Integer) keyMap.get("min_price")));
			priceFilter.put("price", minPrice);
			result = EntityResultTools.dofilter(result, priceFilter);
			priceFilter.remove("price");
		}
		return result;
	}

	/**
	 * Checks if the given fdayes
	 * 
	 * @param startDate
	 * @param endDate
	 * @throws InvalidDateException Sends a message to the user when check_in or
	 *                              check_out fields are invalid
	 */
	private void checkDates(Date startDate, Date endDate) throws InvalidDateException {
		if (endDate.before(startDate) || endDate.equals(startDate)) {
			throw new InvalidDateException("CHECK_IN_MUST_BE_BEFORE_CHECK_OUT");
		}
		if (startDate.before(new Date(System.currentTimeMillis() - 86400000))) {
			throw new InvalidDateException("CHECK_IN_MUST_BE_EQUAL_OR_AFTER_CURRENT_DATE");
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
	public BasicExpression buildExpressionToSearchRooms(Date userCheckIn, Date userCheckOut) {

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
	 * Adds a new register on the bookings table.
	 * 
	 * @since 27/06/2022
	 * @param The fields of the new register
	 * @exception DuplicateKeyException           sends a message to the user when
	 *                                            he is trying to insert duplicate
	 *                                            foreign keys
	 * @exception DataIntegrityViolationException sends a message to the user when
	 *                                            he is trying to insert a null
	 *                                            value or an inexistent id in a
	 *                                            foreign key field
	 * @exception ClassCastException              sends a message to the user when
	 *                                            he is trying to insert an invalid
	 *                                            or inintelligible date
	 * @exception RecordNotFoundException         sends a message to the user when
	 *                                            he is trying to book with an non
	 *                                            active client id
	 * @exception InvalidDateException            Sends a message to the user when
	 *                                            check_in or check_out fields are
	 *                                            invalid
	 * @return The id of the new register and a message with the operation result
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult bookingInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {

		attrMap.put("bk_entry_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		attrMap.put("bk_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			if (attrMap.containsKey("bk_client")) {
				checkIfClientIsActive(attrMap);
			}
			if (attrMap.containsKey("bk_room")) {
				checkDisponibility(attrMap);
				calculateBookingPrice(attrMap);
			}
			insertResult = this.daoHelper.insert(this.bookingDao, attrMap);

			if (insertResult.isEmpty()) {
				throw new AllFieldsRequiredException("FIELDS_MUST_BE_PROVIDED");
			}
		} catch (DuplicateKeyException e) {
			log.error("unable to save booking. Request : {}", attrMap, e);
			control.setErrorMessage(insertResult, "ROOM_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to save booking. Request : {}", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (ClassCastException e) {
			log.error("unable to save booking. Request : {}", attrMap, e);
			control.setMessageFromException(insertResult, "CHECK_IN_AND_CHECK_OUT_MUST_BE_DATES");
		} catch (AllFieldsRequiredException | RecordNotFoundException | OccupiedRoomException | ParseException
				| InvalidDateException | EmptyRequestException e) {
			log.error("unable to save booking. Request : {}", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	/**
	 * Calculates the price of the booking based on the price of the room and
	 * check_in and check_out days
	 * 
	 * @param attrMap the id of the room and check_in, check_out dates
	 * @since 18/07/2022
	 */

	private void calculateBookingPrice(Map<String, Object> attrMap) {
		if (attrMap.get("bk_room") == null)
			throw new EmptyRequestException("bk_room_field_needed");

		Map<String, Object> filter = new HashMap<String, Object>();
		filter.put("id_room", attrMap.get("bk_room"));
		EntityResult roomResult = daoHelper.query(bookingDao, filter, Arrays.asList("rmt_price"), "SEARCH_ROOM_PRICE");

		if (roomResult.isEmpty()) {
			throw new RecordNotFoundException("BK_ROOM_OR_BK_CLIENT_DOESN'T EXISTS");
		}

		Date checkIn = (Date) attrMap.get("bk_check_in");
		Date checkOut = (Date) attrMap.get("bk_check_out");
		long diff = checkOut.getTime() - checkIn.getTime();
		TimeUnit time = TimeUnit.DAYS;
		long days = time.convert(diff, TimeUnit.MILLISECONDS);

		BigDecimal roomPrice = (BigDecimal) roomResult.getRecordValues(0).get("rmt_price");
		BigDecimal bookingDays = new BigDecimal(days);
		BigDecimal bookingPrice = bookingDays.multiply(roomPrice);
		attrMap.put("bk_price", bookingPrice);
	}

	/**
	 * Adds and extra to the given booking and updates the booking extras price. It
	 * also creates a detailed register in the bookingExtra table
	 * 
	 * @param the id of the booking, the id of the extra and an integer as a
	 *            quantity
	 * @return a confirmation message if the updates completes successfully or a
	 *         message indicating the error
	 * @exception RecordNotFoundException         sends a message to the user if
	 *                                            id_booking not exists
	 * @exception EmptyRequestException           sends a message to the user if he
	 *                                            sends anm empty request
	 * @exception ClassCastException              sends a message to the user if
	 *                                            sends null or srting values in a
	 *                                            numeric field
	 * @exception DataIntegrityViolationException sends a message to the user if he
	 *                                            sends an inexistent or null
	 *                                            foreign key
	 * 
	 */
	@Override
	@Transactional
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult addbookingextraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			checkIfBookingActive(keyMap);
			EntityResult existingBooking = this.daoHelper.query(bookingDao, keyMap, Arrays.asList("bk_client"));
			userControl.controlAccessClient((int) existingBooking.getRecordValues(0).get("bk_client"));

			dataValidator.checkDataUpdateExtraPrice(attrMap);
			updateResult = daoHelper.update(bookingDao, calculateAndInsertExtra(attrMap, keyMap), keyMap);
			updateResult.setMessage("SUCCESSFULLY_ADDED");
		} catch (RecordNotFoundException | EmptyRequestException | NotAuthorizedException e) {
			log.error("unable to add an extra to a booking. Request : {} {}", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (ClassCastException | BadSqlGrammarException e) {
			log.error("unable to add an extra to a booking. Request : {} {}", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "INCORRECT_REQUEST");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to add an extra to a booking. Request : {} {}", keyMap, attrMap, e);
			control.setMessageFromException(updateResult, e.getMessage());
		}

		return updateResult;
	}

	/**
	 * Cancels a variable quantity of extras associated with a concrete booking,
	 * updating the booking extra price
	 * 
	 * @param the id of the bookingExtra and an integer as the quantity of extras to
	 *            cancel
	 * 
	 * @return a confirmation message if the updates completes successfully or a
	 *         message indicating the error
	 * 
	 * @exception NotEnoughExtrasException when there aren't enough unenjoyed extras
	 *                                     to cancel
	 * 
	 * @exception EmptyRequestException    when it doesn't receives the required
	 *                                     fields
	 * 
	 * @exception RecordNotFoundException  when it receives an unexisting
	 *                                     bookingExtra
	 * 
	 * @exception ClassCastException       sends when it receives a String instead a
	 *                                     number
	 * 
	 * @exception BadSqlGrammarException   when it receives an incorrect type for a
	 *                                     field
	 * 
	 * 
	 */

	@Override
	@Transactional
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult cancelbookingextraUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult hotelResult = new EntityResultMapImpl();
		try {


			checkIfExtraBookingExists(keyMap);
			dataValidator.checkIfMapIsEmpty(attrMap);
			hotelResult = daoHelper.query(bookingDao, keyMap, Arrays.asList("rm_hotel"), "SEARCH_BOOKING_EXTRA_HOTEL");
			userControl.controlAccess((int) hotelResult.getRecordValues(0).get("rm_hotel"));
			if (attrMap.get("quantity") == null) {
				throw new EmptyRequestException("QUANTITY_FIELD_REQUIRED");
			}

			List<String> columnsExtraBooking = new ArrayList<>();
			columnsExtraBooking.add("id_booking_extra");
			columnsExtraBooking.add("bke_booking");
			columnsExtraBooking.add("bke_name");
			columnsExtraBooking.add("bke_quantity");
			columnsExtraBooking.add("bke_unit_price");
			columnsExtraBooking.add("bke_total_price");
			columnsExtraBooking.add("bke_enjoyed");
			columnsExtraBooking.add("bk_extras_price");

			updateResult = daoHelper.query(bookingExtraDao, keyMap, columnsExtraBooking, "BOOKINGEXTRA_DATA");

			int bookingExtraQuantity = ((Integer) updateResult.getRecordValues(0).get("bke_quantity"));
			int bookingExtraEnjoyed = ((Integer) updateResult.getRecordValues(0).get("bke_enjoyed"));
			int pendingExtras = bookingExtraQuantity - bookingExtraEnjoyed;
			int extrasToCancel = (int) attrMap.get("quantity");
			BigDecimal unitExtraPrice = (BigDecimal) updateResult.getRecordValues(0).get("bke_unit_price");
			BigDecimal totalExtraPrice = (BigDecimal) updateResult.getRecordValues(0).get("bke_total_price");

			if (pendingExtras - extrasToCancel < 0)
				throw new NotEnoughExtrasException("NOT_ENOUGH_PENDING_EXTRAS_TO_CANCEL");

			Map<String, Object> bookingExtraUpdate = new HashMap<>();
			bookingExtraUpdate.put("bke_quantity", bookingExtraQuantity - extrasToCancel);
			BigDecimal canceledExtrasPrice = unitExtraPrice.multiply(BigDecimal.valueOf(extrasToCancel));
			BigDecimal newTotalExtraPrice = totalExtraPrice.subtract(canceledExtrasPrice);
			bookingExtraUpdate.put("bke_total_price", newTotalExtraPrice);
			daoHelper.update(bookingExtraDao, bookingExtraUpdate, keyMap);

			BigDecimal oldBookingExtraPrice = (BigDecimal) updateResult.getRecordValues(0).get("bk_extras_price");
			BigDecimal newBookingExtraPrice = oldBookingExtraPrice.subtract(canceledExtrasPrice);

			Map<String, Object> bookingKeymap = new HashMap<>();
			Map<String, Object> bookingAttrMap = new HashMap<>();

			bookingKeymap.put("id_booking", updateResult.getRecordValues(0).get("bke_booking"));
			bookingAttrMap.put("bk_extras_price", newBookingExtraPrice);
			updateResult = daoHelper.update(bookingDao, bookingAttrMap, bookingKeymap);
			updateResult.setMessage("SUCCESSFULLY_CANCELED");

		} catch (RecordNotFoundException | EmptyRequestException | NotEnoughExtrasException
				| NotAuthorizedException e) {
			log.error("unable to cancel an extra to a booking. Request : {} {}", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (ClassCastException | BadSqlGrammarException e) {
			log.error("unable to cancel an extra to a booking. Request : {} {}", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "INCORRECT_REQUEST");
		}

		return updateResult;

	}

	/**
	 * Calculates the price of the extra added to the booking based on the extra id
	 * and a quantity. It also insert a new register in the bookingExtra table
	 * 
	 * @param attrMap the quantity and the id of the extra
	 * @param keyMap  the id of the booking
	 * 
	 * @exception RecordNotFoundException when the extra doesn't exists
	 * 
	 * @return a hashpMap with the field price updated, ready to update in the
	 *         bookings table
	 */

	public Map<String, Object> calculateAndInsertExtra(Map<String, Object> attrMap, Map<String, Object> keyMap) {
		BigDecimal unitExtraPrice, bookingExtraPrice, updatedExtraPrice, quantity, totalExtraPrice;
		EntityResult extraResult;

		EntityResult bookingExtrasPriceResult = new EntityResultMapImpl();
		List<String> columnsBooking = new ArrayList<>();
		columnsBooking.add("bk_extras_price");
		columnsBooking.add("bk_room");

		bookingExtrasPriceResult = daoHelper.query(bookingDao, keyMap, columnsBooking);
		bookingExtraPrice = (BigDecimal) bookingExtrasPriceResult.getRecordValues(0).get("bk_extras_price");

		Map<String, Object> keyMapRoom = new HashMap<>();
		keyMapRoom.put("id_room", bookingExtrasPriceResult.getRecordValues(0).get("bk_room"));

		EntityResult hotel = daoHelper.query(roomDao, keyMapRoom, Arrays.asList("rm_hotel"));

		Map<String, Object> filter = new HashMap<>();
		filter.put("id_extras_hotel", attrMap.get("id_extras_hotel"));
		filter.put("exh_hotel", hotel.getRecordValues(0).get("rm_hotel"));

		List<String> columns = new ArrayList<>();
		columns.add("exh_price");
		columns.add("ex_name");
		columns.add("exh_active");

		extraResult = this.daoHelper.query(extraHotelDao, filter, columns, "BOOKING_EXTRA_DATA");
		if (extraResult.isEmpty()) {
			throw new RecordNotFoundException("EXTRA_IN_HOTEL_NOT_FOUND");
		} else if ((Integer) extraResult.getRecordValues(0).get("exh_active") != 1) {
			throw new RecordNotFoundException("EXTRA_IS_NOT_ACTIVE");
		}

		unitExtraPrice = (BigDecimal) extraResult.getRecordValues(0).get("exh_price");
		String nameExtra = (String) extraResult.getRecordValues(0).get("ex_name");
		quantity = new BigDecimal((int) attrMap.get("quantity"));
		totalExtraPrice = unitExtraPrice.multiply(quantity);
		updatedExtraPrice = bookingExtraPrice.add(totalExtraPrice);

		HashMap<String, Object> attrMapBookingExtra = new HashMap<>();
		attrMapBookingExtra.put("bke_booking", keyMap.get("id_booking"));
		attrMapBookingExtra.put("bke_name", nameExtra);
		attrMapBookingExtra.put("bke_quantity", quantity);
		attrMapBookingExtra.put("bke_unit_price", unitExtraPrice);
		attrMapBookingExtra.put("bke_total_price", totalExtraPrice);

		daoHelper.insert(bookingExtraDao, attrMapBookingExtra);

		Map<String, Object> finalPrice = new HashMap<>();
		finalPrice.put("bk_extras_price", updatedExtraPrice);
		return finalPrice;

	}

	/**
	 * Changes the dates for a concrete booking
	 * 
	 * @param the id of the booking and the new dates
	 * 
	 * @return a confirmation message if the update completes successfully or a
	 *         message indicating the error
	 * 
	 * 
	 * @exception EmptyRequestException   when it doesn't receives the required
	 *                                    fields
	 * 
	 * @exception RecordNotFoundException when it receives an unexisting booking
	 * 
	 * @exception ParseException          when it receives dates with an incorrect
	 *                                    type
	 * 
	 * @exception InvalidDateException    when the received check in is before the
	 *                                    current date or after the received
	 *                                    check-out
	 * 
	 * @exception SQLWarningException     when it doesn't receives an id_booking
	 * 
	 * 
	 */

	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult changedatesUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		Map<String, Object> mapLeavingDate = new HashMap<>();
		EntityResult updateResult = new EntityResultMapImpl();
		EntityResult bookingResult = new EntityResultMapImpl();

		EntityResult result = new EntityResultMapImpl();
		result = daoHelper.query(bookingDao, keyMap, Arrays.asList("bk_client", "rm_hotel"),"SEARCH_BOOKING_HOTEL");
		try {
					
			if(!userControl.controlAccessClient((int) result.getRecordValues(0).get("bk_client"))){
				userControl.controlAccess((int) result.getRecordValues(0).get("rm_hotel"));
			}		

			checkIfBookingActive(keyMap);
			mapLeavingDate.put("bk_leaving_date", new Date(System.currentTimeMillis()));
			daoHelper.update(bookingDao, mapLeavingDate, keyMap);

			bookingResult = daoHelper.query(bookingDao, keyMap, Arrays.asList("bk_room"));
			attrMap.put("bk_room", bookingResult.getRecordValues(0).get("bk_room"));

			checkDisponibility(attrMap);
			calculateBookingPrice(attrMap);

			attrMap.put("bk_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));

			updateResult = this.daoHelper.update(this.bookingDao, attrMap, keyMap);
			updateResult.setMessage("SUCCESSFUL_UPDATE");

		} catch (InvalidDateException | OccupiedRoomException | EmptyRequestException | NotAuthorizedException
				| RecordNotFoundException e) {
			log.error("unable to update a booking. Request : {} {}", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		} catch (ParseException e) {
			log.error("unable to update a booking. Request : {} {}", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "INVALID_DATE_FORMAT");
		} finally {
			try {
				mapLeavingDate.put("bk_leaving_date", null);
				daoHelper.update(bookingDao, mapLeavingDate, keyMap);
			} catch (SQLWarningException e) {
				log.error("unable to update a booking. Request : {} {}", keyMap, attrMap, e);
				updateResult.setMessage("BOOKING_DOESN'T_EXISTS");
			}
		}
		return updateResult;
	}

//	/**
//	 * 
//	 * 
//	 * TO DO, NOT FUNCIONAL
//	 * 
//	 * @since 27/06/2022
//	 * @param The fields to be updated
//	 * @return A message with the operation result
//	 * @exception RecordNotFoundException         sends a message to the user if the
//	 *                                            id_booking not exists
//	 * @exception DataIntegrityViolationException sends a message to the user if he
//	 *                                            is trying to insert a null or
//	 *                                            inexistent foreign key
//	 */
//	@Override
//	public EntityResult bookingUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
//			throws OntimizeJEERuntimeException {
//
//		attrMap.put("bk_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
//		EntityResult updateResult = new EntityResultMapImpl();
//		try {
//			checkIfBookingExists(keyMap);
//			updateResult = this.daoHelper.update(this.bookingDao, attrMap, keyMap);
//			if (updateResult.getCode() != EntityResult.OPERATION_SUCCESSFUL) {
//				updateResult.setMessage("ERROR_WHILE_UPDATING");
//			} else {
//				updateResult.setMessage("SUCCESSFUL_UPDATE");
//			}
//		} catch (RecordNotFoundException e) {
//			log.error("unable to update a booking. Request : {} {}", keyMap, attrMap, e);
//			control.setErrorMessage(updateResult, e.getMessage());
//		} catch (DataIntegrityViolationException e) {
//			log.error("unable to update a booking. Request : {} {}", keyMap, attrMap, e);
//			control.setMessageFromException(updateResult, e.getMessage());
//		}
//		return updateResult;
//	}

	/**
	 * 
	 * Deletes a existing register on the bookings table
	 * 
	 * @since 27/06/2022
	 * @param The id of the booking
	 * @return A message with the operation result
	 * @exception RecordNotFoundException sends a message to the user if the
	 *                                    id_booking not exists
	 */
	@Override
	@Secured({ PermissionsProviderSecured.SECURED })
	public EntityResult bookingDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {

		EntityResult bookingHotel = daoHelper.query(bookingDao, keyMap, Arrays.asList("rm_hotel"),
				"SEARCH_BOOKING_HOTEL");
		EntityResult deleteResult = new EntityResultMapImpl();
		try {
			userControl.controlAccess((int) bookingHotel.getRecordValues(0).get("rm_hotel"));

			Map<Object, Object> attrMap = new HashMap<>();
			attrMap.put("bk_leaving_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));

			if (checkIfBookingExists(keyMap)) {
				deleteResult.setMessage("ERROR_BOOKING_NOT_FOUND");
				deleteResult.setCode(1);
			} else {
				deleteResult = this.daoHelper.update(this.bookingDao, attrMap, keyMap);
				deleteResult.setMessage("SUCCESSFUL_DELETE");
			}
		} catch (RecordNotFoundException | NotAuthorizedException e) {
			control.setErrorMessage(deleteResult, e.getMessage());
		}
		return deleteResult;
	}

	/**
	 * 
	 * checks if a room has not bookings in the given dates
	 * 
	 * @since 05/07/2022
	 * @param The id of the client
	 * @return True if the client exists, false if it does't exists
	 * @exception OccupiedRoomException sends a message to the user if the given
	 *                                  room is ocuppied in the request dates
	 * @exception InvalidDateException  sends a message to the user if he is
	 *                                  providing an invalid dates, valid dates are
	 *                                  defined in checkDates method
	 */
	private void checkDisponibility(Map<String, Object> attrMap) throws ParseException, InvalidDateException {
		Map<String, Object> filter = new HashMap<>();
		filter.put("bk_room", attrMap.get("bk_room"));
		EntityResult result;

		if (attrMap.get("bk_check_in") == null || attrMap.get("bk_check_out") == null) {
			throw new EmptyRequestException("CHECK_IN_AND_CHECK_OUT_REQUIRED");
		}

		Date startDate = (Date) attrMap.get("bk_check_in");
		Date endDate = (Date) attrMap.get("bk_check_out");
		checkDates(startDate, endDate);

		List<String> columns = new ArrayList<>();
		columns.add("bk_room");

		filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
				buildExpressionToSearchRooms(startDate, endDate));

		result = daoHelper.query(bookingDao, filter, columns, "CHECK_ROOM_DISPONIBILITY");

		if (!result.isEmpty()) {
			throw new OccupiedRoomException("OCCUPIED_ROOM");
		}

	}

	/**
	 * Checks if the provid id_booking exists in bookings table
	 * 
	 * @param keyMap the id of the booking
	 * @return true if the booking exists in bookings table
	 * @exception RecordNotFoundException sends a message to the user if the
	 *                                    provided id_booking not exists in bookings
	 *                                    table
	 */
	private boolean checkIfBookingExists(Map<String, Object> keyMap) {
		if (keyMap.isEmpty()) {
			throw new RecordNotFoundException("ID_BOOKING_REQUIRED");
		}
		EntityResult existingBooking = this.daoHelper.query(bookingDao, keyMap,
				Arrays.asList("id_booking", "bke_leaving_date"));
		if (existingBooking.isEmpty())
			throw new RecordNotFoundException("BOOKING_DOESN'T_EXISTS");
		return existingBooking.isEmpty();

	}

	private boolean checkIfBookingActive(Map<String, Object> keyMap) {
		if (keyMap.isEmpty()) {
			throw new RecordNotFoundException("ID_BOOKING_REQUIRED");
		}
		EntityResult existingBooking = this.daoHelper.query(bookingDao, keyMap,
				Arrays.asList("id_booking", "bk_leaving_date"));
		if (existingBooking.isEmpty())
			throw new RecordNotFoundException("BOOKING_DOESN'T_EXISTS");

		if (existingBooking.getRecordValues(0).get("bk_leaving_date") != null) {
			throw new RecordNotFoundException("BOOKING_ISN'T_ACTIVE");
		}
		return existingBooking.isEmpty();

	}

	private void checkIfExtraBookingExists(Map<String, Object> keyMap) {
		if (keyMap.isEmpty()) {
			throw new RecordNotFoundException("ID_EXTRA_BOOKING_REQUIRED");
		}
		List<String> attrList = new ArrayList<>();
		attrList.add("id_booking_extra");
		EntityResult existingbookingExtra = this.daoHelper.query(bookingExtraDao, keyMap, attrList);
		if (existingbookingExtra.isEmpty())
			throw new RecordNotFoundException("BOOKING_EXTRA_DOESN'T_EXISTS");

	}

	/**
	 * Checks if the id_extra_hotel provid by the user exists in extras_hotel table
	 * 
	 * @param keyMap the id of extras hotel
	 * @return true if the id_extras_hotel provid by the user exists in extras_hotel
	 *         table
	 * @exception RecordNotFoundException sends a message to the user if the
	 *                                    provided id_extras_hotel doesn´t exists in
	 *                                    extras_hotel table
	 */

	private boolean checkIfExtraHotelExists(Map<String, Object> keyMap) {

		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("id_extras_hotel", keyMap.get("id_extras_hotel"));

		List<String> attrList = new ArrayList<>();
		attrList.add("id_extras_hotel");

		EntityResult existingExtraHotel = this.daoHelper.query(extraHotelDao, attrMap, attrList);

		if (existingExtraHotel.isEmpty())
			throw new RecordNotFoundException("EXTRA_HOTEL_DOESN'T_EXISTS");
		return existingExtraHotel.isEmpty();

	}

	/**
	 * Checks if the client provided by the user exists in clients tavble
	 * 
	 * @param attrMap the id_client
	 * @return true id the id provided by the users exists in clients table
	 * @exception RecordNotFoundException sends a message to the user if the
	 *                                    provided id_client not exists in clients
	 *                                    table
	 */
	private boolean checkIfClientExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_client");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_client", attrMap.get("bk_client"));
		EntityResult existingClient = this.daoHelper.query(clientDao, keyMap, attrList);
		if (existingClient.isEmpty())
			throw new RecordNotFoundException("CLIENT_DOESN'T_EXISTS");
		return existingClient.isEmpty();
	}

	/**
	 * checks if the provided client is marked as active in clients table
	 * 
	 * @param keyMap the id of the client
	 * @return true if the client is marked as active
	 * @exception RecordNotFoundException sends a message to the user if the client
	 *                                    provided is not active
	 */
	private boolean checkIfClientIsActive(Map<String, Object> keyMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_client");
		attrList.add("cl_leaving_date");
		Map<String, Object> attrMap = new HashMap<>();
		attrMap.put("id_client", keyMap.get("bk_client"));
		EntityResult activeClient = this.daoHelper.query(clientDao, attrMap, attrList);

		if (activeClient.getRecordValues(0).get("cl_leaving_date") != null) {
			throw new RecordNotFoundException("CLIENT_IS_NOT_ACTIVE");
		}
		return true;
	}

	/**
	 * Checks if the provided id_room is present in rooms table
	 * 
	 * @param attrMap the id of the room
	 * @return true id the provided id_room exists in rooms table
	 * @exception ecordNotFoundException sends a message to the user if the provided
	 *                                   room doesn´t exists in rooms table
	 */
	private boolean checkIfRoomExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_room", attrMap.get("bk_room"));
		EntityResult existingRoom = this.daoHelper.query(roomDao, keyMap, attrList);
		if (existingRoom.isEmpty())
			throw new RecordNotFoundException("ROOM_DOESN'T_EXISTS");
		return existingRoom.isEmpty();
	}

	private void checkIfHotelExists(Map<String, Object> attrMap) {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_hotel");
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_hotel", attrMap.get("id_hotel"));
		EntityResult existingHotel = this.daoHelper.query(hotelDao, keyMap, attrList);
		if (existingHotel.isEmpty())
			throw new RecordNotFoundException("HOTEL_DOESN'T_EXISTS");

	}

}