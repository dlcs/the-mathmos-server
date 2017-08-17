package com.digirati.themathmos.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.digirati.themathmos.exception.CoordinatePayloadException;
import com.digirati.themathmos.service.GetPayloadService;

@Service(GetPayloadServiceImpl.SERVICE_NAME)
public class GetPayloadServiceImpl implements GetPayloadService{


    private static final Logger LOG = Logger.getLogger(GetPayloadServiceImpl.class);

    public static final String SERVICE_NAME = "getPayloadServiceImpl";

    HttpClient httpClient;

    GetPayloadServiceImpl(){

	httpClient = HttpClientBuilder.create().build(); //Use this instead
    }

    @Override
    public String getJsonPayload(String url, String payload){

        try {
            HttpPost request = new HttpPost(url);

            StringEntity params = new StringEntity(payload);
            LOG.info(payload);
            request.addHeader("content-type", "application/json; charset=utf-8");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new CoordinatePayloadException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }
            InputStreamReader isr = new InputStreamReader(response.getEntity().getContent());
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            isr.close();
            LOG.info("JsonPayload from getJsonPayload is " + sb.toString());
            return sb.toString();



            // handle response here...
        }catch (Exception ex) {
            LOG.error("Exception getting post for " + url, ex);
        }
        return "";
    }

}
