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
		List<String> columnList = Arrays.asList("id_booking_extra", "bke_booking", "bke_name", "bke_quantity",
				"bke_unit_price", "bke_total_price", "bke_enjoyed");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_booking_extra", 0);
				put("bke_booking", 0);
				put("bke_name", "Niñero");
				put("bke_quantity", 4);
				put("bke_unit_price", 20);
				put("bke_total_price", 80);
				put("bke_enjoyed", 2);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_booking_extra", 1);
				put("bke_booking", 1);
				put("bke_name", "Desayuno");
				put("bke_quantity", 5);
				put("bke_unit_price", 10);
				put("bke_total_price", 50);
				put("bke_enjoyed", 0);
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_booking_extra", 2);
				put("bke_booking", 2);
				put("bke_name", "Desayuno");
				put("bke_quantity", 5);
				put("bke_unit_price", 10);
				put("bke_total_price", 50);
				put("bke_enjoyed", 1);
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_booking_extra", Types.INTEGER);
				put("bke_booking", Types.INTEGER);
				put("bke_name", Types.VARCHAR);
				put("bke_quantity", Types.INTEGER);
				put("bke_unit_price", Types.DECIMAL);
				put("bke_total_price", Types.DECIMAL);
				put("bke_enjoyed", Types.INTEGER);

			}
		});
		return er;
	}

	public static EntityResult getSpecificBookingExtraData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = getAllBookingExtraData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("id_booking_extra", "bke_booking");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("id_booking_extra", Types.INTEGER);
				put("bke_booking", Types.INTEGER);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("id_booking_extra", 2);
		dataToInsert.put("bke_booking", 1);
		dataToInsert.put("bke_name", "Desayuno");
		dataToInsert.put("bke_quantity", 5);
		dataToInsert.put("bke_unit_price", 10);
		dataToInsert.put("bke_total_price", 50);
		dataToInsert.put("bke_enjoyed", 1);
		return dataToInsert;
	}

	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("id_booking_extra"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("id_booking_extra", 2);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_booking_extra", 32);
		return filter;
	}

	public static Map<String, Object> getQueryFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("bke_booking", 32);
		return filter;
	}

	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("id_booking_extra", "bke_booking"));
		queryResult.put("id_booking_extra", Arrays.asList(1));
		queryResult.put("bke_booking", Arrays.asList(1));
		return queryResult;
	}

	public static EntityResult getQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("bk_client", "rm_hotel"));
		queryResult.put("bk_client", Arrays.asList(1));
		queryResult.put("rm_hotel", Arrays.asList(1));
		return queryResult;
	}

	public static List<String> getGenericAttrList() {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_booking_extra");
		return attrList;
	}
}
