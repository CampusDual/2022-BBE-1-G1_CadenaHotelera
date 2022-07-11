package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities;
import java.util.regex.Pattern;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.*;
import com.ontimize.jee.common.dto.EntityResult;
/**
 * This class implements various data validation to check if the user input is valid and prevents errors while insert data in the database
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
	 * @param result
	 * @param message
	 */
	public void setErrorMessage(EntityResult result, String message) {
		result.setMessage(message);
		result.setCode(1);
	}
	/**
	 * Checks if a city name exists in the given exists by calling google maps API
	 * @param city
	 * @param state
	 * @return true if exists a place in the given state, false if not
	 */
	public boolean checkIfCityAndStateExists(String city, String state) {
		boolean valid = false;
		PlacesResult cityAndStateQuery = places.searchText(city);
		for ( Place place : cityAndStateQuery ) {
			if(place.getFormattedAddress().toLowerCase().contains(city.toLowerCase().trim())
					&& place.getFormattedAddress().toLowerCase().contains(state.toLowerCase().trim())) valid = true;
		}
		return valid;
	}
	
	/**
	 * Checks if an email is well formed by means of a RFC 5322 pattern to validate emails.
	 * @param an email to be validated
	 * @return true if the email is well formed or false in the rest of the cases
	 */
	public boolean checkIfEmailIsValid(String emailAddress) {
		String RFC5322regexPattern = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
		return Pattern.compile(RFC5322regexPattern)
			      .matcher(emailAddress)
			      .matches();
	}
}
