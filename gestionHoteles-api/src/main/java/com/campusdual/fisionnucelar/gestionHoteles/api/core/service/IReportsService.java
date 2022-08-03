package com.campusdual.fisionnucelar.gestionHoteles.api.core.service;

import java.io.ByteArrayOutputStream;

import java.util.List;
import java.util.Map;

import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.util.remote.BytesBlock;


public interface IReportsService {

	public byte[] getReceipt(int bookingId)
			throws OntimizeJEERuntimeException;
}
