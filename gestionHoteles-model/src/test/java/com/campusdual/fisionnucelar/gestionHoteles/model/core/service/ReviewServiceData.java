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

public class ReviewServiceData {
	private static Date date;

	
	public static EntityResult getAllReviewData() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, 6, 6);
		date = calendar.getTime();		
		List<String> columnList = Arrays.asList("id_review", "rv_hotel", "rv_client", "rv_rating","rv_date",
				"rv_comment","rv_response");
		EntityResult er = new EntityResultMapImpl(columnList);		

		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_review", 0);
				put("rv_hotel", 1);
				put("rv_client", 1);
				put("rv_rating", 5);
				put("rv_comment", "Todo bien");
				put("rv_date", date);
				put("rv_response", "Gracias");

			}
		});
		calendar.set(2023, 10, 10);
		date = calendar.getTime();

		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_review", 0);
				put("rv_hotel", 2);
				put("rv_client", 2);
				put("rv_rating", 4);
				put("rv_comment", "Bastante bien");
				put("rv_date", date);
				put("rv_response", "Gracias");
			}
		});
		calendar.set(2023, 3, 3);
		date = calendar.getTime();
		
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_review", 0);
				put("rv_hotel", 3);
				put("rv_client", 3);
				put("rv_rating", 1);
				put("rv_comment", "Todo mal");
				put("rv_date", date);
				put("rv_response", "Sentimos haberte causado mala impresión");
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_review", Types.INTEGER);
				put("rv_hotel", Types.INTEGER);
				put("rv_client", Types.INTEGER);
				put("rv_rating", Types.INTEGER);
				put("rv_comment", Types.VARCHAR);
				put("rv_date", Types.DATE);
				put("rv_response", Types.VARCHAR);

			}
		});
		return er;
	}

	public static EntityResult getSpecificReviewData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllReviewData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("id_review", "rv_hotel");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);

		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_review", Types.INTEGER);
				put("rv_hotel", Types.INTEGER);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericDataToInsert() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2023, 6, 6);
		date = calendar.getTime();		
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("id_review", 4);
		dataToInsert.put("rv_hotel", 4);
		dataToInsert.put("rv_client", 4);
		dataToInsert.put("rv_rating", 5);
		dataToInsert.put("rv_comment", "Todo bien");
		dataToInsert.put("rv_date", date);
		return dataToInsert;
				
			
	}
	
	public static Map<String, Object> getGenericDataToUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("rv_response", "Gracias por su valoración");
		return dataToInsert;
				
			
	}
	
	

	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("id_review"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_review", 2);
			}
		});
		return er;
	}
		
	
	public static EntityResult getReviewResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("id_review","rv_hotel","rv_client"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_review", 2);
				put("rv_hotel", 2);
				put("rv_client", 2);
			}
		});
		return er;
	}
	
	

	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_review", 32);
		return filter;
	}

	public static Map<String, Object> getHotelFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("rv_hotel", 2);
		return filter;
	}
	
	public static Map<String, Object> getClientFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("rv_client", 2);
		return filter;
	}
	
	
	
	
	
	
	
	

}
