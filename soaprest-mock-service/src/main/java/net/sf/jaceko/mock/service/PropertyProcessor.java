/**
 *
 *     Copyright (C) 2012 Jacek Obarymski
 *
 *     This file is part of SOAP/REST Mock Service.
 *
 *     SOAP/REST Mock Service is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License, version 3
 *     as published by the Free Software Foundation.
 *
 *     SOAP/REST Mock Service is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with SOAP/REST Mock Service; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.sf.jaceko.mock.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.jaceko.mock.application.enums.HttpMethod;
import net.sf.jaceko.mock.application.enums.ServiceType;
import net.sf.jaceko.mock.exception.ServiceNotConfiguredException;
import net.sf.jaceko.mock.model.request.MockResponse;
import net.sf.jaceko.mock.model.webservice.WebService;
import net.sf.jaceko.mock.model.webservice.WebserviceCustomResponse;
import net.sf.jaceko.mock.model.webservice.WebserviceOperation;
import net.sf.jaceko.mock.util.FileReader;

import org.apache.log4j.Logger;

/**
 * Parses property file
 * <p/>
 *
 * example property file:
 *
 * <pre>
 * SERVICE[0].NAME=ticketing
 * SERVICE[0].OPERATION[0].INPUT_MESSAGE=reserveRequest
 * SERVICE[0].OPERATION[0].DEFAULT_RESPONSE=reserve_response.xml
 * SERVICE[0].OPERATION[1].INPUT_MESSAGE=confirmRequest
 * SERVICE[0].OPERATION[1].DEFAULT_RESPONSE=confirm_response.xml
 * SERVICE[0].OPERATION[1].CUSTOM_RESPONSE[0].SEARCHSTRING=bla123
 * SERVICE[0].OPERATION[1].CUSTOM_RESPONSE[0].RESPONSE=confirm_bla123_response.xml
 *
 * SERVICE[1].NAME=mptu
 * SERVICE[1].WSDL=mptu.wsdl
 *
 * </pre>
 *
 */
public class PropertyProcessor {
	private static final Logger LOG = Logger.getLogger(PropertyProcessor.class);

	private static final String FILE_CHARSET = "FILE_CHARSET";

	private static final String INPUT_MESSAGE = "INPUT_MESSAGE";

	private static final String HTTP_METHOD = "HTTP_METHOD";

	private static final String DEFAULT_RESPONSE = "DEFAULT_RESPONSE";

	private static final String DEFAULT_RESPONSE_CODE = "DEFAULT_RESPONSE_CODE";

	private static final String DEFAULT_RESPONSE_CONTENT_TYPE = "DEFAULT_RESPONSE_CONTENT_TYPE";

	private static final String CUSTOM_RESPONSE_SEARCHSTRING = "SEARCHSTRING";

	private static final String CUSTOM_RESPONSE_RESPONSE = "RESPONSE";

	private static final String SEQUENCE_RESPONSE_RESPONSE = "RESPONSE";

	private static final String SERVICE_TYPE = "TYPE";

	private static final String SERVICE_NAME = "NAME";

	private static final String SERVICE_WSDL = "WSDL";

	private static final Pattern SERVICE_PATTERN = Pattern.compile("^SERVICE\\[([0-9]+)\\]$");
	private static final Pattern OPERATION_PATTERN = Pattern.compile("^OPERATION\\[([0-9]+)\\]$");
	private static final Pattern CUSTOM_RESPONSE_PATTERN = Pattern.compile("^CUSTOM_RESPONSE\\[([0-9]+)\\]$");
	private static final Pattern SEQUENCE_RESPONSE_PATTERN = Pattern.compile("^SEQUENCE_RESPONSE\\[([0-9]+)\\]$");;

	private final WsdlProcessor wsdlProcessor = new WsdlProcessor();

	private FileReader fileReader;

