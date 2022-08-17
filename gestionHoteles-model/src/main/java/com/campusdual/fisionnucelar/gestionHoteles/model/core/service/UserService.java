package com.campusdual.fisionnucelar.gestionHoteles.model.core.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IClientService;
import com.campusdual.fisionnucelar.gestionHoteles.api.core.service.IUserService;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.ClientDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.HotelDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.RoleDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.UserDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.dao.UserRoleDao;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.AllFieldsRequiredException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.EmptyRequestException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidRolException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.RecordNotFoundException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Control;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.UserControl;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.Validator;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.dto.EntityResultMapImpl;
import com.ontimize.jee.common.exceptions.OntimizeJEERuntimeException;
import com.ontimize.jee.common.security.PermissionsProviderSecured;
import com.ontimize.jee.common.services.user.UserInformation;
import com.ontimize.jee.server.dao.DefaultOntimizeDaoHelper;

/**
 * This class builds the operations over the user table
 * 
 * @since 27/06/2022
 * @version 1.0
 *
 */
@Lazy
@Service("UserService")
public class UserService implements IUserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private UserRoleDao userroleDao;

	@Autowired
	private ClientDao clientDao;

	@Autowired
	private HotelDao hotelDao;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private IClientService clientService;

	@Autowired
	private DefaultOntimizeDaoHelper daoHelper;

	private Control control;
	private Logger log;

	private Validator dataValidator;
	private UserControl userControl;

	public UserService() {
		super();
		this.control = new Control();
		this.log = LoggerFactory.getLogger(this.getClass());
		this.dataValidator = new Validator();
		this.userControl = new UserControl();
	}

	public void loginQuery(Map<?, ?> key, List<?> attr) {
	}

	@Override
	@Secured({ "admin" })
	public EntityResult userQuery(Map<String, Object> keyMap, List<String> attrList)
			throws OntimizeJEERuntimeException {
		EntityResult searchResult = new EntityResultMapImpl();
		try {
			searchResult = this.daoHelper.query(this.userDao, keyMap, attrList, "user_data");
			control.checkResults(searchResult);
		} catch (NoResultsException e) {
			log.error("unable to retrieve a hotel. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, e.getMessage());
		} catch (BadSqlGrammarException e) {
			log.error("unable to retrieve a hotel. Request : {} {} ", keyMap, attrList, e);
			control.setErrorMessage(searchResult, "INCORRECT_REQUEST");
			e.printStackTrace();
		}
		return searchResult;
	}

	@Override
	@Transactional
	@Secured({ "admin", "hotel_manager", "client" })
	public EntityResult userUpdate(Map<String, Object> attrMap, Map<String, Object> keyMap)
			throws OntimizeJEERuntimeException {
		EntityResult updateResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			if (attrMap.get("identifier") != null || attrMap.get("user_") != null || attrMap.get("userblocked") != null
					|| attrMap.get("firstlogin") != null || attrMap.get("lastpasswordupdate") != null
					|| attrMap.get("user_down_date") != null) {
				throw new NotAuthorizedException("NO_AUTORIZED_UPDATE_FIELD");
			}
			checkIfUserExists(keyMap);
			List<GrantedAuthority> userRole = (List<GrantedAuthority>) SecurityContextHolder.getContext()
					.getAuthentication().getAuthorities();
			for (GrantedAuthority x : userRole) {
				if (x.getAuthority().compareTo("admin") != 0) {
					UserInformation user = ((UserInformation) SecurityContextHolder.getContext().getAuthentication()
							.getPrincipal());
					if (user.getLogin().compareTo((String) keyMap.get("user_")) != 0) {
						throw new NotAuthorizedException("NOT_AUTHORIZED");
					}
				}
				if (attrMap.get("password") != null) {
					attrMap.put("lastpasswordupdate", new Timestamp(Calendar.getInstance().getTimeInMillis()));
				}
				updateResult = daoHelper.update(userDao, attrMap, keyMap);
				updateResult.setMessage("SUCCESSFULLY_UPDATE");
			}
		} catch (BadSqlGrammarException e) {
			log.error("unable to update a user. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "IDENTIFIER_BE_NUMERIC");
		} catch (DuplicateKeyException e) {
			log.error("unable to update a user. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, "ROOM_TYPE_ALREADY_EXISTS");
		} catch (RecordNotFoundException | EmptyRequestException | DataIntegrityViolationException
				| NotAuthorizedException e) {
			log.error("unable to update a user. Request : {} {} ", keyMap, attrMap, e);
			control.setErrorMessage(updateResult, e.getMessage());
		}
		return updateResult;
	}

	@Override
	@Secured({ "admin" })
	public EntityResult userDelete(Map<String, Object> keyMap) {
			EntityResult exitingUserResult = new EntityResultMapImpl();
			EntityResult deleteResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(keyMap);
			exitingUserResult = new EntityResultMapImpl();
			List<String> attrListUser = new ArrayList<String>();
			attrListUser.add("user_");
			exitingUserResult = daoHelper.query(userDao, keyMap, attrListUser);
			if (exitingUserResult.isEmpty())
				throw new RecordNotFoundException("ERROR_USER_NOT_FOUND");
			Map<Object, Object> attrMap = new HashMap<>();
			attrMap.put("user_down_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
			deleteResult = this.daoHelper.update(this.userDao, attrMap, keyMap);
			deleteResult.setMessage("SUCCESSFULLY_DELETE");
			return deleteResult;
		} catch (RecordNotFoundException e) {
			control.setErrorMessage(deleteResult, e.getMessage());
		}catch (EmptyRequestException e) {
			log.error("unable to insert a user. Request : {} {} ",keyMap, e);
			control.setErrorMessage(deleteResult, e.getMessage());
		}
		return deleteResult;
	}

	@Override
	@Transactional
	@Secured({ "admin" })
	public EntityResult userAdminInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			// Este if es para comprobar si esiste ese usuario dado de baja

			dataValidator.checkIfMapIsEmpty(attrMap);
			if (attrMap.get("email") != null) {
				control.checkIfEmailIsValid(attrMap.get("email").toString());
			}
			if (!checkIfUserDown(attrMap)) {
				insertResult = this.daoHelper.insert(this.userDao, attrMap);

				Map<String, Object> columnsTuser_role = new HashMap<String, Object>();
				columnsTuser_role.put("id_rolename", 0);
				columnsTuser_role.put("user_", attrMap.get("user_"));

				daoHelper.insert(userroleDao, columnsTuser_role);
				insertResult.setMessage("SUCCESSFULLY_INSERT");
			} else {
				insertResult.setMessage("USER_EXISTING_UP_SUCCESSFULLY_UPDATE");
			}
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException | EmptyRequestException | InvalidEmailException
				| NotAuthorizedException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		}
		return insertResult;
	}

	@Override
	@Transactional
	@Secured({ "admin", "hotel_manager" })
	public EntityResult userWorkerInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			if (attrMap.get("email") != null) {
				control.checkIfEmailIsValid(attrMap.get("email").toString());
			}
			checkIfHotelExists(attrMap);
			checkIfRolExists(attrMap);
			if (!checkIfUserDown(attrMap)) {
				insertResult = this.daoHelper.insert(this.userDao, attrMap);

				Map<String, Object> columnsTuserRole = new HashMap<String, Object>();
				columnsTuserRole.put("id_rolename", attrMap.get("id_rolename"));
				columnsTuserRole.put("user_", attrMap.get("user_"));
				daoHelper.insert(userroleDao, columnsTuserRole);
				insertResult.setMessage("SUCCESSFULLY_INSERT");
			} else {
				insertResult.setMessage("USER_EXISTING_UP_SUCCESSFULLY_UPDATE");
			}

		} catch (InvalidEmailException | AllFieldsRequiredException | InvalidRolException | NotAuthorizedException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (EmptyRequestException | RecordNotFoundException e) {
			log.error("unable to insert a user. Request : {} {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	@Override
	@Transactional
	@Secured({ "admin", "hotel_manager" })
	public EntityResult userClientByManagersInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			if (attrMap.get("email") == null) {
				throw new EmptyRequestException("EMAIL_REQUIRED");
			} else {
				control.checkIfEmailIsValid(attrMap.get("email").toString());
				// Comprobar si ya existe cliente
				Map<String, Object> keyMapIfClientExist = new HashMap<String, Object>();
				keyMapIfClientExist.put("cl_email", attrMap.get("email"));
				List<String> attrListClient = new ArrayList<String>();
				attrListClient.add("id_client");
				attrListClient.add("cl_leaving_date");
				EntityResult clientResult = new EntityResultMapImpl();
				clientResult = clientService.clientQuery(keyMapIfClientExist, attrListClient);
				if (clientResult.isEmpty()) {
					// Insertar en tabla clientes
					Map<String, Object> columnsClient = new HashMap<String, Object>();
					columnsClient.put("cl_nif", attrMap.get("nif"));
					columnsClient.put("cl_name", attrMap.get("name"));
					columnsClient.put("cl_email", attrMap.get("email"));
					if (attrMap.get("cl_phone") != null) {
						columnsClient.put("cl_phone", attrMap.get("cl_phone"));
						columnsClient.put("cl_country_code", attrMap.get("cl_country_code"));
					}
					EntityResult insertClient = clientService.clientInsert(columnsClient);
					attrMap.put("identifier", insertClient.get("id_client"));
				} else {
					if (clientResult.getRecordValues(0).get("cl_leaving_date") != null) {
						Map<String, Object> attrMapClientUpdate = new HashMap<String, Object>();
						attrMapClientUpdate.put("cl_leaving_date", null);
						Map<String, Object> keyMapClientUpdate = new HashMap<String, Object>();
						keyMapClientUpdate.put("id_client", clientResult.getRecordValues(0).get("id_client"));
						EntityResult updateClient = clientService.clientUpdate(attrMapClientUpdate, keyMapClientUpdate);
					}
					attrMap.put("identifier", clientResult.getRecordValues(0).get("id_client"));
				}
				// Insertado en la tabla tuser
				if (!checkIfUserDown(attrMap)) {
					insertResult = this.daoHelper.insert(userDao, attrMap);

					// Insertado en la tabla tuser_role
					Map<String, Object> columnsTuser_role = new HashMap<String, Object>();
					columnsTuser_role.put("id_rolename", 2);
					columnsTuser_role.put("user_", attrMap.get("user_"));
					daoHelper.insert(userroleDao, columnsTuser_role);
					insertResult.setMessage("SUCCESSFULLY_INSERT");
				} else {
					insertResult.setMessage("USER_EXISTING_UP_SUCCESSFULLY_UPDATE");
				}
			}
		} catch (InvalidEmailException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "EMAIL_ALREADY_EXISTS");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (EmptyRequestException | NotAuthorizedException e) {
			log.error("unable to insert a client. Request : {} {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	@Override
	@Transactional
	public EntityResult userClientInsert(Map<String, Object> attrMap) throws OntimizeJEERuntimeException {
		EntityResult insertResult = new EntityResultMapImpl();
		try {
			dataValidator.checkIfMapIsEmpty(attrMap);
			if (attrMap.get("email") == null) {
				throw new EmptyRequestException("EMAIL_REQUIRED");
			} else {
				control.checkIfEmailIsValid(attrMap.get("email").toString());
				// Insertar en tabla clientes
				Map<String, Object> columnsClient = new HashMap<String, Object>();
				columnsClient.put("cl_nif", attrMap.get("nif"));
				columnsClient.put("cl_name", attrMap.get("name"));
				columnsClient.put("cl_email", attrMap.get("email"));
				if (attrMap.get("cl_phone") != null) {
					columnsClient.put("cl_phone", attrMap.get("cl_phone"));
					columnsClient.put("cl_country_code", attrMap.get("cl_country_code"));
				}
				columnsClient.put("cl_entry_date", new Timestamp(Calendar.getInstance().getTimeInMillis()));
				columnsClient.put("cl_last_update", new Timestamp(Calendar.getInstance().getTimeInMillis()));
				EntityResult insertClient=daoHelper.insert(clientDao, columnsClient);
				attrMap.put("identifier", insertClient.get("id_client"));
				// Insertado en la tabla tuser
				insertResult = this.daoHelper.insert(userDao, attrMap);
				// Insertado en la tabla tuser_role
				Map<String, Object> columnsTuser_role = new HashMap<String, Object>();
				columnsTuser_role.put("id_rolename", 2);
				columnsTuser_role.put("user_", attrMap.get("user_"));
				daoHelper.insert(userroleDao, columnsTuser_role);
				insertResult.setMessage("SUCCESSFULLY_INSERT");
			}
		} catch (InvalidEmailException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		} catch (DuplicateKeyException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setErrorMessage(insertResult, "EMAIL_ALREADY_EXISTS. ");
		} catch (DataIntegrityViolationException e) {
			log.error("unable to insert a user. Request : {} ", attrMap, e);
			control.setMessageFromException(insertResult, e.getMessage());
		} catch (EmptyRequestException e) {
			log.error("unable to insert a client. Request : {} {} ", attrMap, e);
			control.setErrorMessage(insertResult, e.getMessage());
		}
		return insertResult;
	}

	public void checkIfHotelExists(Map<String, Object> attrMap) {
		if (!(attrMap.get("identifier") instanceof Integer)) {
			throw new RecordNotFoundException("ID_HOTEL_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_hotel");
		Map<String, Object> filterHotel = new HashMap<>();
		filterHotel.put("id_hotel", attrMap.get("identifier"));
		EntityResult existingHotelResult = daoHelper.query(hotelDao, filterHotel, fields);
		if (existingHotelResult.isEmpty())
			throw new RecordNotFoundException("ERROR_HOTEL_NOT_FOUND");
	}

	public void checkIfRolExists(Map<String, Object> attrMap) throws InvalidRolException {
		if (!(attrMap.get("id_rolename") instanceof Integer)) {
			throw new AllFieldsRequiredException("ID_ROLENAME_NEEDED");
		}
		if (attrMap.get("id_rolename").equals(0) || attrMap.get("id_rolename").equals(2)) {
			throw new InvalidRolException("ID_ROLENAME_INVALID");
		}
		List<String> fields = new ArrayList<>();
		fields.add("id_rolename");
		Map<String, Object> filterRol = new HashMap<>();
		filterRol.put("id_rolename", attrMap.get("id_rolename"));
		EntityResult existingRolResult = daoHelper.query(roleDao, filterRol, fields);
		if (existingRolResult.isEmpty())
			throw new RecordNotFoundException("ERROR_ID_ROLENAME_NOT_FOUND");
	}

	public void checkIfUserExists(Map<String, Object> keyMap) {
		if ((keyMap.get("user_") == null)) {
			throw new RecordNotFoundException("USER_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("user_");
		Map<String, Object> filterUser = new HashMap<>();
		filterUser.put("user_", keyMap.get("user_"));
		EntityResult existingUserResult = daoHelper.query(userDao, filterUser, fields, "user_data");
		if (existingUserResult.isEmpty()) {
			throw new RecordNotFoundException("ERROR_USER_NOT_FOUND");
		}
	}

	public boolean checkIfUserDown(Map<String, Object> attrMap) throws NotAuthorizedException {
		EntityResult updateResult = new EntityResultMapImpl();
		boolean flat = false;
		if ((attrMap.get("user_") == null) || (attrMap.get("email") == null)) {
			throw new RecordNotFoundException("USER_OR_EMAIL_REQUIRED");
		}
		List<String> fields = new ArrayList<>();
		fields.add("user_");
		fields.add("email");
		fields.add("user_down_date");
		Map<String, Object> filterUser = new HashMap<>();
		filterUser.put("user_", attrMap.get("user_"));
		EntityResult existingUserResult = daoHelper.query(userDao, filterUser, fields, "default");
		if (!existingUserResult.isEmpty()) {
			if (existingUserResult.getRecordValues(0).get("user_down_date") == null) {
				throw new NotAuthorizedException("USER_EXISTING_NOT_AUTHORIZED_INSERT");
			} else {
				String email = (String) existingUserResult.getRecordValues(0).get("email");
				if (email.compareTo((String) attrMap.get("email")) == 0) {
					Map<String, Object> keyMap = new HashMap<>();
					keyMap.put("user_", attrMap.get("user_"));
					attrMap.put("user_down_date", null);
					updateResult = daoHelper.update(userDao, attrMap, keyMap);
					flat = true;
				}
			}
		}
		return flat;
	}
}
