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
			System.out.println(req.isEmpty());
			List<String> columns = (List<String>) req.get("columns");
			Map<String, Object> filter = (Map<String, Object>) req.get("filter");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
			EntityResult result;
			if (filter.get("bk_check_in") != null && filter.get("bk_check_out") != null) {
				String checkIn = (String) filter.get("bk_check_in");
				String checkOut = (String) filter.get("bk_check_out");
				filter.remove("bk_check_in");
				filter.remove("bk_check_out");
				Date startDate = formatter.parse(checkIn);
				Date endDate = formatter.parse(checkOut);
				filter.put(SQLStatementBuilder.ExtendedSQLConditionValuesProcessor.EXPRESSION_KEY,
						searchIfAvailable(BookingDao.ATTR_CHECK_IN, BookingDao.ATTR_CHECK_OUT, startDate, endDate));
				result = bookingService.avroomsQuery(filter, columns);
			} else {		
				result = bookingService.avroomsQuery(filter, columns);
				result.setCode(EntityResult.OPERATION_WRONG);
				result.setMessage("CHECK IN AND CHECK OUT FIELDS NEEDED");
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			EntityResult res = new EntityResultMapImpl();
			res.setCode(EntityResult.OPERATION_WRONG);
			return res;

		}
	}

	private BasicExpression searchIfAvailable(String checkIn, String checkOut, Date startDate, Date endDate) {
		BasicField in = new BasicField(checkIn);
		BasicField out = new BasicField(checkOut);
		BasicExpression bexp1 = new BasicExpression(in, BasicOperator.LESS_EQUAL_OP, startDate);
		BasicExpression bexp2 = new BasicExpression(out, BasicOperator.LESS_EQUAL_OP, endDate);
		return new BasicExpression(bexp1, BasicOperator.OR_OP, bexp2);
	}

	protected void processBasicExpression(String key, Map<Object, Object> keysValues, Object basicExpression) {
		this.processBasicExpression(key, keysValues, basicExpression, new HashMap<>());
	}
}