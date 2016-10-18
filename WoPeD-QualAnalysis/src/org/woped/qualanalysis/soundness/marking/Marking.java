package org.woped.qualanalysis.soundness.marking;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.woped.qualanalysis.soundness.algorithms.generic.INode;
import org.woped.qualanalysis.soundness.datamodel.PlaceNode;
import org.woped.qualanalysis.soundness.datamodel.TransitionNode;

/**
 * @see IMarking
 * 
 * @author Patrick Spies, Patrick Kirchgaessner, Joern Liebau, Enrico Moeller, Sebastian Fuss
 */
public class Marking implements IMarking, INode<Marking> {
    // declaration
	private final Map<PlaceNode, Integer> placeToIndexMap = new HashMap<PlaceNode, Integer>();
    private final PlaceNode[] places;
    private final int[] tokens;
    private final boolean[] placeUnlimited;
    private final Set<Arc> successors = new HashSet<Arc>();
    private Marking predecessor;
    private boolean isInitial = false;
    private int markingID;
    
    private static int markingCounter = 0;
    
    // Cache the hash code unless something changes in our marking. -1 means the hash code
    // needs to be updated.
    private int cachedHashCode = -1;    

    /**
     * 
     * @param tokens an array with the number of tokens for each place in the same order as places
     * @param places all Places in the right order
     * @param placeUnlimited an array with true where the places are unlimited in the same order as places
     */
    public Marking(int[] tokens, PlaceNode[] places, boolean[] placeUnlimited) {
        this.places = places;
        this.tokens = tokens.clone();
        this.placeUnlimited = placeUnlimited.clone();
        for (int i = 0; i < tokens.length; i++) {
            placeToIndexMap.put(places[i], new Integer(i));            
        }
        markingID = markingCounter;
        markingCounter++;
    }
    
    public String getID(){
    	return ""+markingID;
    }

    @Override
    public int hashCode() {
    	if (cachedHashCode != -1) {
    		return cachedHashCode;
    	}
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(placeUnlimited);
        result = prime * result + Arrays.hashCode(tokens);
        cachedHashCode = result;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Marking other = (Marking) obj;

        if (!Arrays.equals(placeUnlimited, other.placeUnlimited)) {
            return false;
        }
        
        for (int i = 0; i < tokens.length; i++) {
        	// We only need to check one of the two unlimited arrays, the other one must be the same
        	// due to the Arrays.equals() check above.
			if (!placeUnlimited[i]
					&& tokens[i] != other.tokens[i]) {
				return false;
			}
        }
        return true;
    }

    /**
     * @param successor the succeeding arc
     * @return the success of adding the arc
     */
    public boolean addSuccessor(Arc successor) {
    	return successors.add(successor);
    }

    /**
     * @return a TreeMap with the placeId as String and the number of tokens on the key ID as value
     */
    public TreeMap<String, Integer> getMarking() {
        TreeMap<String, Integer> marking = new TreeMap<String, Integer>();
        for (int i = 0; i < places.length; i++) {
            marking.put(places[i].getId(), tokens[i]);
        }
        return marking;
    }

    /**
     * 
     * @return the array where UnlimitedPlaces are marked as true. 
     * Do not manipulate the returned array!
     */
    public boolean[] getPlaceUnlimited() {
        return placeUnlimited;
    }
    
    /**
     * @return the predecessor (marking)
     */
    public Marking getPredecessor() {
        return predecessor;
    }

    /**
     * @return the arcs that point to the successors (Set<Arc>)
     */
    public Set<Arc> getSuccessors() {
        return this.successors;
    }

    /**
     * @return the tokens
     * Do not manipulate the returned array!
     */
    public int[] getTokens() {
        return this.tokens;
    }

    /**
     * @see IMarking#getActivatedTransitions()
     */
    @Override
    public HashSet<String> getActivatedTransitions() {
        HashSet<String> transitions = new HashSet<String>();
        for (Arc arc : getSuccessors()) {
            transitions.add(arc.getTrigger().getOriginId());
        }
        return transitions;
    }
    
    /**
     * 
     * @param compareMarking marking to compare
     * @return true, if markings are comparable and the marking is smaller or equal than the provided marking.
     */
    public boolean smallerEquals(Marking compareMarking) {
        boolean smallerEquals = true;
        for (int i = 0; i < this.tokens.length && smallerEquals; i++) {
			if (!compareMarking.placeUnlimited[i]
					&& (this.placeUnlimited[i] || this.tokens[i] > compareMarking.tokens[i])) {
				smallerEquals = false;
			}
        }
        return smallerEquals;
    }
    
    /**
     * Returns the index of a given place or -1 if not found
     * @param place
     * @return
     */
    public int getIndexByPlace(PlaceNode place) {
    	Integer index = placeToIndexMap.get(place);
    	if (index == null) {
    		return -1;
    	} else {
    		return index.intValue();
    	}
    }
    
    /**
     * @return the isInitial
     */
    public boolean isInitial() {
        return isInitial;
    }

    /**
     * 
     * checks if the given transition is reachable from the current marking
     * 
     * @param tn the transition to reach
     * @param markings a set of markings already checked
     * @return true if the Transition is reachable
     */
    public boolean isTransitionReachable(TransitionNode tn, Set<Marking> markings) {
        for (Arc arc : successors) {
            if (arc.getTrigger().equals(tn)) {
                return true;
            }
            if (!markings.contains(arc.getTarget())) {
                markings.add(arc.getTarget());
                if (arc.getTarget().isTransitionReachable(tn, markings)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the predecessor as string
     */
    public String predecessorToString() {
        String line = "Predecessor:";
        if (this.predecessor != null) {
            line += "\r\n" + this.predecessor.toString();
        }
        return line;

    }

    /**
     * @param isInitial the isInitial to set
     */
    public void setInitial(boolean isInitial) {
        this.isInitial = isInitial;
    }

    /**
     * @param position position of the place being unlimited
     */
    public void setPlaceUnlimited(Integer position) {
        this.placeUnlimited[position] = true;
        // Reset token count for the new unlimited place to ensure we generate
        // the same hash for equivalent representations.
        this.tokens[position] = 0;
        // We need to recalculate the hash if we do this.
        cachedHashCode = -1; 
    }

    /**
     * @param predecessor the predecessor to set
     */
    public void setPredecessor(Marking predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * @return the token-array as string
     */
    @Override
    public String toString() {
        String line = "(";
        for (int i = 0; i < this.tokens.length; i++) {

            if (this.placeUnlimited[i]) {
                line += "\u221E";
            } else {
                line += this.tokens[i];
            }
            line += " ";
        }
        return line.substring(0, line.length() - 1) + ")";
    }

    /**
     * @see INode#getPostNodes()
     */
    @Override
    public Set<Marking> getPostNodes() {
        Set<Marking> set = new HashSet<Marking>();
        for (Arc arc : getSuccessors()) {
            set.add(arc.getTarget());
        }
        return set;
    }

    /**
     * @see INode#getPreNodes()
     */
    @Override
    public Set<Marking> getPreNodes() {
        Set<Marking> set = new HashSet<Marking>();
        set.add(predecessor);
        return set;
    }
}
