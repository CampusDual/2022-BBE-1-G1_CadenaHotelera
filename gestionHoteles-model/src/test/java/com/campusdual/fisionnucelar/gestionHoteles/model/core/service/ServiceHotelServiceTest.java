package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ServiceHotelTestData.getAllServiceHotelData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ServiceHotelTestData.getSpecificServiceHotelData;
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

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceHotelDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

public class ServiceHotelServiceTest {
	 @Mock
	    DefaultOntimizeDaoHelper daoHelper;
	 @InjectMocks
	    ServiceHotelService serviceHotelService;
	    @Autowired
	    ServiceHotelDao serviceHotelDao;
	    
	    @BeforeEach
	    void setUp() {
	        this.serviceHotelService = new ServiceHotelService();
	        MockitoAnnotations.openMocks(this);
	    }
	
	    @Nested
	    @DisplayName("Test for ServiceHotel queries")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class ServiceHotelQuery {

	        @Test
	        @DisplayName("Obtain all data from Services_Hotel table")
	        void when_queryOnlyWithAllColumns_return_allServiceHotelData() {
	            doReturn(getAllServiceHotelData()).when(daoHelper).query(any(), anyMap(), anyList());
	            EntityResult entityResult = serviceHotelService.servicehotelQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(3, entityResult.calculateRecordNumber());
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }
	        @Test
	        @DisplayName("Query without results")
	        void query_without_results() {
	        	EntityResult queryResult = new EntityResultMapImpl();
	        	doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
	        	EntityResult entityResult = serviceHotelService.servicehotelQuery(new HashMap<>(), new ArrayList<>());
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("NO_RESULTS", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(), anyList());
	        }
	        @Test
	        @DisplayName("Fail when sends a string in a number field")
	        void when_send_string_as_id_throws_exception() {
	        	Map<String,Object> filter = new HashMap<>();
	        	filter.put("id_services_hotel", "string");
	        	List<String> columns =new ArrayList<>();
	        	columns.add("svh_hotel");
	        	columns.add("svh_service");
	        	when(daoHelper.query(serviceHotelDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
	        	EntityResult entityResult = serviceHotelService.servicehotelQuery(filter, columns);
	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	        	assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
	        	verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @Test
	        @DisplayName("Obtain all data columns from Service table when ID is -> 2")
	        void when_queryAllColumns_return_specificData() {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_SERVICES_HOTEL", 2);
	            }};
	            List<String> attrList = Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL");
	            doReturn(getSpecificServiceHotelData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
	            EntityResult entityResult = serviceHotelService.servicehotelQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(1, entityResult.calculateRecordNumber());
	            assertEquals(2, entityResult.getRecordValues(0).get(ServiceHotelDao.ATTR_ID));
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }
	     
	     
	        
	        
	        @Test
	        @DisplayName("Obtain all data columns from Services table when ID not exist")
	        void when_queryAllColumnsNotExisting_return_empty() {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_SERVICES_HOTEL", 5);
	            }};
	            List<String> attrList = Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL", "SVH_SERVICE","SVH_ACTIVE");
	            when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificServiceHotelData(keyMap, attrList));
	            EntityResult entityResult = serviceHotelService.servicehotelQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(0, entityResult.calculateRecordNumber());
	            verify(daoHelper).query(any(), anyMap(), anyList());
	        }

