package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.*;
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

	@BeforeEach
	void setUp() {
		this.clientService = new ClientService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Tests for Client inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class Client_Insert {
		@Test
		@DisplayName("Insert a client successfully")
		void hotel_insert_success() {
			Map<String, Object> attrMap = new HashMap<>();
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("cl_nif", "98766789I");
			dataToInsert.put("cl_email", "alfredoperez@outlook.com");
			dataToInsert.put("cl_name", "Alfredo Pérez");
			dataToInsert.put("cl_phone", "985446789");
			EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_CLIENT"));
			er.addRecord(new HashMap<String, Object>() {{put("ID_CLIENT", 2);}});
			er.setCode(EntityResult.OPERATION_SUCCESSFUL);
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("ID_CLIENT", 2);
			when(daoHelper.insert(clientDao, dataToInsert)).thenReturn(er);
			EntityResult entityResult = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			//assertEquals("INSERT_SUCCESSFULLY", entityResult.getMessage());
			assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_CLIENT"));
			verify(daoHelper).insert(clientDao, attrMap);

		}

		@Test
		@DisplayName("Fail trying to insert duplicated email")
		void hotel_insert_duplicated_mail() {
			Map<String, Object> attrMap = new HashMap<>();
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("cl_nif", "98766789I");
			dataToInsert.put("cl_email", "alfredoperez@outlook.com");
			dataToInsert.put("cl_name", "Alfredo Pérez");
			dataToInsert.put("cl_phone", "985446789");
			attrMap.put("0", dataToInsert);
			List<String> columnList = Arrays.asList("ID_CLIENT");
			EntityResult insertResult = new EntityResultMapImpl(columnList);
			insertResult.addRecord(new HashMap<String, Object>() {{put("ID_CLIENT", 2);}});
			when(daoHelper.insert(clientDao, dataToInsert)).thenReturn(insertResult);
			EntityResult resultSuccess = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			//assertEquals("SUCESSFUL_INSERTION", resultSuccess.getMessage());
			when(daoHelper.insert(clientDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = clientService.clientInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("EMAIL_ALREADY_EXISTS", resultFail.getMessage());
			verify(daoHelper, times(2)).insert(any(), anyMap());
		}
		@Test
		@DisplayName("Fail trying to insert without any fields")
		void hotel_insert_without_any_fields() {
			Map<String, Object> attrMap = new HashMap<>();
			EntityResult insertResult = new EntityResultMapImpl();
			EntityResult entityResult = clientService.clientInsert(attrMap);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
	}
		
	
	
}
