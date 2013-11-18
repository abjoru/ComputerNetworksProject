package edu.fit.cs.computernetworks.model;

import edu.fit.cs.computernetworks.topology.Node;

public interface Address {

	boolean matches(Node self);

}
