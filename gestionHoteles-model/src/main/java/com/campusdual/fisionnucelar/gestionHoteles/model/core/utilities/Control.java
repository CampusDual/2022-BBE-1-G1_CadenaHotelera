package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.*;
import com.ontimize.jee.common.dto.EntityResult;
import com.ontimize.jee.common.services.user.UserInformation;

/**
 * This class implements various data validation to check if the user input is
 * valid and prevents errors while insert data in the database
 * 
 * @author samue
 *
 */
public class Control {
	GooglePlaces places;

	public Control() {
		super();
		this.places = new GooglePlaces(ApiKey.API_KEY);
	}

	/**
	 * Sets a message in the given EntityResult to display it to the user
	 * 
	 * @param result
	 * @param message
	 */
	public void setErrorMessage(EntityResult result, String message) {
		result.setMessage(message);
		result.setCode(1);
	}

	/**
	 * Checks if a city name exists in the given exists by calling google maps API
	 * 
	 * @param city
	 * @param state
	 * @return true if exists a place in the given state, false if not
	 */
	public boolean checkIfCityAndStateExists(String city, String state) {
		boolean valid = false;
		PlacesResult cityAndStateQuery = places.searchText(city);
		for (Place place : cityAndStateQuery) {
			if (place.getFormattedAddress().toLowerCase().contains(city.toLowerCase().trim())
					&& place.getFormattedAddress().toLowerCase().contains(state.toLowerCase().trim()))
				valid = true;
		}
		return valid;
	}

	/**
	 * Checks if an email is well formed by means of a RFC 5322 pattern to validate
	 * emails.
	 * 
	 * @param an email to be validated
	 * @return true if the email is well formed or false in the rest of the cases
	 * @throws InvalidEmailException
	 */
	public void checkIfEmailIsValid(String emailAddress) throws InvalidEmailException {
		if (!Pattern.compile(
				"^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(-[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
				.matcher(emailAddress).matches())
			throw new InvalidEmailException("INVALID_EMAIL");

	}

	public void checkResults(EntityResult searchResult) throws NoResultsException {
		if (searchResult.isEmpty()) {
			throw new NoResultsException("NO_RESULTS");
		}

	}

	/**
	 * En un futuro poner un replaceAll con una expresión
	 * 
	 * @param result
	 * @param exception
	 */

	public EntityResult setMessageFromException(EntityResult result, String exception) {
		String exceptionSplit[];
		String message;
		result.setCode(1);
		if (exception.contains("null value")) {
			exceptionSplit = exception.split("column");
			int index = exceptionSplit[1].indexOf("violates");
			message = exceptionSplit[1].substring(2, index - 2).toUpperCase() + "_MUST_BE_PROVIDED";
			result.setMessage(message);
		} else if (exception.contains("nested")) {
			exceptionSplit = exception.split("Key");
			int index = exceptionSplit[1].indexOf("nested");
			message = exceptionSplit[1].substring(2, index - 3).replace(")", "").replace("\"", "").replace("(", "")
					.replace(" ", "_").replace("=", "_").toUpperCase();
			result.setMessage(message);
		} else {
			result.setMessage(exception);
		}
		return result;
	}

	public void controlAccess(int id) throws NotAuthorizedException {
		UserInformation user = ((UserInformation) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal());
		List<GrantedAuthority> userRole = (List<GrantedAuthority>) SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities();

		for (GrantedAuthority x : userRole) {
			if (x.getAuthority().compareTo("admin") != 0) {
				int identifier = (int) user.getOtherData().get("IDENTIFIER");
				if (identifier != id) {
					throw new NotAuthorizedException("NOT_AUTHORIZED");
				}
			}					
		}
	}

	public boolean controlAccessClient(int id) throws NotAuthorizedException {
		UserInformation user = ((UserInformation) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal());
		List<GrantedAuthority> userRole = (List<GrantedAuthority>) SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities();
		boolean flag=false;
		for (GrantedAuthority x : userRole) {
			if (x.getAuthority().compareTo("client") == 0) {
				int identifier = (int) user.getOtherData().get("IDENTIFIER");
				if (identifier != id) {
					throw new NotAuthorizedException("NOT_AUTHORIZED");
				}else flag=true;
			}					
		}
		return flag;
	}
	
	
	


}
















