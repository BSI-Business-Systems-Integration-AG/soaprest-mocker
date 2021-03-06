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
package net.sf.jaceko.mock.resource;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.jaceko.mock.application.enums.HttpMethod;
import net.sf.jaceko.mock.model.request.MockResponse;
import net.sf.jaceko.mock.service.RequestExecutor;

import org.apache.log4j.Logger;

@Path("/services/REST/{serviceName}/endpoint")
public class RestEndpointResource {
  private static final Logger LOG = Logger.getLogger(RestEndpointResource.class);

  private RequestExecutor svcLayer;

  @GET
  @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public Response performGetRequest(@PathParam("serviceName") String serviceName, @Context HttpServletRequest request, @Context HttpHeaders headers) {

    return performGetRequest(serviceName, request, null, headers);
  }

  @GET
  @Path("/{resourceId}")
  public Response performGetRequest(@PathParam("serviceName") String serviceName, @Context HttpServletRequest request, @PathParam("resourceId") String resourceId, @Context HttpHeaders headers) {
    MockResponse mockResponse = svcLayer.performRequest(serviceName, HttpMethod.GET.toString(), "", request.getQueryString(), resourceId, headers.getRequestHeaders());
    if (LOG.isDebugEnabled()) {
      LOG.debug("serviceName: " + serviceName + ", response:" + mockResponse);
    }
    return buildWebserviceResponse(mockResponse);
  }

  @POST
  @Consumes({"text/*", "application/*"})
  public Response performPostRequest(@PathParam("serviceName") String serviceName,
      @Context HttpServletRequest httpServletRequest, @Context HttpHeaders headers, String request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Http headers " + headers.getRequestHeader("aHeader"));
    }
    MockResponse mockResponse = svcLayer.performRequest(serviceName, HttpMethod.POST.toString(), request, httpServletRequest.getQueryString(), null, headers.getRequestHeaders());
    if (LOG.isDebugEnabled()) {
      LOG.debug("serviceName: " + serviceName + ", response:" + mockResponse);
    }
    return buildWebserviceResponse(mockResponse);
  }

  @PUT
  @Consumes({"text/*", "application/*"})
  public Response performPutRequest(@PathParam("serviceName") String serviceName, @Context HttpHeaders headers, String request) {
    return performPutRequest(serviceName, null, headers, request);
  }

  @PUT
  @Path("/{resourceId}")
  @Consumes({"text/*", "application/*"})
  public Response performPutRequest(@PathParam("serviceName") String serviceName, @PathParam("resourceId") String resourceId, @Context HttpHeaders headers, String request) {
    MockResponse mockResponse = svcLayer.performRequest(serviceName, HttpMethod.PUT.toString(), request, null, resourceId, headers.getRequestHeaders());
    if (LOG.isDebugEnabled()) {
      LOG.debug("serviceName: " + serviceName + ", response:" + mockResponse);
    }
    return buildWebserviceResponse(mockResponse);
  }

  @DELETE
  public Response performDeleteRequest(@PathParam("serviceName") String serviceName, @Context HttpHeaders headers) {
    return performDeleteRequest(serviceName, null, headers);
  }

  @DELETE
  @Path("/{resourceId}")
  public Response performDeleteRequest(@PathParam("serviceName") String serviceName, @PathParam("resourceId") String resourceId, @Context HttpHeaders headers) {
    MockResponse mockResponse = svcLayer.performRequest(serviceName, HttpMethod.DELETE.toString(), "", null, resourceId, headers.getRequestHeaders());
    if (LOG.isDebugEnabled()) {
      LOG.debug("serviceName: " + serviceName + ", response:" + mockResponse);
    }
    return buildWebserviceResponse(mockResponse);

  }

  private Response buildWebserviceResponse(MockResponse mockResponse) {
    Response.ResponseBuilder responseBuilder = Response.status(mockResponse.getCode()).entity(mockResponse.getBody()).type(mockResponse.getContentType());

    addHeadersToResponse(mockResponse.getHeaders(), responseBuilder);

    return responseBuilder.build();
  }

  private void addHeadersToResponse(Map<String, String> headersToReturn, Response.ResponseBuilder responseBuilder) {
    if (headersToReturn != null) {

      for (String headerKey : headersToReturn.keySet()) {
        String headerValue = headersToReturn.get(headerKey);
        responseBuilder.header(headerKey, headerValue);
      }
    }
  }

  public void setWebserviceMockService(RequestExecutor svcLayer) {
    this.svcLayer = svcLayer;
  }

}
