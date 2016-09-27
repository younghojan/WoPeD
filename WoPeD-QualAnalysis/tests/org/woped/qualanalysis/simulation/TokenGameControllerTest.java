package org.woped.qualanalysis.simulation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.PlaceholderConfigurerSupport;
import org.woped.core.controller.IEditor;
import org.woped.core.model.ArcModel;
import org.woped.core.model.CreationMap;
import org.woped.core.model.ModelElementContainer;
import org.woped.core.model.PetriNetModelProcessor;
import org.woped.core.model.petrinet.OperatorTransitionModel;
import org.woped.core.model.petrinet.PlaceModel;
import org.woped.core.model.petrinet.XORJoinSplitOperatorTransitionModel;

import java.beans.PropertyChangeSupport;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.woped.core.model.petrinet.AbstractPetriNetElementModel.PLACE_TYPE;
import static org.woped.core.model.petrinet.AbstractPetriNetElementModel.TRANS_OPERATOR_TYPE;

public class TokenGameControllerTest {

    private TokenGameController sut;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void arcClicked_arcNotActive_doesNothing() throws Exception {

        sut = createTestInstance(createXorJoinNet());
        ArcModel inactiveArc = mock(ArcModel.class);
        when(inactiveArc.isActivated()).thenReturn(false);

        sut.arcClicked(inactiveArc);
        verify(inactiveArc, times(0)).getSourceId();
        verify(inactiveArc, times(0)).getTargetId();
    }

    @Test
    public void executeOperators_targetIsXorJoinOperator_sourceSendsTokens() throws Exception {

        sut = createTestInstance(createXorJoinNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        int initialTokens = 2;
        PlaceModel source = (PlaceModel) container.getElementById("p1");
        source.setTokens(initialTokens);

        int arcWeight = 1;
        ArcModel clickedArc = container.findArc("p1", "t1");
        clickedArc.setInscriptionValue(arcWeight);
        clickedArc.setActivated(true);

        sut.executeOperators(clickedArc);

        int expected = initialTokens - arcWeight;
        int actual = source.getVirtualTokenCount();

        assertEquals(expected, actual);
    }

    @Test
    public void executeOperators_targetIsXorJoinSplitOperatorIncomingArcClicked_centerPlaceReceivesTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();
        PlaceModel source = (PlaceModel) container.getElementById("p1");
        source.setTokens(1);

        OperatorTransitionModel target = (OperatorTransitionModel) container.getElementById("t1");
        PlaceModel centerPlace = target.getCenterPlace();

        int arcWeight = 3;
        ArcModel clickedArc = container.findArc(source.getId(), target.getId());
        clickedArc.setInscriptionValue(arcWeight); // outer arc weight does not influence inner arc weight

        sut.executeOperators(clickedArc);

        int expected = centerPlace.getTokenCount() + 1; // inner arc weight should always be one
        int actual = centerPlace.getVirtualTokenCount();

        assertEquals(expected, actual);
    }

    @Test
    public void executeOperators_sourceIsXorSplit_targetReceivesTokens() throws Exception {
        sut = createTestInstance(createXorSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        PlaceModel sourcePlace = (PlaceModel) container.getElementById("p1");
        PlaceModel targetPlace = (PlaceModel) container.getElementById("p2");
        ArcModel clickedArc = container.findArc("t1", "p2");
        int arcWeight = 1;

        sourcePlace.setTokens(1);
        clickedArc.setInscriptionValue(arcWeight);

        int expected = targetPlace.getVirtualTokenCount() + arcWeight;

        sut.executeOperators(clickedArc);
        int actual = targetPlace.getVirtualTokenCount();

        assertEquals(expected, actual);
    }

    @Test
    public void executeOperators_sourceIsXorJoinSplitOutgoingArcClicked_targetReceivesTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        XORJoinSplitOperatorTransitionModel transition = (XORJoinSplitOperatorTransitionModel) container.getElementById("t1");
        PlaceModel centerPlace = transition.getCenterPlace();
        centerPlace.setTokens(1);

        PlaceModel targetPlace = (PlaceModel) container.getElementById("p3");
        ArcModel clickedArc = container.findArc("t1", "p3");
        int arcWeight = 1;
        clickedArc.setInscriptionValue(arcWeight);

        int expected = targetPlace.getVirtualTokenCount() + arcWeight;

        sut.executeOperators(clickedArc);
        int actual = targetPlace.getVirtualTokenCount();

        assertEquals(expected, actual);
    }

