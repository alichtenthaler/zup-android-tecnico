package com.ntxdev.zuptecnico.api;

import android.net.Uri;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BuilderBasedDeserializer;
import com.ntxdev.zuptecnico.BuildConfig;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

/**
 * Created by igorlira on 3/3/14.
 */
public class ApiHttpClient
{
    //private HttpClient httpClient;
    public static String mBasePath;

    public ApiHttpClient()
    {
        //this.mBasePath = "http://staging.zup.sapience.io/";
        //this.mBasePath = "http://zup-api-sbc.cognita.ntxdev.com.br/";
        if(BuildConfig.APPLICATION_ID != null && BuildConfig.APPLICATION_ID.equals("com.ntxdev.zuptecnico_boavista"))
        {
            ApiHttpClient.mBasePath = "http://zup-api-boa-vista.cognita.ntxdev.com.br/";
        }
        else if(BuildConfig.APPLICATION_ID.equals("com.ntxdev.zuptecnico_sbc_hmg"))
        {
            ApiHttpClient.mBasePath = "http://zuphmg.saobernardo.sp.gov.br:8282/";
        }
        else if(BuildConfig.APPLICATION_ID.equals("com.ntxdev.zuptecnico_sbc_ntx"))
        {
            ApiHttpClient.mBasePath = "http://zup-api-sbc.cognita.ntxdev.com.br/"; //"http://sbc.zeladoriaurbana.com.br/";
        }
        else if(BuildConfig.APPLICATION_ID.equals("com.ntxdev.zuptecnico_floripa"))
        {
            ApiHttpClient.mBasePath = "http://zup-api-florianopolis.cognita.ntxdev.com.br/";
        }
        else if(BuildConfig.APPLICATION_ID.equals("com.ntxdev.zuptecnico_maceio"))
        {
            ApiHttpClient.mBasePath = "http://zup-api-maceio.cognita.ntxdev.com.br/";
        }
        else
            ApiHttpClient.mBasePath = "http://zup-staging.cognita.ntxdev.com.br/";
        //this.mBasePath = "http://dti-zuphmg-01:9292/";
        //this.httpClient = new DefaultHttpClient();
    }

    public <T> ApiHttpResult<T> get(String path, Class<T> resultType)
    {
        //HttpHost host = new HttpHost("staging.zup.sapience.io", 80);
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(mBasePath + path);

        ApiHttpResult<T> result = new ApiHttpResult<T>();

        try
        {
            HttpResponse response = httpClient.execute(request);
            InputStream contentStream = response.getEntity().getContent();

            /*byte[] buffer = new byte[10240];
            String resultStr = "";
            int readed = 0;
            while((readed = contentStream.read(buffer)) > 0)
            {
                resultStr += new String(buffer, 0, readed);
            }*/

            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(contentStream);
            //JsonParser parser = factory.createParser(resultStr);

            T resultObject = parser.readValueAs(resultType);
            result.result = resultObject;
            result.statusCode = response.getStatusLine().getStatusCode();
            result.success = true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("HTTP Request error", ex.getMessage(), ex);
        }

        return result;
    }

    public <T> ApiHttpResult<T> delete(String path, Class<T> resultType)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpDelete request = new HttpDelete(mBasePath + path);

        ApiHttpResult<T> result = new ApiHttpResult<T>();

        try
        {
            HttpResponse response = httpClient.execute(request);
            InputStream contentStream = response.getEntity().getContent();

            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(contentStream);

            T resultObject = parser.readValueAs(resultType);
            result.result = resultObject;
            result.statusCode = response.getStatusLine().getStatusCode();
            result.success = true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("HTTP Request error", ex.getMessage(), ex);
        }

        return result;
    }

    public <T> ApiHttpResult<T> post(String path, Hashtable<String, String> postData, Class<T> resultType)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(mBasePath + path);

        String postDataString = "";
        for(String key : postData.keySet()) {
            String postKey = Uri.encode(key);
            String postValue = Uri.encode(postData.get(key));

            postDataString += postKey + "=" + postValue + "&";
        }

        ApiHttpResult<T> result = new ApiHttpResult<T>();

        try
        {
            byte[] postDataBytes = postDataString.getBytes("utf-8");

            request.setHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setEntity(new ByteArrayEntity(postDataBytes));

            HttpResponse response = httpClient.execute(request);
            InputStream contentStream = response.getEntity().getContent();

            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(contentStream);

            T resultObject = parser.readValueAs(resultType);
            result.result = resultObject;
            result.statusCode = response.getStatusLine().getStatusCode();
            result.success = true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("HTTP Request error", ex.getMessage(), ex);
        }

        return result;
    }

    public <T> ApiHttpResult<T> post(String path, Object postObject, Class<T> resultType)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(mBasePath + path);

        ApiHttpResult<T> result = new ApiHttpResult<T>();

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String str = mapper.writeValueAsString(postObject);
            str.toCharArray();
            byte[] postDataBytes = mapper.writeValueAsBytes(postObject);

            Log.d("HTTP REQUEST", "POST /" + path + "\n" + str);

            request.setHeader("Content-Type", "application/json");
            request.setEntity(new ByteArrayEntity(postDataBytes));

            HttpResponse response = httpClient.execute(request);
            InputStream contentStream = response.getEntity().getContent();

            /*byte[] buffer = new byte[10240];
            String resultStr = "";
            int readed = 0;
            while((readed = contentStream.read(buffer)) > 0)
            {
                resultStr += new String(buffer, 0, readed);
            }*/

            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(contentStream);
            //JsonParser parser = factory.createParser(resultStr);

            T resultObject = parser.readValueAs(resultType);
            result.result = resultObject;
            result.statusCode = response.getStatusLine().getStatusCode();
            result.success = true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("HTTP Request error", ex.getMessage(), ex);
        }

        return result;
    }

    public <T> ApiHttpResult<T> put(String path, Object postObject, Class<T> resultType)
    {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPut request = new HttpPut(mBasePath + path);

        ApiHttpResult<T> result = new ApiHttpResult<T>();

        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String str = mapper.writeValueAsString(postObject);
            str.toCharArray();
            byte[] postDataBytes = mapper.writeValueAsBytes(postObject);

            Log.d("HTTP REQUEST", "PUT /" + path + "\n" + str);

            request.setHeader("Content-Type", "application/json");
            request.setEntity(new ByteArrayEntity(postDataBytes));

            HttpResponse response = httpClient.execute(request);
            InputStream contentStream = response.getEntity().getContent();

            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(contentStream);

            T resultObject = parser.readValueAs(resultType);
            result.result = resultObject;
            result.statusCode = response.getStatusLine().getStatusCode();
            result.success = true;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e("HTTP Request error", ex.getMessage(), ex);
        }

        return result;
    }

    public InputStream get(String url)
    {
        try
        {
            URL connection = new URL(url);
            return connection.openConnection().getInputStream();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }
}
