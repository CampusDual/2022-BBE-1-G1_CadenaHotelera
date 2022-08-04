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
		List<String> columnList = Arrays.asList("id_services_hotel", "svh_hotel", "svh_service","svh_active");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_services_hotel", 0);
				put("svh_hotel", 1);
				put("svh_service", 1);
				put("svh_active", 1);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_services_hotel", 1);
				put("svh_hotel", 2);
				put("svh_service", 2);
				put("svh_active", 2);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_services_hotel", 2);
				put("svh_hotel", 3);
				put("svh_service", 3);
				put("svh_active", 3);
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_services_hotel", Types.INTEGER);
				put("svh_hotel", Types.INTEGER);
				put("svh_service", Types.INTEGER);
				put("svh_active", Types.INTEGER);

			}
		});
		return er;
	}

	public static EntityResult getSpecificServiceHotelData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllServiceHotelData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("id_services_hotel", "svh_hotel");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_services_hotel", Types.INTEGER);
				put("svh_hotel", Types.INTEGER);
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
		EntityResult er = new EntityResultMapImpl(Arrays.asList("id_services_hotel"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_services_hotel", 2);
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
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("id_services_hotel", "svh_hotel"));	
		queryResult.put("id_services_hotel", Arrays.asList(1));
		queryResult.put("svh_hotel", Arrays.asList(1));
		return queryResult;
	}
	
	public static List<String> getGenericAttrList(){
		List<String> attrList = new ArrayList<>();
		attrList.add("id_services_hotel");
		return attrList;
	}
	

}
