package edu.fit.cs.computernetworks.topology;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
	@Type(name="host", value=Host.class),
	@Type(name="router", value=Router.class)
})
public abstract class Node {

	public String id;

	@JsonProperty
	@Deprecated
	public List<String> links;
	
	public List<RoutingEntry> routing;
	
	public List<String> getLinks() {
		if (links == null) {
			links = new ArrayList<>();
		}
		return links;
	}
}
