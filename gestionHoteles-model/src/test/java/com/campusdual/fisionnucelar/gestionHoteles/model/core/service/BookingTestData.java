package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
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

}
