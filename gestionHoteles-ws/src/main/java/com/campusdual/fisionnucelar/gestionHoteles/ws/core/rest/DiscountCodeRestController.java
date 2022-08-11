package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IDiscountCodeService;
import com.ontimize.jee.server.rest.ORestController;

/**
 * This class listens the incoming requests related with the discount_codes table
 *@since 09/08/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/discountcodes")
public class DiscountCodeRestController extends ORestController<IDiscountCodeService> {

 @Autowired
 private IDiscountCodeService discountCodeService;

 @Override
 public IDiscountCodeService getService() {
  return this.discountCodeService;
 }
}