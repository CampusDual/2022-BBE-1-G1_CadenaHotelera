package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.io.InputStream;




import java.sql.*;
import java.util.*;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IReportsService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.*;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * This class generates a booking receipt
 * 
 * @since 2/8/22
 * @version 1.0
 * @author Samuel Purriños
 *
 */
@Service
public class ReportsService implements IReportsService {
	@Autowired
	private BookingDao bookingDao;
	@Autowired
	private BookingHistDao bookingHistDao;
	@Autowired
	private BookingExtraDao bookingExtraDao;
	@Autowired
	private BookingExtraHistDao bookingExtraHistDao;
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	org.slf4j.Logger log;

	public ReportsService() {
		this.log = LoggerFactory.getLogger(this.getClass());
	}
	
	/**
	 * Generates a pdf receipt of a given booking
	 * @exception RecordNotFoundException when the booking requested by the user not exists
	 * @exception SQLException when there are problems connecting to the database
	 * @exception RException when thre are problems jenerating the receipt
	 * @param bookingId the booking id to generate the receipt
	 * @return the receipt in pdf formatted yb a byte array
	 * @since 2/8/22
	 */
	@Override
	public byte[] getReceipt(int bookingId) throws OntimizeJEERuntimeException {
		Map<String, Object> params = new HashMap<>();
		int parameter = bookingId;
		params.put("Parameter1", parameter);
		InputStream jasperStream = this.getClass().getResourceAsStream("billReport.jasper");
		JasperReport jasperReport;
		JasperPrint jasperPrint;
		if (!checkIfQueryReturnsResults(getReceiptQuery(bookingId)))
			throw new RecordNotFoundException("BOOKING_NOT_FOUND");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://45.84.210.174:65432/Backend_2022_G1",
					"Backend_2022_G1", "iexaicaef1iaQuotea");
		} catch (SQLException e1) {
			log.error("unable to connect to database ", e1.getMessage());
		}
		byte[] contents = null;
		try {
			jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
			jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
			contents = JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (JRException e) {
			log.error("unable to build receipt ", e.getMessage());
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("unable to disconnect from database ", e.getMessage());
			}
		}
		this.moveBookingToBookingOld(bookingId);
		return contents;
	}

	/**
	 * Check if the booking provided by the user exists in the database
	 * @param bookingId the id of the booking provided by the user
	 * @return true if exists false if not
	 * @exception RecordNotFoundException if the booking id not exists in the database
	 * @since 4/8/22
	 */
	private boolean checkIfQueryReturnsResults(String query) {
		boolean results = false;
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://45.84.210.174:65432/Backend_2022_G1",
					"Backend_2022_G1", "iexaicaef1iaQuotea");
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				do {
					results = true;
					System.out.println("Query returning data");
				} while (rs.next());
			} else {
				System.out.println("empty results");
				results = false;
			}
			
		} catch (SQLException e) {
			log.error("unable to connect from database ", e.getMessage());
		}finally {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("unable to close connection to database ", e.getMessage());
			}
		}

		return results;
	}

	/**
	 * Query to check if getReceipt method is capable of generate the receipt. The receipt contains all these fields
	 * @param bookingId
	 * @return the query used by the method checkIfQueryReturnsResults
	 */
	private String getReceiptQuery(int bookingId) {
		return "select b.id_booking, c.cl_nif ,c.cl_name, c.cl_email, c.cl_phone, b.bk_check_in, b.bk_check_out ,b.bk_price, "
				+ "be.bke_name, be.bke_quantity,b.bk_extras_price,  be.bke_unit_price, be.bke_total_price, r.rm_number,"
				+ " rt.rmt_name, rt.rmt_price, h.htl_name ,h.htl_address ,h.htl_phone ,h.htl_email ,"
				+ "DATE_PART('day', b.bk_check_out  - b.bk_check_in::timestamp) AS days"
				+ " from clients c inner join bookings b on b.bk_client = c.id_client "
				+ "left join booking_extra be on be.bke_booking = b.id_booking inner join rooms r "
				+ " on r.id_room =b.bk_room inner join room_types rt on rt.id_room_type = r.rm_room_type "
				+ "inner join hotels h on h.id_hotel = r.rm_hotel where b.bk_leaving_date is null and b.id_booking = "
				+ bookingId;
	}
	/**
	 * Query to check if getFinancialReport method is capable of generate the report. The report contains all these fields
	 * @param from to the dates of the billed bookings to elaborate the report
	 * @return the query used by the method checkIfQueryReturnsResults
	 */
	private String getFinancialReportQuery(Date from, Date to) {
		return "select count(b.id_booking)as bookings ,sum(b.bk_price)as total_bookings, sum(b.bk_extras_price) as total_extras ,h.htl_name from bookings_hist b "+ 
				"inner join rooms r on r.id_room =b.bk_room inner join hotels h on h.id_hotel =r.rm_hotel where b.bk_check_out between  "
				+ " '"+from+"' AND  '"+to+"' "+ 
				"group by h.htl_name"  ;  
	}

	/**
	 * Puts the booking and its related booking extras in bookings_old and booking_extra_old, once the booking its completed
	 * @param bookingId
	 */
	@Transactional
	public void moveBookingToBookingOld(int bookingId) {
		Map<String, Object> keyMap = new HashMap<>();
		keyMap.put("id_booking", bookingId);
		EntityResult bookingResult = daoHelper.query(bookingDao, keyMap, Arrays.asList("bk_client", "bk_price",
				"bk_entry_date", "bk_last_update", "bk_extras_price", "bk_check_in", "bk_check_out", "bk_room"));
		keyMap.remove("id_booking");
		keyMap.put("bk_client", bookingResult.getRecordValues(0).get("bk_client"));
		keyMap.put("bk_price", bookingResult.getRecordValues(0).get("bk_price"));
		keyMap.put("bk_entry_date", bookingResult.getRecordValues(0).get("bk_entry_date"));
		keyMap.put("bk_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		keyMap.put("bk_extras_price", bookingResult.getRecordValues(0).get("bk_extras_price"));
		keyMap.put("bk_check_in", bookingResult.getRecordValues(0).get("bk_check_in"));
		keyMap.put("bk_check_out", bookingResult.getRecordValues(0).get("bk_check_out"));
		keyMap.put("bk_room", bookingResult.getRecordValues(0).get("bk_room"));
		keyMap.put("bk_leaving_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
		daoHelper.insert(bookingHistDao, keyMap);
		keyMap.clear();
		keyMap.put("bke_booking", bookingId);
		EntityResult extras = daoHelper.query(bookingExtraDao, keyMap, Arrays.asList("bke_booking", "bke_name",
				"bke_quantity", "bke_unit_price", "bke_total_price", "bke_enjoyed", "id_booking_extra"));
		keyMap.clear();
		if (!extras.isEmpty()) {
			for (int i = 0; i < extras.calculateRecordNumber(); i++) {
				keyMap.put("bke_booking", extras.getRecordValues(i).get("bke_booking"));
				keyMap.put("bke_name", extras.getRecordValues(i).get("bke_name"));
				keyMap.put("bke_quantity", extras.getRecordValues(i).get("bke_quantity"));
				keyMap.put("bke_unit_price", extras.getRecordValues(i).get("bke_unit_price"));
				keyMap.put("bke_total_price", extras.getRecordValues(i).get("bke_total_price"));
				keyMap.put("bke_enjoyed", extras.getRecordValues(i).get("bke_enjoyed"));
				daoHelper.insert(bookingExtraHistDao, keyMap);
				keyMap.clear();
				keyMap.put("id_booking_extra", extras.getRecordValues(i).get("id_booking_extra"));
				daoHelper.delete(bookingExtraDao, keyMap);
				keyMap.clear();
			}
		}
		keyMap.clear();
		keyMap.put("id_booking", bookingId);
		daoHelper.delete(bookingDao, keyMap);
	}

	/**
	 * Generates a pdf financial report between two dates
	 * @exception RecordNotFoundException when there aren´t bookings between the requested dates
	 * @exception SQLException when there are problems connecting to the database
	 * @exception RException when thre are problems jenerating the receipt
	 * @param from to the dates to elaborate the financial report, it gives the bookings billed between these two dates
	 * @return the receipt in pdf formatted yb a byte array
	 * @since 8/8/22
	 */
	@Override
	public byte[] getFinancialReport(Date from, Date to) throws OntimizeJEERuntimeException {
		Map<String, Object> params = new HashMap<>();
		params.put("Parameter1", from);
		params.put("Parameter2", to);
		InputStream jasperStream = this.getClass().getResourceAsStream("financialReport.jasper");
		JasperReport jasperReport;
		JasperPrint jasperPrint;
		if (!checkIfQueryReturnsResults(getFinancialReportQuery(from,to)))
			throw new RecordNotFoundException("THERE_ARE_NOT_BOOKINGS_IN_THESE_DATES");
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:postgresql://45.84.210.174:65432/Backend_2022_G1",
					"Backend_2022_G1", "iexaicaef1iaQuotea");
		} catch (SQLException e1) {
			log.error("unable to connect to database ", e1.getMessage());
		}
		byte[] contents = null;
		try {
			jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);
			jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
			contents = JasperExportManager.exportReportToPdf(jasperPrint);
		} catch (JRException e) {
			log.error("unable to build receipt ", e.getMessage());
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("unable to disconnect from database ", e.getMessage());
			}
		}
		return contents;
	}
	
}
