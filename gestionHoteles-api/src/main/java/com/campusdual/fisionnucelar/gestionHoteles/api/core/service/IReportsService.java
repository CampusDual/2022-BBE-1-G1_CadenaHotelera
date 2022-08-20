package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;
import java.util.Date;


import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;


public interface IReportsService {

	public byte[] getReceipt(int bookingId,String currencyCode) throws OntimizeJEERuntimeException,IllegalArgumentException;
	byte[] getReceiptFromHistoric(int bookingId,String currencyCode) throws OntimizeJEERuntimeException,IllegalArgumentException;

	/**
	 * Generates a pdf financial report between two dates
	 * @exception RecordNotFoundException when there aren´t bookings between the requested dates
	 * @exception SQLException when there are problems connecting to the database
	 * @exception RException when thre are problems jenerating the receipt
	 * @param from to the dates to elaborate the financial report, it gives the bookings billed between these two dates
	 * @return the receipt in pdf formatted yb a byte array
	 * @since 8/8/22
	 */
	byte[] getFinancialReport(Date from, Date to,String currencyCode) throws OntimizeJEERuntimeException,IllegalArgumentException;
}
