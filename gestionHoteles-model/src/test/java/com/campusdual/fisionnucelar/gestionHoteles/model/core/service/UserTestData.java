package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class UserTestData {
	public static EntityResult getAllUsersData() {
		List<String> columnList = Arrays.asList("USER_", "NAME", "SURNAME","EMAIL","NIF","USERBLOCKED","LASTPASSWORDUPDATE","FIRSTLOGIN","IDENTIFIER","ID_ROLENAME","ROLENAME","USER_DOWN_DATE","CL_PHONE", "CL_COUNTRY_CODE");																						
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("USER_", "demo");
				put("NAME", "demo");
				put("SURNAME", "demo");
				put("EMAIL", "demodemo@fnhotels.com");
				put("NIF", "44460713B");
				put("USERBLOCKED", null);
				put("LASTPASSWORDUPDATE", null);
				put("FIRSTLOGIN", true);
				put("IDENTIFIER", null);
				put("ID_ROLENAME", 0);
				put("ROLENAME", "admin");
				put("USER_DOWN_DATE", null);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("USER_", "masterchief");
				put("NAME", "Master");
				put("SURNAME", "Chief");
				put("EMAIL", "masterchief@fnhotels.com");
				put("NIF", "92176591C");
				put("USERBLOCKED", null);
				put("LASTPASSWORDUPDATE", null);
				put("FIRSTLOGIN", true);
				put("IDENTIFIER", 1);
				put("ID_ROLENAME", 1);
				put("ROLENAME", "hotel_manager");
				put("USER_DOWN_DATE", null);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("USER_", "anacleto");
				put("NAME", "Ana");
				put("SURNAME", "Cleto");
				put("EMAIL", "anacleto@fnhotels.com");
				put("NIF", "57598550H");
				put("USERBLOCKED", null);
				put("LASTPASSWORDUPDATE", null);
				put("FIRSTLOGIN", true);
				put("IDENTIFIER", 1);
				put("ID_ROLENAME", 5);
				put("ROLENAME", "hotel_receptionist");
				put("USER_DOWN_DATE", null);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("USER_", "pepito");
				put("NAME", "Pepe");
				put("SURNAME", "Aldao");
				put("EMAIL", "pepitoaldao@fnhotels.com");
				put("NIF", "65247502F");
				put("USERBLOCKED", null);
				put("LASTPASSWORDUPDATE", null);
				put("FIRSTLOGIN", true);
				put("IDENTIFIER", 68);
				put("ID_ROLENAME", 2);
				put("ROLENAME", "client");
				put("USER_DOWN_DATE", null);
				put("CL_PHONE", "685079124");
				put("CL_COUNTRY_CODE", 34);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("USER_", "jjAlonso");
				put("NAME", "Javier");
				put("SURNAME", "Junior");
				put("EMAIL", "jjalonso@fnhotels.com");
				put("NIF", "64829672A");
				put("USERBLOCKED", null);
				put("LASTPASSWORDUPDATE", null);
				put("FIRSTLOGIN", true);
				put("IDENTIFIER", 1);
				put("ID_ROLENAME", 2);
				put("ROLENAME", "client");
				put("USER_DOWN_DATE", "2022-08-12 11:13:20.094");
				put("CL_PHONE", "645079124");
				put("CL_COUNTRY_CODE", 34);
			}
		});
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("USER_",  Types.VARCHAR);
				put("NAME",  Types.VARCHAR);
				put("SURNAME",  Types.VARCHAR);
				put("EMAIL",  Types.VARCHAR);
				put("NIF",  Types.VARCHAR);
				put("USERBLOCKED", Types.BOOLEAN);
				put("LASTPASSWORDUPDATE", Types.TIMESTAMP);
				put("FIRSTLOGIN", Types.BOOLEAN);
				put("IDENTIFIER", Types.INTEGER);
				put("ID_ROLENAME", Types.INTEGER);
				put("ROLENAME", Types.VARCHAR);
				put("USER_DOWN_DATE", Types.TIMESTAMP);
				put("CL_PHONE", Types.VARCHAR);
				put("CL_COUNTRY_CODE", Types.INTEGER);
			}
		});
		return er;
	}

	public static EntityResult getSpecificUserData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = UserTestData.getAllUsersData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("USER_","NAME", "SURNAME","EMAIL","NIF","USER_DOWN_DATE", "IDENTIFIER");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("USER_",  Types.VARCHAR);
				put("NAME",  Types.VARCHAR);
				put("SURNAME",  Types.VARCHAR);
				put("EMAIL",  Types.VARCHAR);
				put("NIF",  Types.VARCHAR);
				put("USER_DOWN_DATE",  Types.VARCHAR);
				put("IDENTIFIER", Types.INTEGER);
			}
		});
		return er;
	}
	public static Map<String, Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("USER_", "jaimito");
		dataToInsert.put("PASSWORD", "1234");
		dataToInsert.put("NAME", "Jaime");
		dataToInsert.put("SURNAME", "Janer");
		dataToInsert.put("EMAIL", "jaimito@gmail.com");
		dataToInsert.put("NIF", "69462853G");
		dataToInsert.put("USER_DOWN_DATE", null);
		dataToInsert.put("LASTPASSWORDUPDATE", null);
		dataToInsert.put("FIRSTLOGIN", true);
		dataToInsert.put("IDENTIFIER", 1);
		dataToInsert.put("ID_ROLENAME", 2);
		dataToInsert.put("ROLENAME", "client");
		dataToInsert.put("USER_DOWN_DATE", "2022-08-12 11:13:20.094");
		dataToInsert.put("CL_PHONE", "645079124");
		dataToInsert.put("CL_COUNTRY_CODE", 34);
		return dataToInsert;
	}
	public static Map<String, Object> getGenericAdminDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("USER_", "jaimito");
		dataToInsert.put("PASSWORD", "1234");
		dataToInsert.put("NAME", "Jaime");
		dataToInsert.put("SURNAME", "Janer");
		dataToInsert.put("EMAIL", "jaimito@gmail.com");
		dataToInsert.put("NIF", "69462853G");
		return dataToInsert;
	}
	public static Map<String, Object> getGenericWorkerDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("USER_", "jaimito");
		dataToInsert.put("PASSWORD", "1234");
		dataToInsert.put("NAME", "Jaime");
		dataToInsert.put("SURNAME", "Janer");
		dataToInsert.put("EMAIL", "jaimito@gmail.com");
		dataToInsert.put("NIF", "69462853G");
		dataToInsert.put("IDENTIFIER", 1);
		dataToInsert.put("ID_ROLENAME", 5);
		return dataToInsert;
	}
	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("USER_"));
		er.addRecord(new HashMap<String, Object>() {{put("USER_", "jaimito");}});
		return er;
	}
	
	
	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("user_", "manolito");
		return filter;
	}

	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("USER_", "NAME", "SURNAME","EMAIL","NIF","USERBLOCKED","LASTPASSWORDUPDATE","FIRSTLOGIN","IDENTIFIER","ID_ROLENAME","ROLENAME","USER_DOWN_DATE"));
		return queryResult;
	}

	public static List<String> getGenericAttrList() {
		List<String> attrList = new ArrayList<>();
		attrList.add("user_");
		return attrList;
	}
	
	
}
