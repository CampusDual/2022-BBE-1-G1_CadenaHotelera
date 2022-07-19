package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraHotelTestData.getAllExtraHotelData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraHotelTestData.getGenericAttrList;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraHotelTestData.getGenericDataToInsertOrUpdate;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraHotelTestData.getGenericFilter;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraHotelTestData.getGenericInsertResult;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraHotelTestData.getGenericQueryResult;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ExtraHotelTestData.getSpecificExtraHotelData;

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

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ExtraHotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@ExtendWith(MockitoExtension.class)
public class ExtraHotelServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	ExtraHotelService extraHotelService;

	@InjectMocks
	HotelService hotelService;

	@Autowired
	ExtraHotelDao extraHotelDao;

	@Mock
	HotelDao hotelDao;

	@Mock
	ExtraDao extraDao;

	@Mock
	ServiceDao serviceDao;

	@BeforeEach
	void setUp() {
		this.extraHotelService = new ExtraHotelService();
		this.hotelService = new HotelService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Test for ExtraHotel queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ServiceHotelQuery {

		@Test
		@DisplayName("Obtain all data from ExtraHotel table")
		void when_queryOnlyWithAllColumns_return_allServiceHotelData() {
			doReturn(getAllExtraHotelData()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = extraHotelService.extrahotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = extraHotelService.extrahotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_extras_hotel", "string");
			List<String> columns = new ArrayList<>();
			columns.add("exh_hotel");
			columns.add("exh_service");
			when(daoHelper.query(extraHotelDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = extraHotelService.extrahotelQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from ExtraHotel table when ID is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_EXTRAS_HOTEL", 2);
				}
			};
			List<String> attrList = Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL");
			doReturn(getSpecificExtraHotelData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = extraHotelService.extrahotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get(ExtraHotelDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from ExtraHotel table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_EXTRAS_HOTEL", 5);
				}
			};
			List<String> attrList = Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL", "EXH_SERVICE", "EX_PRICE",
					"EXH_ACTIVE");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificExtraHotelData(keyMap, attrList));
			EntityResult entityResult = extraHotelService.extrahotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(0, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@ParameterizedTest(name = "Obtain data with ID -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from ExtraHotel table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_EXTRAS_HOTEL", random);
				}
			};
			List<String> attrList = Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificExtraHotelData(keyMap, attrList));
			EntityResult entityResult = extraHotelService.extrahotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get(ExtraHotelDao.ATTR_ID));
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
	@DisplayName("Test for ExtraHotel inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ServiceInsert {
		@Test
		@DisplayName("Insert a ServicesHotel successfully")
		void extraHotel_insert_success() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult er = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("ID_EXTRAS_HOTEL", 2);

			when(daoHelper.insert(extraHotelDao, dataToInsert)).thenReturn(er);

			EntityResult entityResult = extraHotelService.extrahotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_EXTRAS_HOTEL"));
			verify(daoHelper).insert(extraHotelDao, dataToInsert);
		}

		@Test
		@DisplayName("Fail trying to insert duplicated combination of hotel and extra")
		void extraHotel_insert_duplicated() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult insertResult = getGenericInsertResult();

			when(daoHelper.insert(extraHotelDao, dataToInsert)).thenReturn(insertResult);

			EntityResult resultSuccess = extraHotelService.extrahotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCCESSFUL_INSERTION", resultSuccess.getMessage());

			when(daoHelper.insert(extraHotelDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = extraHotelService.extrahotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("DUPLICATED_EXTRAS_IN_HOTEL", resultFail.getMessage());
			verify(daoHelper, times(2)).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without a not null field or a non existing foreign key")
		void extraHotel_insert_without_hotel_price_or_extra() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("exh_active", 1);
			DataIntegrityViolationException dataIntegrityException = new DataIntegrityViolationException("RUN_TIME_EXCEPTION");
			when(daoHelper.insert(extraHotelDao, dataToInsert)).thenThrow(dataIntegrityException);
			EntityResult entityResult = extraHotelService.extrahotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_EXCEPTION", entityResult.getMessage());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert with no data")
		void extraHotel_insert_withouth_data() {
			Map<String, Object> dataToInsert = new HashMap<>();
			EntityResult entityResult = extraHotelService.extrahotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ANY_FIELDS_REQUIRED", entityResult.getMessage());
		}

	
		@Test
		@DisplayName("Insert an ExtraHotel with a non numeric price")
		void insert_with_non_numeric_price() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			when(daoHelper.insert(extraHotelDao, dataToInsert)).thenThrow(BadSqlGrammarException.class);		
			EntityResult entityResult = extraHotelService.extrahotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("PRICE_MUST_BE_NUMERIC", entityResult.getMessage());

		}

	}

	@Nested
	@DisplayName("Test for ExtraHotel updates")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class HotelUpdate {
		@Test
		@DisplayName("ExtraHotel update succesful")
		void hotel_update_success() {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();

			when(daoHelper.update(any(), any(), any())).thenReturn(er);
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);

			EntityResult entityResult = extraHotelService.extrahotelUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), any(), any());
		}

		@Test
		@DisplayName("Fail trying to update an ExtraHotel with a duplicated combination of hotel and extra")
		void hotel_fail_update_with_duplicated_hotel_name_or_email() {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult queryResult = getGenericQueryResult();

			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			when(daoHelper.update(extraHotelDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
			EntityResult entityResult = extraHotelService.extrahotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("DUPLICATED_EXTRA_IN_HOTEL", entityResult.getMessage());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), any(), any());
		}

		@Test
		@DisplayName("Fail trying to update an extra that doesn´t exists")
		void update_extraHotel_doesnt_exists() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			EntityResult entityResult = extraHotelService.extrahotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ERROR_EXTRA_IN_HOTEL_NOT_FOUND", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail trying to update without any fields")
		void extraHotel_update_without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = extraHotelService.extrahotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ANY_FIELDS_REQUIRED", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update without id_extras_hotel")
		void extraHotel_update_without_id_services_hotel() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult updateResult = extraHotelService.extrahotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_EXTRA_HOTEL_REQUIRED", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update with incorrect_boolean")
		void extraHotel_update_with_big_boolean() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("exh_hotel", 1);
			dataToUpdate.put("exh_extra", 1);
			dataToUpdate.put("exh_price", 4);
			dataToUpdate.put("exh_active", 3);

			EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL"));
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			EntityResult entityResult = extraHotelService.extrahotelUpdate(dataToUpdate, filter);
			assertEquals("EXH_ACTIVE_MUST_BE_1_OR_0", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), any(), any());

		}

		@Test
		@DisplayName("Fail trying to update with small boolean")
		void extraHotel_update_with_small_boolean() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("exh_hotel", 1);
			dataToUpdate.put("exh_extra", 1);
			dataToUpdate.put("exh_price", 4);
			dataToUpdate.put("exh_active", -3);

			EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL"));
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			EntityResult entityResult = extraHotelService.extrahotelUpdate(dataToUpdate, filter);
			assertEquals("EXH_ACTIVE_MUST_BE_1_OR_0", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), any(), any());

		}
		
		@Test
		@DisplayName("Fail trying to update with a non existing foreign key")
		void extraHotel_insert_without_hotel_price_or_extra() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("exh_hotel", 1);
			EntityResult queryResult = getGenericQueryResult();
			DataIntegrityViolationException dataIntegrityException = new DataIntegrityViolationException("RUN_TIME_EXCEPTION");
			
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);			
			when(daoHelper.update(extraHotelDao, dataToUpdate, filter)).thenThrow(dataIntegrityException);		
			
			EntityResult entityResult = extraHotelService.extrahotelUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_EXCEPTION", entityResult.getMessage());
			
		}
		
		@Test
		@DisplayName("Fail trying to update with a non numeric price")
		void extraHotel_insert_with_non_numeric_price() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("exh_hotel", 1);
			EntityResult queryResult = getGenericQueryResult();
			DataIntegrityViolationException dataIntegrityException = new DataIntegrityViolationException("RUN_TIME_EXCEPTION");
			
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);			
			when(daoHelper.update(extraHotelDao, dataToUpdate, filter)).thenThrow(dataIntegrityException);		
			
			EntityResult entityResult = extraHotelService.extrahotelUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RUN_TIME_EXCEPTION", entityResult.getMessage());
			
		}
				
		@Test
	      @DisplayName("Fail trying to update rmt_price field as string")
	      void service_insert_string_price() {
	        Map<String, Object> filter = getGenericFilter();
	        Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();

	        when (daoHelper.query(any(), any(), any())).thenReturn(getGenericQueryResult());
	        when(daoHelper.update(any(), any(),any())).thenThrow(BadSqlGrammarException.class);
	      
	        EntityResult updateResult = extraHotelService.extrahotelUpdate(dataToUpdate,filter);
	        assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
	        assertEquals("PRICE_MUST_BE_NUMERIC", updateResult.getMessage());
	    
	      }
		
	}
}
