package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.UserTestData.getGenericWorkerDataToInsertOrUpdate;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ClientDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.UserDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.UserRoleDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidRolException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
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
	ClientDao clientdao;

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
			List<String> attrList = Arrays.asList("USER_", "NAME", "SURNAME", "EMAIL", "NIF", "USER_DOWN_DATE",
					"IDENTIFIER");
			doReturn(getSpecificUserData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList(),
					eq("user_data"));
			EntityResult entityResult = userService.userQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			// assertEquals(4, entityResult.getRecordValues(0).get(userDao.IDENTIFIER));
			verify(daoHelper).query(any(), anyMap(), anyList(), eq("user_data"));
		}

		@Test
		@DisplayName("Obtain all data columns from Tuser table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			EntityResult result = new EntityResultMapImpl();
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
	public class UserAdmin_Insert {
		@Test
		@DisplayName("Insert a user admin by admin successfully")
		void user_insert_success() {
			Map<String, Object> dataToInsert = getGenericAdminDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");

			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userAdminInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());

			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals("jaimito", entityResult.getRecordValues(recordIndex).get("USER_"));

			verify(daoHelper).insert(userDao, dataToInsert);
		}
		@Test
		@DisplayName("Insert a new user admin with leaving_date")
		void user_admin_insert_success_when_cl_leaving_date() {
			Map<String, Object> dataToInsert = getGenericAdminDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");

			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
					put("email", "jaimito@gmail.com");
					put("user_down_date", "2022-08-12 11:13:20.094");
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userAdminInsert(dataToInsert);
			assertEquals("USER_EXISTING_UP_SUCCESSFULLY_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
		}

		@Test
		@DisplayName("Fail trying to insert duplicated email")
		void user_insert_duplicated_mail() {
			Map<String, Object> dataToInsert = getGenericAdminDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			EntityResult insertResult = getGenericInsertResult();

			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult existuser = new EntityResultMapImpl();
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
			EntityResult existuser = new EntityResultMapImpl();
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
	@DisplayName("Tests for UserWorker inserts by Admin")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class UserWorker_Insert {
		@Test
		@DisplayName("Insert a worker user by admin successfully")
		void user_insert_success() {
			Map<String, Object> dataToInsert = getGenericWorkerDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("identifier", 1);
			dataToInsert.put("id_rolename", 5);
			EntityResult insertResult = getGenericInsertResult();
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult hotelResult = new EntityResultMapImpl(Arrays.asList("id_hotel"));
			hotelResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_hotel", 1);
				}
			});

			EntityResult rolResult = new EntityResultMapImpl(Arrays.asList("id_rolename"));
			rolResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_rolename", 5);
				}
			});

			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return hotelResult;
					if (count == 2)
						return rolResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());

			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userWorkerInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals("jaimito", entityResult.getRecordValues(recordIndex).get("USER_"));
			verify(daoHelper).insert(userDao, dataToInsert);
		}
		
		@Test
		@DisplayName("Insert a new user worker with leaving_date")
		void user_worker_insert_success_when_cl_leaving_date() {
			Map<String, Object> dataToInsert = getGenericWorkerDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("identifier", 1);
			dataToInsert.put("id_rolename", 5);
			EntityResult insertResult = getGenericInsertResult();
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult hotelResult = new EntityResultMapImpl(Arrays.asList("id_hotel"));
			hotelResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_hotel", 1);
				}
			});

			EntityResult rolResult = new EntityResultMapImpl(Arrays.asList("id_rolename"));
			rolResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_rolename", 5);
				}
			});

			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return hotelResult;
					if (count == 2)
						return rolResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());

			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
					put("email", "jaimito@gmail.com");
					put("user_down_date", "2022-08-12 11:13:20.094");
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userWorkerInsert(dataToInsert);
			assertEquals("USER_EXISTING_UP_SUCCESSFULLY_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
		}
		
		@Test
		@DisplayName("Fail trying to insert duplicated email")
		void user_insert_duplicated_email() {
			Map<String, Object> dataToInsert = getGenericWorkerDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("identifier", 1);
			dataToInsert.put("id_rolename", 5);
			EntityResult insertResult = getGenericInsertResult();
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult hotelResult = new EntityResultMapImpl(Arrays.asList("id_hotel"));
			hotelResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_hotel", 1);
				}
			});

			EntityResult rolResult = new EntityResultMapImpl(Arrays.asList("id_rolename"));
			rolResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_rolename", 5);
				}
			});

			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return hotelResult;
					if (count == 2)
						return rolResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());

			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult resultSuccess = userService.userWorkerInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", resultSuccess.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());

			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return hotelResult;
					if (count == 2)
						return rolResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			when(daoHelper.insert(userDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = userService.userWorkerInsert(dataToInsert);
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
			dataToInsert.put("identifier", 1);
			dataToInsert.put("id_rolename", 5);
			EntityResult hotelResult = new EntityResultMapImpl(Arrays.asList("id_hotel"));
			hotelResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_hotel", 1);
				}
			});

			EntityResult rolResult = new EntityResultMapImpl(Arrays.asList("id_rolename"));
			rolResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_rolename", 5);
				}
			});

			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return hotelResult;
					if (count == 2)
						return rolResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());

			EntityResult entityResult = userService.userWorkerInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("USER_OR_EMAIL_REQUIRED", entityResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to insert with invalid rol")
		void user_insert_with_invalid_rol() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("NAME", "Isidoro");
			dataToInsert.put("EMAIL", "isidoro@outlook.com");
			dataToInsert.put("identifier", 1);
			dataToInsert.put("id_rolename", 7);
			EntityResult hotelResult = new EntityResultMapImpl(Arrays.asList("id_hotel"));
			hotelResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_hotel", 1);
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(hotelResult);
			when(daoHelper.query(any(), anyMap(), anyList())).thenThrow(new InvalidRolException("ID_ROLENAME_INVALID"));
		}

		@Test
		@DisplayName("Fail trying to insert without rol")
		void user_insert_without_rol() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("NAME", "Isidoro");
			dataToInsert.put("EMAIL", "isidoro@outlook.com");
			dataToInsert.put("identifier", 1);
			EntityResult hotelResult = new EntityResultMapImpl(Arrays.asList("id_hotel"));
			hotelResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_hotel", 1);
				}
			});
			EntityResult rolResult = new EntityResultMapImpl();
			Mockito.doAnswer(new Answer() {
				private int count = 0;

				@Override
				public Object answer(InvocationOnMock invocation) throws Throwable {
					count++;
					if (count == 1)
						return hotelResult;
					if (count == 2)
						return rolResult;
					return invocation;
				}
			}).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult resultFail = userService.userWorkerInsert(dataToInsert);
			assertEquals("ID_ROLENAME_NEEDED", resultFail.getMessage());

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

			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult resultFail = userService.userWorkerInsert(dataToInsert);
			assertEquals("INVALID_EMAIL", resultFail.getMessage());

		}
		@Test
		@DisplayName("Fail trying to insert without email")
		void userclient_insert_without_email() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("NAME", "Isidoro");
			EntityResult resultFail = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("EMAIL_REQUIRED", resultFail.getMessage());
		}
		
		@Test
		@DisplayName("Fail trying to insert with no data")
		void user_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult entityResult = userService.userWorkerInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}

	}
	
	@Nested
	@DisplayName("Test for user client insert by admin")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class UserClient_By_Admin_Insert {
		@Test
		@DisplayName("Insert a new user client by admin successfully")
		void user_client_insert_success_when_exist() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");		
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("cl_email", "jaimito@gmail.com");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client","cl_leaving_date"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
					put("cl_leaving_date", null);
				}
			});
			when(clientService.clientQuery(anyMap(), anyList())).thenReturn(clientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());

			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals("jaimito", entityResult.getRecordValues(recordIndex).get("USER_"));

			verify(daoHelper).insert(userDao, dataToInsert);
		}

		@Test
		@DisplayName("Insert a new user client by admin successfully")
		void user_new_client_insert_success() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");		
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("cl_email", "jaimito@gmail.com");
			dataToInsert.put("id_client", 38);
			dataToInsert.put("identifier", 38);
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult clientResult = new EntityResultMapImpl();
			when(clientService.clientQuery(anyMap(), anyList())).thenReturn(clientResult);
			EntityResult idclientResult = new EntityResultMapImpl(Arrays.asList("id_client"));
			idclientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
				}
			});
			when(clientService.clientInsert(anyMap())).thenReturn(idclientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());

			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals("jaimito", entityResult.getRecordValues(recordIndex).get("USER_"));

			verify(daoHelper).insert(userDao, dataToInsert);
		}
		
		@Test
		@DisplayName("Insert a new user client by admin successfully with leaving_date")
		void user_client_insert_success_when_cl_leaving_date() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");		
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("cl_email", "jaimito@gmail.com");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client","cl_leaving_date"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
					put("cl_leaving_date", "2022-08-12 11:13:20.094");
				}
			});
			when(clientService.clientQuery(anyMap(), anyList())).thenReturn(clientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());

			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals("jaimito", entityResult.getRecordValues(recordIndex).get("USER_"));

			verify(daoHelper).insert(userDao, dataToInsert);
		}
		
		@Test
		@DisplayName("Insert a new user client by admin successfully with user down")
		void user_client_insert_success_when_user_down() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");		
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("cl_email", "jaimito@gmail.com");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client","cl_leaving_date"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
					put("cl_leaving_date", "2022-08-12 11:13:20.094");
				}
			});
			when(clientService.clientQuery(anyMap(), anyList())).thenReturn(clientResult);
			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
					put("email", "jaimito@gmail.com");
					put("user_down_date", "2022-08-12 11:13:20.094");
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("USER_EXISTING_UP_SUCCESSFULLY_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
		}
		
		@Test
		@DisplayName("Insert a new user client with duplicated email")
		void user_client_insert_duplicated_email() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");		
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("cl_email", "jaimito@gmail.com");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client","cl_leaving_date"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
					put("cl_leaving_date", null);
				}
			});
			when(clientService.clientQuery(anyMap(), anyList())).thenReturn(clientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			
			when(clientService.clientQuery(anyMap(), anyList())).thenReturn(clientResult);
			
			when(daoHelper.insert(userDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("EMAIL_ALREADY_EXISTS", resultFail.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			verify(daoHelper, times(3)).insert(any(), anyMap());
		}
		
		@Test
		@DisplayName("Fail trying to insert without user field")
		void userclient_insert_without_user() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("NAME", "Isidoro");
			dataToInsert.put("email", "isidoro@outlook.com");
			
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client","cl_leaving_date"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
					put("cl_leaving_date", null);
				}
			});
			when(clientService.clientQuery(anyMap(), anyList())).thenReturn(clientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			EntityResult entityResult = userService.userClientByManagersInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("USER_OR_EMAIL_REQUIRED", entityResult.getMessage());
		}
		
		@Test
		@DisplayName("Fail trying to insert without email")
		void userclient_insert_without_email() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("NAME", "Isidoro");
			EntityResult resultFail = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("EMAIL_REQUIRED", resultFail.getMessage());
		}
		//revisar
		@Test
		@DisplayName("Fail trying to insert not valid email")
		void userclient_insert_not_valid_mail() {
			EntityResult insertResult = new EntityResultMapImpl();
			insertResult.addRecord(new HashMap<String, Object>() {
				{
					put("user_", "isidoro");
				}
			});
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("email", "isidorooutlook.com");

			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult resultFail = userService.userClientByManagersInsert(dataToInsert);
			assertEquals("INVALID_EMAIL", resultFail.getMessage());
		}
	
		@Test
		@DisplayName("Fail trying to insert with no data")
		void userclient_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult entityResult = userService.userClientByManagersInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
		
	}
	
	@Nested
	@DisplayName("Test for userClientInsert")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class userClientInsert {
		@Test
		@DisplayName("Insert a new user client by client successfully")
		void user_client_insert_success_when_exist() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");		
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("cl_email", "jaimito@gmail.com");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client","cl_leaving_date"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
				}
			});
			when(daoHelper.insert(any(),anyMap())).thenReturn(clientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userClientInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).insert(userDao, dataToInsert);
		}

		@Test
		@DisplayName("Insert a new user client with duplicated email")
		void user_client_insert_duplicated_email() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			dataToInsert.put("email", "jaimito@gmail.com");		
			dataToInsert.put("user_", "jaimito");
			dataToInsert.put("cl_email", "jaimito@gmail.com");
			EntityResult insertResult = getGenericInsertResult();

			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("USER_", "jaimito");
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client","cl_leaving_date"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
				}
			});
			when(daoHelper.insert(any(),anyMap())).thenReturn(clientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult entityResult = userService.userClientInsert(dataToInsert);
			assertEquals("SUCCESSFULLY_INSERT", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			
			when(daoHelper.insert(any(),anyMap())).thenReturn(clientResult);
			when(daoHelper.insert(userDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = userService.userClientInsert(dataToInsert);
			assertEquals("EMAIL_ALREADY_EXISTS. ", resultFail.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			verify(daoHelper, times(5)).insert(any(), anyMap());
		}
		
		@Test
		@DisplayName("Fail trying to insert without user field")
		void userclient_insert_without_user() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("name", "Isidoro");
			dataToInsert.put("email", "isidoro@outlook.com");
			
			EntityResult clientResult = new EntityResultMapImpl(Arrays.asList("id_client"));
			clientResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 38);
				}
			});
			when(daoHelper.insert(any(),anyMap())).thenReturn(clientResult);
			
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			when(daoHelper.insert(userDao, dataToInsert)).thenThrow(new DataIntegrityViolationException("USER_OR_EMAIL_REQUIRED"));
			
			EntityResult resultFail = userService.userClientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
		}

		@Test
		@DisplayName("Fail trying to insert without email")
		void userclient_insert_without_email() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("NAME", "Isidoro");
			EntityResult resultFail = userService.userClientInsert(dataToInsert);
			assertEquals("EMAIL_REQUIRED", resultFail.getMessage());
		}
		//revisar
		@Test
		@DisplayName("Fail trying to insert not valid email")
		void userclient_insert_not_valid_mail() {
			EntityResult insertResult = new EntityResultMapImpl();
			insertResult.addRecord(new HashMap<String, Object>() {
				{
					put("user_", "isidoro");
				}
			});
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("email", "isidorooutlook.com");

			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);

			EntityResult resultFail = userService.userClientInsert(dataToInsert);
			assertEquals("INVALID_EMAIL", resultFail.getMessage());
		}
	
		@Test
		@DisplayName("Fail trying to insert with no data")
		void userclient_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			when(daoHelper.insert(userDao, dataToInsert)).thenReturn(insertResult);
			EntityResult entityResult = userService.userClientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
	}
	
	
	@Nested
	@DisplayName("Test for userUpdate")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class userUpdate {
		@Test
		@DisplayName("User update successful")
		void user_update_success() throws NotAuthorizedException  {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("name", "Manolo");
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();
			List<String> attrList = getGenericAttrList();

			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			
			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			doNothing().when(userControl).checkUserPermission(anyMap());
			
			EntityResult entityResult = userService.userUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFULLY_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
		}
		@Test
		@DisplayName("User update successful with password")
		void user_update_success_with_password() throws NotAuthorizedException  {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("name", "Manolo");
			dataToUpdate.put("password", "1234");
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();
			List<String> attrList = getGenericAttrList();

			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			
			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			doNothing().when(userControl).checkUserPermission(anyMap());
			
			EntityResult entityResult = userService.userUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFULLY_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
		}
		@Test
		@DisplayName("User update duplicated email")
		void user_update_duplicated_email() throws NotAuthorizedException  {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("email", "jaimito@gmail.com");
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();
			List<String> attrList = getGenericAttrList();

			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			
			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			doNothing().when(userControl).checkUserPermission(anyMap());
			
			EntityResult entityResult = userService.userUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFULLY_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			
			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenThrow(DuplicateKeyException.class);
			doNothing().when(userControl).checkUserPermission(anyMap());
			
			EntityResult resultFail =userService.userUpdate(dataToUpdate, filter);
			assertEquals("EMAIL_ALREADY_EXISTS", resultFail.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			verify(daoHelper, times(1)).update(any(), anyMap(), anyMap());
		}
		
		
		
		
		
		@Test
		@DisplayName("User update not autorized update field")
		void user_update_not_autorized_update_field() throws NotAuthorizedException  {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("name", "Manolo");
			dataToUpdate.put("identifier", 5);
			EntityResult er = new EntityResultMapImpl();
			EntityResult queryResult = getGenericQueryResult();
			List<String> attrList = getGenericAttrList();
			attrList.add("identifier");

			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			
			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			doNothing().when(userControl).checkUserPermission(anyMap());
			
			
			EntityResult entityResult = userService.userUpdate(dataToUpdate, filter);
			assertEquals("NO_AUTORIZED_UPDATE_FIELD", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
		}
		
		@Test
		@DisplayName("User update not exist user")
		void user_update_not_user_exist() throws NotAuthorizedException  {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("name", "Manolo");
			EntityResult er = new EntityResultMapImpl();
	
			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			EntityResult entityResult = userService.userUpdate(dataToUpdate, filter);
			assertEquals("ERROR_USER_NOT_FOUND", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), anyMap(), anyList(), anyString());
		}
		
		@Test
		@DisplayName("User update without user")
		void user_update_without_user() throws NotAuthorizedException  {

			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("name", "Manolo");
			EntityResult er = new EntityResultMapImpl();
	
			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			EntityResult existuser = new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			EntityResult entityResult = userService.userUpdate(dataToUpdate, filter);
			assertEquals("USER_REQUIRED", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
		}
		
		@Test
		@DisplayName("Try to delete with a non authorized user")
		void not_authorized() throws NotAuthorizedException {
			
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("name", "Manolo");
			EntityResult er = new EntityResultMapImpl();
			
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).checkUserPermission(anyMap());
	
			when(daoHelper.update(userDao, dataToUpdate, filter)).thenReturn(er);
			EntityResult existuser = new EntityResultMapImpl();
			existuser.addRecord(new HashMap<String, Object>() {
				{
					put("user_", 38);
				}
			});
			when(daoHelper.query(any(), anyMap(), anyList(), anyString())).thenReturn(existuser);
			
			EntityResult entityResult = userService.userUpdate(dataToUpdate, filter);
			assertEquals("NOT_AUTHORIZED", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
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

			when(daoHelper.query(userDao, filter, attrList)).thenReturn(queryResult);

			EntityResult queryResultBooking = new EntityResultMapImpl();

			EntityResult updateResult = new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = userService.userDelete(filter);

			assertEquals("SUCCESSFULLY_DELETE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail because not exist the user ")
		void client_not_delete_not_exist() {
			Map<String, Object> filter = getGenericFilter();
			List<String> attrList = getGenericAttrList();
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(userDao, filter, attrList)).thenReturn(queryResult);

			EntityResult updateResult = new EntityResultMapImpl();
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
			when(daoHelper.query(userDao, filter, attrList)).thenReturn(queryResult);

			EntityResult updateResult = new EntityResultMapImpl();
			updateResult.setCode(0);
			when(daoHelper.update(any(), anyMap(), anyMap())).thenReturn(updateResult);
			EntityResult entityResult = userService.userDelete(filter);

			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
	}
}
