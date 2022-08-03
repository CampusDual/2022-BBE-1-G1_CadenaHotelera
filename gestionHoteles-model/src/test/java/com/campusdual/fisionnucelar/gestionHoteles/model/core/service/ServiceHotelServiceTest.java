package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ServiceHotelTestData.*;

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
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceHotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@ExtendWith(MockitoExtension.class)
public class ServiceHotelServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	ServiceHotelService serviceHotelService;

	@InjectMocks
	HotelService hotelService;

	@Autowired
	ServiceHotelDao serviceHotelDao;

	@Mock
	HotelDao hotelDao;

	@Mock
	ServiceDao serviceDao;
	
	@Mock
	UserControl userControl;

	@BeforeEach
	void setUp() {
		this.serviceHotelService = new ServiceHotelService();
		this.hotelService = new HotelService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Test for ServiceHotel queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ServiceHotelQuery {

		@Test
		@DisplayName("Obtain all data from ServicesHotel table")
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
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_services_hotel", "string");
			List<String> columns = new ArrayList<>();
			columns.add("svh_hotel");
			columns.add("svh_service");		
			when(daoHelper.query(serviceHotelDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = serviceHotelService.servicehotelQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from ServicesHotel table when ID is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_SERVICES_HOTEL", 2);
				}
			};
			List<String> attrList = Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL");
			doReturn(getSpecificServiceHotelData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = serviceHotelService.servicehotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get(ServiceHotelDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from ServicesHotel table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_SERVICES_HOTEL", 5);
				}
			};
			List<String> attrList = Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL", "SVH_SERVICE", "SVH_ACTIVE");
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
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_SERVICES_HOTEL", random);
				}
			};
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
	@DisplayName("Test for ServicesHotel inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ServiceInsert {
		@Test
		@DisplayName("Insert a ServiceHotel successfully")
		void serviceHotel_insert_success() {
			Map<String, Object> dataToInsert= getGenericDataToInsertOrUpdate();
			EntityResult er =getGenericInsertResult();
			
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("ID_SERVICES_HOTEL", 2);
			when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenReturn(er);

			EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_SERVICES_HOTEL"));
			verify(daoHelper).insert(serviceHotelDao, dataToInsert);
		}

		@Test
		@DisplayName("Fail trying to insert duplicated combination of hotel and service")
		void hotelService_insert_duplicated() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult insertResult = getGenericInsertResult();
				
			when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenReturn(insertResult);		
			EntityResult resultSuccess = serviceHotelService.servicehotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCCESSFUL_INSERTION", resultSuccess.getMessage());

			when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = serviceHotelService.servicehotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("DUPLICATED_SERVICES_IN_HOTEL", resultFail.getMessage());
			verify(daoHelper, times(2)).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without a not null field or with a non existing foreign key")
		void serviceHotel_insert_without_service_or_hotel() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("svh_active", 1);
			DataIntegrityViolationException DataIntegrityException=new DataIntegrityViolationException("RUN_TIME_MESSAGE");
					
			when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenThrow(DataIntegrityException);
			EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_MESSAGE", entityResult.getMessage());
			verify(daoHelper).insert(any(), anyMap());
		}
		

		@Test
		@DisplayName("Fail trying to insert with no data")
		void serviceHotel_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			when(daoHelper.insert(serviceHotelDao, dataToInsert)).thenReturn(insertResult);
			EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("FIELDS_REQUIRED", entityResult.getMessage());
		}
	}

	@Nested
	@DisplayName("Test for ServiceHotel updates")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class HotelUpdate {
		@Test
		@DisplayName("ServiceHotel update succesful")
		void serviceHotel_update_success() {
			
			Map<String, Object> filter = getGenericFilter();			
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();			
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();
		
			when(daoHelper.update(any(), any(), any())).thenReturn(er);
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
		
			EntityResult entityResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), any(), any());
		}

		@Test
		@DisplayName("Fail trying to update an hotel with a duplicated combination of hotel and service")
		void serviceHotel_fail_update_with_duplicated() {
		
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult queryResult = getGenericQueryResult();
		
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			when(daoHelper.update(serviceHotelDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
			EntityResult entityResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("DUPLICATED_SERVICES_IN_HOTEL", entityResult.getMessage());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), any(), any());
		}

		@Test
		@DisplayName("Fail trying to update an ServicesHotel that doesn´t exists")
		void update_serviceHotel_doesnt_exists() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			List<String> attrList = getGenericAttrList();
			
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(serviceHotelDao, filter, attrList)).thenReturn(queryResult);
			EntityResult entityResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ERROR_SERVICE_IN_HOTEL_NOT_FOUND", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail trying to insert with a non existing foreign key")
		void serviceHotel_insert_without_service_or_hotel() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();	
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();
			DataIntegrityViolationException DataIntegrityException=new DataIntegrityViolationException("RUN_TIME_MESSAGE");
		
			when(daoHelper.update(any(), any(), any())).thenThrow(DataIntegrityException);
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
		
			EntityResult entityResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_MESSAGE", entityResult.getMessage());
			
		}
		
		@Test
		@DisplayName("Fail trying to update without any fields")
		void serviceHotel_update_without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("EMPTY_REQUEST", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update without id_services_hotel")
		void serviceHotel_update_without_id_services_hotel() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult updateResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_SERVICES_HOTEL_REQUIRED", updateResult.getMessage());
		}
		
		@Test
		@DisplayName("Fail trying to update with boolean bigger than 1 ")
		void serviceHotel_update_with_big_boolean() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("svh_hotel", 1);
			dataToUpdate.put("svh_service", 1);
			dataToUpdate.put("svh_active", 3);
				
			EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL"));	
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			EntityResult entityResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals("SVH_ACTIVE_MUST_BE_1_OR_0", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), any(), any());
		}
		
		@Test
		@DisplayName("Fail trying to update with boolean smaller than 0")
		void serviceHotel_update_with_small_boolean() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("svh_hotel", 1);
			dataToUpdate.put("svh_service", 1);
			dataToUpdate.put("svh_active", -3);
			EntityResult queryResult = getGenericQueryResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			// then
			EntityResult entityResult = serviceHotelService.servicehotelUpdate(dataToUpdate, filter);
			assertEquals("SVH_ACTIVE_MUST_BE_1_OR_0", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), any(), any());
		}
		
	
		//INNECESARIOS TRAS LA REFACTORIZACION, INTERESANTES COMO DOCUMENTACION
