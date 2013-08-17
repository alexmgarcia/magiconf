package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Log;

public class RestClient {

	private ArrayList<NameValuePair> headers;
	private ArrayList<NameValuePair> postParams;

	public RestClient() {
		headers = new ArrayList<NameValuePair>();
		postParams = new ArrayList<NameValuePair>();
	}

	private void setHeadersParameters(HttpUriRequest request) {
		for (NameValuePair h : headers) {
			request.addHeader(h.getName(), h.getValue());
		}
	}

	public void addPostParam(NameValuePair param) {
		postParams.add(param);
	}

	public static final String SUCCESS = "OK";

	private static String convertStream(InputStream is) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	private JSONObject getJsonObject() {
		JSONObject jsonObj = new JSONObject();

		for (NameValuePair p : postParams) {
			try {
				jsonObj.put(p.getName(), p.getValue());
			} catch (JSONException e) {
				;
			}
		}
		return jsonObj;
	}

	public boolean executePost(String url) {
		HttpPost request = new HttpPost(url);
		setHeadersParameters(request);

		try {
			StringEntity entity = new StringEntity(getJsonObject().toString(),
					HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			return false;
		}

		HttpClient client = new DefaultHttpClient();
		try {

			HttpResponse httpResponse = client.execute(request);
			Integer responseCode = httpResponse.getStatusLine().getStatusCode();
			String responseMessage = httpResponse.getStatusLine()
					.getReasonPhrase();

			return responseCode == HttpStatus.SC_OK;

		} catch (ClientProtocolException e) {
			client.getConnectionManager().shutdown();
		} catch (IOException e) {
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			;
		}

		return false;
	}


	public static JSONObject makeRequest(String url,boolean setOffset, int offset) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);
		if(setOffset){
			url = url+ "&offset="+offset;
			httpget = new HttpGet(url);
		}

		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			Log.i("REST Client","response "+response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();


			if (entity != null) {

				InputStream instream = entity.getContent();
				String result= convertStream(instream);

				instream.close();

				JSONObject json=new JSONObject(result);

				return json;
			}


		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static String makeRequest(String url) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(url);

		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			Log.i("REST Client","response "+response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();


			if (entity != null) {

				InputStream instream = entity.getContent();
				String result= convertStream(instream);

				instream.close();

				return result;
			}


		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public JSONObject makePostRequest(String url) {

		HttpPost request = new HttpPost(url);
		setHeadersParameters(request);

		try {
			StringEntity entity = new StringEntity(getJsonObject().toString(),
					HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse httpResponse = client.execute(request);
			Integer responseCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {

				InputStream instream = entity.getContent();
				String result= convertStream(instream);
				//Log.i("REST Client",result);

				instream.close();

				JSONObject json=new JSONObject(result);

				return json;
			}
		} catch (ClientProtocolException e) {
			client.getConnectionManager().shutdown();
		} catch (IOException e) {
			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			;
		}

		return null;
	}


}
