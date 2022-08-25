package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

@ControllerAdvice
public class MvcExceptionHandler {
	private Logger log;
	public MvcExceptionHandler(){
		this.log = LoggerFactory.getLogger(this.getClass());
	}
	
	
	@ExceptionHandler(RecordNotFoundException.class)
	public ResponseEntity<EntityResult> bookingIdNotFound(RecordNotFoundException e){
		log.error("Booking not found {}",e.getMessage());
		EntityResult errorResult = new EntityResultMapImpl();
		errorResult.setMessage(e.getMessage());
		return new ResponseEntity<EntityResult>(errorResult,HttpStatus.NOT_FOUND);
	}
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<EntityResult> currencyCodeError(IllegalArgumentException e){
		log.error("Currency converter error {}",e.getMessage());
		EntityResult errorResult = new EntityResultMapImpl();
		errorResult.setMessage("CURRENCY_CODE_DOESN´T_EXISTS");
		return new ResponseEntity<EntityResult>(errorResult,HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(NullPointerException.class)
	 public ResponseEntity<EntityResult> imageNoutFound(NullPointerException e){
		log.error("Null pointer exception {}",e.getMessage());
	  EntityResult errorResult = new EntityResultMapImpl();
	  errorResult.setMessage("NOT_FOUND");
	  errorResult.setCode(EntityResult.OPERATION_WRONG);
	  return new ResponseEntity<EntityResult>(errorResult,HttpStatus.NOT_FOUND);
	 }

}
