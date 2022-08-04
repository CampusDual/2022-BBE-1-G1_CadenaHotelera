package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

	public class RoomTestData {
		public static EntityResult getAllRoomData() {
			List<String> columnList = Arrays.asList("id_room", "rm_room_type", "rm_hotel","rm_number");
			EntityResult er = new EntityResultMapImpl(columnList);
			er.addRecord(new HashMap<String, Object>() {
				{
					put("id_room", 0);
					put("rm_room_type", 1);
					put("rm_hotel", 1);
					put("rm_number", 709);
				}
			});
			er.addRecord(new HashMap<String, Object>() {
				{
					put("id_room", 1);
					put("rm_room_type", 2);
					put("rm_hotel", 1);
					put("rm_number", 401);
				}
			});
			er.addRecord(new HashMap<String, Object>() {
				{
					put("id_room", 2);
					put("rm_room_type", 1);
					put("rm_hotel", 1);
					put("rm_number", 404);
				}
			});

			er.setCode(EntityResult.OPERATION_SUCCESSFUL);
			er.setColumnSQLTypes(new HashMap<String, Number>() {
				{
					put("id_room", Types.INTEGER);
					put("rm_room_type", Types.INTEGER);
					put("rm_hotel", Types.INTEGER);
					put("rm_number", Types.INTEGER);

				}
			});
			return er;
		}
		
		
		public static EntityResult getSpecificRoomData(Map<String, Object> keyValues, List<String> attributes) {
			EntityResult allData = getAllRoomData();
			int recordIndex = allData.getRecordIndex(keyValues);
			HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
			List<String> columnList = Arrays.asList("id_room", "rm_room_type","rm_hotel","rm_number");
			EntityResult er = new EntityResultMapImpl(columnList);
			if (recordValues != null) {
				er.addRecord(recordValues);
			}
			er.setCode(EntityResult.OPERATION_SUCCESSFUL);
			er.setColumnSQLTypes(new HashMap<String, Number>() {
				{
					put("id_room", Types.INTEGER);
					put("rm_room_type", Types.INTEGER);
					put("rm_hotel", Types.INTEGER);
					put("rm_number", Types.INTEGER);
				}
			});
			return er;
		}
		

		
		public static Map<String,Object> getGenericDataToInsertOrUpdate() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("id_room", 1);
			dataToInsert.put("rm_room_type", 1);
			dataToInsert.put("rm_hotel",1);
			dataToInsert.put("rm_number", 507);
			return dataToInsert;
		}
		
		public static EntityResult getGenericInsertResult() {
			EntityResult er = new EntityResultMapImpl(Arrays.asList("id_room"));
			er.addRecord(new HashMap<String, Object>() {
				{
					put("id_room", 2);
				}
			});
			return er;
		}
		
		public static Map<String,Object> getGenericFilter() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_room", 32);
			return filter;
		}
		public static EntityResult getGenericQueryResult() {
			EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("id_room", "rm_room_type"));	
			return queryResult;
		}
		
		public static List<String> getGenericAttrList(){
			List<String> attrList = new ArrayList<>();
			attrList.add("id_room");
			return attrList;
		}
		
}
