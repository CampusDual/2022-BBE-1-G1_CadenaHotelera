package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities;

import java.util.Currency;

import com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places.ApiKey;
import com.posadskiy.currencyconverter.CurrencyConverter;
import com.posadskiy.currencyconverter.config.ConfigBuilder;
import com.posadskiy.currencyconverter.exception.CurrencyConverterException;

public class CurrencyRates {
	private static CurrencyConverter converter= new CurrencyConverter(
		    new ConfigBuilder()
	        .currencyConverterApiApiKey(ApiKey.KEY_CURRENCY_CONVERTER)
	        .build());
	
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
	
	
	public static double getCurrencyRate(String currencyCode) throws IllegalArgumentException,CurrencyConverterException{
		return converter.rate("EUR", currencyCode);
	}
	public static String getCurrencySymbol(String currencyCode) throws IllegalArgumentException{
		Currency currency = Currency.getInstance(currencyCode);
		return currency.getSymbol();
	}
	
	public enum CurrencyCodes { USD,EUR,GBP,HUF,JPY,BTC,AUD }
}
