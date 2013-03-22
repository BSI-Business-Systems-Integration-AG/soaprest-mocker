package net.sf.jaceko.mock.service;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;

import javax.ws.rs.core.MediaType;

import net.sf.jaceko.mock.application.enums.ServiceType;
import net.sf.jaceko.mock.exception.ServiceNotConfiguredException;
import net.sf.jaceko.mock.matcher.OperationHavingNameEqualTo;
import net.sf.jaceko.mock.model.webservice.WebService;
import net.sf.jaceko.mock.model.webservice.WebserviceOperation;

import org.hamcrest.Description;
import org.junit.Test;
import org.mockito.ArgumentMatcher;


public class PropertyProcessorTest {

	private PropertyProcessor propertyProcessor = new PropertyProcessor() {
		// don't read wsdl and default response files in unit tests
		// file processing is tested in PropertyProcessorFileReadingTest
		@Override
		protected String readFileContents(String fileName) {
			return null;
		}
	};

	@Test
	public void shouldReturnCollectionOfOneRESTService() throws IOException {

		String serviceName = "somerestservice";
		String propertyString = "SERVICE[0].NAME=" + serviceName + "\r\n"
				+ "SERVICE[0].TYPE=REST\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		assertThat(webServices.size(), is(1));
		assertThat(webServices, hasItem(new ServiceHavingNameEqualTo(serviceName)));
		assertThat(webServices, hasItem(new RestService()));

	}

	@Test
	public void shouldReturnCollectionOfOneSoapService() throws IOException {
		String serviceName = "ticketing";

		String propertyString = "SERVICE[0].NAME=" + serviceName + "\r\n"
				+ "SERVICE[0].TYPE=SOAP\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);
		assertThat(webServices, hasItem(new ServiceHavingNameEqualTo(serviceName)));
		assertThat(webServices, hasItem(new SoapService()));

	}

	private Collection<WebService> processPropertiesAndReturnWebServices(String propertyString)
			throws IOException {
		Reader reader = new StringReader(propertyString);
		MockConfigurationHolder configuration = propertyProcessor.process(reader);
		return configuration.getWebServices();
	}

	@Test
	public void shouldReturnCollectionOfTwoServices() throws IOException {
		String serviceName1 = "ticketing";
		String serviceName2 = "mptu";
		String propertyString = "SERVICE[0].NAME=" + serviceName1 + "\r\n" + "SERVICE[1].NAME="
				+ serviceName2 + "\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);
		assertThat(webServices.size(), is(2));
		assertThat(webServices, hasItem(new ServiceHavingNameEqualTo(serviceName1)));
		assertThat(webServices, hasItem(new ServiceHavingNameEqualTo(serviceName2)));

	}

	@Test
	public void shouldReturnEmptyServicesColletion() throws IOException {

		String serviceName = "ticketing";
		String propertyString = "SERVICE_MaLformed[0].NAME=" + serviceName + "\r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		assertThat(webServices.size(), is(0));

	}

	@Test
	public void shouldReturnServiceIndex() {
		String keyPart = "SERVICE[0]";
		assertThat(propertyProcessor.getServiceIndex(keyPart), is(0));

		keyPart = "SERVICE[1]";
		assertThat(propertyProcessor.getServiceIndex(keyPart), is(1));

		keyPart = "SERVICE[3]";
		assertThat(propertyProcessor.getServiceIndex(keyPart), is(3));

		keyPart = "SERVICE[10]";
		assertThat(propertyProcessor.getServiceIndex(keyPart), is(10));

	}

	@Test
	public void shouldReturnNegativeServiceIndex() {
		String malformed = "SERVICE";
		assertThat(propertyProcessor.getServiceIndex(malformed), is(-1));

		malformed = "SERVICE[1";
		assertThat(propertyProcessor.getServiceIndex(malformed), is(-1));

		malformed = "SERVICE[1aaaa";
		assertThat(propertyProcessor.getServiceIndex(malformed), is(-1));

		malformed = "SERVICE[1]aaaa";
		assertThat(propertyProcessor.getServiceIndex(malformed), is(-1));

		malformed = "aaaSERVICE[1]";
		assertThat(propertyProcessor.getServiceIndex(malformed), is(-1));

		malformed = "SERVICE1]";
		assertThat(propertyProcessor.getServiceIndex(malformed), is(-1));

		malformed = "SECE[1]";
		assertThat(propertyProcessor.getServiceIndex(malformed), is(-1));

	}

