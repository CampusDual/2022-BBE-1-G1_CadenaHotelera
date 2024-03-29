package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

public interface IReviewService {

	 public EntityResult reviewQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
	 public EntityResult reviewInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	 public EntityResult reviewresponseUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	 public EntityResult reviewDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	
}
