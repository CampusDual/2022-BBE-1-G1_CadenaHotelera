package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.math.BigDecimal;
import java.text.ParseException;



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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.*;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.*;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.dto.*;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
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
        	Map<String,Object> filter = new HashMap<>();
        	filter.put("id_hotel", "string");
        	List<String> columns =new ArrayList<>();
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
            HashMap<String, Object> keyMap = new HashMap<>() {{
                put("ID_HOTEL", 2);
            }};
            List<String> attrList = Arrays.asList("ID_BOOKING", "BK_CHECK_IN", "BK_CHECK_OUT","BK_PRICE", "BK_ROOM", "BK_CLIENT","BK_EXTRAS_PRICE");
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
            HashMap<String, Object> keyMap = new HashMap<>() {{
                put("ID_HOTEL", 5);
            }};
            List<String> attrList = Arrays.asList("ID_BOOKING", "BK_CHECK_IN", "BK_CHECK_OUT","BK_PRICE", "BK_ROOM", "BK_CLIENT","BK_EXTRAS_PRICE");
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
            HashMap<String, Object> keyMap = new HashMap<>() {{
                put("ID_HOTEL", random);
            }};
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
        	Map<String,Object> filter = new HashMap<>();
        	filter.put("bk_client", 10);
        	List<String> columns = new ArrayList<>();
        	columns.add("id_booking");
        	columns.add("bk_check_in");
        	columns.add("bk_check_out");
        	columns.add("bk_room");
        	columns.add("bk_price");
        	EntityResult clientBookings = new EntityResultMapImpl(columns);
        	Calendar calendar = Calendar.getInstance();
        	calendar.set(2022, 10,10);
        	Date checkIn = calendar.getTime();
        	calendar.set(2022,11, 11);
        	Date checkOut = calendar.getTime();
        	clientBookings.addRecord(new HashMap<String, Object>() {{
    	        put("id_booking", 2);
    	        put("bk_room",2);
    	        put("bk_check_in",checkIn);
    	        put("bk_check_out",checkOut);
    	        put("bk_price",100);
    	        }});
        	when(daoHelper.query(bookingDao, filter, columns, "CLIENT_BOOKINGS")).thenReturn(clientBookings);
        	EntityResult queryResult = bookingService.clientbookingsQuery(filter, columns);
        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
        	assertEquals(1, queryResult.calculateRecordNumber());
        	
        }
        @Test
        @DisplayName("Obtain all active bookings for a given client")
        void search_active_bookings_by_client_success() {
        	Map<String,Object> filter = new HashMap<>();
        	filter.put("bk_client", 10);
        	List<String> columns = new ArrayList<>();
        	columns.add("id_booking");
        	columns.add("bk_check_in");
        	columns.add("bk_check_out");
        	columns.add("bk_room");
        	columns.add("bk_price");
        	EntityResult clientBookings = new EntityResultMapImpl(columns);
        	Calendar calendar = Calendar.getInstance();
        	calendar.set(2022, 10,10);
        	Date checkIn = calendar.getTime();
        	calendar.set(2022,11, 11);
        	Date checkOut = calendar.getTime();
        	clientBookings.addRecord(new HashMap<String, Object>() {{
        		put("id_booking", 2);
        		put("bk_room",2);
        		put("bk_check_in",checkIn);
        		put("bk_check_out",checkOut);
        		put("bk_price",100);
        	}});
        	when(daoHelper.query(bookingDao, filter, columns, "CLIENT_ACTIVE_BOOKINGS")).thenReturn(clientBookings);
        	EntityResult queryResult = bookingService.clientactivebookingsQuery(filter, columns);
        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
        	assertEquals(1, queryResult.calculateRecordNumber());
        	
        }
        @Test
        @DisplayName("search client active bookings without results")
        void search_active_bookings_by_client_no_results() {
        	Map<String,Object> filter = new HashMap<>();
        	filter.put("bk_client", 10);
        	List<String> columns = new ArrayList<>();
        	columns.add("id_booking");
        	columns.add("bk_check_in");
        	columns.add("bk_check_out");
        	columns.add("bk_room");
        	columns.add("bk_price");
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
        	Map<String,Object> filter = new HashMap<>();
        	filter.put("bk_client", 10);
        	List<String> columns = new ArrayList<>();
        	columns.add("id_booking");
        	columns.add("bk_check_in");
        	columns.add("bk_check_out");
        	columns.add("bk_room");
        	columns.add("bk_price");
        	EntityResult clientBookings = new EntityResultMapImpl();
        	when(daoHelper.query(bookingDao, filter, columns, "CLIENT_BOOKINGS")).thenReturn(clientBookings);
        	EntityResult queryResult = bookingService.clientbookingsQuery(filter, columns);
        	assertEquals("NO_RESULTS", queryResult.getMessage());
        	assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
        	assertEquals(0, queryResult.calculateRecordNumber());
        }
	}
	
	@Nested
    @DisplayName("Test for available rooms in a given date")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class availableRoomsQuery {
        @Test
        @DisplayName("Obtain all avaliable rooms for a given hotel")
        void test_availableroomsQuery_success() throws ParseException {
        	//given
        	Map<String,Object> filter = new HashMap<>();
        	filter.put("bk_check_in","2022-30-08");
        	filter.put("bk_check_out","2022-15-09");
        	filter.put("id_hotel", 2);
        	List<String> columns = new ArrayList<>();
        	columns.add("id_booking");
        	columns.add("htl_name");
        	columns.add("id_hotel");
        	columns.add("rm_number");
        	EntityResult availableRooms = new EntityResultMapImpl(columns);
        	availableRooms.addRecord(new HashMap<String, Object>() {{
    	        put("id_hotel", 2);
    	        put("rm_number",401);
    	        put("htl_name","FN As Pontes");
    	        put("id_booking",38);
    	        }});
        	Map<String,Object> hotelFilter = new HashMap<>();
        	hotelFilter.put("id_hotel", 2);
        	
            try (MockedStatic<EntityResultTools> utilities = Mockito.mockStatic(EntityResultTools.class)) {
            	//when(bookingService.searchAvailableRooms(filter, columns)).thenReturn(availableRooms);
            	when(daoHelper.query(bookingDao, filter,columns, "AVAILABLE_ROOMS")).thenReturn(availableRooms);
            
            	utilities.when(()->EntityResultTools.dofilter(availableRooms, hotelFilter)).thenReturn(availableRooms);
            	assertEquals(EntityResultTools.dofilter(availableRooms, hotelFilter),availableRooms);
            	//then
            	EntityResult queryResult = bookingService.availableroomsQuery(filter, columns);
            	assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
            	assertEquals("FN As Pontes", queryResult.getRecordValues(0).get("htl_name"));
            }
            }
            @Test
            @DisplayName("Available rooms request with empty fields")
            void test_availableroomsQuery_emptyRequest() throws ParseException {
            	Map<String,Object> filter = new HashMap<>();
            	List<String> columns = new ArrayList<>();
            	when(bookingService.availableroomsQuery(filter, columns)).thenThrow(AllFieldsRequiredException.class);
            	EntityResult queryResult = bookingService.availableroomsQuery(filter, columns);
            	assertEquals("CHECK_IN_CHECK_OUT_AND HOTEL_NEEDED", queryResult.getMessage());	
            	assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());	
        }
            
            @Test
            @DisplayName("Available rooms request with invalid dates")
            void test_availableroomsQuery_invalid_dates() throws ParseException {
            	Map<String,Object> filter = new HashMap<>();
            	filter.put("bk_check_in","invalid");
            	filter.put("bk_check_out","invalid");
            	filter.put("id_hotel", 2);
            	List<String> columns = new ArrayList<>();
            	//when(bookingService.availableroomsQuery(filter, columns)).thenThrow(AllFieldsRequiredException.class);
            	EntityResult queryResult = bookingService.availableroomsQuery(filter, columns);
            	assertEquals("Unparseable date: \"invalid\"", queryResult.getMessage());	
            	assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());	
        }
            
            @Test
            @DisplayName("Available rooms request with string as id_hotel")
            void test_availableroomsQuery_invalid_fields() throws ParseException {
            	Map<String,Object> filter = new HashMap<>();
            	filter.put("bk_check_in","2022-30-08");
            	filter.put("bk_check_out","2022-15-09");
            	filter.put("id_hotel", "invalid");
            	List<String> columns = new ArrayList<>();
            	when(daoHelper.query(bookingDao, filter, columns, "AVAILABLE_ROOMS")).thenThrow(BadSqlGrammarException.class);
            	EntityResult queryResult = bookingService.availableroomsQuery(filter, columns);
            	assertEquals("INCORRECT_REQUEST", queryResult.getMessage());	
            	assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
            	
        }
	}
	
	@Nested
    @DisplayName("Test for today checkouts query")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class todayCkeckoutsQuery {
		
	  @Test
      @DisplayName("Obtain all rooms with checkout in the current day")
      void test_todayCheckoutQuery_success()  {
		  	Map<String,Object> filter = new HashMap<>();
		  	filter.put("rm_hotel",1);
        	List<String> columns = new ArrayList<>();
        	columns.add("id_room");
        	columns.add("rm_room_type");
        	columns.add("rm_hotel");
        	columns.add("rm_number");
        	EntityResult roomsCheckoutToday = new EntityResultMapImpl(columns);
      		roomsCheckoutToday.addRecord(new HashMap<String, Object>() {{
	        put("id_room", 2);
	        put("rm_number",401);
	        put("rm_room_type", 1);
	        put("rm_hotel",2);
	        }});
      		
      		when(daoHelper.query(bookingDao, filter, columns, "TODAY_CHECKOUTS")).thenReturn(roomsCheckoutToday);
      		
      		EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);
      		
        	//assertEquals("INCORRECT_REQUEST", queryResult.getMessage());	
        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
	  }
	  
	  @Test
      @DisplayName("todayCheckoutQuery without rm_hotel")
      void test_todayCheckoutQuery_without_rm_hotel()  {
		  Map<String,Object> filter = new HashMap<>();
		  List<String> columns = new ArrayList<>();
		   
		  EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);
		  
      	assertEquals("RM_HOTEL_NEEDED", queryResult.getMessage());	
      	assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
	  }
	  @Test
	  @DisplayName("todayCheckoutQuery empty response")
	  void test_todayCheckoutQuery_empty_response()  {
		  	Map<String,Object> filter = new HashMap<>();
		  	List<String> columns = new ArrayList<>();
      		EntityResult todayCheckouts = new EntityResultMapImpl();
      		EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);
      		assertEquals("RM_HOTEL_NEEDED", queryResult.getMessage());	
      		assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
	  }
	  
	  @Test
      @DisplayName("todayCheckoutQuery with rm_hotel as string")
      void test_todayCheckoutQuery_with_rm_hotel_as_string()  {
		  	Map<String,Object> filter = new HashMap<>();
		  	filter.put("rm_hotel", "invalid");
		  	List<String> columns = new ArrayList<>();
		  	when(daoHelper.query(bookingDao, filter,columns,"TODAY_CHECKOUTS")).thenThrow(BadSqlGrammarException.class);
		  	EntityResult queryResult = bookingService.todaycheckoutQuery(filter, columns);
		  
      		assertEquals("INCORRECT_REQUEST", queryResult.getMessage());	
      		assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
	  }
	  
	  @Test
	  @DisplayName("todayCheckoutQuery without results")
	  void test_todayCheckoutQuery_without_results()  {
		  Map<String,Object> filter = new HashMap<>();
		  filter.put("rm_hotel", 1);
		  List<String> columns = new ArrayList<>();
		  EntityResult emptyResult = new EntityResultMapImpl();
		  when(daoHelper.query(bookingDao, filter,columns,"TODAY_CHECKOUTS")).thenReturn(emptyResult);
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
		      void test_booking_insert_success()  {
				  Map<String,Object> dataToInsert = new HashMap<>();
				  dataToInsert.put("bk_check_in", new Date(2022,10,03));
				  dataToInsert.put("bk_check_out", new Date(2022,10,22));
				  dataToInsert.put("bk_room", 2);
				  dataToInsert.put("bk_client", 2);
				  EntityResult insertResult = new EntityResultMapImpl();
				  insertResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
				  EntityResult disponibilityResult = new EntityResultMapImpl();
				  EntityResult clientResult = new EntityResultMapImpl();
				  clientResult.addRecord(new HashMap<String, Object>() {{
					  put("id_client", 2); }});
				  EntityResult roomResult = new EntityResultMapImpl();
				  roomResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); }});
				  EntityResult activeClientResult = new EntityResultMapImpl();
		    	  when(daoHelper.insert(bookingDao, dataToInsert)).thenReturn(insertResult);
		    	  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
		    	  when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(disponibilityResult);
		    	  EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
