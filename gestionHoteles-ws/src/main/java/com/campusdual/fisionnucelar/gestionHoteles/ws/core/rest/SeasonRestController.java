package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.ISeasonService;
import com.ontimize.jee.server.rest.ORestController;

/**
 * This class listens the incoming requests related with the seasons table
 *@since 05/08/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/seasons")
public class SeasonRestController  extends ORestController<ISeasonService>{

	 @Autowired
	 private ISeasonService seasonService;

	 @Override
	 public ISeasonService getService() {
	  return this.seasonService;
	 }
}
