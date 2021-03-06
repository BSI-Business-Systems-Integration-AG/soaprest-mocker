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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.jaceko.mock.dom.DocumentImpl;
import net.sf.jaceko.mock.exception.ClientFaultException;
import net.sf.jaceko.mock.model.request.MockResponse;
import net.sf.jaceko.mock.service.RequestExecutor;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Path("/services/SOAP/{serviceName}/endpoint")
public class SoapEndpointResource {
  private static final Logger LOG = Logger.getLogger(SoapEndpointResource.class);

  private static final String BODY = "Body";
  private static final String ENVELOPE = "Envelope";
  private static final String INVALID_SOAP_REQUEST = "Invalid SOAP request";
  private static final String MALFORMED_XML = "Malformed Xml";
  private RequestExecutor service;

  @POST
  @Consumes(MediaType.TEXT_XML)
  @Produces(MediaType.TEXT_XML)
  public Response performRequest(@PathParam("serviceName") String serviceName,
      String request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("serviceName: " + serviceName + ", request:" + request);
    }
    String requestMessgageName = extractRequestMessageName(request);
    String responseBody = null;
    int code = 200;
    MockResponse response = service.performRequest(serviceName, requestMessgageName,
        request, null, null, null);
    if (response != null) {
      responseBody = response.getBody();
      code = response.getCode();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("serviceName: " + serviceName + ", response:" + responseBody + " ,code: " + code);
    }
    return Response.status(code).entity(responseBody).type(MediaType.TEXT_XML).build();
  }

  private String extractRequestMessageName(String request) {
    Document reqDocument = null;
    try {
      reqDocument = new DocumentImpl(request, true);
    }
    catch (Exception e) {
      throw new ClientFaultException(MALFORMED_XML, e);
    }
    reqDocument.normalize();
    Node envelope = getChildElement(reqDocument, ENVELOPE);
    Node body = getChildElement(envelope, BODY);
    Node requestMessage = getChildElement(body);

    if (requestMessage == null) {
      throw new ClientFaultException(INVALID_SOAP_REQUEST);
    }
    return requestMessage.getLocalName();

  }

  private Node getChildElement(Node parent) {
    NodeList childNodes = parent.getChildNodes();

    Node foundNode = null;
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        foundNode = childNode;
        break;
      }
    }
    return foundNode;
  }

  private Node getChildElement(Node parent, String elementName) {
    NodeList childNodes = parent.getChildNodes();

    Node foundNode = null;
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node childNode = childNodes.item(i);
      if (elementName.equals(childNode.getLocalName())) {
        foundNode = childNode;
        break;
      }
    }
    if (foundNode == null) {
      throw new ClientFaultException(INVALID_SOAP_REQUEST);
    }
    return foundNode;
  }

  public void setWebserviceMockService(RequestExecutor service) {
    this.service = service;
  }

}
