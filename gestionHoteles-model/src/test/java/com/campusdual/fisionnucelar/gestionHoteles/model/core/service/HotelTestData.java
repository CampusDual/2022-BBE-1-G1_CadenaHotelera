package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.math.BigDecimal;
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
        put("HTL_LONGITUDE", 42.2406);
        put("HTL_LATITUDE", -8.720727);
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("ID_HOTEL", 1);
        put("HTL_NAME", "FN Ourense");
        put("HTL_PHONE", 988233367);
        put("HTL_ADDRESS", "Avenida das Burgas 87");
        put("HTL_EMAIL", "fnourense@fnhotels.com");
        put("HTL_LONGITUDE", 42.33579);
        put("HTL_LATITUDE",-7.863881);
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("ID_HOTEL", 2);
        put("HTL_NAME", "FN Lugo");
        put("HTL_PHONE", 982165229);
        put("HTL_ADDRESS", "Avenida San Roque 89");
        put("HTL_EMAIL", "fnlugo@fnhotels.com");
        put("HTL_LONGITUDE", 43.00974);
        put("HTL_LATITUDE", -7.5567584);
    }});
    er.setCode(EntityResult.OPERATION_SUCCESSFUL);
    er.setColumnSQLTypes(new HashMap<String, Number>() {{
        put("ID_HOTEL", Types.INTEGER);
        put("HTL_NAME", Types.VARCHAR);
        put("HTL_PHONE", Types.VARCHAR);
        put("HTL_EMAIL", Types.VARCHAR);
        put("HTL_ADDRESS", Types.VARCHAR);
        put("HTL_LONGITUDE", Types.DECIMAL);
        put("HTL_LATITUDE", Types.DECIMAL);
     
        
    }});
    return er;
}

public static EntityResult getHotelsWithLocation() {
    List<String> columnList = Arrays.asList("id_hotel", "htl_name", "htl_phone","htl_address", "htl_email","htl_longitude","htl_latitude");
    EntityResult er = new EntityResultMapImpl(columnList);
    er.addRecord(new HashMap<String, Object>() {{
        put("id_hotel", 0);
        put("htl_name", "FN Vigo");
        put("htl_phone", 986562722);
        put("htl_address", "Gran Vía 1");
        put("htl_email", "fnvigo@fnhotels.com");
        put("htl_longitude",new BigDecimal (42.2406));
        put("htl_latitude", new BigDecimal (-8.720727));
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("id_hotel", 1);
        put("htl_name", "FN Ourense");
        put("htl_phone", 988233367);
        put("htl_address", "Avenida das Burgas 87");
        put("htl_email", "fnourense@fnhotels.com");
        put("htl_longitude", new BigDecimal (42.33579));
        put("htl_latitude",new BigDecimal (-7.863881));
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("id_hotel", 2);
        put("htl_name", "FN Lugo");
        put("htl_phone", 982165229);
        put("htl_address", "Avenida San Roque 89");
        put("htl_email", "fnlugo@fnhotels.com");
        put("htl_longitude", new BigDecimal(43.00974));
        put("htl_latitude",new BigDecimal(-7.5567584));
    }});
    er.setCode(EntityResult.OPERATION_SUCCESSFUL);
    er.setColumnSQLTypes(new HashMap<String, Number>() {{
        put("id_hotel", Types.INTEGER);
        put("htl_name", Types.VARCHAR);
        put("htl_phone", Types.VARCHAR);
        put("htl_email", Types.VARCHAR);
        put("HTL_ADDRESS", Types.VARCHAR);
        put("htl_longitude", Types.DECIMAL);
        put("htl_latitude", Types.DECIMAL);
            
    }});
    return er;
}

public static EntityResult getHotelsWithLocation2() {
    List<String> columnList = Arrays.asList("id_hotel", "htl_name", "htl_phone","htl_address", "htl_email","htl_longitude","htl_latitude");
    EntityResult er = new EntityResultMapImpl(columnList);
    er.addRecord(new HashMap<String, Object>() {{
        put("id_hotel", 0);
        put("htl_name", "FN Vigo");
        put("htl_phone", 986562722);
        put("htl_address", "Gran Vía 1");
        put("htl_email", "fnvigo@fnhotels.com");
        put("htl_longitude",new BigDecimal (-8.720727));
        put("htl_latitude", new BigDecimal (42.2406));
    
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("id_hotel", 1);
        put("htl_name", "FN Ourense");
        put("htl_phone", 988233367);
        put("htl_address", "Avenida das Burgas 87");
        put("htl_email", "fnourense@fnhotels.com");       
        put("htl_longitude", new BigDecimal (-7.863881));
        put("htl_latitude",new BigDecimal (42.33579));
    }});
    er.addRecord(new HashMap<String, Object>() {{
        put("id_hotel", 2);
        put("htl_name", "FN Lugo");
        put("htl_phone", 982165229);
        put("htl_address", "Avenida San Roque 89");
        put("htl_email", "fnlugo@fnhotels.com");

        put("htl_longitude", new BigDecimal(-7.5567584));
        put("htl_latitude",new BigDecimal(43.00974));
        
        
        
        
    }});
    er.setCode(EntityResult.OPERATION_SUCCESSFUL);
    er.setColumnSQLTypes(new HashMap<String, Number>() {{
        put("id_hotel", Types.INTEGER);
        put("htl_name", Types.VARCHAR);
        put("htl_phone", Types.VARCHAR);
        put("htl_email", Types.VARCHAR);
        put("HTL_ADDRESS", Types.VARCHAR);
        put("htl_longitude", Types.DECIMAL);
        put("htl_latitude", Types.DECIMAL);
            
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
	
	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
    	filter.put("id_hotel", 32);
		return filter;
	}
	
}
