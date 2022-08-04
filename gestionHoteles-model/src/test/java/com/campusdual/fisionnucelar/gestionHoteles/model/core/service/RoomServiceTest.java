package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import org.mockito.Mock;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomTypeDao;
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
	RoomTypeDao roomTypeDao;

	@InjectMocks
	RoomTypeService roomtypeService;

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
	public class RoomQuery {

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

}
