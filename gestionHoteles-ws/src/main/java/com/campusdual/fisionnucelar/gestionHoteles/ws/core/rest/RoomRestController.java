package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomService;
import com.ontimize.jee.server.rest.ORestController;

@RestController
@RequestMapping("/rooms")
public class RoomRestController extends ORestController<IRoomService> {

 @Autowired
 private IRoomService roomService;

 @Override
 public IRoomService getService() {
  return this.roomService;
 }
}