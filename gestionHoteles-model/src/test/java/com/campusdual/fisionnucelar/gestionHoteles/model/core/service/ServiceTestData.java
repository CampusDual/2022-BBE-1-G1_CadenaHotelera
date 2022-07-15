package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class ServiceTestData {
	public static EntityResult getAllServiceData() {
		List<String> columnList = Arrays.asList("ID_SERVICE", "SV_NAME", "SV_DESCRIPTION");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_SERVICE", 0);
				put("SV_NAME", "Wifi");
				put("SV_DESCRIPTION", "Wifi gratis a disposición de los clientes del hotel");
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_SERVICE", 1);
				put("SV_NAME", "Accesibilidad");
				put("SV_DESCRIPTION", "Hotel con accesibilidad en cumplimiento de la normativa Ley 51/2003");
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_SERVICE", 2);
				put("SV_NAME", "Alquiler bicicletas");
				put("SV_DESCRIPTION", "Se alquilan bicicletas");
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_SERVICE", Types.INTEGER);
				put("SV_NAME", Types.VARCHAR);
				put("SV_DESCRIPTION", Types.VARCHAR);

			}
		});
		return er;
	}

	public static EntityResult getSpecificServiceData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = ServiceTestData.getAllServiceData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("ID_SERVICE", "SV_NAME");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_SERVICE", Types.INTEGER);
				put("SV_NAME", Types.VARCHAR);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("sv_name", "Wifi");
		dataToInsert.put("sv_description", "Wireless internet");
		return dataToInsert;
	}

	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_SERVICE"));
        er.addRecord(new HashMap<String, Object>() {{put("ID_SERVICE", 2);}});
		return er;
	}

	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
    	filter.put("id_service", 32);
		return filter;
	}

	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_SERVICE","SV_NAME"));
		return queryResult;
	}

	public static List<String> getGenericAttrList() {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_service");
		return attrList;
	}
}
