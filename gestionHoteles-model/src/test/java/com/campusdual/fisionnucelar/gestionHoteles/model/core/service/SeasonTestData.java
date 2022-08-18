package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class SeasonTestData {

	private static Date startDate;
	private static Date endDate;

	
	public static EntityResult getAllSeasonData() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, 6, 6);
		startDate = calendar.getTime();
		calendar.set(2023, 9, 9);
		endDate = calendar.getTime();
		
		List<String> columnList = Arrays.asList("id_season", "ss_hotel", "ss_multiplier", "ss_start_date",
				"ss_end_date", "ss_name");
		EntityResult er = new EntityResultMapImpl(columnList);		

		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_season", 0);
				put("ss_hotel", 0);
				put("ss_multiplier", 1.2);
				put("ss_start_date", startDate);
				put("ss_end_date", endDate);
				put("ss_name", "summer");

			}
		});
		calendar.set(2023, 10, 10);
		startDate = calendar.getTime();
		calendar.set(2023, 12, 12);
		endDate = calendar.getTime();

		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_season", 1);
				put("ss_hotel", 1);
				put("ss_multiplier", 1.2);
				put("ss_start_date", startDate);
				put("ss_end_date", endDate);
				put("ss_name", "winter");
			}
		});
		calendar.set(2023, 3, 3);
		startDate = calendar.getTime();
		calendar.set(2023, 5, 5);
		endDate = calendar.getTime();
		
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_season", 2);
				put("ss_hotel", 2);
				put("ss_multiplier", 1.2);
				put("ss_start_date", startDate);
				put("ss_end_date", endDate);
				put("ss_name", "spring");
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_season", Types.INTEGER);
				put("ss_hotel", Types.INTEGER);
				put("ss_multiplier", Types.DECIMAL);
				put("ss_start_date", Types.DATE);
				put("ss_end_date", Types.DATE);
				put("ss_name", Types.VARCHAR);

			}
		});
		return er;
	}

	public static EntityResult getSpecificSeasonData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllSeasonData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("id_season", "ss_hotel");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);

		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_season", Types.INTEGER);
				put("ss_hotel", Types.INTEGER);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericDataToInsertOrUpdate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, 6, 6);
		startDate = calendar.getTime();
		calendar.set(2023, 9, 9);
		endDate = calendar.getTime();
		
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("id_season", 1);
		dataToInsert.put("ss_hotel", 1);
		dataToInsert.put("ss_multiplier", 1.2);
		dataToInsert.put("ss_start_date", startDate);
		dataToInsert.put("ss_end_date", endDate);
		dataToInsert.put("ss_name", "spring");
		return dataToInsert;
	}
	
	public static Map<String, Object> getGenericDataToUpdate() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, 6, 6);
		startDate = calendar.getTime();
		calendar.set(2023, 9, 9);
		endDate = calendar.getTime();
		
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("id_season", 1);
		dataToInsert.put("ss_multiplier", 1.2);
		dataToInsert.put("ss_start_date", startDate);
		dataToInsert.put("ss_end_date", endDate);
		dataToInsert.put("ss_name", "spring");
		return dataToInsert;
	}
	
	

	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("id_season"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_season", 2);
			}
		});
		return er;
	}
	
	public static EntityResult getGenericUpdateResult() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, 6, 6);
		startDate = calendar.getTime();
		calendar.set(2023, 9, 9);
		endDate = calendar.getTime();
		
		EntityResult er = new EntityResultMapImpl(Arrays.asList("id_season","ss_hotel","ss_start_date","ss_end_date"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_season", 2);
				put("ss_hotel",2);
				put("ss_start_date",startDate);
				put("ss_end_date",endDate);
				
			}
		});
		return er;
	}
	
	

	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_season", 32);
		return filter;
	}

	public static Map<String, Object> getQueryFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("ss_hotel", 2);
		return filter;
	}

	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("id_season", "ss_hotel"));
		queryResult.put("id_season", Arrays.asList(1));
		queryResult.put("ss_hotel", Arrays.asList(1));
		return queryResult;
	}

	public static List<String> getGenericAttrList() {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_season");
		return attrList;
	}

}