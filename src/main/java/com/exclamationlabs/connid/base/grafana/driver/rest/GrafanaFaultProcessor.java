package com.exclamationlabs.connid.base.grafana.driver.rest;

import com.exclamationlabs.connid.base.connector.driver.rest.RestFaultProcessor;
import com.exclamationlabs.connid.base.grafana.model.response.GrafanaStandardResponse;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

import java.io.IOException;

public class GrafanaFaultProcessor implements RestFaultProcessor
{
    private static final Log LOG = Log.getLog(GrafanaFaultProcessor.class);

    private static final GrafanaFaultProcessor instance = new GrafanaFaultProcessor();

    public static GrafanaFaultProcessor getInstance() {
        return instance;
    }

    @Override
    public void process(HttpResponse httpResponse, GsonBuilder gsonBuilder) throws ConnectorException
    {
        String rawResponse;
        try
        {
            rawResponse = EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
            LOG.warn("Raw Fault response {0}", rawResponse);

            Header responseType = httpResponse.getFirstHeader("Content-Type");
            String responseTypeValue = responseType.getValue();
            if (!StringUtils.contains(responseTypeValue, ContentType.APPLICATION_JSON.getMimeType()))
            {
                // received non-JSON error response from Grafana unable to process
                String errorMessage = "Unable to parse Grafana response, not valid JSON: ";
                LOG.info("{0} {1}", errorMessage, rawResponse);
                throw new ConnectorException(errorMessage + rawResponse);
            }
            handleFaultResponse(rawResponse, gsonBuilder);
        }
        catch (IOException e)
        {
            throw new ConnectorException("Unable to read fault response from Grafana response. " +
                    "Status: " + httpResponse.getStatusLine().getStatusCode() + ", " +
                    httpResponse.getStatusLine().getReasonPhrase(), e);
        }
    }

    private void handleFaultResponse(String rawResponse, GsonBuilder gsonBuilder) {

        GrafanaStandardResponse faultData = gsonBuilder.create().fromJson(rawResponse, GrafanaStandardResponse.class);
        if (faultData != null && faultData.getMessage() != null && (!faultData.getMessage().isEmpty()))
        {
            String message = faultData.getMessage();
            if ( message.equalsIgnoreCase("user not found") ||
                 message.equalsIgnoreCase("data source not found") ||
                 message.equalsIgnoreCase("organization not found") ||
                 message.equalsIgnoreCase("Permission denied") ||
                 message.equalsIgnoreCase("Not Found"))
            {
                LOG.info(message);
                return;
            }
            else
            {
                LOG.error(message);
                throw new ConnectorException("Unknown fault received from Grafana.  Message: " + faultData.getMessage());
            }
        }
        throw new ConnectorException("Unknown fault received from Grafana. Raw response JSON: " + rawResponse);
    }
}