	/**
	 * @param reader
	 *            - reader pointing to configuration file of the mock service
	 * @return
	 * @throws IOException
	 */
	public MockConfigurationHolder process(final Reader reader) throws IOException {
		final Properties properties = new Properties();
		properties.load(reader);
		final Set<Object> keySet = properties.keySet();
		final Map<Integer, WebService> services = new HashMap<Integer, WebService>();

		for (final Iterator<Object> iterator = keySet.iterator(); iterator.hasNext();) {
			final String propertyKey = (String) iterator.next();
			final String propertyValue = ((String) properties.get(propertyKey)).trim();

			final String[] propertyKeyParts = propertyKey.split("\\.");
			if (propertyKeyParts.length >= 2) {

				final int serviceIndex = getServiceIndex(propertyKeyParts[0]);
				if (serviceIndex >= 0) {
					final WebService service = getService(services, serviceIndex);

					final String serviceVariable = propertyKeyParts[1];
					final int operationIndex = getOperationIndex(serviceVariable);
					if (operationIndex >= 0) {
						// operation part
						final WebserviceOperation operation = getOperationFromService(service, operationIndex);

						final String operationProperty = propertyKeyParts[2];
						final int customResponseIndex = getCustomResponseIndex(operationProperty);
						final int sequenceResponseIndex = getSequenceResponseIndex(operationProperty);
						if (customResponseIndex >= 0) {
							// custom response part
							final WebserviceCustomResponse customResponse = getCustomResponseFromOperation(operation, customResponseIndex);

							final String customResponseProperty = propertyKeyParts[3];
							setCustomResponseProperties(customResponse, customResponseProperty, propertyValue);
						} else if(sequenceResponseIndex >= 0) {
							final String sequenceResponseProperty = propertyKeyParts[3];
							setSequenceResponseProperties(operation, sequenceResponseProperty, sequenceResponseIndex, propertyValue);
						} else {
							setOperationProperties(operation, operationProperty, propertyValue);
						}
					} else {
						setServiceProperties(service, serviceVariable, propertyValue);
					}
				}

			} else {
				if (propertyKey.equals(FILE_CHARSET)) {
					fileReader.setCharsetName(propertyValue);
				}
			}
		}

		final MockConfigurationHolder configuration = new MockConfigurationHolder();
		configuration.setWebServices(services.values());

		return configuration;

	}

	private void setServiceProperties(final WebService service, final String serviceProperty, final String propertyValue) {
		final String wsdlFileName = propertyValue;
		if (serviceProperty.equals(SERVICE_WSDL)) {
			final String fileText = fileReader.readFileContents(wsdlFileName);

			if (fileText != null) {
				service.setWsdlText(fileText);
				service.addOperations(wsdlProcessor.getOperationsFromWsdl(wsdlFileName, fileText));

			}

		} else if (serviceProperty.equals(SERVICE_NAME)) {
			service.setName(wsdlFileName);
		} else if (serviceProperty.equals(SERVICE_TYPE)) {
			service.setServiceType(ServiceType.valueOf(wsdlFileName));
		}
	}

	private WebService getService(final Map<Integer, WebService> services, final int serviceIndex) {
		WebService service = services.get(serviceIndex);
		if (service == null) {
			service = new WebService();
			services.put(serviceIndex, service);
		}
		return service;
	}

	private void setOperationProperties(final WebserviceOperation operation, final String operationProperty,
			final String propertyValue) {
		if (operationProperty.equals(DEFAULT_RESPONSE)) {
			operation.setDefaultResponseFile(propertyValue);
			setDefaultResponseText(operation);
		} else if (operationProperty.equals(DEFAULT_RESPONSE_CODE)) {
			operation.setDefaultResponseCode(Integer.valueOf(propertyValue));
		} else if (operationProperty.equals(DEFAULT_RESPONSE_CONTENT_TYPE)) {
			try {
				operation.setDefaultResponseContentType(propertyValue);
			} catch (IllegalArgumentException e) {
				LOG.warn("Error parsing configuration file. Illegal content type: "+ propertyValue);
			}
		} else if (operationProperty.equals(INPUT_MESSAGE)) {
			operation.setOperationName(propertyValue);
		} else if (operationProperty.equals(HTTP_METHOD)) {
			try {
				operation.setOperationName(HttpMethod.valueOf(propertyValue).toString());
			} catch (final IllegalArgumentException e) {
				throw new ServiceNotConfiguredException("Http method not recognized: " + propertyValue);

			}
		}
	}

