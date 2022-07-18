package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class ExtraHotelTestData {
	public static EntityResult getAllExtraHotelData() {
		List<String> columnList = Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL", "EXH_EXTRA","EXH_PRICE","EXH_ACTIVE");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRAS_HOTEL", 0);
				put("EXH_HOTEL", 1);
				put("EXH_EXTRA", 1);
				put("EXH_PRICE", 50);
				put("EXH_ACTIVE", 1);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRAS_HOTEL", 1);
				put("EXH_HOTEL", 2);
				put("EXH_EXTRA", 2);
				put("EXH_PRICE", 100);
				put("EXH_ACTIVE", 1);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRAS_HOTEL", 2);
				put("EXH_HOTEL", 3);
				put("EXH_EXTRA", 3);
				put("EXH_PRICE", 10);
				put("EXH_ACTIVE", 1);
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_EXTRAS_HOTEL", Types.INTEGER);
				put("EXH_HOTEL", Types.INTEGER);
				put("EXH_EXTRA", Types.INTEGER);
				put("EXH_PRICE", Types.NUMERIC);
				put("EXH_ACTIVE", Types.INTEGER);

			}
		});
		return er;
	}

	public static EntityResult getSpecificExtraHotelData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllExtraHotelData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_EXTRAS_HOTEL", Types.INTEGER);
				put("EXH_HOTEL", Types.INTEGER);
			}
		});
		return er;
	}
	
	public static Map<String,Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("exh_hotel", 1);
		dataToInsert.put("exh_service", 1);
		dataToInsert.put("exh_price",50);
		dataToInsert.put("exh_active", 1);
		return dataToInsert;
	}
	
	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_EXTRAS_HOTEL"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRAS_HOTEL", 2);
			}
		});
		return er;
	}
	
	public static Map<String,Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_extras_hotel", 32);
		return filter;
	}
	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_EXTRAS_HOTEL", "EXH_HOTEL"));	
		return queryResult;
	}
	
	public static List<String> getGenericAttrList(){
		List<String> attrList = new ArrayList<>();
		attrList.add("ID_EXTRAS_HOTEL");
		return attrList;
	}
	

	
	}
