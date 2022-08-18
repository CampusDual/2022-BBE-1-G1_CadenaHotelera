package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getGenericAdminDataToInsertOrUpdate;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getAllUsersData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getSpecificUserData;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getGenericDataToInsertOrUpdate;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getGenericInsertResult;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getGenericFilter;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getGenericAttrList;
import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getGenericQueryResult;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.UserDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.UserRoleDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

public class UserServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	UserService userService;
	
	@Autowired
	UserDao userDao;
	
	@Mock
	ClientService clientService;
	
	@Mock
	UserRoleDao userroleDao;
	
	@Mock
	UserControl userControl;
	
	@BeforeEach
	void setUp() {
		this.clientService = new ClientService();
		MockitoAnnotations.openMocks(this);
	}
	
	@Nested
	@DisplayName("Test for Users queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)

	public class UserQuery {

		@Test
		@DisplayName("Obtain all data from Tuser table")
		void when_queryOnlyWithAllColumns_return_allUserData() {
			when(daoHelper.query(any(), anyMap(), anyList(), eq("user_data"))).thenReturn(getAllUsersData());
			EntityResult entityResult = userService.userQuery(new HashMap<>(), getGenericAttrList());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(5, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
		}
	

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
			EntityResult entityResult = userService.userQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
		}
		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("IDENTIFIER", "string");
			List<String> columns = new ArrayList<>();
			columns.add("USER_");
			columns.add("NAME");
			columns.add("IDENTIFIER");
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = userService.userQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
		}

		@Test
		@DisplayName("Obtain all data columns from User table when USER_ is -> pepito")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("IDENTIFIER", 68);
				}
			};
			List<String> attrList = Arrays.asList("USER_", "NAME", "SURNAME","EMAIL","NIF","USER_DOWN_DATE", "IDENTIFIER");
			doReturn(getSpecificUserData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
			EntityResult entityResult = userService.userQuery(new HashMap<>(),new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			//assertEquals(4, entityResult.getRecordValues(0).get(userDao.IDENTIFIER));
			verify(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
		}

		@Test
		@DisplayName("Obtain all data columns from Tuser table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			EntityResult result=new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), eq("user_data"))).thenReturn(result);		
			EntityResult entityResult = userService.userQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, result.getCode());
			assertEquals("NO_RESULTS", result.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
		}

	}
	
	@Nested
	@DisplayName("Tests for UserAdmin inserts by Admin")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class User_Insert {
		@Test
		@DisplayName("Insert a user by admin successfully")
		void user_insert_success() {
			Map<String, Object> dataToInsert = getGenericAdminDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			EntityResult er = getGenericInsertResult();
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(er);
			EntityResult existuser= new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userAdminInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals("jaimito", entityResult.getRecordValues(recordIndex).get("USER_"));
			verify(daoHelper).insert(userDao, dataToInsert);
		}

		@Test
		@DisplayName("Fail trying to insert duplicated email")
		void user_insert_duplicated_mail() {
			Map<String, Object> dataToInsert = getGenericAdminDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			EntityResult insertResult = getGenericInsertResult();

			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult existuser= new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			EntityResult resultSuccess = userService.userAdminInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", resultSuccess.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			when(daoHelper.insert(userDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = userService.userAdminInsert(dataToInsert);
			assertEquals("EMAIL_ALREADY_EXISTS", resultFail.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			verify(daoHelper, times(3)).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without user field")
		void user_insert_without_user() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("NAME", "Isidoro");
			dataToInsert.put("EMAIL", "isidoro@outlook.com");
			EntityResult entityResult = userService.userAdminInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("USER_OR_EMAIL_REQUIRED", entityResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to insert not valid email")
		void user_insert_not_valid_mail() {
			EntityResult insertResult = new EntityResultMapImpl();
			insertResult.addRecord(new HashMap<String, Object>() {
				{
					put("user_", "isidoro");
				}
			});
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("email", "isidorooutlook.com");
			EntityResult existuser= new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult resultFail = userService.userAdminInsert(dataToInsert);
			assertEquals("INVALID_EMAIL", resultFail.getMessage());

		}
		@Test
		@DisplayName("Fail trying to insert with no data")
		void service_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult entityResult = userService.userAdminInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}

	}
	
	
	
	
	
	
	
	@Nested
	@DisplayName("Test for user deletes")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class UserDelete {
		@Test
		@DisplayName("User delete successful")
		void client_delete_success() {
			Map<String, Object> filter = getGenericFilter();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = getGenericQueryResult();
			
			when(daoHelper.query(userDao, filter,attrList)).thenReturn(queryResult);
			
			
			EntityResult queryResultBooking=new EntityResultMapImpl();
			
			EntityResult updateResult=new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = userService.userDelete(filter);
		
			assertEquals("SUCCESSFULLY_DELETE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
//		@Test
//		@DisplayName("Try to delete with a non authorized user")
//		void not_authorized() throws NotAuthorizedException {
//			Map<String, Object> filter = getGenericFilter();
//			List<String> attrList = getGenericAttrList();
//			EntityResult queryResult = getGenericQueryResult();
//			
//			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
//			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccessClient(anyInt());
//			
//			EntityResult entityResult = userService.userUpdate(getGenericDataToInsertOrUpdate(), filter);
//			assertEquals("NO_AUTORIZED_UPDATE_FIELD", entityResult.getMessage());
//			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
//		}
		
		@Test
		@DisplayName("Fail because not exist the user ")
		void client_not_delete_not_exist() {
			Map<String, Object> filter = getGenericFilter();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(userDao, filter,attrList)).thenReturn(queryResult);
			
			EntityResult updateResult=new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = userService.userDelete(filter);
		
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("ERROR_USER_NOT_FOUND", entityResult.getMessage());
		}
		@Test
		@DisplayName("Fail because the request is empty ")
		void client_not_delete_empty_request() {
			Map<String, Object> filter = new HashMap<>();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(userDao, filter,attrList)).thenReturn(queryResult);
			
			EntityResult updateResult=new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = userService.userDelete(filter);
		
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
	}
}

