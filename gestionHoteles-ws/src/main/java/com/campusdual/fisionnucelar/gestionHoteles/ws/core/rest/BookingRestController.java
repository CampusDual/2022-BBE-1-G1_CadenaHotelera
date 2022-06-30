package com.campusdual.fisionnucelar.gestionHoteles.ws.core.rest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IBookingService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingDao;
import com.ontimize.jee.common.db.SQLStatementBuilder;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicExpression;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicField;
import com.ontimize.jee.common.db.SQLStatementBuilder.BasicOperator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.tools.EntityResultTools;
import com.ontimize.jee.server.rest.ORestController;

@RestController
@RequestMapping("/bookings")
public class BookingRestController extends ORestController<IBookingService> {

	@Autowired
	private IBookingService bookingService;

	@Override
	public IBookingService getService() {
		return this.bookingService;
	}

	@RequestMapping(value = "room/search", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public EntityResult roomAvaliableSearch(@RequestBody Map<String, Object> req) {
		try {
			List<String> columns = (List<String>) req.get("columns");
			Map<String, Object> filter = (Map<String, Object>) req.get("filter");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
			EntityResult result;
			EntityResult resultsByHotel = new EntityResultMapImpl();

			if (filter.get("bk_check_in") != null && filter.get("bk_check_out") != null
					&& filter.get("id_hotel") != null) {
				String newCheckIn = (String) filter.get("bk_check_in");
				String newCheckOut = (String) filter.get("bk_check_out");
				filter.remove("bk_check_in");
				filter.remove("bk_check_out");

				Date startDate = formatter.parse(newCheckIn);
				Date endDate = formatter.parse(newCheckOut);

				filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
						searchIfAvailable(startDate, endDate));
				result = bookingService.avroomsQuery(filter, columns);

				Map<String, Object> hotelFilter = new HashMap<>();
				hotelFilter.put("id_hotel", filter.get("id_hotel"));
				resultsByHotel = EntityResultTools.dofilter(result, hotelFilter);
						
			} else {
				resultsByHotel = bookingService.avroomsQuery(filter, columns);
				resultsByHotel.setCode(EntityResult.OPERATION_WRONG);
				resultsByHotel.setMessage("ID_HOTEL, CHECK IN AND CHECK OUT FIELDS NEEDED");
			}
			
//Faltaría devolver un mensaje si no encuentra habitaciones			
//			if (resultsByHotel.get("id_room")=="")
//				resultsByHotel.setMessage("THERE ARE NOT ROOMS AVAILABLE");

			return resultsByHotel;
		} catch (Exception e) {
			e.printStackTrace();
			EntityResult res = new EntityResultMapImpl();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;
		}
	}

	private BasicExpression searchIfAvailable(Date userCheckIn, Date userCheckOut) {

		BasicField bdCheckIn = new BasicField(BookingDao.ATTR_CHECK_IN);
		BasicField bdCheckOut = new BasicField(BookingDao.ATTR_CHECK_OUT);

		//Calcula que la fecha de entrada no esté incluida en una reserva existente
		BasicExpression b1 = new BasicExpression(bdCheckIn, BasicOperator.LESS_EQUAL_OP, userCheckIn);
		BasicExpression b2 = new BasicExpression(bdCheckOut, BasicOperator.MORE_OP, userCheckIn);
		BasicExpression rule1 = new BasicExpression(b1, BasicOperator.AND_OP, b2);
				
		//Calcula que la fecha de salida no esté incluida en una reserva existente
		BasicExpression b3 = new BasicExpression(bdCheckIn, BasicOperator.LESS_OP, userCheckOut);
		BasicExpression b4 = new BasicExpression(bdCheckOut, BasicOperator.MORE_EQUAL_OP, userCheckOut);
		BasicExpression rule2 = new BasicExpression(b3, BasicOperator.AND_OP, b4);

		//Calcula que la nueva reserva no incluya una reserva existente
		BasicExpression b5 = new BasicExpression(bdCheckIn, BasicOperator.MORE_EQUAL_OP, userCheckIn);
		BasicExpression b6 = new BasicExpression(bdCheckOut, BasicOperator.LESS_EQUAL_OP, userCheckOut);
		BasicExpression rule3 = new BasicExpression(b5, BasicOperator.AND_OP, b6);

		//Une las tres reglas anteriores para filtrar las reservas inválidas
		BasicExpression rule1_2 = new BasicExpression(rule1, BasicOperator.OR_OP, rule2);
		BasicExpression rule1_2_3 = new BasicExpression(rule1_2, BasicOperator.OR_OP, rule3);

		return rule1_2_3;
	}

	protected void processBasicExpression(String key, Map<Object, Object> keysValues, Object basicExpression) {
		this.processBasicExpression(key, keysValues, basicExpression, new HashMap<>());
	}
}