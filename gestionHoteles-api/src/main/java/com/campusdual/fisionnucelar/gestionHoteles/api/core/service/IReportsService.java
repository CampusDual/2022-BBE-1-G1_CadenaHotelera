package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;
import java.util.Date;


import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;


public interface IReportsService {

	public byte[] getReceipt(int bookingId)
			throws OntimizeJEERuntimeException;
	
	public byte[] getFinancialReport(Date from, Date to) throws OntimizeJEERuntimeException;
	byte[] getReceiptFromHistoric(int bookingId) throws OntimizeJEERuntimeException;
}
