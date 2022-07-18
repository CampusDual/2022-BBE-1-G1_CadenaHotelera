package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ServiceTestData.*;
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


import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@ExtendWith(MockitoExtension.class)
public class ServiceServiceTest {
	 @Mock
	    DefaultOntimizeDaoHelper daoHelper;
	 
	 @InjectMocks
	    ServiceService serviceService;
	    @Autowired
	    ServiceDao serviceDao;
	    
	    @BeforeEach
	    void setUp() {    	
	        this.serviceService = new ServiceService();
	        MockitoAnnotations.openMocks(this);
	    }
	
	    @Nested
	    @DisplayName("Test for Services queries")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class ServiceQuery {

	        @Test
	        @DisplayName("Obtain all data from Services table")
	        void testServiceQueryAllData() {
	            doReturn(getAllServiceData()).when(daoHelper).query(any(), anyMap(), anyList());
	            EntityResult entityResult = serviceService.serviceQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(3, entityResult.calculateRecordNumber());
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }
	        @Test
	        @DisplayName("Query without results")
	        void query_without_results() {
	        	EntityResult queryResult = new EntityResultMapImpl();
	        	doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
	        	EntityResult entityResult = serviceService.serviceQuery(new HashMap<>(), new ArrayList<>());
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("NO_RESULTS", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(), anyList());
	        }
	        @Test
	        @DisplayName("Fail when sends a string in a number field")
	        void when_send_string_as_id_throws_exception() {
	        	Map<String,Object> filter = new HashMap<>();
	        	filter.put("id_service", "string");
	        	List<String> columns =new ArrayList<>();
	        	columns.add("sv_name");
	        	columns.add("sv_description");
	        	when(daoHelper.query(serviceDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
	        	EntityResult entityResult = serviceService.serviceQuery(filter, columns);
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @Test
	        @DisplayName("Obtain all data columns from Services table when ID is -> 2")
	        void when_queryAllColumns_return_specificData() {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_SERVICE", 2);
	            }};
	            List<String> attrList = Arrays.asList("ID_SERVICE", "SV_NAME");
	            doReturn(getSpecificServiceData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
	            EntityResult entityResult = serviceService.serviceQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(1, entityResult.calculateRecordNumber());
	            assertEquals(2, entityResult.getRecordValues(0).get(ServiceDao.ATTR_ID));
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @Test
	        @DisplayName("Obtain all data columns from Services table when ID not exist")
	        void when_queryAllColumnsNotExisting_return_empty() {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_SERVICE", 5);
	            }};
	            List<String> attrList = Arrays.asList("ID_SERVICE", "SV_NAME", "SV_DESCRIPTION");
	            when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificServiceData(keyMap, attrList));
	            EntityResult entityResult = serviceService.serviceQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(0, entityResult.calculateRecordNumber());
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @ParameterizedTest(name = "Obtain data with ID -> {0}")
	        @MethodSource("randomIDGenerator")
	        @DisplayName("Obtain all data columns from Services table when ID is random")
	        void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_SERVICE", random);
	            }};
	            List<String> attrList = Arrays.asList("ID_SERVICE", "SV_NAME");
	            when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificServiceData(keyMap, attrList));
	            EntityResult entityResult = serviceService.serviceQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(1, entityResult.calculateRecordNumber());
	            assertEquals(random, entityResult.getRecordValues(0).get(ServiceDao.ATTR_ID));
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
	    @DisplayName("Test for Service inserts")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class ServiceInsert {
	        @Test
	        @DisplayName("Insert a service successfully")
	        void service_insert_success() {
	        	Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
	        	EntityResult er = getGenericInsertResult();
	            HashMap<String, Object> keyMap = new HashMap<>();
	            keyMap.put("ID_SERVICE", 2);
	            when(daoHelper.insert(serviceDao, dataToInsert)).thenReturn(er);
	            EntityResult entityResult = serviceService.serviceInsert(dataToInsert);
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            int recordIndex = entityResult.getRecordIndex(keyMap);
	            assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_SERVICE"));
	            verify(daoHelper).insert(serviceDao, dataToInsert);
	            
	            }
	       
	        @Test
	        @DisplayName("Fail trying to insert duplicated name")
	        void service_insert_duplicated_mail() {
	        	Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
	        	List<String> columnList = Arrays.asList("ID_SERVICE");
	    		EntityResult insertResult = getGenericInsertResult();
	        	when(daoHelper.insert(serviceDao, dataToInsert)).thenReturn(insertResult);
	        	EntityResult resultSuccess = serviceService.serviceInsert(dataToInsert);
	        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
	        	assertEquals("SUCESSFUL_INSERTION", resultSuccess.getMessage());
	        	when(daoHelper.insert(serviceDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
	        	EntityResult resultFail =serviceService.serviceInsert(dataToInsert);
	        	assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
	        	assertEquals("SERVICE_NAME_ALREADY_EXISTS", resultFail.getMessage());
	        	verify(daoHelper,times(2)).insert(any(), anyMap());
	        }
	        @Test
	        @DisplayName("Fail trying to insert without sv_name field")
	        void service_insert_without_name() {
	        	Map<String, Object> dataToInsert = new HashMap<>();
	    		dataToInsert.put("sv_description", "Wireless internet");
	    		
	    		DataIntegrityViolationException DataIntegrityException=new DataIntegrityViolationException("RunTimeMessage");		
	    		when(daoHelper.insert(any(), anyMap())).thenThrow(DataIntegrityException);
	    		   		
	    		EntityResult entityResult = serviceService.serviceInsert(dataToInsert);     	
	           	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	verify(daoHelper).insert(any(), anyMap());
	        }     
			
			@Test
			@DisplayName("Fail trying to insert with no data")
			void service_insert_withouth_data() {
				EntityResult insertResult = new EntityResultMapImpl();
				Map<String, Object> dataToInsert = new HashMap<>();
				when(daoHelper.insert(serviceDao, dataToInsert)).thenReturn(insertResult);
				EntityResult entityResult = serviceService.serviceInsert(dataToInsert);
				assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
				assertEquals("FIELDS_REQUIRED", entityResult.getMessage());
			}
	    }
	
	    @Nested
	    @DisplayName("Test for Service updates")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class ServiceUpdate {
	        @Test
	        @DisplayName("Service update successful")
	        void hotel_update_success() {
	        	Map<String, Object> filter = getGenericFilter();
	        	Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
	        	List<String> attrList = getGenericAttrList();
	        	EntityResult er = new EntityResultMapImpl();    
	        	EntityResult queryResult = getGenericQueryResult();
	        	
	        	when(daoHelper.update(serviceDao, dataToUpdate,filter)).thenReturn(er);
	        	when(daoHelper.query(serviceDao, filter, attrList)).thenReturn(queryResult);
	      
	        	EntityResult entityResult = serviceService.serviceUpdate(dataToUpdate  ,filter);
	        	assertEquals("SUCCESSFUL_UPDATE", entityResult.getMessage());
	        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	        	verify(daoHelper).update(any(), anyMap(),anyMap());
	        	verify(daoHelper).query(any(), anyMap(),anyList());
	        }
	        
	        @Test
	        @DisplayName("Fail trying to update a service with an existing name ")
	        void service_fail_update_with_duplicated_name() {

	        	Map<String, Object> filter = getGenericFilter();
	        	Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
	        	List<String> attrList = getGenericAttrList();
	        	EntityResult queryResult = getGenericQueryResult();
	            
	            when(daoHelper.query(serviceDao, filter, attrList)).thenReturn(queryResult);
	        	when(daoHelper.update(serviceDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
	        	EntityResult entityResult = serviceService.serviceUpdate(dataToUpdate,filter);
	        	
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("SERVICE_NAME_ALREADY_EXISTS", entityResult.getMessage());
	        	verify(daoHelper).update(any(), anyMap(),anyMap());
	        	verify(daoHelper).query(any(), anyMap(),anyList());
	        }
	        
	        @Test
	        @DisplayName("Fail trying to update a service that doesn´t exists")
	        void update_service_doesnt_exists() {
	        	Map<String, Object> filter = getGenericFilter();
	        	Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
	        	List<String> attrList = getGenericAttrList();
	        	EntityResult er = new EntityResultMapImpl();
	        	EntityResult queryResult = new EntityResultMapImpl();
	        	
	        	when(daoHelper.query(serviceDao, filter,attrList)).thenReturn(queryResult);
	        	EntityResult entityResult = serviceService.serviceUpdate(dataToUpdate,filter);
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("ERROR_SERVICE_NOT_FOUND", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(),anyList());
	        }
	        
			@Test
			@DisplayName("Fail trying to update without any fields")
			void hotel_update_without_any_fields() {
				Map<String, Object> filter = new HashMap<>();
				Map<String, Object> dataToUpdate = new HashMap<>();
				EntityResult updateResult = serviceService.serviceUpdate(dataToUpdate,filter);
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
				assertEquals("EMPTY_REQUEST", updateResult.getMessage());
			}
			@Test
			@DisplayName("Fail trying to update without id_service")
			void hotel_update_without_id_hotel() {
				Map<String, Object> filter = new HashMap<>();
				Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
				EntityResult updateResult = serviceService.serviceUpdate(dataToUpdate,filter);
				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
				assertEquals("ID_SERVICE_REQUIRED", updateResult.getMessage());
			}
			
	    }
	    
	    
	     
	    
	    
	    
	    
	    
	    
	    
	    
}
