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

import static java.util.Collections.synchronizedList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.jaceko.mock.model.request.MockRequest;

public class RecordedRequestsHolder {
	private MockConfigurationHolder configurationHolder;

	private final ConcurrentMap<String, ConcurrentMap<String, Collection<MockRequest>>> recordedRequestsMap = new ConcurrentHashMap<String, ConcurrentMap<String, Collection<MockRequest>>>();

	public void recordRequest(String serviceName, String operationId, String requestBody, String queryString, String resourceId) {
		ConcurrentMap<String, Collection<MockRequest>> requestsPerOperationMap = fetchRequestsPerOperationMap(serviceName);

		Collection<MockRequest> recordedRequests = fetchRecordedRequests(operationId, requestsPerOperationMap);

		MockRequest request = new MockRequest(requestBody, queryString, resourceId);
		recordedRequests.add(request);
	}

	private Collection<MockRequest> fetchRecordedRequests(String operationId,
			ConcurrentMap<String, Collection<MockRequest>> requestsPerOperationMap) {
		requestsPerOperationMap.putIfAbsent(operationId, synchronizedList(new ArrayList<MockRequest>()));
		return requestsPerOperationMap.get(operationId);
	}

	private ConcurrentMap<String, Collection<MockRequest>> fetchRequestsPerOperationMap(String serviceName) {
		recordedRequestsMap.putIfAbsent(serviceName, new ConcurrentHashMap<String, Collection<MockRequest>>());
		return recordedRequestsMap.get(serviceName);
	}

	public Collection<String> getRecordedRequestBodies(String serviceName, String operationId) {
		Collection<MockRequest> recordedRequests = getRecordedRequests(serviceName, operationId);

		Collection<String> recordedRequestBodies = new ArrayList<String>();
		for (MockRequest request : recordedRequests) {
			recordedRequestBodies.add(request.getBody());
		}
		return recordedRequestBodies;

	}

	public Collection<String> getRecordedUrlParams(String serviceName, String operationId) {
		Collection<MockRequest> recordedRequests = getRecordedRequests(serviceName, operationId);

		Collection<String> recordedRequestParams = new ArrayList<String>();
		for (MockRequest request : recordedRequests) {
			recordedRequestParams.add(request.getQueryString());
		}
		return recordedRequestParams;
	}

	public Collection<MockRequest> getRecordedRequests(String serviceName, String operationId) {
		configurationHolder.getWebServiceOperation(serviceName, operationId);
		Map<String, Collection<MockRequest>> requestsPerOperationMap = recordedRequestsMap.get(serviceName);
		if (requestsPerOperationMap == null) {
			return Collections.emptyList();
		}
		Collection<MockRequest> recordedRequests = requestsPerOperationMap.get(operationId);
		if (recordedRequests == null) {
			return Collections.emptyList();
		}
		return recordedRequests;
	}

	public Collection<String> getRecordedResourceIds(String serviceName, String operationId) {
		Collection<MockRequest> recordedRequests = getRecordedRequests(serviceName, operationId);

		Collection<String> recordedResourceIds = new ArrayList<String>();
		synchronized (recordedRequests) {
			for (MockRequest request : recordedRequests) {
				recordedResourceIds.add(request.getResourceId());
			}
		}
		return recordedResourceIds;
	}

	public void clearRecordedRequests(String serviceName, String operationId) {
		getRecordedRequests(serviceName, operationId).clear();
	}

	public void setMockserviceConfiguration(MockConfigurationHolder configurationHolder) {
		this.configurationHolder = configurationHolder;
	}

}
