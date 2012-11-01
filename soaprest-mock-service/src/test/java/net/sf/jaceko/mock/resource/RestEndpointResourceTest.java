package net.sf.jaceko.mock.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import net.sf.jaceko.mock.model.MockResponse;
import net.sf.jaceko.mock.resource.RestEndpointResource;
import net.sf.jaceko.mock.service.WebserviceMockSvcLayer;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


public class RestEndpointResourceTest {

	private static final MockResponse NOT_USED_RESPONSE = new MockResponse(null, 0);

	private static final String NOT_USED_RESOURCE_ID = null;

	private static final String NOT_USED_SERVICE_NAME = "";

	private RestEndpointResource resource = new RestEndpointResource();

	@Mock
	private WebserviceMockSvcLayer service;
	
	@Mock
	private HttpServletRequest servletContext; 

	@Before
	public void before() {
		initMocks(this);
		resource.setRestserviceMockSvcLayer(service);
		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
				NOT_USED_RESPONSE);
	}

	@Test
	public void shouldPerformGetRequest() {
		String serviceName = "bms_refdata";
		String urlParams = "msg=abc";
		when(servletContext.getQueryString()).thenReturn(urlParams);
		resource.performGetRequest(serviceName, servletContext);
		verify(service).performRequest(serviceName, "GET", "", urlParams, null);
	}

	
	@Test
	public void shouldPerformGetRequestPassingResourceId() {
		String serviceName = "restServiceName";
		String resourceId = "resId12";
		String urlParams = "msg=def";
		when(servletContext.getQueryString()).thenReturn(urlParams);
		resource.performGetRequest(serviceName, servletContext, resourceId);
		verify(service).performRequest(serviceName, "GET", "", urlParams, resourceId);
	}

	
	@Test
	public void shouldReturnGET_OKResponse() {
		String responseReturnedByServiceLayer = "someResponseText";
		int responseCodeReturnedByServiceLayer = HttpStatus.SC_OK;
		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
				new MockResponse(responseReturnedByServiceLayer, responseCodeReturnedByServiceLayer));
		Response getResponse = resource.performGetRequest(NOT_USED_SERVICE_NAME, servletContext);
		assertThat((String)getResponse.getEntity(), is(responseReturnedByServiceLayer));
		assertThat(getResponse.getStatus(), is(responseCodeReturnedByServiceLayer));
	}

	@Test
	public void shouldReturnGET_FORBIDDEN_Response() {
		int responseCodeReturnedByServiceLayer = HttpStatus.SC_FORBIDDEN;
		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
				new MockResponse(null, responseCodeReturnedByServiceLayer));
		Response getResponse = resource.performGetRequest(NOT_USED_SERVICE_NAME, servletContext);
		assertThat(getResponse.getStatus(), is(responseCodeReturnedByServiceLayer));
	}
	
	@Test
	public void shouldReturnGET_OKResponseOnRequestWith_RESOURCE_ID() {
		String responseReturnedByServiceLayer = "someResponseText";
		int responseCodeReturnedByServiceLayer = HttpStatus.SC_OK;
		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
				new MockResponse(responseReturnedByServiceLayer, responseCodeReturnedByServiceLayer));
		
		Response getResponse = resource.performGetRequest(NOT_USED_SERVICE_NAME, servletContext, NOT_USED_RESOURCE_ID);
		assertThat((String)getResponse.getEntity(), is(responseReturnedByServiceLayer));
		assertThat(getResponse.getStatus(), is(responseCodeReturnedByServiceLayer));

	}
	
	@Test
	public void shouldReturnGET_FORBIDDEN_ResponseOnRequestWith_RESOURCE_ID() {
		int responseCodeReturnedByServiceLayer = HttpStatus.SC_FORBIDDEN;
		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
				new MockResponse(null, responseCodeReturnedByServiceLayer));
		Response getResponse = resource.performGetRequest(NOT_USED_SERVICE_NAME, servletContext, NOT_USED_RESOURCE_ID);
		assertThat(getResponse.getStatus(), is(responseCodeReturnedByServiceLayer));
	}


	@Test
	public void shouldPerformPostRequest() {
		String serviceName = "billdesk";
		String urlParams = "msg=abc";
		String request = "<dummyRequest>abc</dummyRequest>";
		when(servletContext.getQueryString()).thenReturn(urlParams);
		resource.performPostRequest(serviceName, servletContext, request);
		verify(service).performRequest(serviceName, "POST", request, urlParams, null);
	}
	
	@Test
	public void shouldReturnPOST_CREATEDResponse() {
		String responseReturnedByServiceLayer = "someResponse";
		int responseCodeReturnedByServiceLayer = HttpStatus.SC_CREATED;
		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
				new MockResponse(responseReturnedByServiceLayer, responseCodeReturnedByServiceLayer));
		Response response = resource.performPostRequest(NOT_USED_SERVICE_NAME, servletContext, null);
		assertThat((String)response.getEntity(), is(responseReturnedByServiceLayer));
		assertThat(response.getStatus(), is(responseCodeReturnedByServiceLayer));

	}
	
	@Test
	public void shouldPerformPutRequest() {
		String serviceName = "dummyService";
		String request = "<dummyRequest>def</dummyRequest>";
		resource.performPutRequest(serviceName, request);
		verify(service).performRequest(serviceName, "PUT", request, null, null);
	}

	@Test
	public void shouldReturnResponseReturnedByPutRequest() {
		String responseReturnedByServuceLayer = "someResponse123";
		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(
				new MockResponse(responseReturnedByServuceLayer, 0));
		assertThat(resource.performPutRequest(NOT_USED_SERVICE_NAME, null), is(responseReturnedByServuceLayer));

	}

}
