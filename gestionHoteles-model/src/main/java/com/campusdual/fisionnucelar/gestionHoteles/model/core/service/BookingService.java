package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingDao;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@Service("BookingService")
@Lazy
public class BookingService implements IBookingService {

	@Autowired
	private BookingDao bookingDao;
	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	@Override
	public EntityResult bookingQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = this.daoHelper.query(this.bookingDao, keyMap, attrList);
		if (searchResult!=null && searchResult.getCode()==EntityResult.OPERATION_WRONG) {
			searchResult.setMessage("ERROR_WHILE_SEARCHING");
		}
		return searchResult;
	}
	

	@Override
	public EntityResult bookingInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = this.daoHelper.insert(this.bookingDao, attrMap);
		if (insertResult!=null && insertResult.getCode()==EntityResult.OPERATION_WRONG) {
			insertResult.setMessage("ERROR_WHILE_INSERTING");
		}else {
			insertResult.setMessage("SUCCESSFUL_INSERTION");
		}
		return insertResult;
	}

	@Override
	public EntityResult bookingUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = this.daoHelper.update(this.bookingDao, attrMap, keyMap);
		if (updateResult!=null && updateResult.getCode()==EntityResult.OPERATION_WRONG) {
			updateResult.setMessage("ERROR_WHILE_UPDATING");
		}else {
			updateResult.setMessage("SUCCESSFUL_UPDATE");
		}
		return updateResult;
	}

	@Override
	public EntityResult bookingDelete(Map<String, Object> keyMap) throws OntimizeJEERuntimeException {
		EntityResult deleteResult = this.daoHelper.delete(this.bookingDao, keyMap);
		if (deleteResult!=null && deleteResult.getCode()==EntityResult.OPERATION_WRONG) {
			deleteResult.setMessage("ERROR_WHILE_DELETING");
		}else {
			deleteResult.setMessage("SUCCESSFUL_DELETE");
		}
		return deleteResult;
	}
}
