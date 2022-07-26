package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.math.BigDecimal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.*;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.BookingTestData.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockedStatic.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.*;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.*;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.SQLStatement;
import com.ontimize.jee.common.dto.*;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.dao.IOntimizeDaoSupport;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
	@InjectMocks
	BookingService bookingService;
	@Mock
	BookingDao bookingDao;
	@Mock
	ClientDao clientDao;
	@Mock
	RoomDao roomDao;
	@Mock
	private ExtraHotelDao extraHotelDao;
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@BeforeEach
	void setUp() {
		this.bookingService = new BookingService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Test for Booking queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class BookingQuery {

		@Test
		@DisplayName("Obtain all data from bookings table")
		void when_queryOnlyWithAllColumns_return_allHotelData() {
			doReturn(getAllBookingData()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = bookingService.bookingQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = bookingService.bookingQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", "string");
			List<String> columns = new ArrayList<>();
			columns.add("htl_name");
			columns.add("htl_email");
			when(daoHelper.query(bookingDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = bookingService.bookingQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Hotels table when ID is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_HOTEL", 2);
				}
			};
			List<String> attrList = Arrays.asList("ID_BOOKING", "BK_CHECK_IN", "BK_CHECK_OUT", "BK_PRICE", "BK_ROOM",
					"BK_CLIENT", "BK_EXTRAS_PRICE");
			doReturn(getSpecificBookingData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = bookingService.bookingQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get(HotelDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Hotels table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_HOTEL", 5);
				}
			};
			List<String> attrList = Arrays.asList("ID_BOOKING", "BK_CHECK_IN", "BK_CHECK_OUT", "BK_PRICE", "BK_ROOM",
					"BK_CLIENT", "BK_EXTRAS_PRICE");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificBookingData(keyMap, attrList));
			EntityResult entityResult = bookingService.bookingQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(0, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@ParameterizedTest(name = "Obtain data with ID -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from HOTELS table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_HOTEL", random);
				}
			};
			List<String> attrList = Arrays.asList("ID_HOTEL", "HTL_NAME");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificBookingData(keyMap, attrList));
			EntityResult entityResult = bookingService.bookingQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get(HotelDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		List<Integer> randomIDGenerator() {
			List<Integer> list = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				list.add(ThreadLocalRandom.current().nextInt(0, 3));
			}
			return list;
		}

		@Nested
		@DisplayName("Test for Client bookings")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		public class ClientBookingsQuery {
			@Test
			@DisplayName("Obtain all bookings for a given client")
			void search_bookings_by_client_success() {
				Map<String, Object> filter = new HashMap<>();
				filter.put("bk_client", 10);
				List<String> columns = getGenericColumns();
				EntityResult clientBookings = getGenericBookingER();
				Calendar calendar = Calendar.getInstance();
				calendar.set(2022, 10, 10);
				Date checkIn = calendar.getTime();
				calendar.set(2022, 11, 11);
				Date checkOut = calendar.getTime();
				when(daoHelper.query(bookingDao, filter, columns, "CLIENT_BOOKINGS")).thenReturn(clientBookings);
				EntityResult queryResult = bookingService.clientbookingsQuery(filter, columns);
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
				assertEquals(1, queryResult.calculateRecordNumber());

			}

			@Test
			@DisplayName("Obtain all active bookings for a given client")
			void search_active_bookings_by_client_success() {
				Map<String, Object> filter = new HashMap<>();
				filter.put("bk_client", 10);
				List<String> columns = getGenericColumns();
				EntityResult clientBookings = getGenericBookingER();
				Calendar calendar = Calendar.getInstance();
				calendar.set(2022, 10, 10);
				Date checkIn = calendar.getTime();
				calendar.set(2022, 11, 11);
				Date checkOut = calendar.getTime();
				when(daoHelper.query(bookingDao, filter, columns, "CLIENT_ACTIVE_BOOKINGS")).thenReturn(clientBookings);
				EntityResult queryResult = bookingService.clientactivebookingsQuery(filter, columns);
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
				assertEquals(1, queryResult.calculateRecordNumber());

			}

			@Test
			@DisplayName("search client active bookings without results")
			void search_active_bookings_by_client_no_results() {
				Map<String, Object> filter = new HashMap<>();
				filter.put("bk_client", 10);
				List<String> columns = getGenericColumns();
				EntityResult clientBookings = new EntityResultMapImpl();
				when(daoHelper.query(bookingDao, filter, columns, "CLIENT_ACTIVE_BOOKINGS")).thenReturn(clientBookings);
				EntityResult queryResult = bookingService.clientactivebookingsQuery(filter, columns);
				assertEquals("NO_RESULTS", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
				assertEquals(0, queryResult.calculateRecordNumber());
			}

			@Test
			@DisplayName("search client bookings without results")
			void search_bookings_by_client_no_results() {
				Map<String, Object> filter = new HashMap<>();
				filter.put("bk_client", 10);
				List<String> columns = getGenericColumns();
				EntityResult clientBookings = new EntityResultMapImpl();
				when(daoHelper.query(bookingDao, filter, columns, "CLIENT_BOOKINGS")).thenReturn(clientBookings);
				EntityResult queryResult = bookingService.clientbookingsQuery(filter, columns);
				assertEquals("NO_RESULTS", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
				assertEquals(0, queryResult.calculateRecordNumber());
			}
		}

		@Nested
		@DisplayName("Test for today checkouts query")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		public class todayCkeckoutsQuery {

			@Test
			@DisplayName("Obtain all rooms with checkout in the current day")
			void test_todayCheckoutQuery_success() {
				Map<String, Object> filter = new HashMap<>();
				filter.put("rm_hotel", 1);
				List<String> columns = new ArrayList<>();
				columns.add("id_room");
				columns.add("rm_room_type");
				columns.add("rm_hotel");
				columns.add("rm_number");
				EntityResult roomsCheckoutToday = new EntityResultMapImpl(columns);
				roomsCheckoutToday.addRecord(new HashMap<String, Object>() {
					{
						put("id_room", 2);
						put("rm_number", 401);
						put("rm_room_type", 1);
						put("rm_hotel", 2);
					}
				});

				when(daoHelper.query(bookingDao, filter, columns, "TODAY_CHECKOUTS")).thenReturn(roomsCheckoutToday);

				EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);

				// assertEquals("INCORRECT_REQUEST", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
			}

			@Test
			@DisplayName("todayCheckoutQuery without rm_hotel")
			void test_todayCheckoutQuery_without_rm_hotel() {
				Map<String, Object> filter = new HashMap<>();
				List<String> columns = new ArrayList<>();

				EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);

				assertEquals("RM_HOTEL_NEEDED", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
			}

			@Test
			@DisplayName("todayCheckoutQuery empty response")
			void test_todayCheckoutQuery_empty_response() {
				Map<String, Object> filter = new HashMap<>();
				List<String> columns = new ArrayList<>();
				EntityResult todayCheckouts = new EntityResultMapImpl();
				EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);
				assertEquals("RM_HOTEL_NEEDED", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
			}

			@Test
			@DisplayName("todayCheckoutQuery with rm_hotel as string")
			void test_todayCheckoutQuery_with_rm_hotel_as_string() {
				Map<String, Object> filter = new HashMap<>();
				filter.put("rm_hotel", "invalid");
				List<String> columns = new ArrayList<>();
				when(daoHelper.query(bookingDao, filter, columns, "TODAY_CHECKOUTS"))
						.thenThrow(BadSqlGrammarException.class);
				EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);

				assertEquals("INCORRECT_REQUEST", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
			}

			@Test
			@DisplayName("todayCheckoutQuery without results")
			void test_todayCheckoutQuery_without_results() {
				Map<String, Object> filter = new HashMap<>();
				filter.put("rm_hotel", 1);
				List<String> columns = new ArrayList<>();
				EntityResult emptyResult = new EntityResultMapImpl();
				when(daoHelper.query(bookingDao, filter, columns, "TODAY_CHECKOUTS")).thenReturn(emptyResult);
				EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);

				assertEquals("NO_RESULTS", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
			}
		}

		@Nested
		@DisplayName("Test booking insert")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		public class test_booking_insert {
			@Test
			@DisplayName("insert booking succesfully")
			void test_booking_insert_success() {
				Map<String, Object> dataToInsert = getDataToInsert();
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = new EntityResultMapImpl();
				EntityResult clientResult = new EntityResultMapImpl();
				clientResult.addRecord(new HashMap<String, Object>() {
					{
						put("id_client", 2);
					}
				});
				EntityResult roomResult = new EntityResultMapImpl();
				roomResult.addRecord(new HashMap<String, Object>() {
					{
						put("id_room", 2);
						put("rmt_price", new BigDecimal(40));
					}
				});
				EntityResult activeClientResult = new EntityResultMapImpl();
				when(daoHelper.insert(bookingDao, dataToInsert)).thenReturn(insertResult);
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return disponibilityResult;
						if (count == 2)
							return roomResult;
						if (count == 3)
							return roomResult;
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
				assertEquals(2, queryResult.getRecordValues(0).get("id_booking"));

			}

			@Test
			@DisplayName("insert booking fails due client is not active")
			void test_booking_insert_not_active_client() {
				Map<String, Object> dataToInsert = getDataToInsert();
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = new EntityResultMapImpl();
				EntityResult clientResult = new EntityResultMapImpl();
				EntityResult activeClientResult = getClientResult();
				EntityResult roomResult = getRoomResult();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("CLIENT_IS_NOT_ACTIVE", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());

			}

			@Test
			@DisplayName("insert booking fails due room is occupied")
			void test_booking_insert_occupied_room() {
				Map<String, Object> dataToInsert = getDataToInsert();
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = getRoomResult();
				EntityResult clientResult = new EntityResultMapImpl();
				EntityResult activeClientResult = new EntityResultMapImpl();
				EntityResult roomResult = getRoomResult();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
				when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(disponibilityResult);
				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("OCCUPIED_ROOM", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());

			}

			@Test
			@DisplayName("insert booking with check_out before check_in")
			void test_booking_insert_with_check_out_before_check_in() {
				Map<String, Object> dataToInsert = new HashMap<>();
				Calendar c = Calendar.getInstance();
				c.set(2022, 10, 10);
				Date checkIn = c.getTime();
				c.set(2022, 9, 10);
				Date checkOut = c.getTime();
				java.util.Date date = c.getTime();
				dataToInsert.put("bk_check_in", checkIn);
				dataToInsert.put("bk_check_out", checkOut);
				dataToInsert.put("bk_room", 2);
				dataToInsert.put("bk_client", 2);
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = getRoomResult();
				EntityResult clientResult = new EntityResultMapImpl();
				EntityResult activeClientResult = new EntityResultMapImpl();
				EntityResult roomResult = getRoomResult();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("CHECK_IN_MUST_BE_BEFORE_CHECK_OUT", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());

			}

			@Test
			@DisplayName("insert booking with date before current date")
			void test_booking_insert_with_dates_before_current_date() {
				Map<String, Object> dataToInsert = new HashMap<>();
				Calendar c = Calendar.getInstance();
				c.set(2020, 8, 10);
				Date checkIn = c.getTime();
				c.set(2020, 9, 10);
				Date checkOut = c.getTime();
				java.util.Date date = c.getTime();
				dataToInsert.put("bk_check_in", checkIn);
				dataToInsert.put("bk_check_out", checkOut);
				dataToInsert.put("bk_room", 2);
				dataToInsert.put("bk_client", 2);
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = getRoomResult();
				EntityResult clientResult = new EntityResultMapImpl();
				EntityResult activeClientResult = new EntityResultMapImpl();
				EntityResult roomResult = getRoomResult();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("CHECK_IN_MUST_BE_EQUAL_OR_AFTER_CURRENT_DATE", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());

			}

			@Test
			@DisplayName("insert booking with invalid dates")
			void test_booking_insert_with_dates_with_invalid_dates() {
				Map<String, Object> dataToInsert = new HashMap<>();
				dataToInsert.put("bk_check_in", "invalid");
				dataToInsert.put("bk_check_out", "invalid");
				dataToInsert.put("bk_room", 2);
				dataToInsert.put("bk_client", 2);
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = getRoomResult();
				EntityResult clientResult = new EntityResultMapImpl();
				EntityResult activeClientResult = new EntityResultMapImpl();
				EntityResult roomResult = getRoomResult();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("CHECK_IN_AND_CHECK_OUT_MUST_BE_DATES", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());

			}

			@Test
			@DisplayName("insert booking with empty request")
			void test_booking_insert_with_dates_with_empty_request() {
				Map<String, Object> emptyMap = new HashMap<>();
				EntityResult emptyResult = new EntityResultMapImpl();
				when(daoHelper.insert(any(), anyMap())).thenReturn(emptyResult);
				EntityResult insertResult = bookingService.bookingInsert(emptyMap);
				assertEquals("FIELDS_MUST_BE_PROVIDED", insertResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, insertResult.getCode());
			}

		}

		@Nested
		@DisplayName("Test booking update")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		public class test_booking_update {
			@Test
			@DisplayName("update booking succesfully")
			void test_booking_update_success() {
				Map<String, Object> attrMap = getGenericAttrMap();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult result = new EntityResultMapImpl();
				result.setCode(0);
				EntityResult queryResult = getGenericBookingER();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(result);
				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				assertEquals("SUCCESSFUL_UPDATE", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, updateResult.getCode());

			}

			@Test
			@DisplayName("update booking without id_booking")
			void test_booking_update_empty_without_id_booking() {
				Map<String, Object> attrMap = new HashMap<>();
				Map<String, Object> keyMap = new HashMap<>();
				EntityResult queryResult = new EntityResultMapImpl();
				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				assertEquals("ID_BOOKING_REQUIRED", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}

			@Test
			@DisplayName("update booking without body request")
			void test_booking_update_empty_without_body_request() {
				Map<String, Object> attrMap = new HashMap<>();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult bookingQuery = getGenericBookingER();
				EntityResult queryResult = new EntityResultMapImpl();
				queryResult.setCode(1);
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				assertEquals("ERROR_WHILE_UPDATING", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}
		}

		@Nested
		@DisplayName("Test change dates update")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		public class test_change_dates_update {
			@Test
			@DisplayName("update booking succesfully")
			void test_booking_update_success() {
				Map<String, Object> attrMap = getGenericAttrMap();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult result = new EntityResultMapImpl();
				EntityResult er = new EntityResultMapImpl();
				er.addRecord(new HashMap<String, Object>() {
					{
						put("id_booking", 2);
						put("bk_room", 2);
						put("bk_check_in", "2000-11-11");
						put("bk_check_out", "2000-12-12");
						put("bk_price", 100);
						put("rmt_price",new BigDecimal(3));
					}
				});
	
				
				EntityResult emptyEr = new EntityResultMapImpl();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(er);
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(result);
				
				
				
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return emptyEr;
						if (count == 2)
							return er;
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("SUCCESSFUL_UPDATE", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, updateResult.getCode());

			}

			@Test
			@DisplayName("update booking without id_booking")
			void test_booking_update_empty_without_id_booking() {
				Map<String, Object> attrMap = new HashMap<>();
				Map<String, Object> keyMap = new HashMap<>();
				EntityResult queryResult = new EntityResultMapImpl();
				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				assertEquals("ID_BOOKING_REQUIRED", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}

			@Test
			@DisplayName("update booking without body request")
			void test_booking_update_empty_without_body_request() {
				Map<String, Object> attrMap = new HashMap<>();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult bookingQuery = getGenericBookingER();
				EntityResult queryResult = new EntityResultMapImpl();
				queryResult.setCode(1);
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				assertEquals("ERROR_WHILE_UPDATING", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}
		}

		@Nested
		@DisplayName("Test booking delete")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		public class test_booking_delete {
			@Test
			@DisplayName("delete booking succesfully")
			void test_booking_delete_success() {
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult bookingQuery = getGenericBookingER();
				EntityResult queryResult = new EntityResultMapImpl();
				queryResult.setCode(0);
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				assertEquals("SUCCESSFUL_DELETE", deleteResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, deleteResult.getCode());

			}

			@Test
			@DisplayName("delete booking without id_booking")
			void test_booking_delete_without_id_booking() {
				Map<String, Object> keyMap = new HashMap<>();
				EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				assertEquals("ID_BOOKING_REQUIRED", deleteResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, deleteResult.getCode());
			}

			@Test
			@DisplayName("delete booking with a booking that doesn´t exists")
			void test_booking_delete_booking_not_exists() {
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult bookingQuery = new EntityResultMapImpl();
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				assertEquals("BOOKING_DOESN'T_EXISTS", deleteResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, deleteResult.getCode());

			}
		}
	}

	@Nested
	@DisplayName("Test booking extra update")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class test_booking_extra_update {
		@Test
		@DisplayName("update an extra successfully")
		void test_booking_extra_update_success() {
			Map<String, Object> attrMap = new HashMap<>();
			attrMap.put("id_extras_hotel", 1);
			attrMap.put("quantity", 2);

			Map<String, Object> keyMap = getBookingKeyMap();

			EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("bk_extras_price"));
			bookingResult.addRecord(new HashMap<String, Object>() {
				{
					put("bk_extras_price", 25);
				}
			});

			EntityResult extrasPriceResult = new EntityResultMapImpl(Arrays.asList("exh_price", "exh_name"));
			extrasPriceResult.addRecord(new HashMap<String, Object>() {
				{
					put("exh_price", new BigDecimal(50));
					put("exh_name", "niñero");
				}
			});

			EntityResult extraResult = new EntityResultMapImpl(Arrays.asList("bk_extras_price"));
			extraResult.addRecord(new HashMap<String, Object>() {
				{
					put("bk_extras_price", new BigDecimal(25));
				}
			});

			EntityResult queryResult = new EntityResultMapImpl();
			queryResult.setCode(0);

			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1) {
						return bookingResult;
					}
					if (count == 2) {
						return extraResult;
					}
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(extrasPriceResult);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
			EntityResult updateResult = bookingService.addbookingextraUpdate(attrMap, keyMap);
			assertEquals("SUCCESSFULLY_ADDED", updateResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, updateResult.getCode());
		}

		@Test
		@DisplayName("update an extra with a booking that doesnt exists")
		void test_booking_extra_booking_not_exists() {
			Map<String, Object> attrMap = new HashMap<>();
			attrMap.put("id_extras_hotel", 1);
			attrMap.put("quantity", 2);

			Map<String, Object> keyMap = getBookingKeyMap();

			EntityResult bookingResult = new EntityResultMapImpl();

			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			EntityResult updateResult = bookingService.addbookingextraUpdate(attrMap, keyMap);
			assertEquals("BOOKING_DOESN'T_EXISTS", updateResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
		}

		@Test
		@DisplayName("update an extra with invalid data")
		void test_booking_extra_booking_invalid_data() {
			Map<String, Object> attrMap = new HashMap<>();
			attrMap.put("id_extras_hotel", 1);
			attrMap.put("quantity", "ss");

			Map<String, Object> keyMap = getBookingKeyMap();

			EntityResult bookingResult = new EntityResultMapImpl();
			bookingResult.addRecord(new HashMap<String, Object>() {
				{
					put("bk_extras_price", new BigDecimal(50));
				}
			});

			EntityResult extrasPriceResult = new EntityResultMapImpl(Arrays.asList("bk_extras_price"));
			extrasPriceResult.addRecord(new HashMap<String, Object>() {
				{
					put("exh_price", new BigDecimal(2));
					put("exh_name", "niñero");
				}
			});

			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(extrasPriceResult);
			EntityResult updateResult = bookingService.addbookingextraUpdate(attrMap, keyMap);
			assertEquals("INCORRECT_REQUEST", updateResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
		}

		@Test
		@DisplayName("cancel an extra with succesfully")
		void test_cancel_booking_extra() {
			Map<String, Object> keyMap = getBookingKeyMap();
			EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("id_booking"));
			Map<String, Object> attrMap = getBKExtraAttrMap();
			List<String> columnsExtraBooking = getExtraBookingColumns();
			EntityResult bookingExtraResult = getBookingExtraResult();
			EntityResult updateResult = new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(bookingExtraResult);
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("SUCCESSFULLY_ADDED", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, dischargeResult.getCode());

		}

		@Test
		@DisplayName("cancel an extra fail due not enough available extras to cancel")
		void test_cancel_booking_fail_due_not_available() {
			EntityResult updateResult = new EntityResultMapImpl();
			updateResult.setCode(EntityResult.OPERATION_WRONG);
			Map<String, Object> keyMap = getBookingKeyMap();
			EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("id_booking"));
			Map<String, Object> attrMap = getBKExtraAttrMap();
			List<String> columnsExtraBooking = getExtraBookingColumns();
			EntityResult bookingExtraResult = new EntityResultMapImpl();
			bookingExtraResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_bookingextra", 2);
					put("bke_booking", 2);
					put("bke_name", "Niñero");
					put("bke_quantity", 2);
					put("bke_unit_price", new BigDecimal(200));
					put("bke_total_price", new BigDecimal(2000));
					put("bke_enjoyed", 2);
					put("bk_extras_price", new BigDecimal(200));
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(bookingExtraResult);
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("NOT_ENOUGH_PENDING_EXTRAS_TO_CANCEL", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, dischargeResult.getCode());

		}

		@Test
		@DisplayName("cancel an extra fail due booking not exists")
		void test_cancel_booking_fail_due_booking_not_exists() {
			Map<String, Object> keyMap = getBookingKeyMap();
			EntityResult bookingResult = new EntityResultMapImpl();
			Map<String, Object> attrMap = getBKExtraAttrMap();
			;
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("BOOKING_EXTRA_DOESN'T_EXISTS", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, dischargeResult.getCode());

		}

		@Test
		@DisplayName("cancel an extra without quantity")
		void test_cancel_booking_without_quantity() {
			Map<String, Object> keyMap = getBookingKeyMap();
			EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("id_booking_extra"));
			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("id_booking_extra", 2);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("QUANTITY_FIELD_REQUIRED", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, dischargeResult.getCode());

		}

		@Test
		@DisplayName("cancel an extra without id_booking_extra")
		void test_cancel_booking_without_id_booking_extra() {
			Map<String, Object> keyMap = new HashMap<String, Object>();
			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("id_booking_extra", 2);
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("ID_EXTRA_BOOKING_REQUIRED", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, dischargeResult.getCode());

		}

		@Test
		@DisplayName("cancel an extra with empty request")
		void test_cancel_booking_with_empty_request() {
			Map<String, Object> keyMap = new HashMap<String, Object>();
			keyMap.put("id_booking_extra", 2);
			Map<String, Object> attrMap = new HashMap<String, Object>();
			EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("id_booking_extra"));
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("EMPTY_REQUEST", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, dischargeResult.getCode());
		}

		@Test
		@DisplayName("cancel an extra with string_as_quantity")
		void test_cancel_booking_with_string_as_quantity() {
			Map<String, Object> keyMap = new HashMap<String, Object>();
			keyMap.put("id_booking", 2);
			EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("id_booking"));
			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("id_booking_extra", 2);
			attrMap.put("quantity", "sss");
			List<String> columnsExtraBooking = getExtraBookingColumns();
			EntityResult bookingExtraResult = getBookingExtraResult();
			EntityResult updateResult = new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(bookingExtraResult);
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("INCORRECT_REQUEST", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, dischargeResult.getCode());
		}
	}

	@Nested
	@DisplayName("Test for available rooms in a given date")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class availableRoomsQuery {
		@Test
		@DisplayName("Obtain all avaliable rooms for a given hotel")
		void test_availableroomsQuery_success() throws ParseException {
			// given
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
			Date startDate = formatter.parse("2022-30-08");
			Date endDate = formatter.parse("2022-15-09");
			Map<String, Object> filter = getAvRoomsFilter();
			List<String> roomColumns = new ArrayList<>();
			roomColumns.add("rm_number");
			EntityResult availableRooms = getAvRoomsER();
			Map<String, Object> hotelFilter = new HashMap<>();
			hotelFilter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
			filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
					bookingService.buildExpressionToSearchRooms(startDate, endDate));

			lenient().when(daoHelper.query(bookingDao, filter, columns, "AVAILABLE_ROOMS", new ISQLQueryAdapter() {
				@Override
				public SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao, Map<?, ?> keysValues,
						Map<?, ?> validKeysValues, List<?> attributes, List<?> validAttributes, List<?> sort,
						String queryId) {
					return new SQLStatement(sqlStatement.getSQLStatement().replaceAll("#days#", Long.toString(9)),
							sqlStatement.getValues());
				}
			})).thenReturn(availableRooms);
			try (MockedStatic<EntityResultTools> utilities = Mockito.mockStatic(EntityResultTools.class)) {

				utilities.when(() -> EntityResultTools.dofilter(any(), anyMap())).thenReturn(availableRooms);
				EntityResult roomResult = bookingService.availableroomsQuery(filter, columns);

				assertEquals(401, roomResult.getRecordValues(0).get("rm_number"));
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, roomResult.getCode());
			}
		}

		@Test
		@DisplayName("Obtain all avaliable rooms for a given hotel with minimun an maximun price")
		void test_availableroomsQuery_per_price_success() throws ParseException {
			// given
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
			Date startDate = formatter.parse("2022-30-08");
			Date endDate = formatter.parse("2022-15-09");
			Map<String, Object> filter = new HashMap<>();
			filter.put("bk_check_in", "2022-30-08");
			filter.put("bk_check_out", "2022-15-09");
			filter.put("id_hotel", 2);
			filter.put("min_price", 100);
			filter.put("mx_price", 300);
			List<String> roomColumns = new ArrayList<>();
			roomColumns.add("rm_number");
			EntityResult availableRooms = getAvRoomsER();
			Map<String, Object> hotelFilter = new HashMap<>();
			hotelFilter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
			filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
					bookingService.buildExpressionToSearchRooms(startDate, endDate));

			lenient().when(daoHelper.query(bookingDao, filter, columns, "AVAILABLE_ROOMS", new ISQLQueryAdapter() {
				@Override
				public SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao, Map<?, ?> keysValues,
						Map<?, ?> validKeysValues, List<?> attributes, List<?> validAttributes, List<?> sort,
						String queryId) {
					return new SQLStatement(sqlStatement.getSQLStatement().replaceAll("#days#", Long.toString(9)),
							sqlStatement.getValues());
				}
			})).thenReturn(availableRooms);
			try (MockedStatic<EntityResultTools> utilities = Mockito.mockStatic(EntityResultTools.class)) {

				utilities.when(() -> EntityResultTools.dofilter(any(), anyMap())).thenReturn(availableRooms);
				utilities.when(() -> EntityResultTools.dofilter(any(), anyMap())).thenReturn(availableRooms);
				EntityResult roomResult = bookingService.availableroomsQuery(filter, columns);

				assertEquals(401, roomResult.getRecordValues(0).get("rm_number"));
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, roomResult.getCode());
			}
		}

		@Test
		@DisplayName("Obtain all avaliable rooms for a given hotel with minimun an maximun price as strings")
		void test_availableroomsQuery_per_price_fail_due_string_as_price() throws ParseException {
			// given
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
			Date startDate = formatter.parse("2022-30-08");
			Date endDate = formatter.parse("2022-15-09");
			Map<String, Object> filter = new HashMap<>();
			filter.put("bk_check_in", "2022-30-08");
			filter.put("bk_check_out", "2022-15-09");
			filter.put("id_hotel", 2);
			filter.put("min_price", "SSSS");
			filter.put("mx_price", "sss");
			List<String> roomColumns = new ArrayList<>();
			roomColumns.add("rm_number");
			EntityResult availableRooms = getAvRoomsER();
			Map<String, Object> hotelFilter = new HashMap<>();
			hotelFilter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
			filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
					bookingService.buildExpressionToSearchRooms(startDate, endDate));

			lenient().when(daoHelper.query(bookingDao, filter, columns, "AVAILABLE_ROOMS", new ISQLQueryAdapter() {
				@Override
				public SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao, Map<?, ?> keysValues,
						Map<?, ?> validKeysValues, List<?> attributes, List<?> validAttributes, List<?> sort,
						String queryId) {
					return new SQLStatement(sqlStatement.getSQLStatement().replaceAll("#days#", Long.toString(9)),
							sqlStatement.getValues());
				}
			})).thenReturn(availableRooms);
			EntityResult roomResult = bookingService.availableroomsQuery(filter, columns);
			assertEquals("INCORRECT_REQUEST", roomResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, roomResult.getCode());

		}

		@Test
		@DisplayName("Obtain all avaliable rooms for a given hotel with invalid minimun an maximun price")
		void test_availableroomsQuery_per_price_fail_due_max_price_lower_min_price() throws ParseException {
			// given
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
			Date startDate = formatter.parse("2022-30-08");
			Date endDate = formatter.parse("2022-15-09");
			Map<String, Object> filter = new HashMap<>();
			filter.put("bk_check_in", "2022-30-08");
			filter.put("bk_check_out", "2022-15-09");
			filter.put("id_hotel", 2);
			filter.put("min_price", 200);
			filter.put("max_price", 100);
			List<String> roomColumns = new ArrayList<>();
			roomColumns.add("rm_number");
			EntityResult availableRooms = getAvRoomsER();
			Map<String, Object> hotelFilter = new HashMap<>();
			hotelFilter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
			filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
					bookingService.buildExpressionToSearchRooms(startDate, endDate));

			lenient().when(daoHelper.query(bookingDao, filter, columns, "AVAILABLE_ROOMS", new ISQLQueryAdapter() {
				@Override
				public SQLStatement adaptQuery(SQLStatement sqlStatement, IOntimizeDaoSupport dao, Map<?, ?> keysValues,
						Map<?, ?> validKeysValues, List<?> attributes, List<?> validAttributes, List<?> sort,
						String queryId) {
					return new SQLStatement(sqlStatement.getSQLStatement().replaceAll("#days#", Long.toString(9)),
							sqlStatement.getValues());
				}
			})).thenReturn(availableRooms);
			EntityResult roomResult = bookingService.availableroomsQuery(filter, columns);
			assertEquals("MAXPRICE_MUST_BE_HIGHER_THAN_MINPRICE", roomResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, roomResult.getCode());

		}

		@Test
		@DisplayName("Available rooms request with empty fields")
		void test_availableroomsQuery_emptyRequest() throws ParseException {
			Map<String, Object> filter = new HashMap<>();
			List<String> columns = new ArrayList<>();
			EntityResult queryResult = bookingService.availableroomsQuery(filter, columns);
			assertEquals("CHECK_IN_CHECK_OUT_AND HOTEL_NEEDED", queryResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
		}

		@Test
		@DisplayName("Available rooms request with invalid dates")
		void test_availableroomsQuery_invalid_dates() throws ParseException {
			Map<String, Object> filter = new HashMap<>();
			filter.put("bk_check_in", "invalid");
			filter.put("bk_check_out", "invalid");
			filter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
			EntityResult roomResult = bookingService.availableroomsQuery(filter, columns);
			assertEquals("Unparseable date: \"invalid\"", roomResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, roomResult.getCode());
		}

	}

}
