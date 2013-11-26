package edu.fit.cs.computernetworks.topology;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

import edu.fit.cs.computernetworks.utils.IP;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
	@Type(name="host", value=Host.class),
	@Type(name="router", value=Router.class)
})
public abstract class Node {

	public String id;

	public List<RoutingEntry> routing;
	
	public abstract IP nextHopTo(final IP destIP);
	
}
