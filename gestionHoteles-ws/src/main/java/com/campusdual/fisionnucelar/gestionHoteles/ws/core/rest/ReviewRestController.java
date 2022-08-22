package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IReviewService;
import com.ontimize.jee.server.rest.ORestController;

/**
 * This class listens the incoming requests related with the reviews table
 *@since 19/08/2022
 *@version 1.0 
 *
 */
@RestController
@RequestMapping("/reviews")
public class ReviewRestController extends ORestController<IReviewService> {

 @Autowired
 private IReviewService reviewService;

 @Override
 public IReviewService getService() {
  return this.reviewService;
 }
}