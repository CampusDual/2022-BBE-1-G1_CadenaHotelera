package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IHotelService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomPicService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IRoomTypeService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoomPicDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.util.remote.BytesBlock;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class builds the operations over the rooms table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
@Service("RoomPicService")
@Lazy
public class RoomPicService implements IRoomPicService {

	private Logger log;

	private Control control;
	private UserControl userControl;

	@Autowired
	private RoomPicDao roomPicDao;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;
	Validator validator;

	public RoomPicService() {
		super();
		this.control = new Control();
		this.validator = new Validator();
		this.userControl = new UserControl();
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public byte[] roompicQuery(Map<String, Object> keyMap, List<String> attrList) throws OntimizeJEERuntimeException {
		EntityResult result = daoHelper.query(roomPicDao, keyMap, attrList);
		BytesBlock image=(BytesBlock) result.getRecordValues(0).get("rp_image");				
		return image.getBytes();
	}

	@Override
	public EntityResult roompicInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		return daoHelper.insert(roomPicDao, attrMap);
	}

}
