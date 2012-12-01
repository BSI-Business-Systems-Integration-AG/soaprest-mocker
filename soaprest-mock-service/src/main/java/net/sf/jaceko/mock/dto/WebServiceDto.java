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
package net.sf.jaceko.mock.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.jaceko.mock.application.enums.ServiceType;

import com.google.common.base.Objects;

@XmlRootElement(name = "service")
public class WebServiceDto {

	private String endpointUri;
	private String wsdlUri;
	private ServiceType type;
	private String name;
	private final List<OperationRefDto> operationRefs = new ArrayList<OperationRefDto>();

	@XmlElementWrapper(name="operations")
	@XmlElement(name = "operation-ref")
	public List<OperationRefDto> getOperationRefs() {
		return operationRefs;
	}

	@XmlAttribute(name = "name")
	public String getName() {
		return name;
	}

	@XmlAttribute(name = "type")
	public ServiceType getType() {
		return type;
	}

	@XmlAttribute(name = "endpoint-uri")
	public String getEndpointUri() {
		return endpointUri;
	}

	@XmlAttribute(name = "wsdl-uri")
	public String getWsdlUri() {
		return wsdlUri;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(ServiceType type) {
		this.type = type;
	}

	public void setWsdlUri(String wsdlUri) {
		this.wsdlUri = wsdlUri;
	}

	public void setEndpointUri(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(endpointUri, wsdlUri, type, name);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof WebServiceDto) {
			WebServiceDto that = (WebServiceDto) object;
			return Objects.equal(this.endpointUri, that.endpointUri) && Objects.equal(this.wsdlUri, that.wsdlUri)
					&& Objects.equal(this.type, that.type) && Objects.equal(this.name, that.name);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("endpointUri", endpointUri).add("wsdlUri", wsdlUri).add("type", type)
				.add("name", name).toString();
	}

}