	private void setDefaultResponseText(final WebserviceOperation operation) {

		final String fileText = fileReader.readFileContents(operation.getDefaultResponseFile());
		if (fileText != null) {
			operation.setDefaultResponseText(fileText);
		}

	}

	private void setCustomResponseProperties(final WebserviceCustomResponse customResponse, final String customResponseProperty, final String propertyValue) {
		if (customResponseProperty.equals(CUSTOM_RESPONSE_SEARCHSTRING)) {
			customResponse.setSearchString(propertyValue);
		} else if (customResponseProperty.equals(CUSTOM_RESPONSE_RESPONSE)) {
			customResponse.setResponseFile(propertyValue);
			setCustomResponseText(customResponse);
		}
	}

	private void setCustomResponseText(final WebserviceCustomResponse customResponse) {

		final String fileText = fileReader.readFileContents(customResponse.getResponseFile());
		if (fileText != null) {
			customResponse.setResponseText(fileText);
		}
	}

	private void setSequenceResponseProperties(WebserviceOperation operation, String sequenceResponseProperty, int sequenceNumber, String propertyValue) {
		if (sequenceResponseProperty.equals(SEQUENCE_RESPONSE_RESPONSE)) {
			operation.setResponseInSequences(true);
			setSequenceResponseText(operation, sequenceNumber, propertyValue);
		}
	}

	private static final int ZERO_BASED_TO_ONE_BASED_INDEX = 1;
	private void setSequenceResponseText(final WebserviceOperation operation, final int sequenceNumber, final String sequenceResponseFile) {

		final String fileText = fileReader.readFileContents(sequenceResponseFile);
		if (fileText != null) {
			MockResponse mockResponse = MockResponse.body(fileText).code(operation.getDefaultResponseCode()).contentType(operation.getDefaultResponseContentType()).build();
			operation.setCustomResponse(mockResponse, sequenceNumber + ZERO_BASED_TO_ONE_BASED_INDEX);
		}
	}


	private WebserviceOperation getOperationFromService(final WebService service, final int operationIndex) {
		WebserviceOperation operation = service.getOperation(operationIndex);
		if (operation == null) {
			operation = new WebserviceOperation();
			service.addOperation(operationIndex, operation);
		}
		return operation;
	}

	private WebserviceCustomResponse getCustomResponseFromOperation(WebserviceOperation operation, int responseIndex) {
		WebserviceCustomResponse customResponse = operation.getCustomResponse(responseIndex);
		if (customResponse == null) {
			customResponse = new WebserviceCustomResponse();
			operation.addCustomResponse(responseIndex, customResponse);
		}
		return customResponse;
	}

	int getOperationIndex(final String keyPart) {
		final Pattern pattern = OPERATION_PATTERN;
		return extractIndex(keyPart, pattern);
	}

	int getServiceIndex(final String keyPart) {
		final Pattern pattern = SERVICE_PATTERN;
		return extractIndex(keyPart, pattern);
	}

	int getCustomResponseIndex(final String keyPart) {
		final Pattern pattern = CUSTOM_RESPONSE_PATTERN;
		return extractIndex(keyPart, pattern);
	}

	int getSequenceResponseIndex(final String keyPart) {
		final Pattern pattern = SEQUENCE_RESPONSE_PATTERN;
		return extractIndex(keyPart, pattern);
	}

	private int extractIndex(final String keyPart, final Pattern pattern) {
		final Matcher matcher = pattern.matcher(keyPart);
		if (matcher.find()) {
			final String indxNumberStr = matcher.group(1);
			return Integer.parseInt(indxNumberStr);
		}

		return -1;
	}

	public MockConfigurationHolder process(final String fileName) throws IOException {
		final String fileContents = fileReader.readFileContents(fileName);
		if (fileContents == null) {
			throw new FileNotFoundException("Property file not found in the classpath: " + fileName);
		}
		final Reader reader = new StringReader(fileContents);
		return process(reader);
	}

	public void setFileReader(FileReader fileReader) {
		this.fileReader = fileReader;
	}

}
