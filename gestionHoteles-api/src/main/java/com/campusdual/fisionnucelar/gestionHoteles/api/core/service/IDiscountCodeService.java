package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

/**
 * This interface defines the queries over the discount_codes table
 *@since 09/08/2022
 *@version 1.0 
 *
 */
public interface IDiscountCodeService {
	 // Discount_code
	 public EntityResult discountcodeQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException;
	 public EntityResult discountcodeInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
	 public EntityResult discountcodeUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap) throws OntimizeJEERuntimeException;
	 public EntityResult discountcodeDelete(Map<String, Object> attrMap) throws OntimizeJEERuntimeException;
}
