package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class RoomTypeTestData {
	public static EntityResult getAllTypeRoomData() {
	    List<String> columnList = Arrays.asList("ID_ROOM_TYPE", "RMT_NAME", "RMT_CAPACITY", "RMT_PRICE");
	    EntityResult er = new EntityResultMapImpl(columnList);
	    er.addRecord(new HashMap<String, Object>() {{
	        put("ID_ROOM_TYPE", 0);
	        put("RMT_NAME", "Individual");
	        put("RMT_CAPACITY", 1);
	        put("RMT_PRICE", 35);
	    }});
	    er.addRecord(new HashMap<String, Object>() {{
	    	put("ID_ROOM_TYPE", 1);
	        put("RMT_NAME", "Doble");
	        put("RMT_CAPACITY", 2);
	        put("RMT_PRICE", 50);
	    }});
	    er.addRecord(new HashMap<String, Object>() {{
	    	put("ID_ROOM_TYPE", 2);
	        put("RMT_NAME", "Triple");
	        put("RMT_CAPACITY", 3);
	        put("RMT_PRICE", 65);
	    }});
	   
	    er.setCode(EntityResult.OPERATION_SUCCESSFUL);
	    er.setColumnSQLTypes(new HashMap<String, Number>() {{
	        put("ID_ROOM_TYPE", Types.INTEGER);
	        put("RMT_NAME", Types.VARCHAR);
	        put("RMT_CAPACITY", Types.INTEGER);
	        put("RMT_PRICE", Types.DOUBLE);
	       
	    }});
	    return er;
	}
	public static EntityResult getSpecificRoomTypeData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = RoomTypeTestData.getAllTypeRoomData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("ID_ROOM_TYPE", "RMT_NAME","RMT_CAPACITY","RMT_PRICE");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {{
			 put("ID_ROOM_TYPE", Types.INTEGER);
		     put("RMT_NAME", Types.VARCHAR);
		     put("RMT_CAPACITY", Types.INTEGER);
		     put("RMT_PRICE", Types.DOUBLE);
		}});
    return er;
	}
	
	public static Map<String,Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("rmt_name", "Suite");
		dataToInsert.put("rmt_capacity", 2);
		dataToInsert.put("rmt_price", 150);
		return dataToInsert;
	}
	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_ROOM_TYPE"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_ROOM_TYPE", 2);
			}
		});
		return er;
	}
	
	public static Map<String,Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_room_type", 14);
		return filter;
	}
	
	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_ROOM_TYPE", "RMT_NAME"));	
		return queryResult;
	}
	
	public static List<String> getGenericAttrList(){
		List<String> attrList = new ArrayList<>();
		attrList.add("id_room_type");
		return attrList;
	}
}
