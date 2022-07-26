package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.BookingExtraData.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.BookingExtraDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@ExtendWith(MockitoExtension.class)
public class BookingExtraServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	BookingExtraService bookingExtraService;
	@Autowired
	BookingExtraDao bookingExtraDao;

	@BeforeEach
	void setUp() {
		this.bookingExtraService = new BookingExtraService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Test for BookingExtra queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class BookingExtraQuery {

		@Test
		@DisplayName("Obtain all data from BookingExtra table")
		void testBookingExtraQueryAllData() {
			doReturn(getAllBookingExtraData()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = bookingExtraService.bookingextraQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = bookingExtraService.bookingextraQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_service", "string");
			List<String> columns = new ArrayList<>();
			columns.add("sv_name");
			columns.add("sv_description");
			when(daoHelper.query(bookingExtraDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = bookingExtraService.bookingextraQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from BookingExtra table when ID is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_BOOKING_EXTRA", 2);
				}
			};
			List<String> attrList = Arrays.asList("ID_BOOKING_EXTRA", "BKE_BOOKING");
			doReturn(getSpecificBookingExtraData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = bookingExtraService.bookingextraQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get(BookingExtraDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Services table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_BOOKING_EXTRA", 5);
				}
			};
			List<String> attrList = Arrays.asList("ID_BOOKING_EXTRA", "BKE_BOOKING", "BKE_NAME");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificBookingExtraData(keyMap, attrList));
			EntityResult entityResult = bookingExtraService.bookingextraQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(0, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@ParameterizedTest(name = "Obtain data with ID -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from Services table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_BOOKING_EXTRA", random);
				}
			};
			List<String> attrList = Arrays.asList("ID_BOOKING_EXTRA", "BKE_BOOKING");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificBookingExtraData(keyMap, attrList));
			EntityResult entityResult = bookingExtraService.bookingextraQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get(BookingExtraDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		List<Integer> randomIDGenerator() {
			List<Integer> list = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				list.add(ThreadLocalRandom.current().nextInt(0, 3));
			}
			return list;
		}
	}

	@Nested
	@DisplayName("Test for BookingExtra inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class BookingExtraInsert {
		@Test
		@DisplayName("Insert a BookingExtra successfully")
		void bookingExtra_insert_success() {
			Map<String, Object> dataToInsert = getGenericDataToInsertOrUpdate();
			EntityResult er = getGenericInsertResult();
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("ID_BOOKING_EXTRA", 2);
			when(daoHelper.insert(bookingExtraDao, dataToInsert)).thenReturn(er);
			EntityResult entityResult = bookingExtraService.bookingextraInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_BOOKING_EXTRA"));
			verify(daoHelper).insert(bookingExtraDao, dataToInsert);

		}

		@Test
		@DisplayName("Fail trying to insert without not null field")
		void bookingextra_insert_without_name() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("ID_BOOKING_EXTRA", 2);
			dataToInsert.put("BKE_BOOKING", 1);
			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(any(), anyMap())).thenThrow(DataIntegrityException);

			EntityResult entityResult = bookingExtraService.bookingextraInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert with no data")
		void service_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			when(daoHelper.insert(bookingExtraDao, dataToInsert)).thenReturn(insertResult);
			EntityResult entityResult = bookingExtraService.bookingextraInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("FIELDS_REQUIRED", entityResult.getMessage());
		}
	}

	@Nested
	@DisplayName("Test for Mark extras as enjoyed ")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class MarkExtraEnjoyedUpdate {
		@Test
		@DisplayName("Mark an extra as enjoyed successfully")
		void mark_as_enjoyed_success() {

			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> quantity = new HashMap<>();
			quantity.put("quantity", 3);

			EntityResult queryResult = new EntityResultMapImpl();
			queryResult.addRecord(new HashMap<String, Object>() {
				{
					put("bke_quantity", 6);
					put("bke_enjoyed", 3);
				}
			});

			when(daoHelper.update(any(), any(), any())).thenReturn(queryResult);
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);

			EntityResult entityResult = bookingExtraService.markextraenjoyedUpdate(quantity, filter);
			assertEquals("SUCCESSFULLY_UPDATED", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());

		}

		@Test
		@DisplayName("Inssuficient extras to mark as enjoyed")
		void inssuficient_extras() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> quantity = new HashMap<>();
			quantity.put("quantity", 3);

			EntityResult queryResult = new EntityResultMapImpl();
			queryResult.addRecord(new HashMap<String, Object>() {
				{
					put("bke_quantity", 4);
					put("bke_enjoyed", 3);
				}
			});
			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);

			EntityResult entityResult = bookingExtraService.markextraenjoyedUpdate(quantity, filter);
			assertEquals("NOT_ENOUGH_PENDING_EXTRAS", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());

		}

		@Test
		@DisplayName("Try to mark as enjoyed an unexisting booking_extra")
		void unexisting_bookingExtra() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToInsertOrUpdate();
			EntityResult queryResult = new EntityResultMapImpl();

			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			EntityResult entityResult = bookingExtraService.markextraenjoyedUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("BOOKING_EXTRA_DOESN'T_EXISTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}
		
		@Test
		@DisplayName("Fail trying to mark as enjoyed without id_booking_extra")
		void without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = bookingExtraService.markextraenjoyedUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_BOOKING_EXTRA_REQUIRED", updateResult.getMessage());
		}
		
		@Test
		@DisplayName("Fail trying to mark as enjoyed without quantity")
		void without_quantity() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = new HashMap<>();			
			EntityResult queryResult = getGenericQueryResult();

			when(daoHelper.query(any(), any(), any())).thenReturn(queryResult);
			
			EntityResult updateResult = bookingExtraService.markextraenjoyedUpdate(dataToUpdate,filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("QUANTITY_FIELD_REQUIRED", updateResult.getMessage());
		}
		
		
		
		
		
	}

}