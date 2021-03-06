package net.sf.jaceko.mock.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import net.sf.jaceko.mock.exception.ClientFaultException;
import net.sf.jaceko.mock.model.request.MockResponse;
import net.sf.jaceko.mock.service.RequestExecutor;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


public class SoapEndpointResourceTest {
	private SoapEndpointResource resource = new SoapEndpointResource();

	@Mock
	private RequestExecutor service;

	@Before
	public void before() {
		initMocks(this);
		resource.setWebserviceMockService(service);
	}
	
	@Test
	public void shouldParseRequestAndPassThroughInputMessageName() throws Exception {
		String serviceName = "ticketing";
		String inputMessageName = "dummyRequest";
		String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">"
				+ "<soapenv:Body><tem:"
				+ inputMessageName
				+ "></tem:"
				+ inputMessageName
				+ "></soapenv:Body></soapenv:Envelope>";

		resource.performRequest(serviceName, request);

		verify(service).performRequest(serviceName, inputMessageName, request, null, null, null);

	}
	
	@Test
	public void shouldParseAnotherRequestAndPassThroughInputMessageName() throws Exception {
		String serviceName = "ticketing";
		
		String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:book=\"http://www.bookmyshow.com/\">\r\n" + 
				"   <soapenv:RecordedHeader/>\r\n" +
				"   <soapenv:Body>\r\n" + 
				"      <book:objExecute>\r\n" + 
				"         <book:strAppCode>TESTAPP</book:strAppCode>\r\n" + 
				"	<book:strCommand>InitTrans</book:strCommand>\r\n" + 
				"         <book:strVenueCode>THRA</book:strVenueCode>\r\n" + 
				"         <book:lngTransactionIdentifier>23275</book:lngTransactionIdentifier>\r\n" + 
				"      </book:objExecute>\r\n" + 
				"   </soapenv:Body>\r\n" + 
				"</soapenv:Envelope>";

		resource.performRequest(serviceName, request);

		verify(service).performRequest(serviceName, "objExecute", request, null, null, null);

	}

	@Test(expected = ClientFaultException.class)
	public void shouldThrowExceptionInCaseOfMalformedRequestXML() throws Exception {
		String badXml = "<malformedXml>malformedXml>";
		resource.performRequest("ticketing", badXml);

	}

	@Test(expected = ClientFaultException.class)
	public void shouldThrowExceptionInCaseOfEmptyRequest() throws Exception {
		String badXml = "";
		resource.performRequest("ticketing", badXml);
	}

	@Test(expected = ClientFaultException.class)
	public void shouldThrowExceptionForImproperSoapRequest() throws Exception {
		String notASoapRequest = "<requestXML></requestXML>";
		resource.performRequest("ticketing", notASoapRequest);

	}

	@Test(expected = ClientFaultException.class)
	public void shouldThrowExceptionForImproperSoapRequest2() throws Exception {
		String notASoapRequest = "<requestXML><aaa></aaa></requestXML>";
		resource.performRequest("ticketing", notASoapRequest);

	}

	@Test(expected = ClientFaultException.class)
	public void shouldThrowExceptionForImproperSoapRequest3() throws Exception {
		String notASoapRequest = "<requestXML><aaa><bbb></bbb></aaa></requestXML>";
		resource.performRequest("ticketing", notASoapRequest);

	}

	@Test(expected = ClientFaultException.class)
	public void shouldThrowExceptionForImproperSoapRequest4() throws Exception {
		String notASoapRequest = "<Envelope><aaa><bbb></bbb></aaa></Envelope>";
		resource.performRequest("ticketing", notASoapRequest);

	}

	@Test(expected = ClientFaultException.class)
	public void shouldThrowExceptionInCaseOfEmptyBody() throws Exception {
		String notASoapRequest = "<Envelope><Body></Body></Envelope>";
		resource.performRequest("ticketing", notASoapRequest);
	}

	@Test
	public void shouldReturnResponse() throws Exception {

		String serviceName = "ticketing";
		String request = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\">"
				+ "<soapenv:Body><tem:dummyRequest></tem:dummyRequest></soapenv:Body></soapenv:Envelope>";

		String responseBody = "<dummyResponse/>";
		int responseCode = 500;
		

		when(service.performRequest(anyString(), anyString(), anyString(), anyString(), anyString(), any(MultivaluedMap.class))).thenReturn(
				new MockResponse(responseBody, responseCode));
		Response response = resource.performRequest(serviceName, request);
		assertThat((String) response.getEntity(), is(responseBody));
		assertThat(response.getStatus(), is(responseCode));

	}

}
