package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;


import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the users table
 *@since 27/06/2022
 *@version 1.0 
 *
 */
public interface IUserService {

	public EntityResult userQuery(Map<String, Object> keyMap, List<String> attrList) ;
	public EntityResult userUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	public EntityResult userDelete(Map<String, Object> keyMap);
	public EntityResult userAdminInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	public EntityResult userWorkerInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	public EntityResult userClientInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	public EntityResult userClientByManagersInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	
}
