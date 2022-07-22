package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class BookingExtraData {
	public static EntityResult getAllBookingExtraData() {
		List<String> columnList = Arrays.asList("ID_BOOKING_EXTRA", "BKE_BOOKING", "BKE_NAME","BKE_QUANTITY","BKE_UNIT_PRICE","BKE_TOTAL_PRICE","BKE_ENJOYED");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_BOOKING_EXTRA", 0);
				put("BKE_BOOKING", 1);
				put("BKE_NAME", "Niñero");
				put("BKE_QUANTITY", 4);
				put("BKE_UNIT_PRICE", 20);
				put("BKE_TOTAL_PRICE", 80);
				put("BKE_ENJOYED", 2);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_BOOKING_EXTRA", 1);
				put("BKE_BOOKING", 2);
				put("BKE_NAME", "Desayuno");
				put("BKE_QUANTITY", 5);
				put("BKE_UNIT_PRICE", 10);
				put("BKE_TOTAL_PRICE", 50);
				put("BKE_ENJOYED", 0);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_BOOKING_EXTRA", 2);
				put("BKE_BOOKING", 1);
				put("BKE_NAME", "Desayuno");
				put("BKE_QUANTITY", 5);
				put("BKE_UNIT_PRICE", 10);
				put("BKE_TOTAL_PRICE", 50);
				put("BKE_ENJOYED", 1);
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_BOOKING_EXTRA", Types.INTEGER);
				put("BKE_BOOKING", Types.INTEGER);
				put("BKE_NAME", Types.VARCHAR);
				put("BKE_QUANTITY", Types.INTEGER);
				put("BKE_UNIT_PRICE", Types.DECIMAL);
				put("BKE_TOTAL_PRICE", Types.DECIMAL);
				put("BKE_ENJOYED", Types.INTEGER);

			}
		});
		return er;
	}

	public static EntityResult getSpecificBookingExtraData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllBookingExtraData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("ID_BOOKING_EXTRA", "BKE_BOOKING");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_BOOKING_EXTRA", Types.INTEGER);
				put("BKE_BOOKING", Types.INTEGER);
			}
		});
		return er;
	}

	public static Map<String,Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("ID_BOOKING_EXTRA", 2);
		dataToInsert.put("BKE_BOOKING", 1);
		dataToInsert.put("BKE_NAME", "Desayuno");
		dataToInsert.put("BKE_QUANTITY", 5);
		dataToInsert.put("BKE_UNIT_PRICE", 10);
		dataToInsert.put("BKE_TOTAL_PRICE", 50);
		dataToInsert.put("BKE_ENJOYED", 1);
		return dataToInsert;
	}
	
	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_BOOKING_EXTRA"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_BOOKING_EXTRA", 2);
			}
		});
		return er;
	}
	
	public static Map<String,Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("ID_BOOKING_EXTRA", 32);
		return filter;
	}
	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_BOOKING_EXTRA", "BKE_BOOKING"));	
		return queryResult;
	}
	
	public static List<String> getGenericAttrList(){
		List<String> attrList = new ArrayList<>();
		attrList.add("ID_BOOKING_EXTRA");
		return attrList;
	}
}
