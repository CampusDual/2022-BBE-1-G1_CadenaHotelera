

package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;


import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTypeTestData.getAllTypeRoomData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTypeTestData.getSpecificRoomTypeData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTypeTestData.getGenericDataToInsertOrUpdate;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTypeTestData.getGenericInsertResult;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTypeTestData.getGenericFilter;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTypeTestData.getGenericAttrList;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.RoomTypeTestData.getGenericQueryResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomTypeDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

public class RoomTypeServiceTest {
  @Mock
  DefaultOntimizeDaoHelper daoHelper;
  @InjectMocks
  RoomTypeService roomtypeService;
  @Autowired
  RoomTypeDao roomtypeDao;

  @BeforeEach
  void setUp() {
    this.roomtypeService = new RoomTypeService();
    MockitoAnnotations.openMocks(this);
  }

  @Nested
  @DisplayName("Test for TypeRooms queries")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)

  public class roomTypeQuery {

    @Test
    @DisplayName("Obtain all data from RoomTypes table")
    void when_queryOnlyWithAllColumns_return_allRoomTypesData() {
      doReturn(getAllTypeRoomData()).when(daoHelper).query(any(), anyMap(), anyList());
      EntityResult entityResult = roomtypeService.roomtypeQuery(new HashMap<>(), new ArrayList<>());
      assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
      assertEquals(3, entityResult.calculateRecordNumber());
      verify(daoHelper).query(any(), anyMap(), anyList());
    }

    @Test
    @DisplayName("Query without results")
    void query_without_results() {
      EntityResult queryResult = new EntityResultMapImpl();
      doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
      EntityResult entityResult = roomtypeService.roomtypeQuery(new HashMap<>(), new ArrayList<>());
      assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
      assertEquals("NO_RESULTS", entityResult.getMessage());
      verify(daoHelper).query(any(), anyMap(), anyList());
    }


@Test
    @DisplayName("Fail when sends a string in a number field")
    void when_send_string_as_id_throws_exception() {
      Map<String, Object> filter = new HashMap<>();
      filter.put("id_room_type", "string");
      List<String> columns = new ArrayList<>();
      columns.add("rmt_name");
      columns.add("rmt_capacity");
      columns.add("rmt_price");
      when(daoHelper.query(roomtypeDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
      EntityResult entityResult = roomtypeService.roomtypeQuery(filter, columns);
      assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
      assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
      verify(daoHelper).query(any(), anyMap(), anyList());
    }

    @Test
    @DisplayName("Obtain all data columns from roomTypes table when ID is -> 1")
    void when_queryAllColumns_return_specificData() {
      HashMap<String, Object> keyMap = new HashMap<>() {
        {
          put("ID_ROOM_TYPE", 1);
        }
      };
      List<String> attrList = Arrays.asList("ID_ROOM_TYPE", "RMT_NAME", "RMT_CAPACITY", "RMT_PRICE");
      doReturn(getSpecificRoomTypeData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
      EntityResult entityResult = roomtypeService.roomtypeQuery(new HashMap<>(), new ArrayList<>());
      assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
      assertEquals(1, entityResult.calculateRecordNumber());
      assertEquals(1, entityResult.getRecordValues(0).get(roomtypeDao.ATTR_ID));
      verify(daoHelper).query(any(), anyMap(), anyList());
    }

    @Test
    @DisplayName("Obtain all data columns from roomTypes table when ID not exist")
    void when_queryAllColumnsNotExisting_return_empty() {
      HashMap<String, Object> keyMap = new HashMap<>() {
        {
          put("ID_EXTRA", 5);
        }
      };
      List<String> attrList = Arrays.asList("ID_ROOM_TYPE", "RMT_NAME", "RMT_CAPACITY", "RMT_PRICE");
      when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificRoomTypeData(keyMap, attrList));
      EntityResult entityResult = roomtypeService.roomtypeQuery(new HashMap<>(), new ArrayList<>());
      assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
      assertEquals(0, entityResult.calculateRecordNumber());
      verify(daoHelper).query(any(), anyMap(), anyList());
    }

    @ParameterizedTest(name = "Obtain data with ID -> {0}")
    @MethodSource("randomIDGenerator")
    @DisplayName("Obtain all data columns from roomTypes table when ID is random")
    void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
      HashMap<String, Object> keyMap = new HashMap<>() {
        {
          put("ID_ROOM_TYPE", random);
        }
      };
      List<String> attrList = Arrays.asList("ID_ROOM_TYPE", "RMT_NAME", "RMT_CAPACITY", "RMT_PRICE");
      when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificRoomTypeData(keyMap, attrList));
      EntityResult entityResult = roomtypeService.roomtypeQuery(new HashMap<>(), new ArrayList<>());
      assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
      assertEquals(1, entityResult.calculateRecordNumber());
      assertEquals(random, entityResult.getRecordValues(0).get(roomtypeDao.ATTR_ID));
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
  @DisplayName("Test for TypeRooms inserts")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  public class RoomTypeInsert {

    @Test
    @DisplayName("Insert a TypeRooms successfully")
    void roomtypes_insert_success() {
      Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
      EntityResult er = getGenericInsertResult();


      HashMap<String, Object> keyMap = new HashMap<>();
      keyMap.put("ID_ROOM_TYPE", 2);
      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenReturn(er);

      EntityResult entityResult = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
      int recordIndex = entityResult.getRecordIndex(keyMap);
      assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_ROOM_TYPE"));
      verify(daoHelper).insert(roomtypeDao, dataToInsert);
    }

    @Test
    @DisplayName("Fail trying to insert with no data")
    void service_insert_withouth_data() {
      EntityResult insertResult = new EntityResultMapImpl();
      Map<String, Object> dataToInsert = new HashMap<>();
      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenReturn(insertResult);
      EntityResult entityResult = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
      assertEquals("FIELDS_REQUIRED", entityResult.getMessage());
    }

    @Test
    @DisplayName("Fail trying to insert duplicated name")
    void service_insert_duplicated_name() {
      Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
      List<String> columnList = Arrays.asList("ID_ROOM_TYPE");
      EntityResult insertResult = getGenericInsertResult();

      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenReturn(insertResult);
      EntityResult resultSuccess = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
      assertEquals("SUCESSFUL_INSERTION", resultSuccess.getMessage());

      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
      EntityResult resultFail = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
      assertEquals("ROOM_TYPE_ALREADY_EXISTS", resultFail.getMessage());
      verify(daoHelper, times(2)).insert(any(), anyMap());
    }

    @Test
    @DisplayName("Fail trying to insert without rmt_name field")
    void service_insert_without_name() {
      Map<String, Object> dataToInsert = new HashMap<>();
      dataToInsert.put("rmt_capacity", 2);
      dataToInsert.put("rmt_price", 150);
      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenThrow(DataIntegrityViolationException.class);
      EntityResult entityResult = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
      assertEquals("ALL_FIELDS_REQUIRED", entityResult.getMessage());
      verify(daoHelper).insert(any(), anyMap());
    }

    @Test
    @DisplayName("Fail trying to insert without rmt_capacity field")
    void service_insert_without_capacity() {
      Map<String, Object> dataToInsert = new HashMap<>();
      dataToInsert.put("rmt_name", "Suite");
      dataToInsert.put("rmt_price", 150);
      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenThrow(DataIntegrityViolationException.class);
      EntityResult entityResult = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
      assertEquals("ALL_FIELDS_REQUIRED", entityResult.getMessage());
      verify(daoHelper).insert(any(), anyMap());
    }

    @Test
    @DisplayName("Fail trying to insert without rmt_price")
    void service_insert_without_price() {
      Map<String, Object> dataToInsert = new HashMap<>();
      dataToInsert.put("rmt_name", "Suite");
      dataToInsert.put("rmt_capacity", 2);
      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenThrow(DataIntegrityViolationException.class);
      EntityResult entityResult = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
      assertEquals("ALL_FIELDS_REQUIRED", entityResult.getMessage());
      verify(daoHelper).insert(any(), anyMap());
    }


@Test
    @DisplayName("Fail trying to insert  rmt_price field as string")
    void service_insert_string_price() {
      Map<String, Object> dataToInsert = new HashMap<>();
      dataToInsert.put("rmt_name", "Suite");
      dataToInsert.put("rmt_capacity", 2);
      dataToInsert.put("rmt_price", "hola");
      when(daoHelper.insert(roomtypeDao, dataToInsert)).thenThrow(BadSqlGrammarException.class);
      EntityResult entityResult = roomtypeService.roomtypeInsert(dataToInsert);
      assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
      assertEquals("PRICE_MUST_BE_NUMERIC", entityResult.getMessage());
      verify(daoHelper).insert(any(), anyMap());
    }

  }
    @Nested
      @DisplayName("Test for RoomType updates")
      @TestInstance(TestInstance.Lifecycle.PER_CLASS)
      public class RoomTypeUpdate {
      
      @Test
          @DisplayName("RoomType update successful")
          void roomType_update_success() {
          Map<String, Object> filter = getGenericFilter();      
        Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
        EntityResult er = new EntityResultMapImpl();
        EntityResult queryResult = getGenericQueryResult();
        List<String> attrList=getGenericAttrList();
            //when
            when(daoHelper.update(roomtypeDao, dataToUpdate,filter)).thenReturn(er);
            when(daoHelper.query(roomtypeDao, filter, attrList)).thenReturn(queryResult);
            //then
            EntityResult entityResult = roomtypeService.roomtypeUpdate(dataToUpdate  ,filter);
            assertEquals("SUCESSFUL_UPDATE", entityResult.getMessage());
            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
            verify(daoHelper).update(any(), anyMap(),anyMap());
            verify(daoHelper).query(any(), anyMap(),anyList());
          }
      
      @Test
      @DisplayName("Fail trying to update an roomType with a duplicated name")
      void roomType_fail_update_with_duplicated() {
      
        Map<String, Object> filter = getGenericFilter();
        Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
        EntityResult queryResult = getGenericQueryResult();
      
        when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
        when(daoHelper.update(roomtypeDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
        EntityResult entityResult = roomtypeService.roomtypeUpdate(dataToUpdate, filter);
        assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
        assertEquals("ROOM_TYPE_ALREADY_EXISTS", entityResult.getMessage());
        verify(daoHelper).update(any(), anyMap(), anyMap());
        verify(daoHelper).query(any(), any(), any());
      }
      
      @Test
      @DisplayName("Fail trying to update an roomType that doesn´t exists")
      void update_roomType_doesnt_exists() {
        Map<String, Object> filter = getGenericFilter();
        Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
        List<String> attrList = getGenericAttrList();
        
        EntityResult queryResult = new EntityResultMapImpl();
        when(daoHelper.query(roomtypeDao, filter, attrList)).thenReturn(queryResult);
        EntityResult entityResult = roomtypeService.roomtypeUpdate(dataToUpdate, filter);
        assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
        assertEquals("ROOM_TYPE_DOESN'T_EXISTS", entityResult.getMessage());
        verify(daoHelper).query(any(), anyMap(), anyList());
      }
    
      @Test
      @DisplayName("Fail trying to update rmt_price field as string")
      void service_insert_string_price() {
        Map<String, Object> filter = getGenericFilter();
        Map<String, Object> dataToUpdate = new HashMap<>();
        dataToUpdate.put("rmt_name", "Suite");
        dataToUpdate.put("rmt_capacity", 2);
        dataToUpdate.put("rmt_price", "hola");
        when(daoHelper.query(any(), any(),any())).thenReturn(getGenericQueryResult());
        when(daoHelper.update(any(), any(),any())).thenThrow(BadSqlGrammarException.class);
      
        EntityResult updateResult = roomtypeService.roomtypeUpdate(dataToUpdate,filter);
        assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
        assertEquals("PRICE_MUST_BE_NUMERIC", updateResult.getMessage());
      }
    
      @Test
      @DisplayName("Fail trying to update without any fields")
      void roomType_update_without_any_fields() {
        Map<String, Object> filter = getGenericFilter();
        Map<String, Object> dataToUpdate = new HashMap<>();
        EntityResult updateResult = roomtypeService.roomtypeUpdate(dataToUpdate, filter);
        assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
        assertEquals("EMPTY_REQUEST", updateResult.getMessage());
      }
      
      
      @Test
      @DisplayName("Fail trying to update without id_room_type")
      void roomType_update_without_id_services_hotel() {
        Map<String, Object> filter = new HashMap<>();
        Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
        
        EntityResult updateResult = roomtypeService.roomtypeUpdate(dataToUpdate, filter);
        assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
        assertEquals("ID_ROOM_TYPE_REQUIRED", updateResult.getMessage());
      }
      
    }

}
