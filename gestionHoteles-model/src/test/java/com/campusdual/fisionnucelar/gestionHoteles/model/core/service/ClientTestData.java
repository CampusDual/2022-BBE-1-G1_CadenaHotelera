package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class ClientTestData {

	public static EntityResult getAllExtraData() {
		List<String> columnList = Arrays.asList("ID_CLIENT", "CL_NAME","CL_EMAIL","CL_NIF");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_CLIENT", 0);
				put("CL_NAME", "Paco Martínez");
				put("CL_EMAIL","pacoMartinez@gmail.com");
				put("CL_NIF", "33333333L");
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_CLIENT", 1);
				put("CL_NAME", "Marta Otero");
				put("CL_EMAIL","martaOtero@gmail.com");
				put("CL_NIF", "33333334L");
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_CLIENT", 2);
				put("CL_NAME", "Isabel Alonso");
				put("CL_EMAIL","isabelAlonso@gmail.com");
				put("CL_NIF", "33333335L");
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_CLIENT", Types.INTEGER);
				put("CL_NAME", Types.VARCHAR);
				put("CL_EMAIL", Types.VARCHAR);
				put("CL_NIF", Types.VARCHAR);

			}
		});
		return er;
	}
	public static EntityResult getSpecificClientData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = ClientTestData.getAllExtraData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("ID_CLIENT", "CL_NAME","CL_EMAIL","CL_NIF");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_CLIENT", Types.INTEGER);
				put("CL_NAME", Types.VARCHAR);
				put("CL_EMAIL", Types.VARCHAR);
				put("CL_NIF", Types.VARCHAR);
			}
		});
		return er;
	}

	
	
	public static Map<String, Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("cl_name", "Alfredo Pérez");
		dataToInsert.put("cl_email", "alfredoperez@outlook.com");
		dataToInsert.put("cl_nif", "98766789I");
		return dataToInsert;
	}

	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_CLIENT"));
		er.addRecord(new HashMap<String, Object>() {{put("ID_CLIENT", 2);}});
		return er;
	}
	
	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_client", 32);
		return filter;
	}

	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_CLIENT","CL_NAME","CL_EMAIL","CL_NIF"));
		return queryResult;
	}

	public static List<String> getGenericAttrList() {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_client");
		return attrList;
	}
	
	
}