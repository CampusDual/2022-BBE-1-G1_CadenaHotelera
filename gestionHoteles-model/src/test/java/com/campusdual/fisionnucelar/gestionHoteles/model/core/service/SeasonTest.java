package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.SeasonTestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.SeasonDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ServiceDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@ExtendWith(MockitoExtension.class)
public class SeasonTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	SeasonService seasonService;
	@Autowired
	SeasonDao seasonDao;

	@BeforeEach
	void setUp() {
		this.seasonService = new SeasonService();
		MockitoAnnotations.openMocks(this);
	}

	@Mock
	UserControl userControl;

	@Nested
	@DisplayName("Test for Seasons queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class SeasonsQuery {

		@Test
		@DisplayName("Obtain all data from Seasons table")
		void testQueryAllData() throws NotAuthorizedException {
			doReturn(getAllSeasonData()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = seasonService.seasonQuery(getQueryFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Try to search all the seasons withouth especifying an hotel")
		void search_without_hotel() throws NotAuthorizedException {			
			Map<String, Object> filter = new HashMap<>();			
			EntityResult entityResult = seasonService.seasonQuery(filter, new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = seasonService.seasonQuery(getQueryFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
		@Test
		@DisplayName("Try to search with an manager user from the incorrect hotel")
		void notAuthorized() throws NotAuthorizedException {
			doReturn(getAllSeasonData()).when(daoHelper).query(any(), anyMap(), anyList());
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccess(anyInt());
			EntityResult entityResult = seasonService.seasonQuery(getQueryFilter(), new ArrayList<>());			
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NOT_AUTHORIZED",entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
				

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("ss_hotel", "string");
			List<String> columns = new ArrayList<>();
			columns.add("id_season");
			columns.add("ss_multiplier");
			when(daoHelper.query(seasonDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = seasonService.seasonQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Seasons table when Hotel is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ss_hotel", 2);
				}
			};
			List<String> attrList = Arrays.asList("ss_hotel", "id_season");
			doReturn(getSpecificSeasonData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());

			EntityResult entityResult = seasonService.seasonQuery(getQueryFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get("ss_hotel"));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}


		@ParameterizedTest(name = "Obtain data with Hotel -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from Services table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ss_hotel", random);
				}
			};
			List<String> attrList = Arrays.asList("id_season", "ss_hotel");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificSeasonData(keyMap, attrList));
			EntityResult entityResult = seasonService.seasonQuery(getQueryFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get("id_season"));
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
	@DisplayName("Test for Season inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class SeasonInsert {
		@Test
		@DisplayName("Insert a season successfully")
		void season_insert_success() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			List<String> columnList = Arrays.asList("id_season");
			EntityResult insertResult = getGenericInsertResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			when(daoHelper.insert(seasonDao, dataToInsert)).thenReturn(insertResult);
			EntityResult resultSuccess = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCESSFULL_INSERTION", resultSuccess.getMessage());

		}

		@Test
		@DisplayName("Fail trying to insert duplicated name")
		void season_insert_duplicated_name() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			List<String> columnList = Arrays.asList("id_season");
			EntityResult insertResult = getGenericInsertResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			when(daoHelper.insert(seasonDao, dataToInsert)).thenReturn(insertResult);
			EntityResult resultSuccess = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCESSFULL_INSERTION", resultSuccess.getMessage());
			when(daoHelper.insert(seasonDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("SS_NAME_ALREADY_EXISTS", resultFail.getMessage());
			verify(daoHelper, times(2)).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without fields")
		void season_insert_without_name() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.remove("ss_name");
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(any(), anyMap())).thenThrow(DataIntegrityException);
			EntityResult entityResult = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert with no data")
		void season_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			EntityResult entityResult = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
		
		@Test
		@DisplayName("Try to insert a season with an start date after end date")
		void start_before_end() {
			Calendar c = Calendar.getInstance();
			c.set(2022, 10, 10);
			Date startDate = c.getTime();
			c.set(2022, 9, 10);
			Date endDate = c.getTime();
			
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("ss_start_date", startDate);
			dataToInsert.put("ss_end_date", endDate);
							
			EntityResult resultSuccess = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());
			assertEquals("START_DATE_MUST_BE_BEFORE_END_DATE", resultSuccess.getMessage());
		}
		
		@Test
		@DisplayName("Try to insert a season with an start date before current date")
		void start_before_current_date() {
			Calendar c = Calendar.getInstance();
			c.set(2020, 8, 10);
			Date startDate = c.getTime();
			c.set(2020, 9, 10);
			Date endDate = c.getTime();
			
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("ss_start_date", startDate);
			dataToInsert.put("ss_end_date", endDate);
							
			EntityResult resultSuccess = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());
			assertEquals("START_DATE_MUST_BE_EQUAL_OR_AFTER_CURRENT_DATE", resultSuccess.getMessage());
		}
		
		@Test
		@DisplayName("Try to insert on dates coincident with another season")
		void coincident_seasons() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			List<String> columnList = Arrays.asList("id_season");
			EntityResult insertResult = getGenericInsertResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(getGenericInsertResult());
			EntityResult resultSuccess = seasonService.seasonInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());
			assertEquals("THERE_IS_ANOTHER_SEASON_ACTIVE_ON_THAT_DATES", resultSuccess.getMessage());
		}
		
	}
	
	

	@Nested
	@DisplayName("Test for Season updates")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class SeasonUpdate {
		@Test
		@DisplayName("Season update successful")
		void season_update_success() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();	
			
			when(daoHelper.update(any(), any(), any())).thenReturn(new EntityResultMapImpl());
		
			
			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return getGenericUpdateResult();
					if (count == 2)
						return new EntityResultMapImpl();
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			

			EntityResult entityResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals("SUCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper,times(2)).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Season update successful without start date")
		void season_update_success_without_start_date() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();	
			dataToUpdate.remove("ss_start_date");
			
			when(daoHelper.update(any(), any(), any())).thenReturn(new EntityResultMapImpl());
		
			
			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return getGenericUpdateResult();
					if (count == 2)
						return new EntityResultMapImpl();
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			

			EntityResult entityResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals("SUCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper,times(2)).query(any(), anyMap(), anyList());
		}

		
		@Test
		@DisplayName("Season update successful without end date")
		void season_update_success_without_end_date() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();	
			dataToUpdate.remove("ss_end_date");
			
			when(daoHelper.update(any(), any(), any())).thenReturn(new EntityResultMapImpl());
		
			
			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return getGenericUpdateResult();
					if (count == 2)
						return new EntityResultMapImpl();
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			

			EntityResult entityResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals("SUCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper,times(2)).query(any(), anyMap(), anyList());
		}
		
		
		@Test
		@DisplayName("Fail trying to update a season with an existing name ")
		void season_fail_update_with_duplicated_name() {
			
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();	
			
			when(daoHelper.update(any(), any(), any())).thenThrow(DuplicateKeyException.class);				
			Mockito.doAnswer(new Answer() {
				private int count = 0;
				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return getGenericUpdateResult();
					if (count == 2)
						return new EntityResultMapImpl();
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			
			EntityResult entityResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals("SS_NAME_ALREADY_EXISTS", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper,times(2)).query(any(), anyMap(), anyList());
					
		}

		
		
		
		
		
		@Test
		@DisplayName("Fail trying to update a season that doesn´t exists")
		void update_season_doesnt_exists() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();						
			doReturn(new EntityResultMapImpl()).when(daoHelper).query(any(), anyMap(), anyList());				
			EntityResult entityResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals("SEASON_DON'T_EXISTS", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), anyMap(), anyList());
			
		}

		@Test
		@DisplayName("Fail trying to update without any fields")
		void season_update_without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_season", 2);
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("EMPTY_REQUEST", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update without id_season")
		void season_update_without_id_season() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult updateResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_SEASON_REQUIRED", updateResult.getMessage());
		}

		
		@Test
		@DisplayName("Fail trying to update the hotel of a season")
		void season_hotel_update() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_season", 2);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("ss_hotel", 2);
			EntityResult updateResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("SS_HOTEL_CAN'T_BE_UPDATED", updateResult.getMessage());
		}
				
		@Test
		@DisplayName("Fail trying to update a season that doesn´t exists")
		void not_authorized() throws NotAuthorizedException {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();
			doReturn(getGenericUpdateResult()).when(daoHelper).query(any(), anyMap(), anyList());
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccess(anyInt());		
			EntityResult entityResult = seasonService.seasonUpdate(dataToUpdate, filter);
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), anyMap(), anyList());		
		}
		
		
	}
	
	@Nested
	@DisplayName("Test for Season deletes")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class SeasonDelete {
		@Test
		@DisplayName("Season delete successful")
		void season_delete_success() {
			Map<String, Object> filter = getGenericFilter();
			when(daoHelper.query(any(), any(), any())).thenReturn(getGenericUpdateResult());
			when(daoHelper.delete(any(), any())).thenReturn(getGenericInsertResult());
			EntityResult er=seasonService.seasonDelete(filter);
			
			assertEquals("SUCCESSFULL_DELETE", er.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, er.getCode());
			
			
		}
		@Test
		@DisplayName("Trying to delete a season from another hotel")
		void not_authorized() throws NotAuthorizedException {
			Map<String, Object> filter = getGenericFilter();
			when(daoHelper.query(any(), any(), any())).thenReturn(getGenericUpdateResult());
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccess(anyInt());	
			EntityResult er=seasonService.seasonDelete(filter);			
			assertEquals("NOT_AUTHORIZED", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());
						
		}
	
		@Test
		@DisplayName("Trying to delete without id_season")
		void delete_without_id_season() {
			Map<String, Object> filter = new HashMap<>();	
			EntityResult er=seasonService.seasonDelete(filter);			
			assertEquals("ID_SEASON_REQUIRED", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());			
			
		}
	
		@Test
		@DisplayName("Season delete without results")
		void season_delete_without_results() {
			Map<String, Object> filter = getGenericFilter();
			when(daoHelper.query(any(), any(), any())).thenReturn(new EntityResultMapImpl());
			EntityResult er=seasonService.seasonDelete(filter);		
			assertEquals("NO_RESULTS", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());
			
			
		}
		
	
	
}
	
	@Nested
	@DisplayName("Test for HotelSeasonsDelete")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class HotelSeasonDelete {
		@Test
		@DisplayName("HotelSeasons delete successful")
		void hotel_season_delete_success() {
			Map<String, Object> filter = getQueryFilter();
			when(daoHelper.query(any(), any(), any(),anyString())).thenReturn(getGenericUpdateResult());
			when(daoHelper.delete(any(), any())).thenReturn(getGenericInsertResult());
			EntityResult er=seasonService.hotelseasonsDelete(filter);
			
			assertEquals("SUCCESSFULL_DELETE", er.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, er.getCode());
					
		}
		@Test
		@DisplayName("Trying to delete the seasons from another hotel")
		void not_authorized() throws NotAuthorizedException {
			Map<String, Object> filter = getGenericFilter();
			when(daoHelper.query(any(), any(), any(),anyString())).thenReturn(getGenericUpdateResult());
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccess(anyInt());	
			EntityResult er=seasonService.hotelseasonsDelete(filter);			
			assertEquals("NOT_AUTHORIZED", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());
						
		}
	
		@Test
		@DisplayName("Trying to delete without ss_hotel")
		void hotel_season_delete_without_ss_hotel() {
			Map<String, Object> filter = new HashMap<>();	
			EntityResult er=seasonService.hotelseasonsDelete(filter);			
			assertEquals("SS_HOTEL_REQUIRED", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());			
			
		}
	
		@Test
		@DisplayName("Hotel Season delete without results")
		void hotel_season_delete_without_results() {
			Map<String, Object> filter = getQueryFilter();
			when(daoHelper.query(any(), any(), any(),anyString())).thenReturn(new EntityResultMapImpl());
			EntityResult er=seasonService.hotelseasonsDelete(filter);		
			assertEquals("NO_RESULTS", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());
			
			
		}
		
	
	
}






}
