package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;

public class ExtraTestData {
	public static EntityResult getAllExtraData() {
		List<String> columnList = Arrays.asList("ID_EXTRA", "EX_NAME", "EX_DESCRIPTION");
		EntityResult er = new EntityResultMapImpl(columnList);
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRA", 0);
				put("EX_NAME", "gimnasio");
				put("EX_DESCRIPTION",
						"Espacio de ejercicio totalmente equipado y con sauna en el vestuario. Precio por día");
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRA", 1);
				put("EX_NAME", "Sala de conferencias");
				put("EX_DESCRIPTION", "Espacio acondicionado para reuniones y eventos. Precio por hora");
			}
		});
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRA", 2);
				put("EX_NAME", "Catering");
				put("EX_DESCRIPTION",
						"Servicio que ofrece un menú adecuado para el cliente en tus celebraciones o reuniones");
			}
		});

		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_EXTRA", Types.INTEGER);
				put("EX_NAME", Types.VARCHAR);
				put("EX_DESCRIPTION", Types.VARCHAR);

			}
		});
		return er;
	}

	public static EntityResult getSpecificExtraData(Map<String, Object> keyValues, List<String> attributes) {
		EntityResult allData = ExtraTestData.getAllExtraData();
		int recordIndex = allData.getRecordIndex(keyValues);
		HashMap<String, Object> recordValues = (HashMap) allData.getRecordValues(recordIndex);
		List<String> columnList = Arrays.asList("ID_EXTRA", "EX_NAME");
		EntityResult er = new EntityResultMapImpl(columnList);
		if (recordValues != null) {
			er.addRecord(recordValues);
		}
		er.setCode(EntityResult.OPERATION_SUCCESSFUL);
		er.setColumnSQLTypes(new HashMap<String, Number>() {
			{
				put("ID_EXTRA", Types.INTEGER);
				put("EX_NAME", Types.VARCHAR);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericDataToInsertOrUpdate() {
		Map<String, Object> dataToInsert = new HashMap<>();
		dataToInsert.put("ex_name", "Sala de conferencias");
		dataToInsert.put("ex_description", "Sala completamente equipada");
		return dataToInsert;
	}

	public static EntityResult getGenericInsertResult() {
		EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_EXTRA"));
		er.addRecord(new HashMap<String, Object>() {
			{
				put("ID_EXTRA", 2);
			}
		});
		return er;
	}

	public static Map<String, Object> getGenericFilter() {
		Map<String, Object> filter = new HashMap<>();
		filter.put("id_extra", 32);
		return filter;
	}

	public static EntityResult getGenericQueryResult() {
		EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_EXTRA","EX_NAME"));
		return queryResult;
	}

	public static List<String> getGenericAttrList() {
		List<String> attrList = new ArrayList<>();
		attrList.add("id_extra");
		return attrList;
	}

}
