package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IReportsService;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;


import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

@Service
public class ReportsService implements IReportsService {

	@Override
	public byte[] getReceipt(int bookingId)
			throws OntimizeJEERuntimeException{
		
		InputStream jasperStream = this.getClass().getResourceAsStream("billReport.jasper");
	    JasperReport jasperReport;
	    JasperPrint jasperPrint;
	    byte[] contents = null;
  	  	JRResultSetDataSource datasource = new JRResultSetDataSource(getReceiptDataSource(bookingId));
  	  	int parameter =154;
		try {
			jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
			jasperPrint = JasperFillManager.fillReport(jasperReport, null, datasource);			
			contents =JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	    
        return contents;
	}
	
	private ResultSet getReceiptDataSource(int bookingId){
		Connection conn;
		ResultSet rs = null;
		String query = getReceiptQuery(bookingId);
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://45.84.210.174:65432/Backend_2022_G1", "Backend_2022_G1", "iexaicaef1iaQuotea");
		  	Statement stmt = conn.createStatement();
		    rs = stmt.executeQuery(query);
		    if (rs.next()) {
		        do {
		        	System.out.println("Query returning data");
		        } while(rs.next());
		    } else {
		    	System.out.println("Empty results");
		    }
		    conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	  			
	    return rs;
	}

	private String getReceiptQuery(int bookingId) {
		return "select b.id_booking, c.cl_nif ,c.cl_name, c.cl_email, c.cl_phone, b.bk_check_in, b.bk_check_out ,b.bk_price, "
				+ "be.bke_name, be.bke_quantity,b.bk_extras_price,  be.bke_unit_price, be.bke_total_price, r.rm_number,"+
		" rt.rmt_name, rt.rmt_price, h.htl_name ,h.htl_address ,h.htl_phone ,h.htl_email ,"
		+ "DATE_PART('day', b.bk_check_out  - b.bk_check_in::timestamp) AS days"+
		" from clients c inner join bookings b on b.bk_client = c.id_client "
		+ "inner join booking_extra be on be.bke_booking = b.id_booking inner join rooms r "+
		" on r.id_room =b.bk_room inner join room_types rt on rt.id_room_type = r.rm_room_type "
		+ "inner join hotels h on h.id_hotel = r.rm_hotel where b.id_booking = "+bookingId;
	}
}
