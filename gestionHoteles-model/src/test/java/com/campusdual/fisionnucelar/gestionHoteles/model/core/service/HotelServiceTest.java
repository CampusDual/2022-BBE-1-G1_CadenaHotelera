package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.HotelTestData.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.GooglePlaces;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.PlacesResult;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;
import com.ontimize.jee.server.dao.ISQLQueryAdapter;

@ExtendWith(MockitoExtension.class)
public class HotelServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;


	GooglePlaces googleSearch;
	
	@InjectMocks
	HotelService hotelService;
	@Mock
	HotelDao hotelDao;
	

	@BeforeEach
	void setUp() {
		this.hotelService = new HotelService();
		MockitoAnnotations.openMocks(this);
	}

	@Nested
	@DisplayName("Test for Hotel queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class HotelQuery {

		@Test
		@DisplayName("Obtain all data from Hotels table")
		void when_queryOnlyWithAllColumns_return_allHotelData() {
			doReturn(getAllHotelData()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = hotelService.hotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = hotelService.hotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", "string");
			List<String> columns = new ArrayList<>();
			columns.add("htl_name");
			columns.add("htl_email");
			when(daoHelper.query(hotelDao, filter, columns)).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = hotelService.hotelQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Hotels table when ID is -> 2")
		void when_queryAllColumns_return_specificData() {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_HOTEL", 2);
				}
			};
			List<String> attrList = Arrays.asList("ID_HOTEL", "HTL_NAME");
			doReturn(getSpecificHotelData(keyMap, attrList)).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = hotelService.hotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(2, entityResult.getRecordValues(0).get(HotelDao.ATTR_ID));
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Obtain all data columns from Hotels table when ID not exist")
		void when_queryAllColumnsNotExisting_return_empty() {
			EntityResult result=new EntityResultMapImpl();
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(result);		
			result = hotelService.hotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, result.getCode());
			assertEquals("NO_RESULTS", result.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@ParameterizedTest(name = "Obtain data with ID -> {0}")
		@MethodSource("randomIDGenerator")
		@DisplayName("Obtain all data columns from HOTELS table when ID is random")
		void when_queryAllColumnsWithRandomValue_return_specificData(int random) {
			HashMap<String, Object> keyMap = new HashMap<>() {
				{
					put("ID_HOTEL", random);
				}
			};
			List<String> attrList = Arrays.asList("ID_HOTEL", "HTL_NAME");
			when(daoHelper.query(any(), anyMap(), anyList())).thenReturn(getSpecificHotelData(keyMap, attrList));
			EntityResult entityResult = hotelService.hotelQuery(new HashMap<>(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(1, entityResult.calculateRecordNumber());
			assertEquals(random, entityResult.getRecordValues(0).get(HotelDao.ATTR_ID));
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
	@DisplayName("Test for Hotel inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class HotelInsert {
		@Test
		@DisplayName("Insert a hotel successfully")
		void hotel_insert_success() {
			Map<String, Object> dataToInsert = getDataToInsert();
			EntityResult er = new EntityResultMapImpl(Arrays.asList("ID_HOTEL"));
			er.addRecord(new HashMap<String, Object>() {
				{
					put("ID_HOTEL", 2);
				}
			});
			er.setCode(EntityResult.OPERATION_SUCCESSFUL);
			HashMap<String, Object> keyMap = new HashMap<>();
			keyMap.put("ID_HOTEL", 2);
			when(daoHelper.insert(hotelDao, dataToInsert)).thenReturn(er);
			EntityResult entityResult = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			int recordIndex = entityResult.getRecordIndex(keyMap);
			assertEquals(2, entityResult.getRecordValues(recordIndex).get("ID_HOTEL"));
			verify(daoHelper).insert(hotelDao, dataToInsert);

		}

		@Test
		@DisplayName("Fail trying to insert duplicated email")
		void hotel_insert_duplicated_mail() {
			Map<String, Object> dataToInsert = getDataToInsert();
			List<String> columnList = Arrays.asList("ID_HOTEL");
			EntityResult insertResult = new EntityResultMapImpl(columnList);
			insertResult.addRecord(new HashMap<String, Object>() {
				{
					put("ID_HOTEL", 2);
				}
			});
			when(daoHelper.insert(hotelDao, dataToInsert)).thenReturn(insertResult);
			EntityResult resultSuccess = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCESSFUL_INSERTION", resultSuccess.getMessage());
			when(daoHelper.insert(hotelDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultFail = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultFail.getCode());
			assertEquals("HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS", resultFail.getMessage());
			verify(daoHelper, times(2)).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert without hotel name or email fields")
		void hotel_insert_without_mail_or_hotel_name() {
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("htl_address", "Calle Uria, 98");
			dataToInsert.put("htl_phone", "985446789");
			dataToInsert.put("htl_country_code", 34);
			dataToInsert.put("htl_phone", "985446789");
			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(hotelDao, dataToInsert)).thenThrow(DataIntegrityException);
			EntityResult entityResult = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert with invalid email")
		void hotel_insert_with_invalid_email() {
			EntityResult insertResult = new EntityResultMapImpl();
			insertResult.addRecord(new HashMap<String, Object>() {
				{
					put("ID_HOTEL", 2);
				}
			});
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("htl_name", "FN Oviedo");
			dataToInsert.put("htl_email", "fnoviedofnhotels.com");
			dataToInsert.put("htl_address", "Calle Uria, 98");
			dataToInsert.put("htl_phone", "985446789");
			EntityResult entityResult = hotelService.hotelInsert(dataToInsert);
			// assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INVALID_EMAIL", entityResult.getMessage());
		}

		@Test
		@DisplayName("Fail due invalid phone")
		void hotel_insert_fails_due_invalid_phone() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("htl_name", "FN Oviedo");
			dataToInsert.put("htl_email", "fnoviedo@fnhotels.com");
			dataToInsert.put("htl_address", "Calle Uria, 98");
			dataToInsert.put("htl_phone", "9854467");
			dataToInsert.put("htl_country_code", 34);
			EntityResult entityResult = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INVALID_PHONE", entityResult.getMessage());
		}
		@Test
		@DisplayName("Fail due empty phone field")
		void hotel_insert_withouth_phone() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("htl_name", "FN Oviedo");
			dataToInsert.put("htl_email", "fnoviedo@fnhotels.com");
			dataToInsert.put("htl_address", "Calle Uria, 98");
			dataToInsert.put("htl_country_code", 34);
			EntityResult entityResult = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("HTL_PHONE_REQUIRED", entityResult.getMessage());
		}
		@Test
		@DisplayName("Fail due empty country code field")
		void hotel_insert_withouth_country_code() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("htl_name", "FN Oviedo");
			dataToInsert.put("htl_email", "fnoviedo@fnhotels.com");
			dataToInsert.put("htl_address", "Calle Uria, 98");
			dataToInsert.put("htl_phone","981458989");
			EntityResult entityResult = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("HTL_COUNTRY_CODE_REQUIRED", entityResult.getMessage());
		}
		@Test
		@DisplayName("Fail due string as country code")
		void hotel_insert_with_country_code_as_string() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			dataToInsert.put("htl_name", "FN Oviedo");
			dataToInsert.put("htl_email", "fnoviedo@fnhotels.com");
			dataToInsert.put("htl_address", "Calle Uria, 98");
			dataToInsert.put("htl_phone", "981458787");
			dataToInsert.put("htl_country_code", "Calle Uria, 98");
			EntityResult entityResult = hotelService.hotelInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INVALID_PHONE", entityResult.getMessage());
		}
	}

	@Nested
	@DisplayName("Test for Hotel updates")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class HotelUpdate {
		@Test
		@DisplayName("hotel update succesful")
		void hotel_update_success() {
			// given
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 32);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("htl_name", "FN Oviedo");
			dataToUpdate.put("htl_email", "fnoviedo@fnhotels.com");
			dataToUpdate.put("htl_address", "Calle Uria, 98");
			dataToUpdate.put("htl_phone", "985446789");
			dataToUpdate.put("htl_country_code", 34);
			List<String> attrList = new ArrayList<>();
			attrList.add("id_hotel");
			EntityResult er = new EntityResultMapImpl();

			EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_HOTEL", "HTL_NAME"));
			// when
			when(daoHelper.update(hotelDao, dataToUpdate, filter)).thenReturn(er);
			when(daoHelper.query(hotelDao, filter, attrList)).thenReturn(queryResult);
			// then
			EntityResult entityResult = hotelService.hotelUpdate(dataToUpdate, filter);
			assertEquals("SUCESSFUL_UPDATE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail trying to update an hotel with an existing name or email")
		void hotel_fail_update_with_duplicated_hotel_name_or_email() {
			EntityResult er = new EntityResultMapImpl();
			er.setCode(EntityResult.OPERATION_WRONG);
			er.setMessage("HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS");
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 32);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("htl_name", "FN Oviedo");
			dataToUpdate.put("htl_email", "fnoviedo@fnhotels.com");
			List<String> attrList = new ArrayList<>();
			attrList.add("id_hotel");
			EntityResult queryResult = new EntityResultMapImpl(Arrays.asList("ID_HOTEL", "HTL_NAME"));
			er.addRecord(new HashMap<String, Object>() {
				{
					put("ID_HOTEL", 2);
					put("HTL_NAME", "FN Vigo");
				}
			});
			er.setCode(EntityResult.OPERATION_SUCCESSFUL);
			when(daoHelper.query(hotelDao, filter, attrList)).thenReturn(queryResult);
			when(daoHelper.update(hotelDao, dataToUpdate, filter)).thenThrow(DuplicateKeyException.class);
			EntityResult entityResult = hotelService.hotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("HOTEL_NAME_OR_EMAIL_ALREADY_EXISTS", entityResult.getMessage());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail trying to update an hotel that doesn´t exists")
		void update_hotel_doesnt_exists() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 222);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("htl_name", "FN Oviedo");
			dataToUpdate.put("htl_email", "fnoviedo@fnhotels.com");
			dataToUpdate.put("htl_address", "Calle Uria, 98");
			dataToUpdate.put("htl_phone", "985446789");
			List<String> attrList = new ArrayList<>();
			attrList.add("id_hotel");
			EntityResult er = new EntityResultMapImpl();
			er.setCode(EntityResult.OPERATION_WRONG);
			er.setMessage("HOTEL_DOESN'T_EXISTS");
			EntityResult queryResult = new EntityResultMapImpl();
			when(daoHelper.query(hotelDao, filter, attrList)).thenReturn(queryResult);
			EntityResult entityResult = hotelService.hotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("HOTEL_DOESN'T_EXISTS", entityResult.getMessage());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		@Test
		@DisplayName("Fail trying to update without any fields")
		void hotel_update_without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = hotelService.hotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("EMPTY_REQUEST", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update without id_hotel")
		void hotel_update_without_id_hotel() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("htl_name", "FN Oviedo");
			dataToUpdate.put("htl_email", "fnoviedo@fnhotels.com");
			dataToUpdate.put("htl_address", "Calle Uria, 98");
			dataToUpdate.put("htl_phone", "985446789");
			EntityResult updateResult = hotelService.hotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_HOTEL_REQUIRED", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update with invalid email")
		void hotel_insert_with_invalid_email() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 222);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("htl_name", "FN Oviedo");
			dataToUpdate.put("htl_email", "fnoviedofnhotels.com");
			dataToUpdate.put("htl_address", "Calle Uria, 98");
			dataToUpdate.put("htl_phone", "985446789");
			List<String> attrList = new ArrayList<>();
			attrList.add("id_hotel");
			EntityResult queryResult = new EntityResultMapImpl(attrList);
			queryResult.addRecord(new HashMap<String, Object>() {
				{
					put("ID_HOTEL", 2);
				}
			});
			when(daoHelper.query(hotelDao, filter, attrList)).thenReturn(queryResult);
			EntityResult entityResult = hotelService.hotelUpdate(dataToUpdate, filter);
			// assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INVALID_EMAIL", entityResult.getMessage());
		}
		@Test
		@DisplayName("Fail trying to update with invalid phone")
		void hotel_insert_with_invalid_phone() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 222);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("htl_name", "FN Oviedo");
			dataToUpdate.put("htl_email", "fnoviedo@fnhotels.com");
			dataToUpdate.put("htl_address", "Calle Uria, 98");
			dataToUpdate.put("htl_phone", "9854");
			dataToUpdate.put("htl_country_code", 34);
			List<String> attrList = new ArrayList<>();
			attrList.add("id_hotel");
			EntityResult queryResult = new EntityResultMapImpl(attrList);
			queryResult.addRecord(new HashMap<String, Object>() {
				{
					put("ID_HOTEL", 2);
				}
			});
			when(daoHelper.query(hotelDao, filter, attrList)).thenReturn(queryResult);
			EntityResult entityResult = hotelService.hotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INVALID_PHONE", entityResult.getMessage());
		}
		@Test
		@DisplayName("Fail trying to update with a string as country code")
		void hotel_insert_with_string_as_country_code() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 222);
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("htl_name", "FN Oviedo");
			dataToUpdate.put("htl_email", "fnoviedo@fnhotels.com");
			dataToUpdate.put("htl_address", "Calle Uria, 98");
			dataToUpdate.put("htl_phone", "9854");
			dataToUpdate.put("htl_country_code", "ssss");
			List<String> attrList = new ArrayList<>();
			attrList.add("id_hotel");
			EntityResult queryResult = new EntityResultMapImpl(attrList);
			queryResult.addRecord(new HashMap<String, Object>() {
				{
					put("ID_HOTEL", 2);
				}
			});
			when(daoHelper.query(hotelDao, filter, attrList)).thenReturn(queryResult);
			EntityResult entityResult = hotelService.hotelUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INVALID_PHONE", entityResult.getMessage());
		}
	}

	@Nested
	@DisplayName("Test for Hotel searchs by service")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class HotelSearchByservice {
		@Test
		@DisplayName("Search hotels by services succesful")
		void search_success() {
			List<Integer> services = new ArrayList<>();
			services.add(1);
			services.add(2);
			services.add(3);
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 2);
			filter.put("services", services);

			EntityResult searchResult = getAllHotelData();
			when(daoHelper.query(any(), any(), any(), anyString(), any(ISQLQueryAdapter.class)))
					.thenReturn(searchResult);
			EntityResult hotelByServices = hotelService.hotelsbyservicesQuery(filter, Arrays.asList("id_hotel"));
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, hotelByServices.getCode());
			assertEquals(3, hotelByServices.calculateRecordNumber());
		}

		@Test
		@DisplayName("Search hotels by services with no results")
		void no_results() {
			List<Integer> services = new ArrayList<>();
			services.add(1);
			services.add(2);
			services.add(3);
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 2);
			filter.put("services", services);

			EntityResult searchResult = new EntityResultMapImpl();
			when(daoHelper.query(any(), any(), any(), anyString(), any(ISQLQueryAdapter.class)))
					.thenReturn(searchResult);
			EntityResult hotelByServices = hotelService.hotelsbyservicesQuery(filter, Arrays.asList("id_hotel"));
			assertEquals(EntityResult.OPERATION_WRONG, searchResult.getCode());
			assertEquals("NO_RESULTS", hotelByServices.getMessage());
		}

		@Test
		@DisplayName("Search hotels by services withouth introducing services")
		void search_without_fields() {
			Map<String, Object> filter = new HashMap<>();
			EntityResult hotelByServices = hotelService.hotelsbyservicesQuery(filter, Arrays.asList("id_hotel"));
			assertEquals(EntityResult.OPERATION_WRONG, hotelByServices.getCode());
			assertEquals("SERVICES_REQUIRED", hotelByServices.getMessage());
		}

		@Test
		@DisplayName("Search hotels by services introducing an incorrect type")
		void search_with_incorrect_type() {
			Integer services = 3;
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_hotel", 2);
			filter.put("services", services);
			EntityResult hotelByServices = hotelService.hotelsbyservicesQuery(filter, Arrays.asList("id_hotel"));
			assertEquals(EntityResult.OPERATION_WRONG, hotelByServices.getCode());
			assertEquals("INCORRECT_REQUEST", hotelByServices.getMessage());
		}
	}

	@Nested
	@DisplayName("Test for Hotel searchs by location")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class SearchByLocation {
		@Test
		@DisplayName("Search hotels by location succesful")
		void search_success() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("longitude", 43.362343);
			filter.put("latitude", -8.41154);
			filter.put("radius", 200);
			EntityResult searchResult = getHotelsWithLocation();
			when(daoHelper.query(any(), any(), any())).thenReturn(searchResult);

			EntityResult hotelByLocation = hotelService.searchbylocationQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, hotelByLocation.getCode());
			assertEquals(3, hotelByLocation.calculateRecordNumber());
		}

		@Test
		@DisplayName("Search hotels by services with no results")
		void no_results() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("longitude", 43.362343);
			filter.put("latitude", -8.41154);
			filter.put("radius", 1);
			EntityResult searchResult = getHotelsWithLocation();
			when(daoHelper.query(any(), any(), any())).thenReturn(searchResult);
			EntityResult hotelByLocation = hotelService.searchbylocationQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByLocation.getCode());
			assertEquals(0, hotelByLocation.calculateRecordNumber());
			assertEquals("NO_HOTELS_IN_REQUESTED_AREA", hotelByLocation.getMessage());
		}

		@Test
		@DisplayName("Search hotels by location without introducing latitude,longitude or radius")
		void search_without_fields() {
			Map<String, Object> filter = new HashMap<>();
			EntityResult hotelByLocation = hotelService.searchbylocationQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByLocation.getCode());
			assertEquals("LONGITUDE_LATITUDE_AND_RADIUS_REQUIRED", hotelByLocation.getMessage());
		}

		@Test
		@DisplayName("Search hotels by location introducing an incorrect type")
		void search_with_incorrect_type() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("longitude", "ssss");
			filter.put("latitude","ssss");
			filter.put("radius", 1);
			EntityResult searchResult = getHotelsWithLocation();
			when(daoHelper.query(any(), any(), any())).thenReturn(searchResult);
			EntityResult hotelByLocation = hotelService.searchbylocationQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByLocation.getCode());
			assertEquals(0, hotelByLocation.calculateRecordNumber());
			assertEquals("INCORRECT_TYPE", hotelByLocation.getMessage());
		}
	}

	
	@Nested
	@DisplayName("Test for Hotel searchs by city")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class SearchByCity {
		@Test
		@DisplayName("Search hotels by city succesful")
		void search_success() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("location", "vigo");
			filter.put("radius", 200);			
			EntityResult searchResult = getHotelsWithLocation2();	
						
			when(daoHelper.query(any(), any(), any())).thenReturn(searchResult);						
			
			EntityResult hotelByCity = hotelService.searchbycityQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, hotelByCity.getCode());
			assertEquals(3, hotelByCity.calculateRecordNumber());
		}
		
		
		@Test
		@DisplayName("Search hotels by city with no results")
		void no_results() {	
			Map<String, Object> filter = new HashMap<>();
			filter.put("location", "lisboa");
			filter.put("radius", 200);		
			EntityResult searchResult = getHotelsWithLocation2();	
						
			when(daoHelper.query(any(), any(), any())).thenReturn(searchResult);						
			
			EntityResult hotelByCity = hotelService.searchbycityQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByCity.getCode());
			assertEquals(0, hotelByCity.calculateRecordNumber());
			assertEquals("NO_HOTELS_IN_REQUESTED_AREA", hotelByCity.getMessage());				
		}
		
		
		@Test
		@DisplayName("Search hotels by city with no coincidences")
		void no_results_without_region() {	
			Map<String, Object> filter = new HashMap<>();
			filter.put("location", "springfield");
			filter.put("radius", 200);
				
			EntityResult hotelByCity = hotelService.searchbycityQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByCity.getCode());
			assertEquals(0, hotelByCity.calculateRecordNumber());
			assertEquals("NO_RESULTS_TRY_ADDING_REGION", hotelByCity.getMessage());				
		}
		
		@Test
		@DisplayName("Search hotels by city with no coincidences introducing region")
		void no_results_with_region() {	
			Map<String, Object> filter = new HashMap<>();
			filter.put("location", "springfield");
			filter.put("radius", 200);
			filter.put("region", "portugal");	
			
			EntityResult hotelByCity = hotelService.searchbycityQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByCity.getCode());
			assertEquals(0, hotelByCity.calculateRecordNumber());
			assertEquals("NO_RESULTS", hotelByCity.getMessage());				
		}
		
		
		@Test
		@DisplayName("Search hotels by city with too many coincidences")
		void too_many_coincidences() {	
			Map<String, Object> filter = new HashMap<>();
			filter.put("location", "aranda");
			filter.put("radius", 200);
			
			EntityResult hotelByCity = hotelService.searchbycityQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByCity.getCode());
			assertEquals(0, hotelByCity.calculateRecordNumber());
			assertEquals("TOO_MANY_COINCIDENCES_TRY_TO_ADD_REGION_OR_COUNTRY", hotelByCity.getMessage());				
		}
			
		@Test
		@DisplayName("Search hotels by city without introducing location or radius")
		void search_without_fields() {
			Map<String, Object> filter = new HashMap<>();
			EntityResult hotelByLocation = hotelService.searchbycityQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByLocation.getCode());
			assertEquals("LOCATION_AND_RADIUS_REQUIRED", hotelByLocation.getMessage());
		}

		@Test
		@DisplayName("Search hotels by city introducing an incorrect type")
		void search_with_incorrect_type() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("location", 1);
			filter.put("radius", "sss");
			EntityResult searchResult = getHotelsWithLocation();
			EntityResult hotelByLocation = hotelService.searchbycityQuery(filter,new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, hotelByLocation.getCode());
			assertEquals(0, hotelByLocation.calculateRecordNumber());
			assertEquals("INVALID_TYPE", hotelByLocation.getMessage());
		}

	}
	

	
	
	
	
	
	
	
	
	
	
	
	
}
