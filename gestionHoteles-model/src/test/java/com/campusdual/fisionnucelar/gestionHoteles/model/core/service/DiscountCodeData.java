package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class DiscountCodeData {
	public static EntityResult getAllDiscountCodeData() {
		List<String> columnList = Arrays.asList("id_code", "dc_name", "dc_multiplier", "dc_leaving_date");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_code", 0);
				put("dc_name", "CRAZY_WINTER_2023");
				put("dc_multiplier", 0.90);
				put("dc_leaving_date", null);
			
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_code", 1);
				put("dc_name", "CRAZY_SPRING_2023");
				put("dc_multiplier", 0.95);
				put("dc_leaving_date", null);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_code", 2);
				put("dc_name", "CRAZY_SUMER_2023");
				put("dc_multiplier", 0.97);
				put("dc_leaving_date", null);
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_code", Types.INTEGER);
				put("dc_name", Types.VARCHAR);
				put("dc_multiplier", Types.DECIMAL);
				put("dc_leaving_date", Types.DATE);

			}
		});
		return er;
	}

	public static EntityResult getSpecificDiscountCodeData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllDiscountCodeData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("id_code", "dc_name");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_code", Types.INTEGER);
				put("dc_name", Types.VARCHAR);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("id_code", 2);
		dataToInsert.put("dc_name", "CRAZY_SUMER_2023");
		dataToInsert.put("dc_multiplier", 0.97);
		dataToInsert.put("dc_leaving_date", null);
		return dataToInsert;
	}

	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("id_code"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_code", 2);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_code", 32);
		return filter;
	}


	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("id_code", "dc_name"));
		queryResult.put("id_code", Arrays.asList(1));
		queryResult.put("dc_name", Arrays.asList(1));
		return queryResult;
	}


	public static List<String> getGenericAttrList() {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_code");
		return attrList;
	}
}
