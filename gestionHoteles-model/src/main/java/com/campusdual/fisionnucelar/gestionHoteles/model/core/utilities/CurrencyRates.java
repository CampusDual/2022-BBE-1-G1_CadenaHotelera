package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities;

import java.util.Currency;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.ApiKey;
import com.posadskiy.currencyconverter.CurrencyConverter;
import com.posadskiy.currencyconverter.config.ConfigBuilder;
import com.posadskiy.currencyconverter.exception.CurrencyConverterException;
/**
 * This class manages the currency conversion, by calling an api
 * @author Samuel Purriños
 * @since 24/8/22
 * @version 1.0
 * 
 *
 */
public class CurrencyRates {
	/**
	 * Builds the client for the currency converter api
	 */
	private static CurrencyConverter converter= new CurrencyConverter(
		    new ConfigBuilder()
	        .currencyConverterApiApiKey(ApiKey.KEY_CURRENCY_CONVERTER)
	        .build());
	
	/**
	 * Returns currency rate when the api is down
	 * @param currencyCode a currency code from the user
	 * @exception IllegalArgumentException if the currency code passed as a paremeter does´nt exists in the enum of this class
	 * @return the currency rate for a given currency code
	 */
	public static double getHardCodedRate(String currencyCode) {
		CurrencyCodes.valueOf(currencyCode);
		
		double rate;
		switch(currencyCode){
		case "USD" : rate = 0.99146371; break;
		case "EUR" : rate = 1.00; break;
		case "GBP" : rate = 1.19; break;
		case "HUF" : rate = 411.15; break;
		case "JPY" : rate = 135.71; break;
		case "BTC" : rate = 0.000047; break;
		case "AUD" : rate = 1.44; break;
		default : rate = 1.00;
		}
		return rate;
	}
	
	/**^da
	 * Calls freecurrencyconverter api to get the currency rate
	 * @param currencyCode the currency code provided by the user
	 * @return the currency rate of the given currency code
	 * @throws IllegalArgumentException if the currency code doesn´t exists
	 * @throws CurrencyConverterException if freecurrencyconverter api is down
	 */
	public static double getCurrencyRate(String currencyCode) throws IllegalArgumentException,CurrencyConverterException{
		return converter.rate("EUR", currencyCode);
	}
	
	/*
	 * gets a currency simbol from a currency code provided by the user
	 * @param currencyCode a currency code from the user
	 */
	public static String getCurrencySymbol(String currencyCode) throws IllegalArgumentException{
		Currency currency = Currency.getInstance(currencyCode);
		return currency.getSymbol();
	}
	
	public enum CurrencyCodes { USD,EUR,GBP,HUF,JPY,BTC,AUD }
}
