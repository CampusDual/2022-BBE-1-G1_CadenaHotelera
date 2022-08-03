package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IReportsService;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.util.remote.BytesBlock;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
@Controller
@RequestMapping("/reports")
public class ReportsRestController {
	@Autowired
	IReportsService reportsService;
	@GetMapping("/receipt/{id_booking}")
    public ResponseEntity<byte[]> getReceipt(@PathVariable("id_booking") int id_booking) throws OntimizeJEERuntimeException, JRException, IOException, SQLException {
    		HttpHeaders headers = new HttpHeaders();
    	    byte[] contents = reportsService.getReceipt(id_booking);
    	    headers.setContentType(MediaType.APPLICATION_PDF);
    	    // Here you have to set the actual filename of your pdf
    	    String filename = "output.pdf";
    	    headers.setContentDispositionFormData(filename, filename);
    	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
    	    ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
    	    return response;    
    }
}