	@Test
	public void shouldReturnOperationIndex() {
		String keyPart = "OPERATION[0]";
		assertThat(propertyProcessor.getOperationIndex(keyPart), is(0));

		keyPart = "OPERATION[1]";
		assertThat(propertyProcessor.getOperationIndex(keyPart), is(1));

		keyPart = "OPERATION[3]";
		assertThat(propertyProcessor.getOperationIndex(keyPart), is(3));

		keyPart = "OPERATION[10]";
		assertThat(propertyProcessor.getOperationIndex(keyPart), is(10));

	}

	@Test
	public void shouldReturnNegativeOperationIndex() {
		String malformed = "OPERATION";
		assertThat(propertyProcessor.getOperationIndex(malformed), is(-1));

		malformed = "OPERATION[1";
		assertThat(propertyProcessor.getOperationIndex(malformed), is(-1));

		malformed = "OPERATION[1aaaa";
		assertThat(propertyProcessor.getOperationIndex(malformed), is(-1));

		malformed = "OPERATION[1]aaaa";
		assertThat(propertyProcessor.getOperationIndex(malformed), is(-1));

		malformed = "aaaOPERATION[1]";
		assertThat(propertyProcessor.getOperationIndex(malformed), is(-1));

		malformed = "OPERATION1]";
		assertThat(propertyProcessor.getOperationIndex(malformed), is(-1));

		malformed = "OPON[1]";
		assertThat(propertyProcessor.getOperationIndex(malformed), is(-1));

	}