    @Test
    public void reverseOperatorTransition_targetIsXorJoinOperator_sourceReceivesTokens() throws Exception {
        sut = createTestInstance(createXorJoinNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        PlaceModel sourcePlace = (PlaceModel) container.getElementById("p1");
        ArcModel reversedArc = container.findArc("p1", "t1");

        int expected = sourcePlace.getVirtualTokenCount() + reversedArc.getInscriptionValue();
        sut.reverseOperatorTransition(reversedArc);

        int actual = sourcePlace.getVirtualTokenCount();

        assertEquals(expected, actual);
    }

    @Test
    public void reverseOperatorTransition_targetIsXorJoinOperator_targetPlaceRemovesTokens() throws Exception {
        sut = createTestInstance(createXorJoinNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        PlaceModel targetPlace = (PlaceModel) container.getElementById("p3");
        targetPlace.setTokens(2);

        ArcModel reversedArc = container.findArc("p1", "t1");
        ArcModel outgoingArc = container.findArc("t1", "p3");

        int expected = targetPlace.getVirtualTokenCount() - outgoingArc.getInscriptionValue();
        sut.reverseOperatorTransition(reversedArc);

        int actual = targetPlace.getVirtualTokenCount();

        assertEquals(expected, actual);
    }

    @Test
    public void reverseOperatorTransition_targetIsXorJoinOperator_independentPlaceDoesNotReceiveTokens() throws Exception {
        sut = createTestInstance(createXorJoinNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        PlaceModel independentPlace = (PlaceModel) container.getElementById("p2");
        ArcModel reversedArc = container.findArc("p1", "t1");

        int expected = independentPlace.getVirtualTokenCount();
        sut.reverseOperatorTransition(reversedArc);

        int actual = independentPlace.getVirtualTokenCount();

        assertEquals(expected, actual);
    }

    @Test
    public void reverseOperatorTransition_targetIsXorJoinOperator_returnsTrue() throws Exception {
        sut = createTestInstance(createXorJoinNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("p1", "t1");

        boolean actionPerformed = sut.reverseOperatorTransition(reversedArc);
        assertTrue(actionPerformed);
    }

    @Test
    public void reversedOperatorTransition_targetIsXorJoinSplitOperator_centerPlaceRemovesToken() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("p1", "t1");
        reversedArc.setInscriptionValue(2);

        XORJoinSplitOperatorTransitionModel transition = (XORJoinSplitOperatorTransitionModel) container.getElementById(reversedArc.getTargetId());
        PlaceModel centerPlace = transition.getCenterPlace();
        centerPlace.setVirtualTokens(1);

        int expected = centerPlace.getVirtualTokenCount() - 1; // Indepentend of arc weight
        sut.reverseOperatorTransition(reversedArc);

        int actual = centerPlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reversedOperatorTransition_targetIsXorJoinSplitOperator_sourcePlaceReceivesTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("p1", "t1");

        PlaceModel sourcePlace = (PlaceModel) container.getElementById(reversedArc.getSourceId());
        int expected = sourcePlace.getVirtualTokenCount() + reversedArc.getInscriptionValue();
        sut.reverseOperatorTransition(reversedArc);

        int actual = sourcePlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reversedOperatorTransition_targetIsXorJoinSplitOperator_otherSourcePlacesDoNotChangeTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("p1", "t1");

        PlaceModel otherSourcePlace = (PlaceModel) container.getElementById("p2");
        int expected = otherSourcePlace.getVirtualTokenCount();
        sut.reverseOperatorTransition(reversedArc);

        int actual = otherSourcePlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reversedOperatorTransition_targetIsXorJoinSplitOperator_targetPlacesDoNotChangeTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("p1", "t1");

        PlaceModel targetPlace1 = (PlaceModel) container.getElementById("p3");
        PlaceModel targetPlace2 = (PlaceModel) container.getElementById("p4");
        int target1TokensBefore = targetPlace1.getVirtualTokenCount();
        int target2TokensBefore = targetPlace2.getVirtualTokenCount();
        sut.reverseOperatorTransition(reversedArc);

        int target1TokensAfter = targetPlace1.getVirtualTokenCount();
        int target2TokensAfter = targetPlace2.getVirtualTokenCount();
        assertEquals(target1TokensBefore, target1TokensAfter);
        assertEquals(target2TokensBefore, target2TokensAfter);
    }

    @Test
    public void reverseOperatorTransition_sourceIsXorSplitOperator_returnsTrue() throws Exception {
        sut = createTestInstance(createXorSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("t1", "p2");

        boolean actionPerformed = sut.reverseOperatorTransition(reversedArc);
        assertTrue(actionPerformed);
    }

    @Test
    public void reverseOperatorTransition_sourceIsXorSplitOperator_targetPlaceRemovesTokens() throws Exception {
        sut = createTestInstance(createXorSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("t1", "p2");
        PlaceModel targetPlace = (PlaceModel) container.getElementById(reversedArc.getTargetId());
        targetPlace.setVirtualTokens(reversedArc.getInscriptionValue());

        int expected = targetPlace.getVirtualTokenCount() - reversedArc.getInscriptionValue();
        sut.reverseOperatorTransition(reversedArc);

        int actual = targetPlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reverseOperatorTransition_sourceIsXorSplitOperator_sourcePlaceReceivesTokens() throws Exception {
        sut = createTestInstance(createXorSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("t1", "p2");
        ArcModel incomingArc = container.findArc("p1", "t1");
        PlaceModel sourcePlace = (PlaceModel) container.getElementById(incomingArc.getSourceId());

        int expected = sourcePlace.getVirtualTokenCount() + incomingArc.getInscriptionValue();
        sut.reverseOperatorTransition(reversedArc);

        int actual = sourcePlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reverseOperatorTransition_sourceIsXorSplitOperator_independentPlaceDoesNotChangeTokens() throws Exception {
        sut = createTestInstance(createXorSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("t1", "p2");
        PlaceModel independentPlace = (PlaceModel) container.getElementById("p3");

        int expected = independentPlace.getVirtualTokenCount();
        sut.reverseOperatorTransition(reversedArc);

        int actual = independentPlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reversedOperatorTransition_sourceIsXorJoinSplitOperator_centerPlaceReceivesToken() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();
        ArcModel reversedArc = container.findArc("t1", "p3");
        reversedArc.setInscriptionValue(2);

        XORJoinSplitOperatorTransitionModel transition = (XORJoinSplitOperatorTransitionModel) container.getElementById(reversedArc.getSourceId());
        PlaceModel centerPlace = transition.getCenterPlace();

        int expected = centerPlace.getVirtualTokenCount() + 1; // independent of arc weight
        sut.reverseOperatorTransition(reversedArc);

        int actual = centerPlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reversedOperatorTransition_sourceIsXorJoinSplitOperator_targetPlaceRemovesTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("t1", "p3");

        PlaceModel targetPlace = (PlaceModel) container.getElementById(reversedArc.getTargetId());
        targetPlace.setVirtualTokens(reversedArc.getInscriptionValue());

        int expected = targetPlace.getVirtualTokenCount() - reversedArc.getInscriptionValue();
        sut.reverseOperatorTransition(reversedArc);

        int actual = targetPlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reversedOperatorTransition_sourceIsXorJoinSplitOperator_otherTargetPlacesDoesNotChangeTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();

        ArcModel reversedArc = container.findArc("t1", "p3");
        PlaceModel otherTargetPlace = (PlaceModel) container.getElementById("p4");

        int expected = otherTargetPlace.getVirtualTokenCount();
        sut.reverseOperatorTransition(reversedArc);

        int actual = otherTargetPlace.getVirtualTokenCount();
        assertEquals(expected, actual);
    }

    @Test
    public void reversedOperatorTransition_sourceIsXorJoinSplitOperator_sourcePlacesDoNotChangeTokens() throws Exception {
        sut = createTestInstance(createXorJoinSplitNet());
        ModelElementContainer container = sut.getThisEditor().getModelProcessor().getElementContainer();
        ArcModel reversedArc = container.findArc("t1", "p3");

        PlaceModel source1 = (PlaceModel) container.getElementById("p1");
        PlaceModel source2 = (PlaceModel) container.getElementById("p2");
        source2.setVirtualTokens(source1.getVirtualTokenCount() + 2);

        int expectedSource1 = source1.getVirtualTokenCount();
        int expectedSource2 = source2.getVirtualTokenCount();
        sut.reverseOperatorTransition(reversedArc);

        int actualSource1 = source1.getVirtualTokenCount();
        int actualSource2 = source2.getVirtualTokenCount();
        assertEquals("First place should not change tokens", expectedSource1, actualSource1);
        assertEquals("Second place should not change tokens", expectedSource2, actualSource2);
    }

    private TokenGameController createTestInstance(PetriNetModelProcessor net) {

        IEditor editor = mock(IEditor.class);
        when(editor.getModelProcessor()).thenReturn(net);
        when(editor.getGraph()).thenReturn(null);

        PropertyChangeSupport propertyChangedSupport = new PropertyChangeSupport(editor);
        return new TokenGameController(editor, propertyChangedSupport);
    }

    private PetriNetModelProcessor createXorJoinNet() {
        PetriNetModelProcessor processor = new PetriNetModelProcessor();

        CreationMap placeMap = CreationMap.createMap();
        placeMap.setType(PLACE_TYPE);
        for (int i = 1; i <= 3; i++) {
            placeMap.setId("p" + i);
            processor.createElement(placeMap);
        }

        CreationMap transitionMap = CreationMap.createMap();
        transitionMap.setType(TRANS_OPERATOR_TYPE);
        transitionMap.setOperatorType(OperatorTransitionModel.XOR_JOIN_TYPE);
        transitionMap.setId("t1");
        processor.createElement(transitionMap);

        processor.createArc("p1", "t1");
        processor.createArc("p2", "t1");
        processor.createArc("t1", "p3");

        return processor;
    }

    private PetriNetModelProcessor createXorJoinSplitNet() {
        PetriNetModelProcessor processor = new PetriNetModelProcessor();

        CreationMap placeMap = CreationMap.createMap();
        placeMap.setType(PLACE_TYPE);
        for (int i = 1; i <= 4; i++) {
            placeMap.setId("p" + i);
            processor.createElement(placeMap);
        }

        CreationMap transitionMap = CreationMap.createMap();
        transitionMap.setType(TRANS_OPERATOR_TYPE);
        transitionMap.setOperatorType(OperatorTransitionModel.XORJOIN_XORSPLIT_TYPE);
        transitionMap.setId("t1");
        processor.createElement(transitionMap);

        processor.createArc("p1", "t1");
        processor.createArc("p2", "t1");
        processor.createArc("t1", "p3");
        processor.createArc("t1", "p4");

        return processor;
    }

    private PetriNetModelProcessor createXorSplitNet() {
        PetriNetModelProcessor processor = new PetriNetModelProcessor();

        CreationMap placeMap = CreationMap.createMap();
        placeMap.setType(PLACE_TYPE);
        for (int i = 1; i <= 3; i++) {
            placeMap.setId("p" + i);
            processor.createElement(placeMap);
        }

        CreationMap transitionMap = CreationMap.createMap();
        transitionMap.setType(TRANS_OPERATOR_TYPE);
        transitionMap.setOperatorType(OperatorTransitionModel.XOR_SPLIT_TYPE);
        transitionMap.setId("t1");
        processor.createElement(transitionMap);

        processor.createArc("p1", "t1");
        processor.createArc("t1", "p2");
        processor.createArc("t1", "p3");

        return processor;
    }
}