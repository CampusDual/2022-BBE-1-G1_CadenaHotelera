package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.DiscountCodeData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.DiscountCodeDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.SeasonDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

public class DiscountCodeServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	DiscountCodeService discountCodeService;
	@Autowired
	DiscountCodeDao discountCodeDao;

	@BeforeEach
	void setUp() {
		this.discountCodeService = new DiscountCodeService();
		MockitoAnnotations.openMocks(this);
	}


	@Nested
	@DisplayName("Test for Discount code queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class DiscountCodeQuery {

		@Test
		@DisplayName("Obtain all data from Discount Code table")
		void testDiscountCodeQueryAllData() throws NotAuthorizedException {
			doReturn(getAllDiscountCodeData()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = discountCodeService.discountcodeQuery(getGenericFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = discountCodeService.discountcodeQuery(getGenericFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
				
		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_code", "string");
			List<String> columns = new ArrayList<>();
			columns.add("id_code");
			columns.add("dc_name");
			when(daoHelper.query(discountCodeDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = discountCodeService.discountcodeQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Seasons table when ID_CODE is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("id_code", 2);
				}
			};
			List<String> attrList = Arrays.asList("id_code", "dc_name");
			doReturn(getSpecificDiscountCodeData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			
			EntityResult entityResult = discountCodeService.discountcodeQuery(getGenericFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get("id_code"));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}


		@ParameterizedTest(name = "Obtain data with Hotel -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from Discount Code table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("id_code", random);
				}
			};
			List<String> attrList = Arrays.asList("id_season", "ss_hotel");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificDiscountCodeData(keyMap, attrList));
			EntityResult entityResult = discountCodeService.discountcodeQuery(getGenericFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get("id_code"));
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
	@DisplayName("Test for Discount code inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class DiscountCodeInsert {
		@Test
		@DisplayName("Insert a discount code successfully")
		void discount_code_insert_success() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			List<String> columnList = Arrays.asList("id_season");
			EntityResult insertResult = getGenericInsertResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			when(daoHelper.insert(discountCodeDao, dataToInsert)).thenReturn(insertResult);
			EntityResult resultSuccess = discountCodeService.discountcodeInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCCESSFUL_INSERTION", resultSuccess.getMessage());

		}
		
		@Test
		@DisplayName("Insert a discount code with a multiplier below 0")
		void discount_code_insert_multiplier_below_0() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("dc_multiplier", -1.1);
			List<String> columnList = Arrays.asList("id_season");
			EntityResult insertResult = getGenericInsertResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			when(daoHelper.insert(discountCodeDao, dataToInsert)).thenReturn(insertResult);
			EntityResult result = discountCodeService.discountcodeInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, result.getCode());
			assertEquals("DC_MULTIPLIER_MUST_BE_HIGHER_THAN_0", result.getMessage());

		}
		

		@Test
		@DisplayName("Fail trying to insert duplicated name")
		void discount_code_insert_duplicated_name() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			List<String> columnList = Arrays.asList("id_season");
			EntityResult insertResult = getGenericInsertResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			when(daoHelper.insert(discountCodeDao, dataToInsert)).thenReturn(insertResult);
			EntityResult resultSuccess = discountCodeService.discountcodeInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCCESSFUL_INSERTION", resultSuccess.getMessage());
			when(daoHelper.insert(discountCodeDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = discountCodeService.discountcodeInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("DISCOUNT_CODE_NAME_ALREADY_EXISTS", resultFail.getMessage());
			verify(daoHelper, times(2)).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without fields")
		void discount_code_insert_without_name() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.remove("dc_name");
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(any(), anyMap())).thenThrow(DataIntegrityException);
			EntityResult entityResult = discountCodeService.discountcodeInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert with no data")
		void discount_code_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			EntityResult entityResult = discountCodeService.discountcodeInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
		
		@Test
		@DisplayName("Fail trying to insert with incorrect type")
		void discount_code_insert_with_incorrect_type() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("dc_multiplier","sss");
			when(daoHelper.insert(any(), anyMap())).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = discountCodeService.discountcodeInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
	
		}
		
	}
	
	

	@Nested
	@DisplayName("Test for Discount Code updates")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class DiscountCodeUpdate {
		@Test
		@DisplayName("Discount code update successful")
		void discount_code_update_success() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();				
			when(daoHelper.query(any(), any(), any())).thenReturn(getGenericInsertResult());
			when(daoHelper.update(any(), any(), any())).thenReturn(new EntityResultMapImpl());	
			EntityResult entityResult = discountCodeService.discountcodeUpdate(dataToUpdate, filter);
			assertEquals("SUCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

	
		
		@Test
		@DisplayName("Fail trying to update a discount code with an existing name ")
		void discount_code_fail_update_with_duplicated_name() {
			
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();	
			
			when(daoHelper.query(any(), any(), any())).thenReturn(getGenericInsertResult());
			when(daoHelper.update(any(), any(), any())).thenThrow(new DuplicateKeyException("DISCOUNT_CODE_NAME_ALREADY_EXISTS"));	
			
			EntityResult entityResult = discountCodeService.discountcodeUpdate(dataToUpdate, filter);
			assertEquals("DISCOUNT_CODE_NAME_ALREADY_EXISTS", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
					
		}
	
		
		@Test
		@DisplayName("Fail trying to update a discount code that doesn´t exists")
		void discount_code_doesnt_exists() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();						
			doReturn(new EntityResultMapImpl()).when(daoHelper).query(any(), anyMap(), anyList());				
			EntityResult entityResult = discountCodeService.discountcodeUpdate(dataToUpdate, filter);
			assertEquals("CODE_DOESN'T_EXISTS", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), anyMap(), anyList());
			
		}

		@Test
		@DisplayName("Fail trying to update without any fields")
		void discount_code_update_without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_code", 2);
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = discountCodeService.discountcodeUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("EMPTY_REQUEST", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update without id_code")
		void season_update_without_id_season() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult updateResult = discountCodeService.discountcodeUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_CODE_REQUIRED", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update with incorrect types")
		void discount_code_update_with_incorrect_types() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_code", 2);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("dc_multiplier", "sss");
			when(daoHelper.query(any(), any(), any())).thenReturn(getGenericInsertResult());
			when(daoHelper.update(any(), any(), any())).thenThrow(BadSqlGrammarException.class);	
			
			EntityResult updateResult = discountCodeService.discountcodeUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
		}
		
		
	}
	
	@Nested
	@DisplayName("Test for Discount code deletes")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class DiscountCodeDelete {
		@Test
		@DisplayName("Discount code delete successful")
		void discount_code_delete_success() {
			Map<String, Object> filter = getGenericFilter();
			when(daoHelper.query(any(), any(), any())).thenReturn(getGenericInsertResult());
			when(daoHelper.update(any(), any(),any())).thenReturn(getGenericQueryResult());
			EntityResult er=discountCodeService.discountcodeDelete(filter);
			
			assertEquals("SUCCESSFUL_DELETE", er.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, er.getCode());
			
			
		}
	
		@Test
		@DisplayName("Trying to delete without id_code")
		void delete_without_id_code() {
			Map<String, Object> filter = new HashMap<>();	
			EntityResult er=discountCodeService.discountcodeDelete(filter);			
			assertEquals("ID_CODE_REQUIRED", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());			
			
		}
	
		@Test
		@DisplayName("Season delete without results")
		void season_delete_without_results() {
			Map<String, Object> filter = getGenericFilter();
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			EntityResult er=discountCodeService.discountcodeDelete(filter);	
			assertEquals("CODE_DOESN'T_EXISTS", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());
			
			
		}
		
	
	
}
}
