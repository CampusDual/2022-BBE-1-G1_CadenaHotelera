package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IReportsService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;


import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;


import net.sf.jasperreports.engine.JRException;
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
    Map<String,Object> params = new HashMap<String, Object>();
      int parameter =bookingId;
        params.put("Parameter1", parameter);
    InputStream jasperStream = this.getClass().getResourceAsStream("billReport.jasper");
      JasperReport jasperReport;
      JasperPrint jasperPrint;
      if(!checkIfQueryReturnsResults(bookingId)) throw new RecordNotFoundException("BOOKING_NOT_FOUND");
      Connection conn = null;
    try {
      conn = DriverManager.getConnection("jdbc:postgresql://45.84.210.174:65432/Backend_2022_G1", "Backend_2022_G1", "iexaicaef1iaQuotea");
    } catch (SQLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
      byte[] contents = null;
    try {
      jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
      jasperPrint = JasperFillManager.fillReport(jasperReport, params,conn);
      contents =JasperExportManager.exportReportToPdf(jasperPrint);
      JasperExportManager.exportReportToPdfFile(jasperPrint,"report.pdf");
    } catch (JRException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }finally {
      try {
        conn.close();
      } catch (SQLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
        
        return contents;
  }
  
  private boolean checkIfQueryReturnsResults(int bookingId){
    boolean results=false;
    Connection conn;
    ResultSet rs = null;
    String query = getReceiptQuery(bookingId);
    try {
      conn = DriverManager.getConnection("jdbc:postgresql://45.84.210.174:65432/Backend_2022_G1", "Backend_2022_G1", "iexaicaef1iaQuotea");
        Statement stmt = conn.createStatement();
        rs = stmt.executeQuery(query);
        if (rs.next()) {
            do {
              results=true;
            } while(rs.next());
        } else {
          results=false;
        }
        conn.close();
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
      
      return results;
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
