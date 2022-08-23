package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import static com.campusdual.fisionnucelar.gestionHoteles.model.core.service.ReviewServiceData.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ReviewDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
	@Mock
	DefaultOntimizeDaoHelper daoHelper;

	@InjectMocks
	ReviewService reviewService;
	@Autowired
	ReviewDao reviewDao;

	@BeforeEach
	void setUp() {
		this.reviewService = new ReviewService();
		MockitoAnnotations.openMocks(this);
	}

	@Mock
	UserControl userControl;

	@Nested
	@DisplayName("Test for Reviews queries")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ReviewQuery {

		@Test
		@DisplayName("Obtain all data from reviews table filtered by hotel")
		void queryFilteredByHotel() {
			doReturn(getAllReviewData()).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			EntityResult entityResult = reviewService.reviewQuery(getHotelFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
		}

		@Test
		@DisplayName("Obtain all data from reviews table filtered by client")
		void queryFilteredByClient() {
			doReturn(getAllReviewData()).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			EntityResult entityResult = reviewService.reviewQuery(getClientFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			assertEquals(3, entityResult.calculateRecordNumber());
		}

		@Test
		@DisplayName("Try to search all the reviews withouth especifying an hotel")
		void search_without_hotel() throws NotAuthorizedException {
			Map<String, Object> filter = new HashMap<>();
			EntityResult entityResult = reviewService.reviewQuery(filter, new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("RV_HOTEL_OR_RV_CLIENT_REQUIRED", entityResult.getMessage());
		}

		@Test
		@DisplayName("Query without results")
		void query_without_results() {
			EntityResult queryResult = new EntityResultMapImpl();
			doReturn(queryResult).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			EntityResult entityResult = reviewService.reviewQuery(getHotelFilter(), new ArrayList<>());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("NO_RESULTS", entityResult.getMessage());
		}

		@Test
		@DisplayName("Fail when sends a string in a number field")
		void when_send_string_as_id_throws_exception() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("rv_hotel", "string");
			List<String> columns = new ArrayList<>();
			columns.add("id_review");
			columns.add("rv_hotel");
			when(daoHelper.query(any(), any(), any(), anyString())).thenThrow(BadSqlGrammarException.class);
			EntityResult entityResult = reviewService.reviewQuery(filter, columns);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INCORRECT_REQUEST", entityResult.getMessage());

		}
	}

	@Nested
	@DisplayName("Test for reviews inserts")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ReviewInsert {
		@Test
		@DisplayName("Insert a review successfully")
		void review_insert_success() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			List<String> columnList = Arrays.asList("id_review");
			doReturn(getGenericInsertResult()).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			when(daoHelper.insert(reviewDao, dataToInsert)).thenReturn(getGenericInsertResult());
			EntityResult resultSuccess = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, resultSuccess.getCode());
			assertEquals("SUCCESSFUL_INSERTION", resultSuccess.getMessage());

		}

		@Test
		@DisplayName("Trying to insert with a response")
		void review_insert_with_response() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			dataToInsert.put("rv_response", "Bien comentado");
			EntityResult resultSuccess = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());
			assertEquals("CLIENTS_CAN'T_RESPONSE_THEIR_OWN_RATINGS", resultSuccess.getMessage());
		}

		@Test
		@DisplayName("Trying to insert withouth completed bookings on the hotel")
		void review_insert_without_completed_bookings() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			doReturn(new EntityResultMapImpl()).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			EntityResult resultSuccess = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());
			assertEquals("THAT_CLIENT_DOESN'T_HAVE_COMPLETED_BOOKINGS_ON_THAT_HOTEL", resultSuccess.getMessage());

		}

		@Test
		@DisplayName("Trying to insert on a hotel with ratings from that client")
		void review_duplicated_comment() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			List<String> columnList = Arrays.asList("id_review");
			doReturn(getGenericInsertResult()).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			when(daoHelper.insert(reviewDao, dataToInsert)).thenThrow(DuplicateKeyException.class);
			EntityResult resultSuccess = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());

		}

		@Test
		@DisplayName("Trying to insert with an incorrect type")
		void review_incorrect_type() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			dataToInsert.put("rv_hotel", "sss");
			List<String> columnList = Arrays.asList("id_review");
			doReturn(getGenericInsertResult()).when(daoHelper).query(any(), anyMap(), anyList(), anyString());
			when(daoHelper.insert(reviewDao, dataToInsert)).thenThrow(BadSqlGrammarException.class);
			EntityResult resultSuccess = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());

		}

		@Test
		@DisplayName("Trying to insert with a rating higher than 5")
		void review_insert_rating_higher_than_5() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			dataToInsert.put("rv_rating", 6);
			EntityResult resultSuccess = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());
			assertEquals("RATING_MUST_BE_BETWEEN_1_AND_5", resultSuccess.getMessage());
		}

		@Test
		@DisplayName("Trying to insert with a rating below 1")
		void review_insert_rating_below_1() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			dataToInsert.put("rv_rating", 0);
			EntityResult resultSuccess = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, resultSuccess.getCode());
			assertEquals("RATING_MUST_BE_BETWEEN_1_AND_5", resultSuccess.getMessage());
		}

		@Test
		@DisplayName("Fail trying to insert without fields")
		void review_insert_without_fields() {
			Map<String, Object> dataToInsert = getGenericDataToInsert();
			dataToInsert.remove("rv_hotel");
			DataIntegrityViolationException DataIntegrityException = new DataIntegrityViolationException(
					"RunTimeMessage");
			when(daoHelper.insert(any(), anyMap())).thenThrow(DataIntegrityException);
			EntityResult entityResult = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).insert(any(), anyMap());
		}

		@Test
		@DisplayName("Fail trying to insert with no data")
		void review_insert_withouth_data() {
			EntityResult insertResult = new EntityResultMapImpl();
			Map<String, Object> dataToInsert = new HashMap<>();
			EntityResult entityResult = reviewService.reviewInsert(dataToInsert);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("EMPTY_REQUEST", entityResult.getMessage());
		}
	}

	@Nested
	@DisplayName("Test for add responses to a review")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ReviewUpdate {
		@Test
		@DisplayName("Add response successful")
		void add_response_success() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();

			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());
			doReturn(getReviewResult()).when(daoHelper).update(any(), any(), any());

			EntityResult entityResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals("SUCCESSFUL_RESPONSE", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		
		@Test
		@DisplayName("Trying to response with an invalid type")
		void invalid_type() {
			Map<String, Object> filter = getGenericFilter();
			filter.put("id_review", "sss");
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();
			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());	
			doThrow(BadSqlGrammarException.class).when(daoHelper).update(any(), any(), any());
			EntityResult entityResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			assertEquals("INVALID_TYPE", entityResult.getMessage());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		
		
		@Test
		@DisplayName("Fail trying to response a review that doesn´t exists")
		void response_review_doesnt_exists() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();
			doReturn(new EntityResultMapImpl()).when(daoHelper).query(any(), anyMap(), anyList());
			EntityResult entityResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals("ID_REVIEW_DOESN'T_EXISTS", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), anyMap(), anyList());

		}

		@Test
		@DisplayName("Fail trying to update without any fields")
		void review_update_without_any_fields() {
			Map<String, Object> filter = new HashMap<>();
			filter.put("id_review", 2);
			Map<String, Object> dataToUpdate = new HashMap<>();
			EntityResult updateResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("EMPTY_REQUEST", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update without id_review")
		void review_update_without_id_review() {
			Map<String, Object> filter = new HashMap<>();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();
			EntityResult updateResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("ID_REVIEW_REQUIRED", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to update anything except the response")
		void review_not_allowed_update() {
			Map<String, Object> filter = getGenericFilter();
			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());
			Map<String, Object> dataToUpdate = new HashMap<>();
			dataToUpdate.put("rv_hotel", 2);
			EntityResult updateResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals(EntityResult.OPERATION_WRONG, updateResult.getCode());
			assertEquals("MANAGERS_CAN_ONLYPUBLISH_REPONSES", updateResult.getMessage());
		}

		@Test
		@DisplayName("Fail trying to response a review from another hotel ")
		void not_authorized() throws NotAuthorizedException {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();
			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());
			doThrow(new NotAuthorizedException("MANAGERS_CAN_ONLY_RESPONSE_REVIEWS_FROM_THEIR_HOTEL")).when(userControl)
					.controlAccess(anyInt());
			EntityResult entityResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals("MANAGERS_CAN_ONLY_RESPONSE_REVIEWS_FROM_THEIR_HOTEL", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

		
		
		
		
		
		
		@Test
		@DisplayName("Try to add a too long response")
		void response_too_long() {
			Map<String, Object> filter = getGenericFilter();
			Map<String, Object> dataToUpdate = getGenericDataToUpdate();
			dataToUpdate.put("rv_response",
					"sacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacasc"
					+ "ascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsac"
					+ "nsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbs"
					+ "akcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablk"
					+ "jcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabb"
					+ "ajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbss"
					+ "acascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjk"
					+ "sablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsab"
					+ "bajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkks"
					+ "bssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsa"
					+ "bcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcb"
					+ "aslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsa"
					+ "bjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacasca"
					+ "scsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsab"
					+ "cbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbj"
					+ "ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "ascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsac"
					+ "nsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbs"
					+ "akcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablk"
					+ "jcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabb"
					+ "ajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbss"
					+ "acascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjk"
					+ "sablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsab"
					+ "bajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkks"
					+ "bssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsa"
					+ "bcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcb"
					+ "aslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsa"
					+ "bjabcsabkksbssacascascsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacasca"
					+ "scsacnsabcbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsab"
					+ "cbsakcbjksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbj"
					+ "ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"
					+ "+ \"ksablkjcbaslkjcbkjsabbajklsabjabcsabkksbssacascascsacnsabcbsakcbjksablkjc"				
					);

			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());
			
			DataIntegrityViolationException exception=new DataIntegrityViolationException("FIELD_TOO_LONG");
			
			doThrow(exception).when(daoHelper).update(any(), any(), any());

			EntityResult entityResult = reviewService.reviewresponseUpdate(dataToUpdate, filter);
			assertEquals("FIELD_TOO_LONG", entityResult.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, entityResult.getCode());
			verify(daoHelper).update(any(), anyMap(), anyMap());
			verify(daoHelper).query(any(), anyMap(), anyList());
		}

	}


	@Nested
	@DisplayName("Test for reviews deletes")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class ReviewDelete {
		@Test
		@DisplayName("Review delete successful")
		void review_delete_success() {
			Map<String, Object> filter = getGenericFilter();
			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());
			doReturn(getReviewResult()).when(daoHelper).delete(any(), any());
			
			EntityResult er=reviewService.reviewDelete(filter);		
			assertEquals("SUCCESSFUL_DELETE", er.getMessage());
			assertEquals(EntityResult.OPERATION_SUCCESSFUL, er.getCode());		
			
		}
			
		@Test
		@DisplayName("Trying to delete a review from another client")
		void not_authorized() throws NotAuthorizedException {
			Map<String, Object> filter = getGenericFilter();
			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());
			doThrow(new NotAuthorizedException("NOT_AUTHORIZED")).when(userControl).controlAccessClient(anyInt());	
			EntityResult er=reviewService.reviewDelete(filter);			
			assertEquals("CLIENTS_CAN'T_ONLY_DELETE_THEIR_OWN_REVIEWS", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());
						
		}
	
		@Test
		@DisplayName("Trying to delete without id_review")
		void delete_without_id_review() {
			Map<String, Object> filter = new HashMap<>();	
			EntityResult er=reviewService.reviewDelete(filter);			
			assertEquals("ID_REVIEW_REQUIRED", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());			
			
		}
		
		@Test
		@DisplayName("Trying to delete with an incorrect type")
		void incorrect_type()  {
			Map<String, Object> filter = getGenericFilter();
			filter.put("id_review", "sss");
			doReturn(getReviewResult()).when(daoHelper).query(any(), anyMap(), anyList());
			doThrow(BadSqlGrammarException.class).when(daoHelper).delete(any(), any());
			EntityResult er=reviewService.reviewDelete(filter);			
			assertEquals("INVALID_TYPE", er.getMessage());
			assertEquals(EntityResult.OPERATION_WRONG, er.getCode());
						
		}

		
	
	
}}
