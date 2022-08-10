package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.InvalidEmailException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NoResultsException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.exception.NotAuthorizedException;
import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.*;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
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
	private GooglePlaces places;
	PhoneNumberUtil phoneNumberUtil; 
	PhoneNumber number;

	public Control() {
		super();
		this.places = new GooglePlaces(ApiKey.KEY_GOOGLE_PLACES);
		this.number = new PhoneNumber();
		this.phoneNumberUtil = PhoneNumberUtil.getInstance();
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
			int valueIndex = message.indexOf("IS_NOT_PRESENT");
			int first = message.indexOf("_");
			int second = message.indexOf("_",first+1);
			message = message.substring(0,second)+"_"+message.substring(valueIndex);
			result.setMessage(message);
		} else {
			result.setMessage(exception);
		}
		return result;
	}


	public boolean checkIfPhoneNumberIsValid(int countryCode, String phoneNumber) {
		boolean valid=false;
		if(isNumber(phoneNumber)) {
			this.number.setCountryCode(countryCode).setNationalNumber(Long.parseLong(phoneNumber));
			valid=this.phoneNumberUtil.isPossibleNumber(number);
		}else valid = false;
		return valid;
	}
	
	public boolean isNumber(String number) {
		boolean isNumber=false;
		try {
			Integer.parseInt(number);
			isNumber=true;
		}catch(Exception e) {
			isNumber=false;
		}
		
		return isNumber;
	}

}
















