package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.List;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomTypeService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomTypeDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;


@Service("RoomTypeService")
@Lazy
public class RoomTypeService implements IRoomTypeService {

	 @Autowired
	 private RoomTypeDao roomTypeDao;
	 @Autowired
	 private DefaultOntimizeDaoHelper daoHelper;

	 @Override
	 public EntityResult roomtypeQuery(Map<String, Object> keyMap, List<String> attrList)
	   throws OntimizeJEERuntimeException {
	  EntityResult searchResult = this.daoHelper.query(this.roomTypeDao, keyMap, attrList);
	  if (searchResult.OPERATION_WRONG == 0) {
	   searchResult.setMessage("No se ha encontrado ningún resultado");
	  }
	  return searchResult;
	 }

	 @Override
	 public EntityResult roomtypeInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
	  EntityResult insertResult = this.daoHelper.insert(this.roomTypeDao, attrMap);
	  if (insertResult.OPERATION_SUCCESSFUL == 0) {
	   insertResult.setMessage("Inserción realizada correctamente");
	  } else {
	   insertResult.setMessage("Error. No se ha podido realizar la inserción");
	  }
	  return insertResult;
	 }

	 @Override
	 public EntityResult roomtypeUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
	   throws OntimizeJEERuntimeException {
	  EntityResult updateResult = this.daoHelper.update(this.roomTypeDao, attrMap, keyMap);
	  if (updateResult.OPERATION_SUCCESSFUL == 0) {
	   updateResult.setMessage("Actualización realizada correctamente");
	  } else {
	   updateResult.setMessage("Error. No se ha podido realizar la actualización");
	  }
	  return updateResult;
	 }

	 @Override
	 public EntityResult roomtypeDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
	  EntityResult deleteResult = this.daoHelper.delete(this.roomTypeDao, keyMap);
	  if (deleteResult.OPERATION_SUCCESSFUL == 0) {
	   deleteResult.setMessage("Borrado realizado correctamente");
	  } else {
	   deleteResult.setMessage("Error. No se ha podido realizar el borrado");
	  }
	  return deleteResult;
	 }
	}


