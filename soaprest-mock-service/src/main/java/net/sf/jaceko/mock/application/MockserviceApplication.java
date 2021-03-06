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
package net.sf.jaceko.mock.application;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import net.sf.jaceko.mock.resource.RestEndpointResource;
import net.sf.jaceko.mock.resource.RestServiceMockSetupResource;
import net.sf.jaceko.mock.resource.RestServiceMockVerificatonResource;
import net.sf.jaceko.mock.resource.ServicesResource;
import net.sf.jaceko.mock.resource.SoapEndpointResource;
import net.sf.jaceko.mock.resource.SoapServiceMockSetupResource;
import net.sf.jaceko.mock.resource.SoapServiceMockVerificatonResource;
import net.sf.jaceko.mock.resource.WsdlExposingResource;
import net.sf.jaceko.mock.resource.RestServiceResourceWithoutEndPoint;
import net.sf.jaceko.mock.service.Delayer;
import net.sf.jaceko.mock.service.MockConfigurationHolder;
import net.sf.jaceko.mock.service.MockSetupExecutor;
import net.sf.jaceko.mock.service.PropertyProcessor;
import net.sf.jaceko.mock.service.RecordedRequestsHolder;
import net.sf.jaceko.mock.service.RequestExecutor;
import net.sf.jaceko.mock.util.FileReader;

public class MockserviceApplication extends Application {
	private static final String PROPERTY_FILE = "ws-mock.properties";

	public MockserviceApplication() {
		super();
		PropertyProcessor propertyProcessor = new PropertyProcessor();
		propertyProcessor.setFileReader(new FileReader());
		MockConfigurationHolder configurationHolder = null;
		try {
			configurationHolder = propertyProcessor.process(PROPERTY_FILE);
		} catch (IOException e) {
			throw new RuntimeException("Problem reading property file", e);
		}
		RecordedRequestsHolder recordedRequestsHolder = new RecordedRequestsHolder();
		recordedRequestsHolder.setMockserviceConfiguration(configurationHolder);
		Delayer delayer = new Delayer();
		
		MockSetupExecutor mockSetupExecutor = new MockSetupExecutor();
		mockSetupExecutor.setMockserviceConfiguration(configurationHolder);
		mockSetupExecutor.setRecordedRequestsHolder(recordedRequestsHolder);

		RequestExecutor svcLayer = new RequestExecutor();
		svcLayer.setMockserviceConfiguration(configurationHolder);
		svcLayer.setDelayer(delayer);
		svcLayer.setRecordedRequestsHolder(recordedRequestsHolder);

		RestServiceMockSetupResource restMockSetupResource = new RestServiceMockSetupResource();
		restMockSetupResource.setMockSetupExecutor(mockSetupExecutor);
		
		RestServiceMockVerificatonResource restVerificationResource = new RestServiceMockVerificatonResource();
		restVerificationResource.setRecordedRequestsHolder(recordedRequestsHolder);

		SoapServiceMockSetupResource soapMockSetupResource = new SoapServiceMockSetupResource();
		soapMockSetupResource.setMockSetupExecutor(mockSetupExecutor);
		
		SoapServiceMockVerificatonResource soapVerificationResource = new SoapServiceMockVerificatonResource();
		soapVerificationResource.setRecordedRequestsHolder(recordedRequestsHolder);

		
		SoapEndpointResource mockSoapEndpointResource = new SoapEndpointResource();
		mockSoapEndpointResource.setWebserviceMockService(svcLayer);

		RestEndpointResource mockRestEndpointResource = new RestEndpointResource();
		mockRestEndpointResource.setWebserviceMockService(svcLayer);

        RestEndpointResource mockRestServiceResourceWithoutEndPoint = new RestServiceResourceWithoutEndPoint();
        mockRestServiceResourceWithoutEndPoint.setWebserviceMockService(svcLayer);

		WsdlExposingResource wsdlExposingResource = new WsdlExposingResource();
		wsdlExposingResource.setWebserviceMockService(svcLayer);
		
		ServicesResource servicesResource = new ServicesResource();
		servicesResource.setMockConfigurationService(configurationHolder);

		singletons.add(mockSoapEndpointResource);
		singletons.add(mockRestEndpointResource);
		singletons.add(mockRestServiceResourceWithoutEndPoint);
		singletons.add(restMockSetupResource);
		singletons.add(restVerificationResource);
		singletons.add(soapMockSetupResource);
		singletons.add(soapVerificationResource);
		singletons.add(wsdlExposingResource);
		singletons.add(servicesResource);

	}

	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> empty = new HashSet<Class<?>>();

	@Override
	public Set<Class<?>> getClasses() {
		return empty;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}

}
