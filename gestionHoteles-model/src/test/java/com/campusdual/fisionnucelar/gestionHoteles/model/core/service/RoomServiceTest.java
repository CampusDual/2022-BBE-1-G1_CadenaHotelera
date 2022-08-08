package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import org.mockito.Mock;


import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTestData.getSpecificRoomData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTestData.getAllRoomData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTestData.getGenericAttrList;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTestData.getGenericDataToInsertOrUpdate;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTestData.getGenericFilter;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTestData.getGenericInsertResult;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTestData.getGenericQueryResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	RoomService roomService;

	@Autowired
	RoomDao roomDao;

	@Mock
	UserControl userControl;
	
	

	@BeforeEach
	void setUp() {
		this.roomService = new RoomService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Test for Room queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class Rooms_Query {

		@Test
		@DisplayName("Obtain all data from Room table")
		void when_queryOnlyWithAllColumns_return_allRoomsData() throws NotAuthorizedException {
			doReturn(getAllRoomData()).when(daoHelper).query(any(), anyMap(), anyList());
			doNothing().when(userControl).controlAccess(anyInt());
			EntityResult entityResult = roomService.roomQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		@Test
		  @DisplayName("Try to search with a non authorized user")
		  void when_not_authorized() throws NotAuthorizedException  {
		   doReturn(getAllRoomData()).when(daoHelper).query(any(), anyMap(), anyList());   
		   doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccess(anyInt());
		   EntityResult entityResult = roomService.roomQuery(new HashMap<>(), new ArrayList<>());
		   assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
		   assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
		   verify(daoHelper).query(any(), anyMap(), anyList());
		  }
		
		@Test
		@DisplayName("Query without results")
		void query_without_results() throws NotAuthorizedException {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = roomService.roomQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_room", "string");
			List<String> columns = new ArrayList<>();
			columns.add("rm_room_type");
			columns.add("rm_hotel");
			when(daoHelper.query(roomDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = roomService.roomQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		@Test
		@DisplayName("Obtain all data columns from Rooms table when ID is -> 2")
		void when_queryAllColumns_return_specificData() throws NotAuthorizedException {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("id_room", 2);
				}
			};
			List<String> attrList = Arrays.asList("id_room", "rm_room_type", "rm_hotel", "rm_number");
			doReturn(getSpecificRoomData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			doNothing().when(userControl).controlAccess(anyInt());
			EntityResult entityResult = roomService.roomQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get("id_room"));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
		@Test
		@DisplayName("Obtain all data columns from Rooms table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() throws NotAuthorizedException {
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(queryResult);
			EntityResult entityResult = roomService.roomQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
		@ParameterizedTest(name = "Obtain data with ID -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from Rooms table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) throws NotAuthorizedException {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("id_room", random);
				}
			};
			List<String> attrList = Arrays.asList("id_room", "rm_room_type", "rm_hotel", "rm_number");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificRoomData(keyMap, attrList));
			doNothing().when(userControl).controlAccess(anyInt());
			EntityResult entityResult = roomService.roomQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get("id_room"));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
	
		
		List<Integer> randomIDGenerator() {
			List<Integer> list = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				list.add(ThreadLocalRandom.current().nextInt(0, 3));
			}
			return list;
		}
	}
	
	@Nested
	@DisplayName("Tests for Rooms inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class Rooms_Insert {
		
		@Test
		@DisplayName("Insert a room successfully")
		void room_insert_success() throws NotAuthorizedException {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult er = getGenericInsertResult();
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("id_room", 2);
			doNothing().when(userControl).controlAccess(anyInt());
			when(daoHelper.insert(roomDao, dataToInsert)).thenReturn(er);

			EntityResult entityResult = roomService.roomInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals(2, entityResult.getRecordValues(recordIndex).get("id_room"));
			verify(daoHelper).insert(roomDao, dataToInsert);
		}
		
	
		@Test
		@DisplayName("Fail trying to update an Room with a duplicated combination of hotel and room")
		void room_insert_duplicated() throws NotAuthorizedException {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult insertResult = getGenericInsertResult();
			
			doNothing().when(userControl).controlAccess(anyInt());
			when(daoHelper.insert(roomDao, dataToInsert)).thenReturn(insertResult);

			EntityResult resultSuccess = roomService.roomInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCCESSFUL_INSERTION", resultSuccess.getMessage());

			doNothing().when(userControl).controlAccess(anyInt());
			when(daoHelper.insert(roomDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = roomService.roomInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("ROOM_ALREADY_EXISTS", resultFail.getMessage());
			//verify(daoHelper).insert(any(), anyMap());
		}
		
		@Test
		  @DisplayName("Try to insert with a non authorized user")
		  void when_not_authorized() throws NotAuthorizedException  {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult er = getGenericInsertResult();
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("id_room", 2);
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccess(anyInt());
			
			EntityResult entityResult = roomService.roomInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
		  }
		
		@Test
		@DisplayName("Fail trying to insert without a not null field or a non existing foreign key")
		void room_insert_without_hotel_or_room() throws NotAuthorizedException {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("id_room", 1);
			dataToInsert.put("rm_hotel", 1);
			DataIntegrityViolationException dataIntegrityException = new DataIntegrityViolationException("RUN_TIME_EXCEPTION");
			doNothing().when(userControl).controlAccess(anyInt());
			when(daoHelper.insert(roomDao, dataToInsert)).thenThrow(dataIntegrityException);
			EntityResult entityResult = roomService.roomInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_EXCEPTION", entityResult.getMessage());
			verify(daoHelper).insert(any(), anyMap());
		}
		
		@Test
		@DisplayName("Fail trying to insert with no data")
		void room_insert_withouth_data() {
			Map<String, Object> dataToInsert = new HashMap<>();
			EntityResult entityResult = roomService.roomInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
		
		@Test
		@DisplayName("Insert an Rooms with a non numeric field")
		void insert_with_non_numeric_price() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			when(daoHelper.insert(roomDao, dataToInsert)).thenThrow(BadSqlGrammarException.class);		
			EntityResult entityResult = roomService.roomInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("FIELDS_MUST_BE_NUMERIC", entityResult.getMessage());

		}
	}

	
	@Nested
	@DisplayName("Test for Rooms updates")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class Rooms_Update {
		@Test
		@DisplayName("Room update succesful")
		void room_update_success() throws NotAuthorizedException {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();

			when(daoHelper.update(any(), any(), any())).thenReturn(er);
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);

			EntityResult entityResult = roomService.roomUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper,times(2)).query(any(), any(), any());
		}
		
		@Test
		@DisplayName("Try to update with a non authorized user")
		void not_authorized() throws NotAuthorizedException {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult queryResult = getGenericQueryResult();

			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccess(anyInt());
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);

			EntityResult entityResult = roomService.roomUpdate(dataToUpdate, filter);
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
		}
		
		@Test
		@DisplayName("Fail trying to update an ExtraHotel with a duplicated combination of hotel and room")
		void room_fail_update_with_duplicated_hotel_and_room() {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult queryResult = getGenericQueryResult();

			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			when(daoHelper.update(roomDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
			EntityResult entityResult = roomService.roomUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ROOM_ALREADY_EXISTS", entityResult.getMessage());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper,times(2)).query(any(), any(), any());
		}
		
		@Test
		@DisplayName("Fail trying to update an room that doesn´t exists")
		void update_room_doesnt_exists() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			EntityResult entityResult = roomService.roomUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ROOM_DOESN'T_EXISTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
		@Test
		@DisplayName("Fail trying to update without any fields")
		void room_update_without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = roomService.roomUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("EMPTY_REQUEST", updateResult.getMessage());
		}
		
		@Test
		@DisplayName("Fail trying to update without id_room")
		void room_update_without_id_services_hotel() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult updateResult = roomService.roomUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_ROOM_REQUIRED", updateResult.getMessage());
		}
		
		@Test
		@DisplayName("Fail trying to update with a non existing foreign key")
		void room_insert_without_hotel_or_roomType() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("rm_hotel", 1);
			EntityResult queryResult = getGenericQueryResult();
			DataIntegrityViolationException dataIntegrityException = new DataIntegrityViolationException("RUN_TIME_EXCEPTION");
			
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);			
			when(daoHelper.update(roomDao, dataToUpdate, filter)).thenThrow(dataIntegrityException);		
			
			EntityResult entityResult = roomService.roomUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_EXCEPTION", entityResult.getMessage());
			
		}
		
		@Test
		@DisplayName("Fail trying to update with a non numeric field")
		void room_insert_with_non_numeric_field() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("rm_hotel", 1);
			EntityResult queryResult = getGenericQueryResult();
			DataIntegrityViolationException dataIntegrityException = new DataIntegrityViolationException("RUN_TIME_EXCEPTION");
			
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);			
			when(daoHelper.update(roomDao, dataToUpdate, filter)).thenThrow(dataIntegrityException);		
			
			EntityResult entityResult = roomService.roomUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_EXCEPTION", entityResult.getMessage());
			
		}
				
		@Test
	      @DisplayName("Fail trying to update a field as string")
	      void room_insert_string_field() {
	        Map<String, Object> filter = getGenericFilter();
	        Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();

	        when (daoHelper.query(any(), any(), any())).thenReturn(getGenericQueryResult());
	        when(daoHelper.update(any(), any(),any())).thenThrow(BadSqlGrammarException.class);
	      
	        EntityResult updateResult = roomService.roomUpdate(dataToUpdate,filter);
	        assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
	        assertEquals("FIELDS_MUST_BE_NUMERIC", updateResult.getMessage());
	    
	      }
	}
}
