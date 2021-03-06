package net.sf.jaceko.mock.it;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.xml.HasXPath.hasXPath;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.jaceko.mock.dom.DocumentImpl;
import net.sf.jaceko.mock.it.helper.request.HttpRequestSender;
import net.sf.jaceko.mock.model.request.MockResponse;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Integration tests of REST mock, DELETE method
 * 
 * @author Jacek Obarymski
 *
 */
public class RestMockDELETEMethodIntegrationTest {

	//mocked endpoints configured in ws-mock.properties
	private static String REST_MOCK_ENDPOINT = "http://localhost:8088/mock/services/REST/dummy-rest/endpoint";

	private static final String REST_MOCK_DELETE_SETUP_INIT			 	= "http://localhost:8088/mock/services/REST/dummy-rest/operations/DELETE/init";
	private static final String REST_MOCK_DELETE_RESPONSES 			 	= "http://localhost:8088/mock/services/REST/dummy-rest/operations/DELETE/responses";

    private static final String REST_MOCK_DELETE_RECORDED_REQUESTS_HEADERS = "http://localhost:8088/mock/services/REST/dummy-rest/operations/DELETE/recorded-request-headers";
	
	HttpRequestSender requestSender = new HttpRequestSender();
	
	@Before
	public void initMock() throws UnsupportedEncodingException, ClientProtocolException, IOException {
		//initalizing mock, clearing history of previous requests
		requestSender.sendPostRequest(REST_MOCK_DELETE_SETUP_INIT, "", MediaType.TEXT_XML);
	}

	@Test
	public void shouldReturnDefaultRESTPostResponse()
			throws ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		
		MockResponse response = requestSender.sendDeleteRequest(REST_MOCK_ENDPOINT);
		assertThat(response.getCode(), is(HttpStatus.SC_OK));
		Document serviceResponseDoc = new DocumentImpl(response.getBody());
		assertThat(
				serviceResponseDoc,
				hasXPath("//delete_response_data",
						equalTo("default REST DELETE response text")));
	}
	
	@Test
	public void shouldReturnCustomRESTDeleteResponseBodyAndDefaultResponseCode() throws UnsupportedEncodingException, ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		//setting up response body on mock
		//not setting custom response code
		String customResponseXML = "<custom_response>custom REST DELETE response text</custom_response>";
		requestSender.sendPostRequest(REST_MOCK_DELETE_RESPONSES, customResponseXML, MediaType.TEXT_XML);
		
		//sending REST DELETE request 
		MockResponse response = requestSender.sendDeleteRequest(REST_MOCK_ENDPOINT);
		
		
		assertThat("default response code", response.getCode(), is(HttpStatus.SC_OK));
		Document serviceResponseDoc = new DocumentImpl(response.getBody());
		assertThat("custom response body", serviceResponseDoc,
				hasXPath("//custom_response",
						equalTo("custom REST DELETE response text")));

		
	}

	
	@Test
	public void shouldReturnCustomRESTDeleteResponseBodyAndCode() throws UnsupportedEncodingException, ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		String customResponseXML = "<custom_response>conflict</custom_response>";
		requestSender.sendPostRequest(REST_MOCK_DELETE_RESPONSES + "?code=409", customResponseXML, MediaType.TEXT_XML);
		
		//sending REST DELETE request 
		MockResponse response = requestSender.sendDeleteRequest(REST_MOCK_ENDPOINT);
		
		Document serviceResponseDoc = new DocumentImpl(response.getBody());
		assertThat("custom response body", serviceResponseDoc,
				hasXPath("//custom_response",
						equalTo("conflict")));

		
		assertThat("custom response code", response.getCode(), is(HttpStatus.SC_CONFLICT));
		
	}

	@Test
	public void shouldReturnConsecutiveCustomRESTDeleteResponses() throws UnsupportedEncodingException, ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		//setting up consecutive responses on mock		
		String customResponseXML1 = "<custom_delete_response>custom REST DELETE response text 1</custom_delete_response>";
		requestSender.sendPutRequest(REST_MOCK_DELETE_RESPONSES + "/1", customResponseXML1, MediaType.TEXT_XML);

		String customResponseXML2 = "<custom_delete_response>custom REST DELETE response text 2</custom_delete_response>";
		requestSender.sendPutRequest(REST_MOCK_DELETE_RESPONSES + "/2", customResponseXML2, MediaType.TEXT_XML);
		
		MockResponse response = requestSender.sendDeleteRequest(REST_MOCK_ENDPOINT);
		Document serviceResponseDoc = new DocumentImpl(response.getBody());
		
		assertThat(
				serviceResponseDoc,
				hasXPath("//custom_delete_response",
						equalTo("custom REST DELETE response text 1")));

		response = requestSender.sendDeleteRequest(REST_MOCK_ENDPOINT);
		serviceResponseDoc = new DocumentImpl(response.getBody());
		assertThat(
				serviceResponseDoc,
				hasXPath("//custom_delete_response",
						equalTo("custom REST DELETE response text 2")));
	}
	
	@Test
	public void shouldReturnCustomRESTDeleteResponseBodyAndDefaultResponseCode_WhilePassingResourceId() throws UnsupportedEncodingException, ClientProtocolException, IOException, ParserConfigurationException, SAXException {
		//setting up response body on mock
		//not setting custom response code
		String customResponseXML = "<custom_delete_response>custom REST DELETE response text</custom_delete_response>";
		requestSender.sendPostRequest(REST_MOCK_DELETE_RESPONSES, customResponseXML, MediaType.TEXT_XML);
		
		//sending REST DELETE request 
		MockResponse response = requestSender.sendDeleteRequest(REST_MOCK_ENDPOINT + "/someResourceId");
		
		Document serviceResponseDoc = new DocumentImpl(response.getBody());
		assertThat("custom response body", serviceResponseDoc,
				hasXPath("//custom_delete_response",
						equalTo("custom REST DELETE response text")));

		
		assertThat("default response code", response.getCode(), is(HttpStatus.SC_OK));
	}

    @Test
    public void shouldVerifyRecordedRequestsWithHeaders() throws Exception {
        // Given we've sent a get request with headers
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("aHeader","aValue");
        requestSender.sendDeleteRequest(REST_MOCK_ENDPOINT, headers);

        //When we get the recorded headers
        MockResponse recordedRequestsHeaders = requestSender.sendGetRequest(REST_MOCK_DELETE_RECORDED_REQUESTS_HEADERS);

        //Then the header sent in the Get request is returned
        System.out.println(recordedRequestsHeaders.getBody());
        assertThat("Expected a response body", recordedRequestsHeaders.getBody(), notNullValue());
        Document requestUrlParamsDoc = new DocumentImpl(recordedRequestsHeaders.getBody());

        assertThat(recordedRequestsHeaders.getCode(), equalTo(200));
        assertThat(requestUrlParamsDoc, hasXPath("/recorded-request-headers/single-request-recorded-headers[1]/header/name[text()='aHeader']//..//value", equalTo("aValue")));
    }

}
