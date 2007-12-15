package org.woped.qualanalysis.reachabilitygraph.data;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.woped.core.analysis.StructuralAnalysis;
import org.woped.core.controller.IEditor;
import org.woped.core.model.AbstractElementModel;
import org.woped.core.model.ArcModel;
import org.woped.core.model.ModelElementContainer;
import org.woped.core.model.PetriNetModelProcessor;
import org.woped.core.model.petrinet.OperatorTransitionModel;
import org.woped.core.model.petrinet.PetriNetModelElement;
import org.woped.core.model.petrinet.PlaceModel;
import org.woped.core.model.petrinet.SubProcessModel;
import org.woped.core.model.petrinet.TransitionModel;
import org.woped.core.utilities.LoggerManager;
import org.woped.qualanalysis.Constants;

public class BuildReachability {
	IEditor thisEditor=null;
	private Map<String, AbstractElementModel>                    allTransitions        = null;
	private PetriNetModelProcessor petrinet              = null;
	public BuildReachability(IEditor thisEditor){
		this.thisEditor=thisEditor;
		this.petrinet = (PetriNetModelProcessor)thisEditor.getModelProcessor();
		allTransitions = getPetriNet().getElementContainer().getElementsByType(PetriNetModelElement.TRANS_SIMPLE_TYPE);
        allTransitions.putAll(getPetriNet().getElementContainer().getElementsByType(PetriNetModelElement.TRANS_OPERATOR_TYPE));
        allTransitions.putAll(getPetriNet().getElementContainer().getElementsByType(PetriNetModelElement.SUBP_TYPE)); 
        Marking begin_stat=new Marking(thisEditor);
        //Get Current Active Transitions
        checkNet();
        
        //Create MarkingList and add Current Status to List
        MarkingList markings=new MarkingList();
        markings.addMarking(new Marking(thisEditor));
        
        //Create Reachabilitylist
        ReachabilityDataSet transactions=new ReachabilityDataSet();
        
        //Start Iterator and mark current status
        markings.getIterator();
        int hash=markings.gross();
        
        while(markings.hasNext()){
        	//Get Current Marking and the possible Transitions to Activate
        	Marking start=markings.getMarking();
        	// Set all Places according to start Marking
        	setStatus(start);
        	Iterator netIt=start.getTransitions().iterator();
        	
        	// Activate every Transition that can be activated from the current Marking
        	while(netIt.hasNext()){
        		resetVirtualTokensInElementContainer(getPetriNet().getElementContainer());
        		checkNet();        		
        		TransitionModel trans= (TransitionModel) getPetriNet().getElementContainer().getElementById(netIt.next());
        		
        		//Use the Arc Method for those transitions who need it
        		if(trans.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE &&
        		 (    ((OperatorTransitionModel) trans).getOperatorType() == OperatorTransitionModel.XOR_SPLIT_TYPE
        		   || ((OperatorTransitionModel) trans).getOperatorType() == OperatorTransitionModel.ANDJOIN_XORSPLIT_TYPE
        		   || ((OperatorTransitionModel) trans).getOperatorType() == OperatorTransitionModel.XOR_JOIN_TYPE
        		   || ((OperatorTransitionModel) trans).getOperatorType() == OperatorTransitionModel.XORJOIN_ANDSPLIT_TYPE
        		   || ((OperatorTransitionModel) trans).getOperatorType() == OperatorTransitionModel.XOR_SPLITJOIN_TYPE
        		)
        		){  
        			Iterator outgoingIter;
        			if(((OperatorTransitionModel) trans).getOperatorType() == OperatorTransitionModel.XOR_JOIN_TYPE){
        				outgoingIter = getPetriNet().getElementContainer().getIncomingArcs(trans.getId()).keySet().iterator();
        			}
        			else{
        				outgoingIter = getPetriNet().getElementContainer().getOutgoingArcs(trans.getId()).keySet().iterator();
        			}
        		    while (outgoingIter.hasNext())
        		    {
        		    	resetVirtualTokensInElementContainer(getPetriNet().getElementContainer());
        		    	checkNet(); 
        		    	ArcModel arc=getPetriNet().getElementContainer().getArcById(outgoingIter.next());
        		    	if(arc.isActivated()){
         		    	arcClicked(arc);
        		    	checkNet();
        		    	Marking ende=new Marking(thisEditor);
                		markings.addMarking(ende);
                		transactions.add(start, trans.getId(), ende);
        		    	}
        		    }
        			
        		}
        		else {
        		transitionClicked(trans);
        		checkNet();
        		Marking ende=new Marking(thisEditor);
        		markings.addMarking(ende);
        		transactions.add(start, trans.getId(), ende);
        		}
        	}
        	
        	//If new Markings were added to the markings set, start all over again
        	if(markings.gross()!=hash){
        		hash=markings.gross();
        		markings.getIterator();
        	}
        }
        new Marking(thisEditor).printseq();
        markings.print();
        transactions.print();
        setStatus(begin_stat);
        checkNet();
        resetTransitionStatus();
	}

private void checkNet()
{
    long begin = System.currentTimeMillis();
    LoggerManager.debug(Constants.QUALANALYSIS_LOGGER, "TokenGame: CHECK NET");
    Iterator transIter = allTransitions.keySet().iterator();

    resetArcStatus();
    // Iterate over all Transitions
    while (transIter.hasNext())
    {
        checkTransition((TransitionModel) allTransitions.get(transIter.next()));
    }
    // Have a look at sink places
    // and see whether we need to activate them
    // Do so only inside of sub-processes
    /*
    if ((petrinet.getElementContainer().getOwningElement()!=null)&&(sinkPlaces!=null))
    {
    	Iterator<PlaceModel> i = sinkPlaces.iterator();
    	while (i.hasNext())
    	{
    		PlaceModel currentSink = i.next();
    		currentSink.setActivated(currentSink.getVirtualTokenCount()>0);
    	}        	
    }  
    */            
    LoggerManager.debug(Constants.QUALANALYSIS_LOGGER, "           ... DONE (" + (System.currentTimeMillis() - begin) + " ms)");
}
private void checkTransition(TransitionModel transition)
{
    transition.setActivated(false);
    transition.setFireing(false);
    Map incomingArcs = getPetriNet().getElementContainer().getIncomingArcs(transition.getId());
    // temporary variables

    if (transition.getType() == PetriNetModelElement.TRANS_SIMPLE_TYPE || transition.getType() == PetriNetModelElement.SUBP_TYPE)
    {
        if (countIncomingActivePlaces(incomingArcs) == incomingArcs.size()) transition.setActivated(true);
    } else if (transition.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE)
    {
        OperatorTransitionModel operator = (OperatorTransitionModel) transition;
        if (operator.getOperatorType() == OperatorTransitionModel.AND_JOIN_TYPE || operator.getOperatorType() == OperatorTransitionModel.AND_SPLIT_TYPE 
        		|| operator.getOperatorType() == OperatorTransitionModel.AND_SPLITJOIN_TYPE)
        {
            if (countIncomingActivePlaces(incomingArcs) == incomingArcs.size()) transition.setActivated(true);

        } else if ((operator.getOperatorType() == OperatorTransitionModel.XOR_SPLIT_TYPE)||
        		(operator.getOperatorType() == OperatorTransitionModel.ANDJOIN_XORSPLIT_TYPE))
        {
            if (countIncomingActivePlaces(incomingArcs) == incomingArcs.size())
            {
                setOutgoingArcsActive(transition.getId(), true);
                // added mf
                transition.setActivated(true);
                operator.setFireing(true);
            }
        } else if ((operator.getOperatorType() == OperatorTransitionModel.XOR_JOIN_TYPE)||
        		(operator.getOperatorType() == OperatorTransitionModel.XORJOIN_ANDSPLIT_TYPE))
        {
            if (countIncomingActivePlaces(incomingArcs) > 0)
            {
                setIncomingArcsActive(transition.getId(), true);
                // added mf
                transition.setActivated(true);
                operator.setFireing(true);
            }
        } else if (operator.getOperatorType() == OperatorTransitionModel.XOR_SPLITJOIN_TYPE)
        {
            // This is the XOR split-join combination type.
            // Check whether the center place already contains (a) token(s)
            if ((operator.getCenterPlace() != null) && (operator.getCenterPlace().getVirtualTokenCount() > 0))
            {
                // The center place does in fact have at least one token.
                // We have to make the outgoing arcs active
                setOutgoingArcsActive(transition.getId(), true);
                // Set this transition active
                // added mf
                transition.setActivated(true);
                operator.setFireing(true);
            } else
            {
                // There must at least be one token at the input side for
                // the transition to be
                // activated
                if (countIncomingActivePlaces(incomingArcs) > 0)
                {
                    // Activate all incoming arcs. This will allow the user
                    // to click them
                    // and choose where the token will come from
                    setIncomingArcsActive(transition.getId(), true);
                    // Set this transition active.
                    // added mf
                    transition.setActivated(true);
                    operator.setFireing(true);
                }
            }
        }
    }
}
private int countIncomingActivePlaces(Map arcsFromPlaces)
{
    Iterator incomingArcsIter = arcsFromPlaces.keySet().iterator();
    int activePlaces = 0;
    while (incomingArcsIter.hasNext())
    {
        ArcModel arc = getPetriNet().getElementContainer().getArcById(incomingArcsIter.next());
        try
        {
            PlaceModel place = (PlaceModel) getPetriNet().getElementContainer().getElementById(arc.getSourceId());
            if (place != null && place.getVirtualTokenCount() > 0)
            {
                // 	TODO: when ARC WEIGTH implemented check tokens >= weigth
                activePlaces++;
            }
        } catch (ClassCastException cce)
        {
            LoggerManager.warn(Constants.QUALANALYSIS_LOGGER, "TokenGame: Source not a Place. Ignore arc: " + arc.getId());
        }
    }
    return activePlaces;
}
private void setIncomingArcsActive(Object transitionId, boolean active)
{
    Iterator incomingArcsIter = getPetriNet().getElementContainer().getIncomingArcs(transitionId).keySet().iterator();
    while (incomingArcsIter.hasNext())
    {
        ArcModel arc = getPetriNet().getElementContainer().getArcById(incomingArcsIter.next());
        if (active)
        {
            PlaceModel place = (PlaceModel) getPetriNet().getElementContainer().getElementById(arc.getSourceId());
            if (place.getVirtualTokenCount() > 0)
            {
                arc.setActivated(true);
                // 	TODO: check weight
            }
        } else
        {
            arc.setActivated(false);
        }
    }
}

private void transitionClicked(TransitionModel transition)
{
    if (transition.isActivated())
    {
        // Rememeber whether we actually did something here
        // and only deactivate the transition after a *successful* click
        boolean actionPerformed = false;
        if (transition.getType() == PetriNetModelElement.TRANS_SIMPLE_TYPE || transition.getType() == PetriNetModelElement.SUBP_TYPE)
        {
            //LoggerManager.debug(Constants.EDITOR_LOGGER, "TokenGame: FIRE
            // simple Transition:
            // "+transition.getId());
            receiveTokens(getPetriNet().getElementContainer().getOutgoingArcs(transition.getId()));
            sendTokens(getPetriNet().getElementContainer().getIncomingArcs(transition.getId()));
            if (transition.getType() == PetriNetModelElement.SUBP_TYPE)
            {
            	
            	// the lower left half of the transition will trigger 'step into'
            actionPerformed = true;
        }
        }else if (transition.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE)
        {
            OperatorTransitionModel operator = (OperatorTransitionModel) transition;
            if (operator.getOperatorType() == OperatorTransitionModel.AND_JOIN_TYPE || operator.getOperatorType() == OperatorTransitionModel.AND_SPLIT_TYPE
            		|| operator.getOperatorType() == OperatorTransitionModel.AND_SPLITJOIN_TYPE)
            {
                //LoggerManager.debug(Constants.EDITOR_LOGGER, "TokenGame:
                // FIRE AND-Transition:
                // "+transition.getId());
                receiveTokens(getPetriNet().getElementContainer().getOutgoingArcs(transition.getId()));
                sendTokens(getPetriNet().getElementContainer().getIncomingArcs(transition.getId()));
                actionPerformed = true;

            }
            else if ((operator.getOperatorType() == OperatorTransitionModel.XOR_SPLIT_TYPE)||
            		(operator.getOperatorType() == OperatorTransitionModel.ANDJOIN_XORSPLIT_TYPE))
            {
                // Do nothing: Only controlled by Arc Clicking
            } else if ((operator.getOperatorType() == OperatorTransitionModel.XOR_JOIN_TYPE)||
            		(operator.getOperatorType() == OperatorTransitionModel.XORJOIN_ANDSPLIT_TYPE))
            {
                // Do nothing: Only controlled by Arc Clicking
            } else if (operator.getOperatorType() == OperatorTransitionModel.XOR_SPLITJOIN_TYPE)
            {
                // Do nothing: Only controlled by Arc Clicking as the user
                // has to select the
                // token source
            }
        }
        if (actionPerformed == true)
        {
            // Now update the status of the petri net by checking all
            // transitions and activating them
            // if their input conditions are fulfilled
            // This will also trigger a redraw
            checkNet();
        }
        }
}

private void sendTokens(Map arcsToFire)
{
    Iterator arcIter = arcsToFire.keySet().iterator();
    while (arcIter.hasNext())
    {
        sendTokens(getPetriNet().getElementContainer().getArcById(arcIter.next()));

    }
}

/*
 * Send (subtract) Tokens from the arc's source. ATTENTION: source must be a
 * Place.
 */
private void sendTokens(ArcModel arc)
{
    try
    {
        PlaceModel place = (PlaceModel) getPetriNet().getElementContainer().getElementById(arc.getSourceId());
        if (place != null)
        {
            place.sendToken();
            // TODO: when ARC WEIGTH implemented send tokens weigth times
        }
    } catch (ClassCastException cce)
    {
        LoggerManager.warn(Constants.QUALANALYSIS_LOGGER, "TokenGame: Cannot send token. Source is not a place. Ignore arc: " + arc.getId());
    }
}

/*
 * Receive (add) Tokens to the places that are the target or the Map-filled
 * arcs. ATTENTION: targets must be places.
 */
private void receiveTokens(Map arcsToFire)
{
    Iterator arcIter = arcsToFire.keySet().iterator();
    while (arcIter.hasNext())
    {
        receiveTokens(getPetriNet().getElementContainer().getArcById(arcIter.next()));
    }
}

/*
 * Receive (add) Tokens to the place that is the target of the arc.
 * ATTENTION: Target must be a place.
 */
private void receiveTokens(ArcModel arc)
{
    try
    {
        PlaceModel place = (PlaceModel) getPetriNet().getElementContainer().getElementById(arc.getTargetId());
        if (place != null)
        {
            place.receiveToken();
            // TODO: when ARC WEIGTH implemented receive tokens weigth times
        }
    } catch (ClassCastException cce)
    {
        LoggerManager.warn(Constants.QUALANALYSIS_LOGGER, "TokenGame: Cannot receive token. Target is not a place. Ignore arc: " + arc.getId());
    }
}




private void setOutgoingArcsActive(Object transitionId, boolean active)
{
    Iterator outgoingIter = getPetriNet().getElementContainer().getOutgoingArcs(transitionId).keySet().iterator();
    while (outgoingIter.hasNext())
    {
    	getPetriNet().getElementContainer().getArcById(outgoingIter.next()).setActivated(active);
    }
}

private void resetArcStatus()
{
    Iterator arcIter = getPetriNet().getElementContainer().getArcMap().keySet().iterator();
    while (arcIter.hasNext())
    {
        getPetriNet().getElementContainer().getArcById(arcIter.next()).setActivated(false);
    }
}
private void resetVirtualTokensInElementContainer(ModelElementContainer container)
{
    // restore origin tokencount
    Iterator placeIter = container.getElementsByType(PetriNetModelElement.PLACE_TYPE).keySet().iterator();
    while (placeIter.hasNext())
    {
        ((PlaceModel) container.getElementById(placeIter.next())).resetVirtualTokens();
    }    	
    Iterator subpIter = container.getElementsByType(PetriNetModelElement.SUBP_TYPE).keySet().iterator();
    while (subpIter.hasNext())
    {
    	// Now we call ourselves recursively for all sub-processes
    	ModelElementContainer innerContainer =
    		((SubProcessModel) container.getElementById(subpIter.next())).getSimpleTransContainer();
    	resetVirtualTokensInElementContainer(innerContainer);
    }    	        
}

private void arcClicked(ArcModel arc)
{
    if (arc.isActivated())
    {
        PetriNetModelElement source = (PetriNetModelElement) getPetriNet().getElementContainer().getElementById(arc.getSourceId());
        PetriNetModelElement target = (PetriNetModelElement) getPetriNet().getElementContainer().getElementById(arc.getTargetId());

        OperatorTransitionModel tempOperator;

        // As a reminder, an arc is generally going from a place to a
        // transition or from a
        // transition to a place.
        // When pointing to a transition it is referencing a potential
        // provider of a token.
        // When pointing to a place that place is potential receiver for a
        // token.
        // First, we check if the origin of our clicked arc is a transition
        // (Note that we check for the operator type only as ordinary
        // transitions are not triggered
        // by clicking the arrow but by clicking the transition itself which
        // is handled in transitionClicked())
        if (source.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE)
        {
            tempOperator = (OperatorTransitionModel) source;
            if (tempOperator.isFireing())
            {
                if (tempOperator.getOperatorType() == OperatorTransitionModel.XOR_SPLIT_TYPE || tempOperator.getOperatorType() == OperatorTransitionModel.XOR_SPLITJOIN_TYPE ||
                		tempOperator.getOperatorType() == OperatorTransitionModel.ANDJOIN_XORSPLIT_TYPE)
                {
                    receiveTokens(arc);
                    if (tempOperator.getOperatorType() != OperatorTransitionModel.XOR_SPLITJOIN_TYPE) sendTokens(getPetriNet().getElementContainer().getIncomingArcs(tempOperator.getId()));
                    else
                    {
                        // Special code for splitjoin. We have to take the
                        // token from the center place
                        if (tempOperator.getCenterPlace() != null)
                        // FIXME: Once implemented, this place will also
                        // have to remove weighted tokens
                        tempOperator.getCenterPlace().sendToken();
                    }
                }
            }
        } else if (target.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE)
        {
            tempOperator = (OperatorTransitionModel) target;
            if (tempOperator.getOperatorType() == OperatorTransitionModel.XOR_JOIN_TYPE ||
            		tempOperator.getOperatorType() == OperatorTransitionModel.XORJOIN_ANDSPLIT_TYPE ||
            		tempOperator.getOperatorType() == OperatorTransitionModel.XOR_SPLITJOIN_TYPE)
            {
                sendTokens(arc);
                if (tempOperator.getOperatorType() != OperatorTransitionModel.XOR_SPLITJOIN_TYPE) receiveTokens(getPetriNet().getElementContainer().getOutgoingArcs(tempOperator.getId()));
                else
                {
                    // Special code for splitjoin. We have to send the token
                    // to the center place
                    if (tempOperator.getCenterPlace() != null)
                    // FIXME: Once implemented, this place will also have to
                    // receive weighted tokens
                    tempOperator.getCenterPlace().receiveToken();
                }
            }
        }
        // Update net status
        // and trigger redraw
        checkNet();

    }
}

public void setStatus(Marking marking){
	TreeMap<String,Integer> neu=marking.getMarking();
	Iterator placeIter = getPetriNet().getElementContainer().getElementsByType(PetriNetModelElement.PLACE_TYPE).keySet().iterator();
	
	// Set Current tokens according to the current Element
    while (placeIter.hasNext())
    {
    	String place=(String) placeIter.next();
        ((PlaceModel) getPetriNet().getElementContainer().getElementById(place)).setTokens(neu.get(place));
    }
}
private void resetTransitionStatus()
{
    Iterator eleIter = allTransitions.keySet().iterator();
    TransitionModel transition;
    // Iterate over all Transitions
    while (eleIter.hasNext())
    {
        transition = (TransitionModel) allTransitions.get(eleIter.next());
        transition.setActivated(false);
        transition.setFireing(false);
        if (transition.getType() == PetriNetModelElement.TRANS_OPERATOR_TYPE)
        {
            // When starting a new token game we have to reset all center
            // places that
            // may contain tokens
            OperatorTransitionModel tempOperator = (OperatorTransitionModel) transition;
            if (tempOperator.getCenterPlace() != null) tempOperator.getCenterPlace().resetVirtualTokens();
        }
    }
}

private PetriNetModelProcessor getPetriNet()
{
    return petrinet;
}
}