	        @ParameterizedTest(name = "Obtain data with ID -> {0}")
	        @MethodSource("randomIDGenerator")
	        @DisplayName("Obtain all data columns from Services table when ID is random")
	        void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
	            HashMap<String, Object> keyMap = new HashMap<>() {{
	                put("ID_SERVICES_HOTEL", random);
	            }};
	            List<String> attrList = Arrays.asList("ID_SERVICES_HOTEL", "SV_HOTEL");
	            when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificServiceHotelData(keyMap, attrList));
	            EntityResult entityResult = serviceHotelService.servicehotelQuery(new HashMap<>(), new ArrayList<>());
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            assertEquals(1, entityResult.calculateRecordNumber());
	            assertEquals(random, entityResult.getRecordValues(0).get(ServiceHotelDao.ATTR_ID));
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
	    @DisplayName("Test for Service_hotel inserts")
	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
	    public class ServiceInsert {
	        @Test
	        @DisplayName("Insert a Service_hotel successfully")
	        void service_insert_success() {
	        	Map<String, Object> dataToInsert = new HashMap<>();
	        	dataToInsert.put("svh_hotel", 1);
	        	dataToInsert.put("svh_service", 1);        	
	        	EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_SERVICES_HOTEL"));
	            er.addRecord(new HashMap<String, Object>() {{put("ID_SERVICES_HOTEL", 2);}});
	            er.setCode(EntityResult.OPERATION_SUCCESSFUL);
	            HashMap<String, Object> keyMap = new HashMap<>();
	            keyMap.put("ID_SERVICES_HOTEL", 2);
	            when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenReturn(er);
	            EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
	            assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
	            int recordIndex = entityResult.getRecordIndex(keyMap);
	            assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_SERVICE"));
	            verify(daoHelper).insert(serviceHotelDao, dataToInsert);
	            
	            }}}