	@Test
	public void shouldReturnPropertyIndex() {

	}

	
	@Test
	public void shouldReturnSoapServiceHavingOneOperation() throws IOException {
		String expectedInputMessageName = "reserveRequest";
		String propertyString = "SERVICE[0].NAME=ticketing\r\n"
				+ "SERVICE[0].OPERATION[0].INPUT_MESSAGE="
				+ expectedInputMessageName + "\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(1));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(
				expectedInputMessageName)));
	}

	@Test
	public void shouldReturnServiceHavingOneOperation2() throws IOException {
		String expectedInputMessageName = "confirmRequest";
		String propertyString = "SERVICE[0].NAME=ticketing\r\n"
				+ "SERVICE[0].OPERATION[0].INPUT_MESSAGE="
				+ expectedInputMessageName + "\r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(1));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(
				expectedInputMessageName)));
	}

	@Test
	public void shouldReturnRestServiceHavingOneOperation() throws IOException {
		String expectedHttpMethod = "GET";
		String propertyString = "SERVICE[0].NAME=service1\r\n"
				+ "SERVICE[0].OPERATION[0].HTTP_METHOD=" + expectedHttpMethod + "\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(1));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(expectedHttpMethod)));

	}

	@Test(expected = ServiceNotConfiguredException.class)
	public void shouldThrowExceptionIfHTTpMethodNotRecognized() throws IOException {
		String badHttpMethod = "GOT";
		String propertyString = "SERVICE[0].NAME=service1\r\n"
				+ "SERVICE[0].OPERATION[0].HTTP_METHOD=" + badHttpMethod + "\r\n";
		processPropertiesAndReturnWebServices(propertyString);

	}

	@Test
	public void shouldIgnoreUknownPropertiesOfService() throws IOException {
		String expectedInputMessageName = "confirmRequest";
		String propertyString = "SERVICE[0].NAME=ticketing\r\n"
				+ "SERVICE[0].WSDL=ticketing.wsdl\r\n" + "SERVICE[0].OPERATION[0].INPUT_MESSAGE="
				+ expectedInputMessageName + "\r\n"
				+ "SERVICE[0].UNKNOWN_Property=unknow_prop_vaue\r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(1));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(
				expectedInputMessageName)));
	}

	@Test
	public void shouldReturnServiceHavingTwoOperations() throws IOException {
		String expectedInputMessageName = "confirmRequest";
		String expectedInputMessageName2 = "reserveRequest";
		String propertyString = "SERVICE[0].NAME=ticketing\r\n"
				+ "SERVICE[0].WSDL=ticketing.wsdl\r\n" + "SERVICE[0].OPERATION[0].INPUT_MESSAGE="
				+ expectedInputMessageName + "\r\n" + "SERVICE[0].OPERATION[1].INPUT_MESSAGE="
				+ expectedInputMessageName2 + "\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(2));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(
				expectedInputMessageName)));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(
				expectedInputMessageName2)));
	}

	@Test
	public void shouldIgnoreUknownPropertiesOfOperation() throws IOException {
		String expectedResponseFile = "confirmRequestResponse.xml";
		String expectedResponseFile2 = "reserveRequestResponse.xml";
		String expectedInputMessageName = "confirmRequest";
		String expectedInputMessageName2 = "reserveRequesT";
		String propertyString = "SERVICE[0].NAME=ticketing\r\n"
				+ "SERVICE[0].WSDL=ticketing.wsdl\r\n"
				+ "SERVICE[0].OPERATION[0].DEFAULT_RESPONSE=" + expectedResponseFile + "\r\n"
				+ "SERVICE[0].OPERATION[0].INPUT_MESSAGE=" + expectedInputMessageName + "\r\n"
				+ "SERVICE[0].OPERATION[1].DEFAULT_RESPONSE=" + expectedResponseFile2 + "\r\n"
				+ "SERVICE[0].OPERATION[1].INPUT_MESSAGE=" + expectedInputMessageName2 + "\r\n"
				+ "SERVICE[0].OPERATION[1].UNKNOWN_PROP123=unknown_prop_value\r\n"
				+ "SERVICE[0].OPERATION[0].UNKNOWN_PROP543=unknown_prop_value5634\r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(2));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseEqualTo(
				expectedResponseFile)));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseEqualTo(
				expectedResponseFile2)));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(
				expectedInputMessageName)));
		assertThat(operations, hasItem(new OperationHavingNameEqualTo(
				expectedInputMessageName2)));

	}

	@Test
	public void shouldReturnServiceHavingTwoOperationsHavingDefaultResponses() throws IOException {
		String expectedResponseFile = "confirmRequestResponse.xml";
		String expectedResponseFile2 = "reserveRequestResponse.xml";
		String propertyString = "SERVICE[0].NAME=ticketing\r\n"
				+ "SERVICE[0].WSDL=ticketing.wsdl\r\n"
				+ "SERVICE[0].OPERATION[0].DEFAULT_RESPONSE=" + expectedResponseFile + "    \r\n"
				+ "SERVICE[0].OPERATION[1].DEFAULT_RESPONSE=" + expectedResponseFile2 + "\r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);
		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(2));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseEqualTo(
				expectedResponseFile)));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseEqualTo(
				expectedResponseFile2)));

	}

	@Test
	public void shouldReturnServiceHavingDefaultResponseCode() throws IOException {
		String propertyString = "SERVICE[0].NAME=some_service\r\n"
				+ "SERVICE[0].OPERATION[0].DEFAULT_RESPONSE_CODE=204\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(1));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseCodeEqualTo(
				204)));

	}

	
	@Test
	public void shouldReturnTwoOperationsHavingDefaultResponseContentType() throws IOException {
		String propertyString = "SERVICE[0].NAME=some_service\r\n"
				+ "SERVICE[0].OPERATION[0].DEFAULT_RESPONSE_CONTENT_TYPE=application/json\r\n"
				+ "SERVICE[0].OPERATION[1].DEFAULT_RESPONSE_CONTENT_TYPE=application/xml\r\n";
		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);

		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(2));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseContentTypeEqualTo(
				MediaType.APPLICATION_JSON_TYPE)));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseContentTypeEqualTo(
				MediaType.APPLICATION_XML_TYPE)));

	}

	@Test
	public void shouldReturnTwoOperationsHavingDefaultResponseCodes() throws IOException {
		String propertyString = "SERVICE[0].NAME=some_service\r\n"
				+ "SERVICE[0].OPERATION[0].DEFAULT_RESPONSE_CODE=200\r\n"
				+ "SERVICE[0].OPERATION[1].DEFAULT_RESPONSE_CODE=201 \r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);
		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(2));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseCodeEqualTo(
				200)));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseCodeEqualTo(
				201)));

	}

	@Test
	public void shouldReturnOperationsHavingDefaultResponseCodesSetToXMLIfNotSpecified() throws IOException {
		String propertyString = "SERVICE[0].NAME=some_service\r\n"
				+ "SERVICE[0].OPERATION[0].HTTP_METHOD=GET \r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);
		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(1));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseContentTypeEqualTo(
				MediaType.TEXT_XML_TYPE)));

	}

	@Test
	public void shouldReturnOperationsHavingDefaultResponseCodesSetToXMLIfNotValidContentType() throws IOException {
		String propertyString = "SERVICE[0].NAME=some_service\r\n"
				+ "SERVICE[0].OPERATION[0].DEFAULT_RESPONSE_CONTENT_TYPE=NoT_Valid \r\n";

		Collection<WebService> webServices = processPropertiesAndReturnWebServices(propertyString);
		WebService service = webServices.iterator().next();
		Collection<WebserviceOperation> operations = service.getOperations();
		assertThat(operations.size(), is(1));
		assertThat(operations, hasItem(new OperationHavingDefaultResponseContentTypeEqualTo(
				MediaType.TEXT_XML_TYPE)));

	}
	
	class ServiceHavingNameEqualTo extends ArgumentMatcher<WebService> {
		private WebService service;
		private final String name;

		@Override
		public boolean matches(Object argument) {
			service = (WebService) argument;
			return name.equals(service.getName());
		}

		private ServiceHavingNameEqualTo(String name) {
			super();
			this.name = name;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("name should be " + name + " but is " + service);

		}

	}

	class SoapService extends ArgumentMatcher<WebService> {
		private WebService service;

		@Override
		public boolean matches(Object argument) {
			service = (WebService) argument;
			return ServiceType.SOAP.equals(service.getServiceType());

		}

		@Override
		public void describeTo(Description description) {
			description.appendText("service type: SOAP but is " + service.getServiceType());

		}

	}

	class RestService extends ArgumentMatcher<WebService> {
		private WebService service;

		@Override
		public boolean matches(Object argument) {
			service = (WebService) argument;
			return ServiceType.REST.equals(service.getServiceType());

		}

		@Override
		public void describeTo(Description description) {
			description.appendText("service type: REST but is " + service.getServiceType());

		}

	}

	class OperationHavingDefaultResponseEqualTo extends ArgumentMatcher<WebserviceOperation> {
		private WebserviceOperation operation;
		private final String defaultResponse;

		@Override
		public boolean matches(Object argument) {
			operation = (WebserviceOperation) argument;
			return defaultResponse.equals(operation.getDefaultResponseFile());
		}

		private OperationHavingDefaultResponseEqualTo(String defaultResponse) {
			super();
			this.defaultResponse = defaultResponse;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("operation having defaultResponse = " + defaultResponse + " but is "
					+ operation);

		}

	}

	class OperationHavingDefaultResponseCodeEqualTo extends ArgumentMatcher<WebserviceOperation> {
		private WebserviceOperation operation;
		private final int defaultResponseCode;

		@Override
		public boolean matches(Object argument) {
			operation = (WebserviceOperation) argument;
			return defaultResponseCode==operation.getDefaultResponseCode();
		}

		private OperationHavingDefaultResponseCodeEqualTo(int defaultResponseCode) {
			super();
			this.defaultResponseCode = defaultResponseCode;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("operation having defaultResponseCode = " + defaultResponseCode + " but is operation having defaultResponseCode = "
					+ operation.getDefaultResponseCode());
		}

	}

	class OperationHavingDefaultResponseContentTypeEqualTo extends ArgumentMatcher<WebserviceOperation> {
		private WebserviceOperation operation;
		private final MediaType defaultResponseContentType;

		@Override
		public boolean matches(Object argument) {
			operation = (WebserviceOperation) argument;
			return defaultResponseContentType.equals(operation.getDefaultResponseContentType());
		}

		private OperationHavingDefaultResponseContentTypeEqualTo(MediaType defaultResponseContentType) {
			super();
			this.defaultResponseContentType = defaultResponseContentType;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("operation having defaultResponseContentType = " + defaultResponseContentType + " but is operation having defaultResponseContentType = "
					+ operation.getDefaultResponseContentType());
		}

	}

}
