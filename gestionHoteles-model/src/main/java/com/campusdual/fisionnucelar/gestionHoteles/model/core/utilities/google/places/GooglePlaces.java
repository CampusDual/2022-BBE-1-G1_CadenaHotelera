package com.campusdual.fisionnucelar.gestionHoteles.model.core.utilities.google.places;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GooglePlaces {
    private static final String TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    
    private static final String NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

	private String apikey;
	
	private HttpClient client;
	
	private Gson gson;

	public GooglePlaces( String apikey ) {
		this( HttpClientBuilder.create( ).useSystemProperties( ).build( ), apikey );
	};
	public GooglePlaces( HttpClient client, String apikey ) {
		this.apikey = apikey;
		
		GsonBuilder gb = new GsonBuilder( );
		gb.setFieldNamingPolicy( FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES );
		this.gson = gb.create( );
		
		this.client = client;
	}
	
    public PlacesResult searchText( String query ) {
    	HttpGet get = null;
    	PlacesResult result = null;
        try {
        	URIBuilder url = new URIBuilder( TEXT_SEARCH_URL );
        	url.addParameter( "key", this.apikey );
        	url.addParameter( "query", query );


            get = new HttpGet( url.build( ) );
           

        } catch( Exception e ) {
        }
        try {
				result=this.parseSearchResponse( this.client.execute( get ) );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return result;
    }
    
	private PlacesResult parseSearchResponse( HttpResponse response ) throws IOException {
		return this.gson.fromJson( new InputStreamReader( response.getEntity( ).getContent( ) ), PlacesResult.class );
	}
	
	public PlacesResult searchNearby( float lat, float lon, int radius, PlacesQueryOptions options ) {
		try {
			URIBuilder url = new URIBuilder( NEARBY_SEARCH_URL );
			url.addParameter( "key", this.apikey );
			url.addParameter( PlacesQueryOptions.LOCATION, lat + "," + lon );
			url.addParameter( PlacesQueryOptions.RADIUS, String.valueOf( radius ) );
						
			if ( options != null )
				for ( String param : options.params( ).keySet( ) )
					url.addParameter( param, options.param( param ) );
						
			HttpGet get = new HttpGet( url.build( ) );
			return this.parseSearchResponse( this.client.execute( get ) );
			
		} catch( Exception e ) {
			throw new PlacesException( e );
		}
	}
	
	
	

}
