//
//  SuccessorSelector.java
//  JavaFF
//
//  Created by Andrew Coles on Thu Jan 31 2008.
//

package javaff.search;

import javaff.planning.State;
import java.util.Set;

public interface SuccessorSelector {

	State choose(Set toChooseFrom);

};
