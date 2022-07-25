package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.math.BigDecimal;
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

public class BookingTestData {
	private static Date checkIn;
	private static Date checkOut;
	public static EntityResult getAllBookingData() {
	    List<String> columnList = Arrays.asList("ID_BOOKING", "BK_CHECK_IN", "BK_CHECK_OUT","BK_PRICE", "BK_ROOM", "BK_CLIENT","BK_EXTRAS_PRICE");
	    EntityResult er = new EntityResultMapImpl(columnList);
	    Calendar calendar = Calendar.getInstance();
	    calendar.set(2023,3,3);
	    checkIn = calendar.getTime();
	    calendar.set(2023,4,4);
	    checkOut = calendar.getTime();
	    er.addRecord(new HashMap<String, Object>() {{
	        put("ID_BOOKING", 1);
	        put("BK_CHECK_IN", checkIn);
	        put("BK_CHECK_OUT", checkOut);
	        put("BK_ROOM", 1);
	        put("BK_CLIENT", 1);
	        put("BK_PRICE", 250);
	        put("BK_EXTRAS_PRICE", 50);
	    }});
	    calendar.set(2023,6,12);
	    checkIn = calendar.getTime();
	    calendar.set(2023,7,1);
	    checkOut= calendar.getTime();
	    er.addRecord(new HashMap<String, Object>() {{
	        put("ID_BOOKING", 2);
	        put("BK_CHECK_IN", checkIn);
	        put("BK_CHECK_OUT", checkOut);
	        put("BK_ROOM",4);
	        put("BK_CLIENT", 16);
	        put("BK_PRICE", 340);
	        put("BK_EXTRAS_PRICE", 75);
	    }});
	    calendar.set(2022,9,12);
	    checkIn = calendar.getTime();
	    calendar.set(2022,9,31);
	    checkOut= calendar.getTime();
	    er.addRecord(new HashMap<String, Object>() {{
	        put("ID_BOOKING", 2);
	        put("BK_CHECK_IN", checkIn);
	        put("BK_CHECK_OUT", checkOut);
	        put("BK_ROOM",48);
	        put("BK_CLIENT", 103);
	        put("BK_PRICE", 175);
	        put("BK_EXTRAS_PRICE", 20);
	    }});
	    er.setCode(EntityResult.OPERATION_SUCCESSFUL);
	    er.setColumnSQLTypes(new HashMap<String, Number>() {{
			put("ID_BOOKING", Types.INTEGER);
			put("BK_CHECK_IN", Types.DATE);
			put("BK_CHECK_OUT", Types.DATE);
			put("BK_PRICE", Types.DOUBLE);
			put("BK_ROOM", Types.INTEGER);
	        put("BK_EXTRAS_PRICE",Types.DOUBLE);
	    }});
	    return er;
	}

		public static EntityResult getSpecificBookingData(Map<String, Object> keyValues, List<String> attributes) {
			EntityResult allData = HotelTestData.getAllHotelData();
			int recordIndex = allData.getRecordIndex(keyValues);
			HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
			List<String> columnList = Arrays.asList("ID_HOTEL", "HTL_NAME");
			EntityResult er = new EntityResultMapImpl(columnList);
			if (recordValues != null) {
				er.addRecord(recordValues);
			}
			er.setCode(EntityResult.OPERATION_SUCCESSFUL);
			er.setColumnSQLTypes(new HashMap<String, Number>() {{
				put("ID_BOOKING", Types.INTEGER);
				put("BK_CHECK_IN", Types.DATE);
				put("BK_CHECK_OUT", Types.DATE);
				put("BK_PRICE", Types.DOUBLE);
				put("BK_ROOM", Types.INTEGER);
				put("BK_EXTRAS_PRICE", Types.DOUBLE);
			}});
	    return er;
	}

		public static List<String> getGenericColumns(){
			List<String> columns = new ArrayList<>();
			columns.add("id_booking");
			columns.add("bk_check_in");
			columns.add("bk_check_out");
			columns.add("bk_room");
			columns.add("bk_price");
			return columns;
		}
		