//		      	  assertEquals("INCORRECT_REQUEST", queryResult.getMessage());	
		      	  assertEquals(EntityResult.OPERATION_SUCCESSFUL, queryResult.getCode());
		      	  assertEquals(2, queryResult.getRecordValues(0).get("id_booking"));
				  
			  }
			  
			  @Test
		      @DisplayName("insert booking fails due client is not active")
		      void test_booking_insert_not_active_client()  {
				  Map<String,Object> dataToInsert = new HashMap<>();
				  dataToInsert.put("bk_check_in", new Date(2022,10,03));
				  dataToInsert.put("bk_check_out", new Date(2022,10,22));
				  dataToInsert.put("bk_room", 2);
				  dataToInsert.put("bk_client", 2);
				  EntityResult insertResult = new EntityResultMapImpl();
				  insertResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
				  EntityResult disponibilityResult = new EntityResultMapImpl();
				  EntityResult clientResult = new EntityResultMapImpl();
				  EntityResult activeClientResult = new EntityResultMapImpl();
				  activeClientResult.addRecord(new HashMap<String, Object>() {{
					  put("id_client", 2); 
					  put("cl_leaving_date", new Date()); 
					  
				  }});
				  EntityResult roomResult = new EntityResultMapImpl();
				  roomResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); }});
		    	  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
		    	  EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
		      	  assertEquals("CLIENT_IS_NOT_ACTIVE", queryResult.getMessage());	
		      	  assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
				  
			  }
			  
			  @Test
		      @DisplayName("insert booking fails due room is occupied")
		      void test_booking_insert_occupied_room()  {
				  Map<String,Object> dataToInsert = new HashMap<>();
				  dataToInsert.put("bk_check_in", new Date(2022,10,03));
				  dataToInsert.put("bk_check_out", new Date(2022,10,22));
				  dataToInsert.put("bk_room", 2);
				  dataToInsert.put("bk_client", 2);
				  EntityResult insertResult = new EntityResultMapImpl();
				  insertResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
				  EntityResult disponibilityResult = new EntityResultMapImpl();
				  disponibilityResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); 
					  put("rm_number", 401); 
					  
				  }});
				  EntityResult clientResult = new EntityResultMapImpl();
				  EntityResult activeClientResult = new EntityResultMapImpl();
				  EntityResult roomResult = new EntityResultMapImpl();
				  roomResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); }});
		    	  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
		    	  when(daoHelper.query(any(), anyMap(), anyList(),anyString())).thenReturn(disponibilityResult);
		    	  EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
		      	  assertEquals("OCCUPIED_ROOM", queryResult.getMessage());	
		      	  assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
				  
			  }
			  
			  @Test
		      @DisplayName("insert booking with check_out before check_in")
		      void test_booking_insert_with_check_out_before_check_in()  {
				  Map<String,Object> dataToInsert = new HashMap<>();
				  Calendar c = Calendar.getInstance();
				  c.set(2022, 10, 10);
				  Date checkIn = c.getTime();
				  c.set(2022, 9,10);
				  Date checkOut = c.getTime();
				  java.util.Date date = c.getTime();
				  dataToInsert.put("bk_check_in", checkIn);
				  dataToInsert.put("bk_check_out", checkOut);
				  dataToInsert.put("bk_room", 2);
				  dataToInsert.put("bk_client", 2);
				  EntityResult insertResult = new EntityResultMapImpl();
				  insertResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
				  EntityResult disponibilityResult = new EntityResultMapImpl();
				  disponibilityResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); 
					  put("rm_number", 401); 
					  
				  }});
				  EntityResult clientResult = new EntityResultMapImpl();
				  EntityResult activeClientResult = new EntityResultMapImpl();
				  EntityResult roomResult = new EntityResultMapImpl();
				  roomResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); }});
		    	  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
		    	  EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
		      	  assertEquals("CHECK_IN_MUST_BE_BEFORE_CHECK_OUT", queryResult.getMessage());	
		      	  assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
				  
			  }
			  
			  @Test
		      @DisplayName("insert booking with date before current date")
		      void test_booking_insert_with_dates_before_current_date()  {
				  Map<String,Object> dataToInsert = new HashMap<>();
				  Calendar c = Calendar.getInstance();
				  c.set(2020, 8, 10);
				  Date checkIn = c.getTime();
				  c.set(2020, 9,10);
				  Date checkOut = c.getTime();
				  java.util.Date date = c.getTime();
				  dataToInsert.put("bk_check_in", checkIn);
				  dataToInsert.put("bk_check_out", checkOut);
				  dataToInsert.put("bk_room", 2);
				  dataToInsert.put("bk_client", 2);
				  EntityResult insertResult = new EntityResultMapImpl();
				  insertResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
				  EntityResult disponibilityResult = new EntityResultMapImpl();
				  disponibilityResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); 
					  put("rm_number", 401); 
					  
				  }});
				  EntityResult clientResult = new EntityResultMapImpl();
				  EntityResult activeClientResult = new EntityResultMapImpl();
				  EntityResult roomResult = new EntityResultMapImpl();
				  roomResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); }});
		    	  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
		    	  EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
		      	  assertEquals("CHECK_IN_MUST_BE_EQUAL_OR_AFTER_CURRENT_DATE", queryResult.getMessage());	
		      	  assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
				  
			  }
			  
			  @Test
		      @DisplayName("insert booking with invalid dates")
		      void test_booking_insert_with_dates_with_invalid_dates()  {
				  Map<String,Object> dataToInsert = new HashMap<>();
				  dataToInsert.put("bk_check_in", "invalid");
				  dataToInsert.put("bk_check_out", "invalid");
				  dataToInsert.put("bk_room", 2);
				  dataToInsert.put("bk_client", 2);
				  EntityResult insertResult = new EntityResultMapImpl();
				  insertResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
				  EntityResult disponibilityResult = new EntityResultMapImpl();
				  disponibilityResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); 
					  put("rm_number", 401); 
					  
				  }});
				  EntityResult clientResult = new EntityResultMapImpl();
				  EntityResult activeClientResult = new EntityResultMapImpl();
				  EntityResult roomResult = new EntityResultMapImpl();
				  roomResult.addRecord(new HashMap<String, Object>() {{
					  put("id_room", 2); }});
		    	  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(activeClientResult);
		    	  EntityResult queryResult = bookingService.bookingInsert(dataToInsert);
		      	  assertEquals("CHECK_IN_AND_CHECK_OUT_MUST_BE_DATES", queryResult.getMessage());	
		      	  assertEquals(EntityResult.OPERATION_WRONG, queryResult.getCode());
				  
			  }
			  
			  @Test
		      @DisplayName("insert booking with empty request")
		      void test_booking_insert_with_dates_with_empty_request()  {
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
		      void test_booking_update_success()  {
				  Map<String,Object> attrMap = new HashMap<>();
				  Calendar calendar = Calendar.getInstance();
				  calendar.set(2023, 9,10);
				  Date checkIn = calendar.getTime();
				  calendar.set(2023, 10,11);
				  Date checkOut = calendar.getTime();
				  attrMap.put("bk_room", 2);
				  attrMap.put("bk_client", 2);
				  attrMap.put("bk_check_in", checkIn);
				  attrMap.put("bk_check_out", checkOut);
				  attrMap.put("bk_price", 100);
				  Map<String,Object> keyMap = new HashMap<>();
				  keyMap.put("id_booking", 50);
				  EntityResult result = new EntityResultMapImpl();
				  result.setCode(0);
				  EntityResult queryResult = new EntityResultMapImpl();
				  queryResult.addRecord(new HashMap<String, Object>() {{
			    	  put("id_booking", 2); }});
				  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
				  when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(result);
				  EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				  assertEquals("SUCCESSFUL_UPDATE",updateResult.getMessage());
				  assertEquals(EntityResult.OPERATION_SUCCESSFUL,updateResult.getCode());


			  }
			  
			  @Test
			  @DisplayName("update booking without id_booking")
			  void test_booking_update_empty_without_id_booking()  {
				  Map<String,Object> attrMap = new HashMap<>();
				  Map<String,Object> keyMap = new HashMap<>();
				  EntityResult queryResult = new EntityResultMapImpl();
				  EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				  assertEquals("ID_BOOKING_REQUIRED",updateResult.getMessage());
				  assertEquals(EntityResult.OPERATION_WRONG,updateResult.getCode()); 
			  }
			  @Test
			  @DisplayName("update booking without body request")
			  void test_booking_update_empty_without_body_request()  {
				  Map<String,Object> attrMap = new HashMap<>();
				  Map<String,Object> keyMap = new HashMap<>();
				  keyMap.put("id_booking", 50);
				  EntityResult bookingQuery = new EntityResultMapImpl();
				  bookingQuery.addRecord(new HashMap<String, Object>() {{
			    	  put("id_booking", 2); }});
				  EntityResult queryResult = new EntityResultMapImpl();
				  queryResult.setCode(1);
				  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				  when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
				  EntityResult updateResult = bookingService.bookingUpdate(attrMap, keyMap);
				  assertEquals("ERROR_WHILE_UPDATING",updateResult.getMessage());
				  assertEquals(EntityResult.OPERATION_WRONG,updateResult.getCode()); 
			  }
		}
		
		@Nested
	    @DisplayName("Test booking delete")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class test_booking_delete {
			  @Test
		      @DisplayName("delete booking succesfully")
		      void test_booking_delete_success()  {
				  Map<String,Object> keyMap = new HashMap<>();
				  keyMap.put("id_booking", 50);
				  EntityResult bookingQuery = new EntityResultMapImpl();
				  bookingQuery.addRecord(new HashMap<String, Object>() {{
			    	  put("id_booking", 2); }});
				  EntityResult queryResult = new EntityResultMapImpl();
				  queryResult.setCode(0);
				  when(daoHelper.update(any(), anyMap(),anyMap())).thenReturn(queryResult);
				  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				  EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				  assertEquals("SUCCESSFUL_DELETE",deleteResult.getMessage());
				  assertEquals(EntityResult.OPERATION_SUCCESSFUL,deleteResult.getCode()); 
				  
			  }
			  
			  @Test
		      @DisplayName("delete booking without id_booking")
		      void test_booking_delete_without_id_booking()  {
				  Map<String,Object> keyMap = new HashMap<>();
				  EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				  assertEquals("ID_BOOKING_REQUIRED",deleteResult.getMessage());
				  assertEquals(EntityResult.OPERATION_WRONG,deleteResult.getCode()); 
			  }
			  
			  @Test
		      @DisplayName("delete booking with a booking that doesn´t exists")
		      void test_booking_delete_booking_not_exists()  {
				  Map<String,Object> keyMap = new HashMap<>();
				  keyMap.put("id_booking", 50);
				  EntityResult bookingQuery = new EntityResultMapImpl();
				  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingQuery);
				  EntityResult deleteResult = bookingService.bookingDelete(keyMap);
				  assertEquals("BOOKING_DOESN'T_EXISTS",deleteResult.getMessage());
				  assertEquals(EntityResult.OPERATION_WRONG,deleteResult.getCode()); 
				  
			  }
		}}
    
	@Nested
    @DisplayName("Test booking extra update")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    public class test_booking_extra_update {
		  @Test
	      @DisplayName("update an extra successfully")
	      void test_booking_extra_update_success()  {
			  Map<String,Object> attrMap = new HashMap<>();
			  attrMap.put("id_extras_hotel", 1);
			  attrMap.put("quantity", 2);
			  
			  Map<String,Object> keyMap = new HashMap<>();
			  keyMap.put("id_booking", 2);
			  
			  EntityResult bookingResult = new EntityResultMapImpl(Arrays.asList("id_booking"));
			  bookingResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
			  
			  EntityResult extrasPriceResult = new EntityResultMapImpl(Arrays.asList("bk_extras_price"));
			  extrasPriceResult.addRecord(new HashMap<String, Object>() {{
				  put("bk_extras_price", new BigDecimal(50)); }});
			  
			  EntityResult extraResult = new EntityResultMapImpl(Arrays.asList("exh_price"));
			  extraResult.addRecord(new HashMap<String, Object>() {{
		    	  put("exh_price", new BigDecimal(50)); }});
			  
			  EntityResult queryResult = new EntityResultMapImpl();
			  queryResult.setCode(0);
			  
	    	  Mockito.doAnswer(new Answer() {
	    		    private int count = 0;
	    		    
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						count++;
						if(count == 1) {return bookingResult;}
						if(count ==2) {return extrasPriceResult;}
						if(count ==3) {return extraResult;}
						return invocation;
					}
	    		}).when(daoHelper).query(any(), anyMap(), anyList());
	    	  when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(queryResult);
	    	  EntityResult updateResult = bookingService.bookingextraUpdate(attrMap, keyMap);
//			  assertEquals("BOOKING_DOESN'T_EXISTS",updateResult.getMessage());
			  assertEquals(EntityResult.OPERATION_SUCCESSFUL,updateResult.getCode()); 
		  }
		  @Test
		  @DisplayName("update an extra with a booking that doesnt exists")
		  void test_booking_extra_booking_not_exists()  {
			  Map<String,Object> attrMap = new HashMap<>();
			  attrMap.put("id_extras_hotel", 1);
			  attrMap.put("quantity", 2);
			  
			  Map<String,Object> keyMap = new HashMap<>();
			  keyMap.put("id_booking", 2);
			  
			  EntityResult bookingResult = new EntityResultMapImpl();
			  
			  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			  EntityResult updateResult = bookingService.bookingextraUpdate(attrMap, keyMap);
			  assertEquals("BOOKING_DOESN'T_EXISTS",updateResult.getMessage());
			  assertEquals(EntityResult.OPERATION_WRONG,updateResult.getCode()); 
		  }
		  @Test
		  @DisplayName("update an extra with invalid data")
		  void test_booking_extra_booking_invalid_data()  {
			  Map<String,Object> attrMap = new HashMap<>();
			  attrMap.put("id_extras_hotel", 1);
			  attrMap.put("quantity", "ss");
			  
			  Map<String,Object> keyMap = new HashMap<>();
			  keyMap.put("id_booking", 2);
			  
			  EntityResult bookingResult = new EntityResultMapImpl();
			  bookingResult.addRecord(new HashMap<String, Object>() {{
		    	  put("id_booking", 2); }});
			  
			  EntityResult extrasPriceResult = new EntityResultMapImpl(Arrays.asList("bk_extras_price"));
			  extrasPriceResult.addRecord(new HashMap<String, Object>() {{
				  put("bk_extras_price","ssss"); }});
			  
			  EntityResult extraResult = new EntityResultMapImpl(Arrays.asList("exh_price"));
			  extraResult.addRecord(new HashMap<String, Object>() {{
		    	  put("exh_price", "ssss"); }});
			  
			  EntityResult queryResult = new EntityResultMapImpl();
			  queryResult.setCode(0);
			  
	    	  Mockito.doAnswer(new Answer() {
	    		    private int count = 0;
	    		    
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						return bookingResult;
					}
	    		}).when(daoHelper).query(any(), anyMap(), anyList());
			  when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(bookingResult);
			  EntityResult updateResult = bookingService.bookingextraUpdate(attrMap, keyMap);
			  assertEquals("INCORRECT_REQUEST",updateResult.getMessage());
			  assertEquals(EntityResult.OPERATION_WRONG,updateResult.getCode()); 
		  }
	}
}
		
		