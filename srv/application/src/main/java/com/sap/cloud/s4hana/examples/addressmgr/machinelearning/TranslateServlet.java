package com.sap.cloud.s4hana.examples.addressmgr.machinelearning;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sap.cloud.s4hana.examples.addressmgr.util.HttpServlet;
import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpEntityUtil;
import com.sap.cloud.sdk.cloudplatform.logging.CloudLoggerFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@WebServlet("/api/translate")
public class TranslateServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = CloudLoggerFactory.getLogger(TranslateServlet.class);
    private static final String TRANSLATION_PATH = "translation/translation";

    @Override
    protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
            throws ServletException, IOException
    {

        try {
            String targetLang = "en";
            String sourceLang = "de";
            String source = request.getParameter("input");

            String requestJson = createRequestJson(sourceLang, targetLang, source);

            // TODO: Prepare HttpPost query to translate "input"
            // TODO 1. Get the Destination object from the SCP destination configuration using the S/4HANA Cloud SDK
            // final Destination mlDestination = DestinationAccessor.getDestination("sap_api_business_hub_ml");

            // TODO 2. Using the Destination object construct the API endpoint for API sandbox by combining info from destination and TRANSLATION_PATH. Create HttpPost request using the created URL.
            //HttpPost postRequest = new HttpPost(mlDestination.getUri().resolve(TRANSLATION_PATH));

            // TODO 3. Set additional postRequest headers: "Content-Type", "application/json" and "Accept", "application/json;charset=UTF-8"
            //postRequest.setHeader("Content-Type", "application/json");
            //postRequest.setHeader("Accept", "application/json;charset=UTF-8");

            // TODO 4. Using the Destination object retrieve APIKey and add it to the postReqwuest header
            //final String apiKey = mlDestination.getPropertiesByName().get("API_KEY");
            //if (Strings.isNullOrEmpty(apiKey)) {
            //    throw new IllegalStateException("Missing API_KEY destination property");
            //}
            //postRequest.setHeader("APIKey", apiKey);

            // TODO: Add body, execute the request and parse the response
            /*
            // Add http body
            HttpEntity body = new StringEntity(requestJson, ContentType.APPLICATION_JSON);
            postRequest.setEntity(body);

            try {
                // Getting cached http client for base URL, reuse of connection - and send request
                final HttpResponse mlResponse = HttpClientAccessor.getHttpClient(mlDestination).execute(postRequest);
                if (HttpStatus.SC_OK != mlResponse.getStatusLine().getStatusCode()) {
                    throw new Exception("Request failed: " + mlResponse.getStatusLine());
                }

                // retrieve entity content (requested json with Accept header, so should be text) and close request
                final String responseBody = HttpEntityUtil.getResponseBody(mlResponse);
                final Map responseMap = new Gson().fromJson(responseBody, Map.class);
                logger.debug("Response: {}", responseMap);
                String translation = parseResponse(responseBody);
                response.getWriter().write("Translation: " + translation);
            } finally {
                postRequest.releaseConnection();
            }
            */

        } catch (Exception e) {
            logger.error("Failure: " + e.getMessage(), e);

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error: " + e.getMessage());
        }

    }

    static String parseResponse(String response) {
        Gson gson = new Gson();
        TranslationResult translationResult = gson.fromJson(response, TranslationResult.class);

        // Taking the first translation as a particular use case for the address manager
        if (!translationResult.getUnits().isEmpty()) {
            TranslationUnit translationUnit = translationResult.getUnits().get(0);
            return translationUnit.getTranslations().get(0).getValue();

        } else {
            logger.error("No translations found");
            throw new IllegalStateException("No tranlations found");
        }
    }

    private class TranslationResult {
        List<TranslationUnit> units = Lists.newArrayList();

        private List<TranslationUnit> getUnits() {
            return units;
        }

        private void setUnits(final List<TranslationUnit> units) {
            this.units = units;
        }
    }

    private class TranslationUnit {
        String value;
        List<Translation> translations = Lists.newArrayList();

        private String getValue() {
            return value;
        }
        private void setValue(final String value) {
            this.value = value;
        }
        private List<Translation> getTranslations() {
            return translations;
        }
        private void setTranslations(final List<Translation> translations) {
            this.translations = translations;
        }
    }

    private class Translation {
        String language;
        String value;

        private String getLanguage() {
            return language;
        }
        private void setLanguage(final String language) {
            this.language = language;
        }
        private String getValue() {
            return value;
        }
        private void setValue(final String value) {
            this.value = value;
        }
    }

    static String createRequestJson( String sourceLanguage, String targetLanguage, String source )
    {
        final JsonObject requestContent = new JsonObject();
        requestContent.addProperty("sourceLanguage", sourceLanguage);
        requestContent.add("targetLanguages", new Gson().toJsonTree(new String[]{targetLanguage}));
        requestContent.add("units", new Gson().toJsonTree(new Object[]{Collections.singletonMap("value", source)}));

        return new Gson().toJson(requestContent);
    }
}