		public static EntityResult getGenericBookingER() {
			EntityResult er = new EntityResultMapImpl();
			er.addRecord(new HashMap<String, Object>() {
				{
					put("id_booking", 2);
					put("bk_room", 2);
					put("bk_check_in", checkIn);
					put("bk_check_out", checkOut);
					put("bk_price", 100);
				}
			});
			return er;
		}
		
		public static Map<String,Object> getDataToInsert(){
			  Map<String,Object> dataToInsert = new HashMap<>();
			  dataToInsert.put("bk_check_in", new Date(2022,10,03));
			  dataToInsert.put("bk_check_out", new Date(2022,10,22));
			  dataToInsert.put("bk_room", 2);
			  dataToInsert.put("bk_client", 2);
			  return dataToInsert;
		}
		
		public static EntityResult getClientResult() {
			EntityResult er = new EntityResultMapImpl();
			er.addRecord(new HashMap<String, Object>() {
				{
					put("id_client", 2);
					put("cl_leaving_date", new Date());

				}
			});
			return er;
			
			
		}
		public static EntityResult getRoomResult() {
			EntityResult roomResult = new EntityResultMapImpl();
			roomResult.addRecord(new HashMap<String, Object>() {
				{
					put("id_room", 2);
				}
			});
			return roomResult;
		}
		
		public static Map<String,Object> getGenericAttrMap(){
			Map<String, Object> attrMap = new HashMap<>();
			Calendar calendar = Calendar.getInstance();
			calendar.set(2023, 9, 10);
			Date checkIn = calendar.getTime();
			calendar.set(2023, 10, 11);
			Date checkOut = calendar.getTime();
			attrMap.put("bk_room", 2);
			attrMap.put("bk_client", 2);
			attrMap.put("bk_check_in", checkIn);
			attrMap.put("bk_check_out", checkOut);
			attrMap.put("bk_price", 100);
			return attrMap;
		}
		
		public static Map<String,Object> getBookingKeyMap(){
			Map<String, Object> keyMap = new HashMap<>();
			keyMap.put("id_booking", 50);
			return keyMap;
		
		}
		
		public static List<String> getExtraBookingColumns(){
		List<String> columnsExtraBooking = new ArrayList<>();
		columnsExtraBooking.add("id_booking_extra");
		columnsExtraBooking.add("bke_booking");
		columnsExtraBooking.add("bke_name");
		columnsExtraBooking.add("bke_quantity");
		columnsExtraBooking.add("bke_unit_price");
		columnsExtraBooking.add("bke_total_price");
		columnsExtraBooking.add("bke_enjoyed");
		columnsExtraBooking.add("bk_extras_price");
		return columnsExtraBooking;
		}
		
		public static EntityResult getBookingExtraResult() {
			EntityResult bookingExtraResult = new EntityResultMapImpl();
			bookingExtraResult.addRecord(new HashMap<String, Object>() {{
				put("id_bookingextra", 2);
				put("bke_booking", 2); 
				put("bke_name", "Niñero"); 
				put("bke_quantity", 2); 
				put("bke_unit_price", new BigDecimal(200)); 
				put("bke_total_price", new BigDecimal(2000)); 
				put("bke_enjoyed", 0); 
				put("bk_extras_price", new BigDecimal(200)); }});
			return bookingExtraResult;
		}
		
		public static Map<String,Object> getBKExtraAttrMap(){
			Map<String,Object> attrMap = new HashMap<String,Object>();
			attrMap.put("id_booking_extra", 2);
			attrMap.put("quantity", 2);
			return attrMap;
		}
		
		public static Map<String,Object> getAvRoomsFilter() {
	    	Map<String,Object> filter = new HashMap<>();
	    	filter.put("bk_check_in", "2022-30-08");
	    	filter.put("bk_check_out", "2022-15-09");
	    	filter.put("id_hotel", 2);
	    	return filter;
			
		}
		
		public static EntityResult getAvRoomsER() {
		 	EntityResult availableRooms = new EntityResultMapImpl();
        	availableRooms.addRecord(new HashMap<String, Object>() {{
        		put("id_hotel", 2);
        		put("rm_number",401);
        		put("htl_name","FN As Pontes");
        		put("id_booking",38);
        	}});
        	return availableRooms;
			
		}
}