//	       
//	        @Test
//	        @DisplayName("Fail trying to insert duplicated name")
//	        void service_insert_duplicated_mail() {
//	        	Map<String, Object> dataToInsert = new HashMap<>();
//	        	dataToInsert.put("sv_name", "Wifi");
//	        	dataToInsert.put("sv_description", "Wireless internet");
//	        	List<String> columnList = Arrays.asList("ID_SERVICE");
//	    		EntityResult insertResult = new EntityResultMapImpl(columnList);
//	    	    insertResult.addRecord(new HashMap<String, Object>() {{
//	    	        put("ID_SERVICE", 2);}});
//	        	when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenReturn(insertResult);
//	        	EntityResult resultSuccess = serviceHotelService.servicehotelInsert(dataToInsert);
//	        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
//	        	assertEquals("SUCESSFUL_INSERTION", resultSuccess.getMessage());
//	        	when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
//	        	EntityResult resultFail =serviceHotelService.servicehotelInsert(dataToInsert);
//	        	assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
//	        	assertEquals("SERVICE_NAME_ALREADY_EXISTS", resultFail.getMessage());
//	        	verify(daoHelper,times(2)).insert(any(), anyMap());
//	        }
//	        @Test
//	        @DisplayName("Fail trying to insert without sv_name field")
//	        void service_insert_without_name() {
//	        	Map<String, Object> dataToInsert = new HashMap<>();
//	        	dataToInsert.put("sv_description", "Wireless internet");
//	        	when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenThrow(DataIntegrityViolationException.class);
//	        	EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
//	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
//	        	assertEquals("SERVICE_NAME_REQUIRED", entityResult.getMessage());
//	        	verify(daoHelper).insert(any(), anyMap());
//	        }     
//			
//			@Test
//			@DisplayName("Fail trying to insert with no data")
//			void service_insert_withouth_data() {
//				EntityResult insertResult = new EntityResultMapImpl();
//				Map<String, Object> dataToInsert = new HashMap<>();
//				when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenReturn(insertResult);
//				EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
//				//assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
//				assertEquals("FIELDS_REQUIRED", entityResult.getMessage());
//			}
//	    }
//	
//	    @Nested
//	    @DisplayName("Test for Service updates")
//	    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//	    public class ServiceUpdate {
//	        @Test
//	        @DisplayName("Service update successful")
//	        void hotel_update_success() {
//	        	//given
//	        	Map<String, Object> filter = new HashMap<>();
//	        	filter.put("id_service", 32);
//	        	Map<String, Object> dataToUpdate = new HashMap<>();
//	        	dataToUpdate.put("sv_name", "wifi");
//	        	dataToUpdate.put("sv_description", "free wifi");
//	        	List<String> attrList = new ArrayList<>();
//	    		attrList.add("id_service");
//	        	EntityResult er = new EntityResultMapImpl();
//	        	er.setCode(EntityResult.OPERATION_SUCCESSFUL);
//	        	EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_HOTEL","SV_NAME"));
//	        	//when
//	        	when(daoHelper.update(serviceDao, dataToUpdate,filter)).thenReturn(er);
//	        	when(daoHelper.query(serviceDao, filter, attrList)).thenReturn(queryResult);
//	        	//then
//	        	EntityResult entityResult = serviceHotelService.serviceUpdate(dataToUpdate  ,filter);
//	        	assertEquals("SUCCESSFUL_UPDATE", entityResult.getMessage());
//	        	assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
//	        	verify(daoHelper).update(any(), anyMap(),anyMap());
//	        	verify(daoHelper).query(any(), anyMap(),anyList());
//	        }
//	        
//	        @Test
//	        @DisplayName("Fail trying to update a service with an existing name ")
//	        void service_fail_update_with_duplicated_name() {
//	        	EntityResult er = new EntityResultMapImpl();
//	        	er.setCode(EntityResult.OPERATION_WRONG);
//	        	er.setMessage("SERVICE_NAME_ALREADY_EXISTS");
//	        	Map<String, Object> filter = new HashMap<>();
//	        	filter.put("id_service", 32);
//	        	Map<String, Object> dataToUpdate = new HashMap<>();
//	        	dataToUpdate.put("sv_name", "wifi");
//	        	dataToUpdate.put("sv_description", "wireless internet");
//	        	List<String> attrList = new ArrayList<>();
//	    		attrList.add("id_service");
//	        	EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_SERVICE","SV_NAME"));
//	            er.addRecord(new HashMap<String, Object>() {{put("ID_SERVICE", 2);put("ID_SERVICE","SV_NAME");}});
//	            er.setCode(EntityResult.OPERATION_SUCCESSFUL);
//	            when(daoHelper.query(serviceDao, filter, attrList)).thenReturn(queryResult);
//	        	when(daoHelper.update(serviceDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
//	        	EntityResult entityResult = serviceHotelService.serviceUpdate(dataToUpdate,filter);
//	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
//	        	assertEquals("SERVICE_NAME_ALREADY_EXISTS", entityResult.getMessage());
//	        	verify(daoHelper).update(any(), anyMap(),anyMap());
//	        	verify(daoHelper).query(any(), anyMap(),anyList());
//	        }
//	        @Test
//	        @DisplayName("Fail trying to update a service that doesn´t exists")
//	        void update_service_doesnt_exists() {
//	        	Map<String, Object> filter = new HashMap<>();
//	        	filter.put("id_service",222);
//	        	Map<String, Object> dataToUpdate = new HashMap<>();
//	        	dataToUpdate.put("sv_name", "wifi");
//	        	dataToUpdate.put("sv_description", "wireless internet");
//	        	List<String> attrList = new ArrayList<>();
//	    		attrList.add("id_service");
//	        	EntityResult er = new EntityResultMapImpl();
//	        	er.setCode(EntityResult.OPERATION_WRONG);
//	        	er.setMessage("SERVICE_DOESN'T_EXISTS");
//	        	EntityResult queryResult = new EntityResultMapImpl();
//	        	when(daoHelper.query(serviceDao, filter,attrList)).thenReturn(queryResult);
//	        	EntityResult entityResult = serviceHotelService.serviceUpdate(dataToUpdate,filter);
//	        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
//	        	assertEquals("ERROR_SERVICE_NOT_FOUND", entityResult.getMessage());
//	        	verify(daoHelper).query(any(), anyMap(),anyList());
//	        }
//	        
//			@Test
//			@DisplayName("Fail trying to update without any fields")
//			void hotel_update_without_any_fields() {
//				Map<String, Object> filter = new HashMap<>();
//				Map<String, Object> dataToUpdate = new HashMap<>();
//				EntityResult updateResult = serviceHotelService.serviceUpdate(dataToUpdate,filter);
//				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
//				assertEquals("EMPTY_REQUEST", updateResult.getMessage());
//			}
//			@Test
//			@DisplayName("Fail trying to update without id_service")
//			void hotel_update_without_id_hotel() {
//				Map<String, Object> filter = new HashMap<>();
//				Map<String, Object> dataToUpdate = new HashMap<>();
//				dataToUpdate.put("sv_name", "wifi");
//	        	dataToUpdate.put("sv_description", "wireless internet");
//				EntityResult updateResult = serviceHotelService.serviceUpdate(dataToUpdate,filter);
//				assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
//				assertEquals("ID_SERVICE_REQUIRED", updateResult.getMessage());
//			}
//			
//	    }
	    
	    
	     