//		@Test
//		@DisplayName("Fail trying to insert with a non existing hotel")
//		void hotel_doesnt_exists() {
//			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
//			EntityResult er = getGenericInsertResult();
//
//			EntityResult hotel = new EntityResultMapImpl();
//			when(daoHelper.query(any(), any(), any())).thenReturn(hotel);
//
//			EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
//			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
//			assertEquals("HOTEL_DOESN'T_EXISTS", entityResult.getMessage());
//
//		}
//
//		@Test
//		@DisplayName("Fail trying to insert with a non existing service")
//		void service_doesnt_exists() {
//			Map<String, Object> dataToInsert = new HashMap<>();
//			dataToInsert.put("svh_hotel", 1);
//			dataToInsert.put("svh_service", 2);
//			dataToInsert.put("svh_active", 1);
//
//			EntityResult er = getGenericInsertResult();
//
//			EntityResult hotel = new EntityResultMapImpl();
//			Map<String, Object> keyMapHotel = new HashMap<>();
//			List<String> attrListHotel = new ArrayList<>();
//			keyMapHotel.put("id_hotel", 1);
//			attrListHotel.add("id_hotel");
//			hotel.put("id_hotel", 1);
//
//			EntityResult service = new EntityResultMapImpl();
//			Map<String, Object> keyMapService = new HashMap<>();
//			List<String> attrListService = new ArrayList<>();
//			keyMapService.put("id_service", 2);
//			attrListService.add("id_service");
//
//			when(daoHelper.query(hotelDao, keyMapHotel, attrListHotel)).thenReturn(hotel);
//			when(daoHelper.query(serviceDao, keyMapService, attrListService)).thenReturn(service);
//				
//			EntityResult entityResult = serviceHotelService.servicehotelInsert(dataToInsert);
//			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
//			assertEquals("SERVICE_DOESN'T_EXISTS", entityResult.getMessage());
//
//		}


	}
}