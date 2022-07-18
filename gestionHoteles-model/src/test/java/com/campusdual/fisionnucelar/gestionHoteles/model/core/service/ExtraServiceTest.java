package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraTestData.getAllExtraData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraTestData.getSpecificExtraData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ServiceTestData.getAllServiceData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ServiceTestData.getSpecificServiceData;
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


import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

public class ExtraServiceTest {
	 @Mock
	    DefaultOntimizeDaoHelper daoHelper;
	 @InjectMocks
	    ExtraService extraService;
	    @Autowired
	    ExtraDao extraDao;
	    
	    @BeforeEach
	    void setUp() {
	        this.extraService = new ExtraService();
	        MockitoAnnotations.openMocks(this);
	    }
	
	    @Nested
	    @DisplayName("Test for Extras queries")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    
	    public class ServiceQuery {

	        @Test
	        @DisplayName("Obtain all data from Extras table")
	        void when_queryOnlyWithAllColumns_return_allExtraData() {
	            doReturn(getAllExtraData()).when(daoHelper).query(any(), anyMap(), anyList());
	            EntityResult entityResult = extraService.extraQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(3, entityResult.calculateRecordNumber());
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }
	        @Test
	        @DisplayName("Query without results")
	        void query_without_results() {
	        	EntityResult queryResult = new EntityResultMapImpl();
	        	doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
	        	EntityResult entityResult = extraService.extraQuery(new HashMap<>(), new ArrayList<>());
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("NO_RESULTS", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(), anyList());
	        }
	        @Test
	        @DisplayName("Fail when sends a string in a number field")
	        void when_send_string_as_id_throws_exception() {
	        	Map<String,Object> filter = new HashMap<>();
	        	filter.put("id_extra", "string");
	        	List<String> columns =new ArrayList<>();
	        	columns.add("ex_name");
	        	columns.add("ex_description");
	        	when(daoHelper.query(extraDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
	        	EntityResult entityResult = extraService.extraQuery(filter, columns);
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @Test
	        @DisplayName("Obtain all data columns from Extras table when ID is -> 2")
	        void when_queryAllColumns_return_specificData() {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_EXTRA", 2);
	            }};
	            List<String> attrList = Arrays.asList("ID_EXTRA", "EX_NAME");
	            doReturn(getSpecificExtraData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
	            EntityResult entityResult = extraService.extraQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(1, entityResult.calculateRecordNumber());
	            assertEquals(2, entityResult.getRecordValues(0).get(ExtraDao.ATTR_ID));
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @Test
	        @DisplayName("Obtain all data columns from Services table when ID not exist")
	        void when_queryAllColumnsNotExisting_return_empty() {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_EXTRA", 5);
	            }};
	            List<String> attrList = Arrays.asList("ID_EXTRA", "EX_NAME", "EX_DESCRIPTION");
	            when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificExtraData(keyMap, attrList));
	            EntityResult entityResult = extraService.extraQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(0, entityResult.calculateRecordNumber());
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @ParameterizedTest(name = "Obtain data with ID -> {0}")
	        @MethodSource("randomIDGenerator")
	        @DisplayName("Obtain all data columns from Extra table when ID is random")
	        void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_EXTRA", random);
	            }};
	            List<String> attrList = Arrays.asList("ID_EXTRA", "EX_NAME");
	            when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificExtraData(keyMap, attrList));
	            EntityResult entityResult = extraService.extraQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(1, entityResult.calculateRecordNumber());
	            assertEquals(random, entityResult.getRecordValues(0).get(ExtraDao.ATTR_ID));
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
	    @DisplayName("Test for Extras inserts")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class ExtraInsert {
	        @Test
	        @DisplayName("Insert a extra successfully")
	        void extra_insert_success() {
	        	Map<String, Object> dataToInsert = new HashMap<>();
	        	dataToInsert.put("ex_name", "Sala de conferencias");
	        	dataToInsert.put("ex_description", "Sala completamente equipada");
	        	EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_EXTRA"));
	            er.addRecord(new HashMap<String, Object>() {{put("ID_EXTRA", 2);}});
	            er.setCode(EntityResult.OPERATION_SUCCESSFUL);
	            HashMap<String, Object> keyMap = new HashMap<>();
	            keyMap.put("ID_EXTRA", 2);
	            when(daoHelper.insert(extraDao, dataToInsert)).thenReturn(er);
	            EntityResult entityResult = extraService.extraInsert(dataToInsert);
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            int recordIndex = entityResult.getRecordIndex(keyMap);
	            assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_EXTRA"));
	            verify(daoHelper).insert(extraDao, dataToInsert);
	            
	            }
	       
	        @Test
	        @DisplayName("Fail trying to insert duplicated name")
	        void service_insert_duplicated_mail() {
	        	Map<String, Object> dataToInsert = new HashMap<>();
	        	dataToInsert.put("ex_name", "Sala de conferencias");
	        	dataToInsert.put("ex_description", "Sala completamente equipada");
	        	List<String> columnList = Arrays.asList("ID_EXTRA");
	    		EntityResult insertResult = new EntityResultMapImpl(columnList);
	    	    insertResult.addRecord(new HashMap<String, Object>() {{
	    	        put("ID_EXTRA", 2);}});
	        	when(daoHelper.insert(extraDao, dataToInsert)).thenReturn(insertResult);
	        	EntityResult resultSuccess = extraService.extraInsert(dataToInsert);
	        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
	        	assertEquals("SUCCESSFUL_INSERTION", resultSuccess.getMessage());
	        	when(daoHelper.insert(extraDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
	        	EntityResult resultFail =extraService.extraInsert(dataToInsert);
	        	assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
	        	assertEquals("EXTRA_NAME_ALREADY_EXISTS", resultFail.getMessage());
	        	verify(daoHelper,times(2)).insert(any(), anyMap());
	        }
	        @Test
	        @DisplayName("Fail trying to insert without ex_name field")
	        void service_insert_without_name() {
	        	Map<String, Object> dataToInsert = new HashMap<>();
	        	dataToInsert.put("ex_description", "Sala completamente equipada");
	        	when(daoHelper.insert(extraDao, dataToInsert)).thenThrow(DataIntegrityViolationException.class);
	        	EntityResult entityResult = extraService.extraInsert(dataToInsert);
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("EXTRA_NAME_REQUIRED", entityResult.getMessage());
	        	verify(daoHelper).insert(any(), anyMap());
	        }     
			
			@Test
			@DisplayName("Fail trying to insert with no data")
			void service_insert_withouth_data() {
				EntityResult insertResult = new EntityResultMapImpl();
				Map<String, Object> dataToInsert = new HashMap<>();
				when(daoHelper.insert(extraDao, dataToInsert)).thenReturn(insertResult);
				EntityResult entityResult = extraService.extraInsert(dataToInsert);
				assertEquals("FIELDS_REQUIRED", entityResult.getMessage());
			}
	    }
	    
	    @Nested
	    @DisplayName("Test for Extra updates")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class ServiceUpdate {
	        @Test
	        @DisplayName("Extra update successful")
	        void hotel_update_success() {
	        	//given
	        	Map<String, Object> filter = new HashMap<>();
	        	filter.put("id_extra", 32);
	        	Map<String, Object> dataToUpdate = new HashMap<>();
	        	dataToUpdate.put("ex_name", "Sala de conferencias");
	        	dataToUpdate.put("ex_description", "Sala completamente equipada modificada");
	        	List<String> attrList = new ArrayList<>();
	    		attrList.add("id_extra");
	        	EntityResult er = new EntityResultMapImpl();
	        	er.setCode(EntityResult.OPERATION_SUCCESSFUL);
	        	EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_HOTEL","EX_NAME"));
	        	//when
	        	when(daoHelper.update(extraDao, dataToUpdate,filter)).thenReturn(er);
	        	when(daoHelper.query(extraDao, filter, attrList)).thenReturn(queryResult);
	        	//then
	        	EntityResult entityResult = extraService.extraUpdate(dataToUpdate  ,filter);
	        	assertEquals("SUCCESSFUL_UPDATE", entityResult.getMessage());
	        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	        	verify(daoHelper).update(any(), anyMap(),anyMap());
	        	verify(daoHelper).query(any(), anyMap(),anyList());
	        }
	        
	        @Test
	        @DisplayName("Fail trying to update a extra with an existing name ")
	        void service_fail_update_with_duplicated_name() {
	        	EntityResult er = new EntityResultMapImpl();
	        	er.setCode(EntityResult.OPERATION_WRONG);
	        	er.setMessage("EXTRA_NAME_ALREADY_EXISTS");
	        	Map<String, Object> filter = new HashMap<>();
	        	filter.put("id_extra", 32);
	        	Map<String, Object> dataToUpdate = new HashMap<>();
	        	dataToUpdate.put("ex_name", "Sala de conferencias");
	        	dataToUpdate.put("ex_description", "Sala completamente equipada modificada");
	        	List<String> attrList = new ArrayList<>();
	    		attrList.add("id_extra");
	        	EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_EXTRA","EX_NAME"));
	            er.addRecord(new HashMap<String, Object>() {{put("ID_SERVICE", 2);put("ID_EXTRA","EX_NAME");}});
	            er.setCode(EntityResult.OPERATION_SUCCESSFUL);
	            when(daoHelper.query(extraDao, filter, attrList)).thenReturn(queryResult);
	        	when(daoHelper.update(extraDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
	        	EntityResult entityResult = extraService.extraUpdate(dataToUpdate,filter);
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("EXTRA_NAME_ALREADY_EXISTS", entityResult.getMessage());
	        	verify(daoHelper).update(any(), anyMap(),anyMap());
	        	verify(daoHelper).query(any(), anyMap(),anyList());
	        }
	        @Test
	        @DisplayName("Fail trying to update a extra that doesn´t exists")
	        void update_service_doesnt_exists() {
	        	Map<String, Object> filter = new HashMap<>();
	        	filter.put("id_extra",222);
	        	Map<String, Object> dataToUpdate = new HashMap<>();
	        	dataToUpdate.put("ex_name", "Sala de conferencias");
	        	dataToUpdate.put("ex_description", "Sala completamente equipada modificada");
	        	List<String> attrList = new ArrayList<>();
	    		attrList.add("id_extra");
	        	EntityResult er = new EntityResultMapImpl();
	        	er.setCode(EntityResult.OPERATION_WRONG);
	        	er.setMessage("EXTRA_DOESN'T_EXISTS");
	        	EntityResult queryResult = new EntityResultMapImpl();
	        	when(daoHelper.query(extraDao, filter,attrList)).thenReturn(queryResult);
	        	EntityResult entityResult = extraService.extraUpdate(dataToUpdate,filter);
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("ERROR_EXTRA_NOT_FOUND", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(),anyList());
	        }
	        
			@Test
			@DisplayName("Fail trying to update without any fields")
			void hotel_update_without_any_fields() {
				Map<String, Object> filter = new HashMap<>();
				Map<String, Object> dataToUpdate = new HashMap<>();
				EntityResult updateResult = extraService.extraUpdate(dataToUpdate,filter);
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
				assertEquals("ANY_FIELDS_REQUIRED", updateResult.getMessage());
			}
			@Test
			@DisplayName("Fail trying to update without id_extra")
			void hotel_update_without_id_hotel() {
				Map<String, Object> filter = new HashMap<>();
				Map<String, Object> dataToUpdate = new HashMap<>();
				dataToUpdate.put("ex_name", "Sala de conferencias");
	        	dataToUpdate.put("ex_description", "Sala completamente equipada modificada");
				EntityResult updateResult = extraService.extraUpdate(dataToUpdate,filter);
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
				assertEquals("ID_EXTRA_REQUIRED", updateResult.getMessage());
			}
			
	    }
	    
}