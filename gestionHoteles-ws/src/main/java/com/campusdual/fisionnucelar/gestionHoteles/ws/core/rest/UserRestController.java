package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IUserService;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.server.rest.ORestController;

/**
 * This class listens the incoming requests related with the users table
 *@since 30/06/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/users")
public class UserRestController extends ORestController<IUserService> {

	@Autowired
	private IUserService userSrv;

	@Override
	public IUserService getService() {
		return this.userSrv;
	}

	@RequestMapping(
		value = "/login",
		method = RequestMethod.POST,
		produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityResult> login() {
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
