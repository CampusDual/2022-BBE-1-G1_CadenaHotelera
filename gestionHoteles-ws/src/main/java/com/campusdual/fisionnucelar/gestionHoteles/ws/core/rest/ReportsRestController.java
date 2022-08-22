package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;
/**
 * Listens for requests to make a receipt og the given booking id
 * @author Samuel Purriños
 * @since 2/8/22
 * @version 1.0
 * 
 */
import java.io.IOException;


import java.sql.SQLException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IReportsService;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;

import net.sf.jasperreports.engine.JRException;
@Controller
@RequestMapping("/reports")
public class ReportsRestController {
  @Autowired
  IReportsService reportsService;
  @GetMapping("/receipt")
    public ResponseEntity<byte[]> getReceipt(@RequestParam("id_booking") int bookingId,@RequestParam("currency_code")String currencyCode) throws OntimizeJEERuntimeException, JRException, IOException, SQLException {
        HttpHeaders headers = new HttpHeaders();
        byte[] contents=null;
        contents = reportsService.getReceipt(bookingId,currencyCode);
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "receipt.pdf";
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
       
        return new ResponseEntity<>(contents, headers, HttpStatus.OK);  
    }
    
    @GetMapping("/historic/receipt")
    public ResponseEntity<byte[]> getHistoricReceipt(@RequestParam("id_booking") int bookingId,@RequestParam("currency_code")String currencyCode) throws OntimizeJEERuntimeException, JRException, IOException, SQLException {
    	HttpHeaders headers = new HttpHeaders();
    	byte[] contents=null;
    	contents = reportsService.getReceiptFromHistoric(bookingId,currencyCode);
    	headers.setContentType(MediaType.APPLICATION_PDF);
    	String filename = "receipt.pdf";
    	headers.setContentDispositionFormData(filename, filename);
    	headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    	
    	return new ResponseEntity<>(contents, headers, HttpStatus.OK);  
    }
    
    @GetMapping("/financial")
    public ResponseEntity<byte[]> getFinancialReport(@RequestParam("from") 
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,@RequestParam("to") 
    	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to,
    	@RequestParam("currency_code") String currencyCode) {
        HttpHeaders headers = new HttpHeaders();
        byte[] contents=null;
        contents = reportsService.getFinancialReport(from, to,currencyCode);
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "financialReport.pdf";
        headers.setContentDispositionFormData(filename, filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(contents, headers, HttpStatus.OK);    
    }
}