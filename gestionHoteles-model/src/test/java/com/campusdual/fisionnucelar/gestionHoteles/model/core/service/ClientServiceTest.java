package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ClientTestData.getAllExtraData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ClientTestData.getSpecificClientData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ClientTestData.getGenericDataToInsertOrUpdate;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ClientTestData.getGenericInsertResult;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ClientTestData.getGenericFilter;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ClientTestData.getGenericAttrList;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ClientTestData.getGenericQueryResult;
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
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.*;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

public class ClientServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	ClientService clientService;
	
	@Autowired
	ClientDao clientDao;

	@Mock
	BookingService bookingService;
	
	@Mock
	BookingDao bookingDao;
	
	@Mock
	UserControl userControl;
	
	@BeforeEach
	void setUp() {
		this.clientService = new ClientService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Test for Clients queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)

	public class ServiceQuery {

		@Test
		@DisplayName("Obtain all data from Extras table")
		void when_queryOnlyWithAllColumns_return_allExtraData() {
			doReturn(getAllExtraData()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = clientService.clientQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = clientService.clientQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_client", "string");
			List<String> columns = new ArrayList<>();
			columns.add("ex_name");
			columns.add("ex_description");
			when(daoHelper.query(clientDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = clientService.clientQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Clients table when ID is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_CLIENT", 2);
				}
			};
			List<String> attrList = Arrays.asList("ID_CLIENT", "CL_NAME", "CL_EMAIL", "CL_NIF");
			doReturn(getSpecificClientData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = clientService.clientQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get(clientDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Clients table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			EntityResult result=new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(result);		
			EntityResult entityResult = clientService.clientQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, result.getCode());
			assertEquals("NO_RESULTS", result.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		
		@ParameterizedTest(name = "Obtain data with ID -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from Extra table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_CLIENT", random);
				}
			};
			List<String> attrList = Arrays.asList("ID_CLIENT", "CL_NAME", "CL_EMAIL", "CL_NIF");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificClientData(keyMap, attrList));
			EntityResult entityResult = clientService.clientQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get(clientDao.ATTR_ID));
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
	@DisplayName("Tests for Client inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class Client_Insert {
		@Test
		@DisplayName("Insert a client successfully")
		void hotel_insert_success() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult er = getGenericInsertResult();
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("ID_CLIENT", 2);
			when(daoHelper.insert(clientDao, dataToInsert)).thenReturn(er);

			EntityResult entityResult = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_CLIENT"));
			verify(daoHelper).insert(clientDao, dataToInsert);

		}

		@Test
		@DisplayName("Fail trying to insert duplicated email")
		void client_insert_duplicated_mail() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			Map<String, Object> attrMap = new HashMap<>();
			attrMap.put("0", dataToInsert);
			// List<String> columnList = Arrays.asList("ID_CLIENT");
			EntityResult insertResult = getGenericInsertResult();

			when(daoHelper.insert(clientDao, dataToInsert)).thenReturn(insertResult);
			EntityResult resultSuccess = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			when(daoHelper.insert(clientDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("EMAIL_ALREADY_EXISTS", resultFail.getMessage());
			verify(daoHelper, times(2)).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without cl_name field")
		void client_insert_without_name() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("cl_nif", "98766789I");
			dataToInsert.put("cl_email", "alfredoperez@outlook.com");

			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(clientDao, dataToInsert)).thenThrow(DataIntegrityException);

			EntityResult entityResult = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without cl_email field")
		void client_insert_without_email() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("cl_nif", "98766789I");
			dataToInsert.put("cl_name", "Alfredo Pérez");
			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(clientDao, dataToInsert)).thenThrow(DataIntegrityException);
			EntityResult entityResult = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without cl_dni field")
		void client_insert_without_dni() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("cl_email", "alfredoperez@outlook.com");
			dataToInsert.put("cl_name", "Alfredo Pérez");
			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(clientDao, dataToInsert)).thenThrow(DataIntegrityException);
			EntityResult entityResult = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert not valid email")
		void client_insert_not_valid_mail() {
			EntityResult insertResult = new EntityResultMapImpl();
			insertResult.addRecord(new HashMap<String, Object>() {
				{
					put("ID_CLIENT", 2);
				}
			});
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("cl_nif", "98766789I");
			dataToInsert.put("cl_email", "alfredoperezoutlook.com");
			dataToInsert.put("cl_name", "Alfredo Pérez");
			dataToInsert.put("cl_phone", "985446789");

			EntityResult resultFail = clientService.clientInsert(dataToInsert);
			assertEquals("INVALID_EMAIL", resultFail.getMessage());

		}

	}

	@Nested
	@DisplayName("Test for Clients updates")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ServiceUpdate {
		@Test
		@DisplayName("Client update successful")
		void client_update_success() throws NotAuthorizedException  {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();
			List<String> attrList = getGenericAttrList();
	

			when(daoHelper.update(clientDao, dataToUpdate, filter)).thenReturn(er);
			when(daoHelper.query(clientDao, filter, attrList)).thenReturn(queryResult);
			// then
			EntityResult entityResult = clientService.clientUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
		@Test
		@DisplayName("Try to update with a non authorized user")
		void not_authorized() throws NotAuthorizedException {
			Map<String, Object> filter = getGenericFilter();			
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();			
			EntityResult queryResult = getGenericQueryResult();
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccessClient(anyInt());
			EntityResult entityResult = clientService.clientUpdate(dataToUpdate, filter);
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
		}

			

		@Test
		@DisplayName("Fail trying to update a client with a duplicated email")
		void client_fail_update_with_duplicated_email() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			List<String> attrList = new ArrayList<>();
			attrList.add("id_client");
			EntityResult queryResult = getGenericQueryResult();
			when(daoHelper.query(clientDao, filter, attrList)).thenReturn(queryResult);
			when(daoHelper.update(clientDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
			EntityResult entityResult = clientService.clientUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMAIL_ALREADY_EXISTS", entityResult.getMessage());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail trying to update with invalid email")
		void client_insert_with_invalid_email() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("cl_nif", "98766789I");
			dataToUpdate.put("cl_email", "alfredoperezoutlook.com");
			dataToUpdate.put("cl_name", "Alfredo Pérez");
			dataToUpdate.put("cl_phone", "985446789");
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = new EntityResultMapImpl(attrList);
			queryResult.addRecord(new HashMap<String, Object>() {
				{
					put("ID_CLIENT", 2);
				}
			});
			when(daoHelper.query(clientDao, filter, attrList)).thenReturn(queryResult);
			EntityResult entityResult = clientService.clientUpdate(dataToUpdate, filter);
			assertEquals("INVALID_EMAIL", entityResult.getMessage());
		}
		
        @Test
        @DisplayName("Fail trying to update a extra that doesn´t exists")
        void update_client_doesnt_exists() {
        	Map<String, Object> filter = getGenericFilter();
        	Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
        	List<String> attrList = getGenericAttrList();
        	
        	EntityResult queryResult = new EntityResultMapImpl();
        	when(daoHelper.query(clientDao, filter,attrList)).thenReturn(queryResult);
        	EntityResult entityResult = clientService.clientUpdate(dataToUpdate,filter);
        	assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
        	assertEquals("ERROR_CLIENT_NOT_FOUND", entityResult.getMessage());
        	verify(daoHelper).query(any(), anyMap(),anyList());
        }
        
    	@Test
		@DisplayName("Fail trying to update without any fields")
		void client_update_without_any_fields() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = clientService.clientUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("EMPTY_REQUEST", updateResult.getMessage());
		}
    	@Test
		@DisplayName("Fail trying to update without id_client")
		void client_update_without_id_hotel() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			
			EntityResult updateResult = clientService.clientUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_CLIENT_REQUIRED", updateResult.getMessage());
		}
		
	}

	@Nested
	@DisplayName("Test for Extra deletes")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ServiceDelete {
		@Test
		@DisplayName("Client delete successful")
		void client_delete_success() {
			Map<String, Object> filter = getGenericFilter();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = getGenericQueryResult();
			
			when(daoHelper.query(clientDao, filter,attrList)).thenReturn(queryResult);
			
			
			EntityResult queryResultBooking=new EntityResultMapImpl();
			
			when(bookingService.clientactivebookingsQuery(anyMap(), anyList())).thenReturn(queryResultBooking);
			
			EntityResult updateResult=new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = clientService.clientDelete(filter);
		
			assertEquals("SUCCESSFUL_DELETE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
		@Test
		@DisplayName("Try to delete with a non authorized user")
		void not_authorized() throws NotAuthorizedException {
			Map<String, Object> filter = getGenericFilter();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = getGenericQueryResult();
			
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccessClient(anyInt());
			
			EntityResult entityResult = clientService.clientUpdate(getGenericDataToInsertOrUpdate(), filter);
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
		}
		
	
		@Test
		@DisplayName("Fail because it has Active Reservations ")
		void client_not_delete_with_active_reservations() {
			Map<String, Object> filter = getGenericFilter();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = getGenericQueryResult();
			when(daoHelper.query(clientDao, filter,attrList)).thenReturn(queryResult);
			
			EntityResult queryResultBooking=new EntityResultMapImpl(Arrays.asList("ID_BOOKING"));
			when(bookingService.clientactivebookingsQuery(anyMap(), anyList())).thenReturn(queryResultBooking);
		
			EntityResult updateResult=new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = clientService.clientDelete(filter);
		
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ERROR_ACTIVE_BOOKINGS_FOUND", entityResult.getMessage());
	
		}
		
		@Test
		@DisplayName("Fail because not exist the client ")
		void client_not_delete_not_exist() {
			Map<String, Object> filter = getGenericFilter();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = new EntityResultMapImpl();
			
			when(daoHelper.query(clientDao, filter,attrList)).thenReturn(queryResult);
			
			EntityResult queryResultBooking=new EntityResultMapImpl();
			
			when(bookingService.clientactivebookingsQuery(anyMap(), anyList())).thenReturn(queryResultBooking);
			
			EntityResult updateResult=new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = clientService.clientDelete(filter);
		
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ERROR_CLIENT_NOT_FOUND", entityResult.getMessage());
		}
	}
}
