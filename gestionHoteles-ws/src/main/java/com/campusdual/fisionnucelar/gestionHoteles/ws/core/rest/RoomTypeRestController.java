package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomTypeService;
import com.ontimize.jee.server.rest.ORestController;

@RestController
@RequestMapping("/roomtypes")
public class RoomTypeRestController extends ORestController<IRoomTypeService> {

 @Autowired
 private IRoomTypeService roomTypeService;

 @Override
 public IRoomTypeService getService() {
  return this.roomTypeService;
 }
}