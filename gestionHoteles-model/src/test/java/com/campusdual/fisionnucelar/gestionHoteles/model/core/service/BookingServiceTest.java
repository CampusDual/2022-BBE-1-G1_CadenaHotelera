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
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.*;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
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
	
	@Mock
	UserControl userControl;

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
		void when_queryOnlyWithAllColumns_return_allHotelData() throws NotAuthorizedException {
			doReturn(getAllBookingData()).when(daoHelper).query(any(), anyMap(), anyList());
			doNothing().when(userControl).controlAccess(anyInt());
			EntityResult entityResult = bookingService.bookingQuery(getBookingQueryKeyMap(), getGenericColumns());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		@Test
		@DisplayName("Query fails due not authorized")
		void when_query_fails_due_not_authorized() throws NotAuthorizedException {
			NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
			doThrow(exception).when(userControl).controlAccess(anyInt());
			EntityResult entityResult = bookingService.bookingQuery(getBookingQueryKeyMap(), getGenericColumns());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = bookingService.bookingQuery(getBookingQueryKeyMap(), getGenericColumns());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			when(daoHelper.query(any(), anyMap(), anyList())).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = bookingService.bookingQuery(getBookingQueryKeyMap(), getGenericColumns());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from bookings table when ID is -> 2")
		void when_queryAllColumns_return_specificData() throws NotAuthorizedException {
			doReturn(getGenericBookingER()).when(daoHelper).query(any(), anyMap(), anyList());
			doNothing().when(userControl).controlAccess(anyInt());
			EntityResult entityResult = bookingService.bookingQuery(getBookingQueryKeyMap(), getGenericColumns());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("query fails table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() throws NotAuthorizedException {
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(new EntityResultMapImpl());
			doNothing().when(userControl).controlAccess(anyInt());
			EntityResult entityResult = bookingService.bookingQuery(getBookingQueryKeyMap(),getGenericColumns());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
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
			EntityResult entityResult = bookingService.bookingQuery(getBookingQueryKeyMap(),getGenericColumns());
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
			@DisplayName("search client active bookings fails due not authorized")
			void search_active_bookings_by_client_not_authorized() throws NotAuthorizedException {
				Map<String, Object> filter = new HashMap<>();
				filter.put("bk_client", 10);
				List<String> columns = getGenericColumns();
				EntityResult clientBookings = new EntityResultMapImpl();
				NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
				doThrow(exception).when(userControl).controlAccessClient(anyInt());
				EntityResult queryResult = bookingService.clientactivebookingsQuery(filter, columns);
				assertEquals("NOT_AUTHORIZED", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
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
			@Test
			@DisplayName("search client bookings fails due not authorized")
			void search_bookings_by_client_not_authorized() throws NotAuthorizedException {
				Map<String, Object> filter = new HashMap<>();
				filter.put("bk_client", 10);
				List<String> columns = getGenericColumns();
				EntityResult clientBookings = new EntityResultMapImpl();
				NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
				doThrow(exception).when(userControl).controlAccessClient(anyInt());
				EntityResult queryResult = bookingService.clientbookingsQuery(filter, columns);
				assertEquals("NOT_AUTHORIZED", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
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
			@DisplayName("todayCheckoutQuery fails due not authorized")
			void test_todayCheckoutQuery_fails_due_not_authorized() throws NotAuthorizedException {
				Map<String, Object> filter = new HashMap<>();
				filter.put("rm_hotel", 2);
				List<String> columns = new ArrayList<>();
				NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
				doThrow(exception).when(userControl).controlAccess(anyInt());
				EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);
				assertEquals("NOT_AUTHORIZED", queryResult.getMessage());
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
				EntityResult clientResult = getClientVipResult();
				EntityResult roomResult = new EntityResultMapImpl();
				roomResult.addRecord(new HashMap<String, Object>() {
					{
						put("id_room", 2);
						put("rmt_price", new BigDecimal(40));
					}
				});
				EntityResult activeClientResult = new EntityResultMapImpl();
				when(daoHelper.insert(bookingDao, dataToInsert)).thenReturn(insertResult);
		
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return clientResult;
						if (count == 2)
							return activeClientResult;
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
								
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return disponibilityResult;
						if (count == 2)
							return roomResult;
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("SUCESSFULL_INSERTION", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
	
			}

			@Test
			@DisplayName("insert booking succesfully by a vip client")
			void test_booking_insert_success_vip_client() {
				Map<String, Object> dataToInsert = getDataToInsert();
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = new EntityResultMapImpl();
				EntityResult clientResult = new EntityResultMapImpl();
				clientResult.addRecord(new HashMap<String, Object>() {
					{
						put("id_client", 2);
						put("cl_booking_count",55);
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
		
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return clientResult;
						if (count == 2)
							return activeClientResult;
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
								
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return disponibilityResult;
						if (count == 2)
							return roomResult;
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("SUCESSFULL_INSERTION_VIP_DISCOUNT_APPLIED", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
	
			}
			
			
			
			@Test
			@DisplayName("insert booking with a non existing room or client")
			void test_booking_insert_with_non_existing_room_or_client_() {
				Map<String, Object> dataToInsert = getDataToInsert();
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = new EntityResultMapImpl();
				EntityResult clientResult = getClientVipResult();
				EntityResult roomResult = new EntityResultMapImpl();
				roomResult.addRecord(new HashMap<String, Object>() {
					{
						put("id_room", 2);
						put("rmt_price", new BigDecimal(40));
					}
				});
				EntityResult activeClientResult = new EntityResultMapImpl();
				
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return clientResult;
						if (count == 2)
							return activeClientResult;
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
				
				
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return disponibilityResult;
						if (count == 2)
							throw new DataIntegrityViolationException("CLIENT_OR_ROOM_DOESN'T_EXIST");
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("CLIENT_OR_ROOM_DOESN'T_EXIST", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
	
			}
								
			
			@Test
			@DisplayName("insert booking succesfully with discount code")
			void test_booking_insert_with_discount_code() {
				Map<String, Object> dataToInsert = getDataToInsert();
				dataToInsert.put("discount_code", "CRAZY_SUMMER_2023");
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = new EntityResultMapImpl();
				EntityResult clientResult = getClientVipResult();
				
				EntityResult discountResult = new EntityResultMapImpl();
				
				discountResult.addRecord(new HashMap<String, Object>() {
					{
						put("dc_name","CRAZY_SUMMER_2023");
						put("dc_multiplier",new BigDecimal("0.95"));
						put("dc_leaving_date",null);
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
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return clientResult;
						if (count == 2)
							return activeClientResult;
						if (count == 3)
							return discountResult;
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
				
				
				when(daoHelper.insert(bookingDao, dataToInsert)).thenReturn(insertResult);
	
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return disponibilityResult;
						if (count == 2)
							return roomResult;
						
						
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("SUCESSFULL_INSERTION_DISCOUNT_CODE_APPLIED", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());

			}
			
			
			@Test
			@DisplayName("insert booking succesfully with discount code and vip discount")
			void test_booking_insert_with_discount_code_and_vip_discount() {
				Map<String, Object> dataToInsert = getDataToInsert();
				dataToInsert.put("discount_code", "CRAZY_SUMMER_2023");
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = new EntityResultMapImpl();
				EntityResult clientResult = getClientVipResultDiscount();
				
				EntityResult discountResult = new EntityResultMapImpl();
				
				discountResult.addRecord(new HashMap<String, Object>() {
					{
						put("dc_name","CRAZY_SUMMER_2023");
						put("dc_multiplier",new BigDecimal("0.95"));
						put("dc_leaving_date",null);
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
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return clientResult;
						if (count == 2)
							return activeClientResult;
						if (count == 3)
							return discountResult;
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
				
				
				when(daoHelper.insert(bookingDao, dataToInsert)).thenReturn(insertResult);
	
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return disponibilityResult;
						if (count == 2)
							return roomResult;
						
						
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("SUCESSFULL_INSERTION_DISCOUNT_CODE_APPLIED_VIP_DISCOUNT_APPLIED", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());

			}
			
			
		
					
						
			@Test
			@DisplayName("insert booking succesfully with season")
			void test_booking_insert_with_season() {
				Map<String, Object> dataToInsert = getDataToInsert();
	
				EntityResult insertResult = getGenericBookingER();
				EntityResult disponibilityResult = new EntityResultMapImpl();
				EntityResult clientResult = getClientVipResult();
				EntityResult roomResult = new EntityResultMapImpl();
				roomResult.addRecord(new HashMap<String, Object>() {
					{
						put("id_room", 2);
						put("rmt_price", new BigDecimal(40));
					}
				});
				
				
				EntityResult seasonResult = getSeasonResult();
				EntityResult activeClientResult = new EntityResultMapImpl();
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return clientResult;
						if (count == 2)
							return seasonResult;
					
					
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
				
							
				
				when(daoHelper.insert(bookingDao, dataToInsert)).thenReturn(insertResult);
				
	
				Mockito.doAnswer(new Answer() {
					private int count = 0;

					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return disponibilityResult;
						if (count == 2)
							return roomResult;

						
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
				assertEquals("SUCESSFULL_INSERTION", queryResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
	

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
				EntityResult insertResult = bookingService.bookingInsert(emptyMap);
				assertEquals("FIELDS_MUST_BE_PROVIDED", insertResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, insertResult.getCode());
			}
		}

//		@Nested
//		@DisplayName("Test booking update")
//		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//		public class test_booking_update {
//			@Test
//			@DisplayName("update booking succesfully")
//			void test_booking_update_success() {
//				Map<String, Object> attrMap = getGenericAttrMap();
//				Map<String, Object> keyMap = getBookingKeyMap();
//				EntityResult result = new EntityResultMapImpl();
//				result.setCode(0);
//				EntityResult queryResult = getGenericBookingER();
//				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
//				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(result);
//				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
//				assertEquals("SUCCESSFUL_UPDATE", updateResult.getMessage());
//				assertEquals(EntityResult.OPERATION_SUCCESSFUL, updateResult.getCode());
//
//			}
//
//			@Test
//			@DisplayName("update booking without id_booking")
//			void test_booking_update_empty_without_id_booking() {
//				Map<String, Object> attrMap = new HashMap<>();
//				Map<String, Object> keyMap = new HashMap<>();
//				EntityResult queryResult = new EntityResultMapImpl();
//				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
//				assertEquals("ID_BOOKING_REQUIRED", updateResult.getMessage());
//				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
//			}
//
//			@Test
//			@DisplayName("update booking without body request")
//			void test_booking_update_empty_without_body_request() {
//				Map<String, Object> attrMap = new HashMap<>();
//				Map<String, Object> keyMap = getBookingKeyMap();
//				EntityResult bookingQuery = getGenericBookingER();
//				EntityResult queryResult = new EntityResultMapImpl();
//				queryResult.setCode(1);
//				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
//				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
//				EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
//				assertEquals("ERROR_WHILE_UPDATING", updateResult.getMessage());
//				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
//			}
//		}

		@Nested
		@DisplayName("Test change dates update")
		@TestInstance(TestInstance.Lifecycle.PER_CLASS)
		public class test_change_dates_update {
			@Test
			@DisplayName("change dates succesfully")
			void change_dates_success() throws NotAuthorizedException {
				Map<String, Object> attrMap = getGenericAttrMap();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult result = new EntityResultMapImpl();
				EntityResult er = getChangeDatesResult();
				EntityResult clientResult=getClientVipResult();			
				EntityResult disponibilityResult = new EntityResultMapImpl();
				disponibilityResult.addRecord(new HashMap<String, Object>() {
					{
						put("rm_room_type", 2);
						put("rm_hotel",3);
						put("id_room",3);
					}
				});
											
				EntityResult emptyEr = new EntityResultMapImpl();				
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return er;
						if (count == 2)
							return er;
						if (count == 3)
							return emptyEr;
											
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
				
		
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(result);	
				
				doReturn(false).when(userControl).controlAccessClient(anyInt());
				doNothing().when(userControl).controlAccess(anyInt());
						
									
				Mockito.doAnswer(new Answer() {
					private int count = 0;
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return er;
						if (count == 2)
							return disponibilityResult;
						if (count == 3)
							return er;
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				
				try (MockedStatic<EntityResultTools> utilities = Mockito.mockStatic(EntityResultTools.class)) {

					utilities.when(() -> EntityResultTools.dofilter(any(), anyMap())).thenReturn(disponibilityResult);				
					EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
					assertEquals("SUCCESSFUL_UPDATE", updateResult.getMessage());
					assertEquals(EntityResult.OPERATION_SUCCESSFUL, updateResult.getCode());
				}
				
			

			}
			
			@Test
			@DisplayName("try to change dates on a promotional booking")
			void fail_change_promotional_booking() throws NotAuthorizedException {
				Map<String, Object> attrMap = getGenericAttrMap();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult result = new EntityResultMapImpl();
				EntityResult er = getChangeDatesResultPromotional();
	
				EntityResult clientResult=getClientVipResult();			
				EntityResult disponibilityResult = new EntityResultMapImpl();
				disponibilityResult.addRecord(new HashMap<String, Object>() {
					{
						put("rm_room_type", 2);
						put("rm_hotel",3);
						put("id_room",3);
					}
				});
											
				EntityResult emptyEr = new EntityResultMapImpl();				
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return er;
						if (count == 2)
							return er;
						if (count == 3)
							return emptyEr;
											
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
				
		
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(result);	
				
						
									
				Mockito.doAnswer(new Answer() {
					private int count = 0;
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return er;
						if (count == 2)
							return disponibilityResult;
						if (count == 3)
							return er;
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				
				try (MockedStatic<EntityResultTools> utilities = Mockito.mockStatic(EntityResultTools.class)) {

					utilities.when(() -> EntityResultTools.dofilter(any(), anyMap())).thenReturn(disponibilityResult);				
					EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
					assertEquals("PROMOTIONAL_BOOKING_CAN'T_BE_UPDATED", updateResult.getMessage());
					assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
				}
				
			

			}
			
			
			
			@Test
			@DisplayName("no room disponibility")
			void no_room_disponibility() throws NotAuthorizedException {
				Map<String, Object> attrMap = getGenericAttrMap();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult result = new EntityResultMapImpl();
				EntityResult er = getChangeDatesResult();
					
				EntityResult emptyEr = new EntityResultMapImpl();				
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return er;
						if (count == 2)
							return er;
						if (count == 3)
							return emptyEr;
											
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList());
				
		
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(result);	
				
				doReturn(false).when(userControl).controlAccessClient(anyInt());
				doNothing().when(userControl).controlAccess(anyInt());
						
				
				Mockito.doAnswer(new Answer() {
					private int count = 0;
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if (count == 1)
							return er;
						if (count == 2)
							return emptyEr;
						if (count == 3)
							return er;
						return invocation;
					}
				}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());

				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("NO_ROOM_DISPONIBILITY", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());

			}	
				

			@Test
			@DisplayName("change dates without id_booking")
			void test_change_dates_empty_without_id_booking() {
				Map<String, Object> attrMap = getGenericAttrMap();
				Map<String, Object> keyMap = new HashMap<>();
				EntityResult queryResult = new EntityResultMapImpl();
				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("ID_BOOKING_REQUIRED", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}


			@Test
			@DisplayName("change dates without body request")
			void test_change_dates_empty_without_body_request() {
				Map<String, Object> attrMap = new HashMap<>();
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult bookingQuery = getGenericBookingER();
				EntityResult queryResult = new EntityResultMapImpl();			
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getChangeDatesResult());	
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("CHECK_IN_AND_CHECK_OUT_REQUIRED", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}
			@Test
			@DisplayName("change dates without dates")
			void test_change_dates_empty_without_dates() {
				Map<String, Object> attrMap = new HashMap<>();
				attrMap.put("bk_room", 2);
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult bookingQuery = getGenericBookingER();
				EntityResult queryResult = new EntityResultMapImpl();
				
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getChangeDatesResult());	
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("CHECK_IN_AND_CHECK_OUT_REQUIRED", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}
			
			@Test
			@DisplayName("change dates in an inactive booking")
			void test_change_dates_inactive_booking() {
				Map<String, Object> attrMap =getGenericAttrMap();
				attrMap.put("bk_room", 2);
				Map<String, Object> keyMap = getBookingKeyMap();
				EntityResult bookingQuery = new EntityResultMapImpl();
				bookingQuery.addRecord(new HashMap<String, Object>() {
					{
						put("id_booking", 2);	
						put("bk_leaving_date", "2022-11-11");
					
					}
				});
	
				EntityResult queryResult = new EntityResultMapImpl();								
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);	
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getChangeDatesResult());	
				when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("BOOKING_ISN'T_ACTIVE", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}
			
			
			
			@Test
			@DisplayName("change dates fails due not authorized client")
			void test_change_dates_fails_due_not_authorized_client() throws NotAuthorizedException {
				Map<String, Object> attrMap =getGenericAttrMap();
				attrMap.put("bk_room", 2);
				EntityResult er = getChangeDatesResult();
				Map<String, Object> keyMap = getBookingKeyMap();
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(er);
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(er);
					
				NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
				doThrow(exception).when(userControl).controlAccessClient(anyInt());
				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("NOT_AUTHORIZED", updateResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			}
			@Test
			@DisplayName("change dates fails due not authorized")
			void test_change_dates_fails_due_not_authorized() throws NotAuthorizedException {
				Map<String, Object> attrMap =getGenericAttrMap();
				attrMap.put("bk_room", 2);
				Map<String, Object> keyMap = getBookingKeyMap();
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getChangeDatesResult());
				when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getChangeDatesResult());
				NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
				doReturn(false).when(userControl).controlAccessClient(anyInt());
				doThrow(exception).when(userControl).controlAccess(anyInt());
				EntityResult updateResult = bookingService.changedatesUpdate(attrMap, keyMap);
				assertEquals("NOT_AUTHORIZED", updateResult.getMessage());
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
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getRMHotelER());
				EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				assertEquals("SUCCESSFUL_DELETE", deleteResult.getMessage());
				assertEquals(EntityResult.OPERATION_SUCCESSFUL, deleteResult.getCode());

			}
			
			@Test
			@DisplayName("delete booking fails due not authorized")
			void test_booking_delete_fails_due_not_authorized() throws NotAuthorizedException {
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getRMHotelER());
				Map<String, Object> keyMap = getBookingKeyMap();
				NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
				doThrow(exception).when(userControl).controlAccess(anyInt());
				EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				assertEquals("NOT_AUTHORIZED", deleteResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, deleteResult.getCode());
			}

			@Test
			@DisplayName("delete booking without id_booking")
			void test_booking_delete_without_id_booking() {
				Map<String, Object> keyMap = new HashMap<>();
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getRMHotelER());
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
				when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getRMHotelER());
				EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				assertEquals("BOOKING_DOESN'T_EXISTS", deleteResult.getMessage());
				assertEquals(EntityResult.OPERATION_WRONG, deleteResult.getCode());
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
					put("bk_extras_price", new BigDecimal(25));
					put("rm_hotel",2);
				}
			});

			EntityResult extrasPriceResult = new EntityResultMapImpl(Arrays.asList("exh_price", "exh_name","exh_active"));
			extrasPriceResult.addRecord(new HashMap<String, Object>() {
				{
					put("exh_price", new BigDecimal(50));
					put("exh_name", "niñero");
					put("exh_active", 1);
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
						return getChangeDatesER();
					}
					if (count == 2) {
						return getChangeDatesER();
					}
					if (count == 3) {
						return bookingResult;
					}
					if (count == 4) {
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
		@DisplayName("update an extra fails due not outhorized")
		void test_booking_extra_update_not_authorized() throws NotAuthorizedException {
			Map<String, Object> attrMap = new HashMap<>();
			attrMap.put("id_extras_hotel", 1);
			attrMap.put("quantity", 2);
			Map<String, Object> keyMap = getBookingKeyMap();
			NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
			doThrow(exception).when(userControl).controlAccessClient(anyInt());
			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1) {
						return getChangeDatesER();
					}
					if (count == 2) {
						return getChangeDatesER();
					}
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult updateResult = bookingService.addbookingextraUpdate(attrMap, keyMap);
			assertEquals("NOT_AUTHORIZED", updateResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			
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
		@DisplayName("update an extra with an inactive booking")
		void test_booking_extra_inactive_booking() {
			Map<String, Object> attrMap = new HashMap<>();
			attrMap.put("id_extras_hotel", 1);
			attrMap.put("quantity", 2);

			Map<String, Object> keyMap = getBookingKeyMap();

			EntityResult bookingResult = new EntityResultMapImpl();
			bookingResult.addRecord(new HashMap<String, Object>() {{
				put("id_booking", 2);
				put("bk_leaving_date","2022-11-11");}}); 
	
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			EntityResult updateResult = bookingService.addbookingextraUpdate(attrMap, keyMap);
			assertEquals("BOOKING_ISN'T_ACTIVE", updateResult.getMessage());
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

			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return bookingResult;
					if (count == 2)
						return getChangeDatesER();
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult updateResult = bookingService.addbookingextraUpdate(attrMap, keyMap);
			assertEquals("INCORRECT_REQUEST", updateResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
		}

		@Test
		@DisplayName("cancel an extra succesfully")
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
			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return getChangeDatesER();
					if (count == 2)
						return bookingExtraResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("SUCCESSFULLY_CANCELED", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, dischargeResult.getCode());

		}

		@Test
		@DisplayName("cancel an extra fail due not authorized")
		void test_cancel_booking_fail_due_not_authorized() throws NotAuthorizedException {
			Map<String, Object> attrMap = getBKExtraAttrMap();
			Map<String, Object> keyMap = getBookingKeyMap();
			EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("id_booking"));
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getRMHotelER());
			NotAuthorizedException exception = new NotAuthorizedException("NOT_AUTHORIZED");
			doThrow(exception).when(userControl).controlAccess(anyInt());
			EntityResult dischargeResult = bookingService.cancelbookingextraUpdate(attrMap, keyMap);
			assertEquals("NOT_AUTHORIZED", dischargeResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, dischargeResult.getCode());
			
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
			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return getChangeDatesER();
					if (count == 2)
						return bookingExtraResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
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
			when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(getChangeDatesER());
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
			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return getChangeDatesER();
					if (count == 2)
						return bookingExtraResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
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
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = formatter.parse("2022-11-07");
			Date endDate = formatter.parse("2022-11-09");
				
			Map<String, Object> filter = getAvRoomsFilter();
			List<String> roomColumns = new ArrayList<>();
			roomColumns.add("rm_number");
			EntityResult availableRooms = getAvRoomsER();
			Map<String, Object> hotelFilter = new HashMap<>();
			hotelFilter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			
			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return queryResult;
					if (count == 2)
						return getSeasonResult();
				
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
					
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
		void test_availableroomsQuery_price_range() throws ParseException {
			// given
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = formatter.parse("2022-11-07");
			Date endDate = formatter.parse("2022-11-09");
				
			Map<String, Object> filter = new HashMap<>();
			filter.put("bk_check_in", "2022-11-12");
			filter.put("bk_check_out", "2022-11-14");
			filter.put("id_hotel", 2);
			filter.put("min_price", 100);
			filter.put("max_price", 300);
			List<String> roomColumns = new ArrayList<>();
			roomColumns.add("rm_number");
			EntityResult availableRooms = getAvRoomsER();
			Map<String, Object> hotelFilter = new HashMap<>();
			hotelFilter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			
			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return queryResult;
					if (count == 2)
						return getSeasonResult();
				
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
					
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
		@DisplayName("Obtain all avaliable rooms for a given hotel with minimun an maximun price as strings")
		void test_availableroomsQuery_incorrec_type() throws ParseException {
			// given
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = formatter.parse("2022-11-07");
			Date endDate = formatter.parse("2022-11-09");
				
			Map<String, Object> filter = new HashMap<>();
			filter.put("bk_check_in", "2022-11-12");
			filter.put("bk_check_out", "2022-11-14");
			filter.put("id_hotel", 2);
			filter.put("min_price", "sss");
			filter.put("max_price", 300);
			List<String> roomColumns = new ArrayList<>();
			roomColumns.add("rm_number");
			EntityResult availableRooms = getAvRoomsER();
			Map<String, Object> hotelFilter = new HashMap<>();
			hotelFilter.put("id_hotel", 2);
			List<String> columns = new ArrayList<>();
			columns.add("rm_number");
			EntityResult queryResult = new EntityResultMapImpl(columns);
			
			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return queryResult;
					if (count == 2)
						return getSeasonResult();
				
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
					
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

				assertEquals(EntityResult.OPERATION_WRONG, roomResult.getCode());
				assertEquals("INCORRECT_REQUEST", roomResult.getMessage());
			}
		}

		@Test
		@DisplayName("Obtain all avaliable rooms for a given hotel with invalid minimun an maximun price")
		void test_availableroomsQuery_per_price_fail_due_max_price_lower_min_price() throws ParseException {
			// given
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						Date startDate = formatter.parse("2022-11-07");
						Date endDate = formatter.parse("2022-11-09");
							
						Map<String, Object> filter = new HashMap<>();
						filter.put("bk_check_in", "2022-11-12");
						filter.put("bk_check_out", "2022-11-14");
						filter.put("id_hotel", 2);
						filter.put("min_price", 300);
						filter.put("max_price", 100);
						List<String> roomColumns = new ArrayList<>();
						roomColumns.add("rm_number");
						EntityResult availableRooms = getAvRoomsER();
						Map<String, Object> hotelFilter = new HashMap<>();
						hotelFilter.put("id_hotel", 2);
						List<String> columns = new ArrayList<>();
						columns.add("rm_number");
						EntityResult queryResult = new EntityResultMapImpl(columns);
						
						Mockito.doAnswer(new Answer() {
							private int count = 0;

							@Override
							public Object answer(InvocationOnMock invocation) throws Throwable {
								count++;
								if (count == 1)
									return queryResult;
								if (count == 2)
									return getSeasonResult();
							
								return invocation;
							}
						}).when(daoHelper).query(any(), anyMap(), anyList());
								
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

							assertEquals(EntityResult.OPERATION_WRONG, roomResult.getCode());
							assertEquals("MAXPRICE_MUST_BE_HIGHER_THAN_MINPRICE", roomResult.getMessage());
						}

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
