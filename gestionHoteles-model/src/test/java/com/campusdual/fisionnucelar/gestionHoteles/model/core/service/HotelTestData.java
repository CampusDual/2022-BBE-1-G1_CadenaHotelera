package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class HotelTestData {
public static EntityResult getAllHotelData() {
    List<String> columnList = Arrays.asList("ID_HOTEL", "HTL_NAME", "HTL_PHONE","HTL_ADDRESS", "HTL_EMAIL");
    EntityResult er = new EntityResultMapImpl(columnList);
    er.addRecord(new HashMap<String, Object>() {{
        put("ID_HOTEL", 0);
        put("HTL_NAME", "FN Vigo");
        put("HTL_PHONE", 986562722);
        put("HTL_ADDRESS", "Gran Vía 1");
        put("HTL_EMAIL", "fnvigo@fnhotels.com");
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("ID_HOTEL", 1);
        put("HTL_NAME", "FN Ourense");
        put("HTL_PHONE", 988233367);
        put("HTL_ADDRESS", "Avenida das Burgas 87");
        put("HTL_EMAIL", "fnourense@fnhotels.com");
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("ID_HOTEL", 2);
        put("HTL_NAME", "FN Lugo");
        put("HTL_PHONE", 982165229);
        put("HTL_ADDRESS", "Avenida San Roque 89");
        put("HTL_EMAIL", "fnlugo@fnhotels.com");
    }});
    er.setCode(EntityResult.OPERATION_SUCCESSFUL);
    er.setColumnSQLTypes(new HashMap<String, Number>() {{
        put("ID_HOTEL", Types.INTEGER);
        put("HTL_NAME", Types.VARCHAR);
        put("HTL_PHONE", Types.VARCHAR);
        put("HTL_EMAIL", Types.VARCHAR);
        put("HTL_ADDRESS", Types.VARCHAR);
    }});
    return er;
}

	public static EntityResult getSpecificHotelData(Map<String, Object> keyValues, List<String> attributes) {
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
			put("ID_HOTEL", Types.INTEGER);
			put("HTL_NAME", Types.VARCHAR);
		}});
    return er;
}
}
