package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class ServiceHotelTestData {
	public static EntityResult getAllServiceHotelData() {
		List<String> columnList = Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL", "SVH_SERVICE","SVH_ACTIVE");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_SERVICES_HOTEL", 0);
				put("SVH_HOTEL", 1);
				put("SVH_SERVICE", 1);
				put("SVH_ACTIVE", 1);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_SERVICES_HOTEL", 1);
				put("SVH_HOTEL", 2);
				put("SVH_SERVICE", 2);
				put("SVH_ACTIVE", 2);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_SERVICES_HOTEL", 2);
				put("SVH_HOTEL", 3);
				put("SVH_SERVICE", 3);
				put("SVH_ACTIVE", 3);
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_SERVICES_HOTEL", Types.INTEGER);
				put("SVH_HOTEL", Types.INTEGER);
				put("SVH_SERVICE", Types.INTEGER);
				put("SVH_ACTIVE", Types.INTEGER);

			}
		});
		return er;
	}

	public static EntityResult getSpecificServiceHotelData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllServiceHotelData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_SERVICES_HOTEL", Types.INTEGER);
				put("SVH_HOTEL", Types.INTEGER);
			}
		});
		return er;
	}

	public static Map<String,Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("svh_hotel", 1);
		dataToInsert.put("svh_service", 1);
		dataToInsert.put("svh_active", 1);
		return dataToInsert;
	}
	
	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_SERVICES_HOTEL"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_SERVICES_HOTEL", 2);
			}
		});
		return er;
	}
	
	public static Map<String,Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_services_hotel", 32);
		return filter;
	}
	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_SERVICES_HOTEL", "SVH_HOTEL"));	
		return queryResult;
	}
	
	public static List<String> getGenericAttrList(){
		List<String> attrList = new ArrayList<>();
		attrList.add("id_services_hotel");
		return attrList;
	}
	

}
