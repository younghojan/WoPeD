package WorldModelToPetrinet;

import TextToWorldModel.transform.DummyAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

public class PetriNet {

    public enum REFERENCE_DIRECTION {ingoing,outgoing,all};
    public enum ISOLATED_PLACE_TYPE {source,sink};

	private ArrayList<Arc> arcList = new ArrayList<Arc>();
	private ArrayList<Place> placeList = new ArrayList<Place>();
	private ArrayList<Transition> transitionList = new ArrayList<Transition>();
	private PetrinetElementBuilder elementBuilder;

	public PetriNet(){
        elementBuilder= new PetrinetElementBuilder();
    }

    public PetrinetElementBuilder getElementBuilder() {
        return elementBuilder;
    }
	
	public void add(Place p) {
		placeList.add(p);

	}

	public void add(Arc a) {
		arcList.add(a);
	}

	public void add(Transition t) {
		transitionList.add(t);
	}

	public ArrayList<Arc> getArcList() {
		return arcList;
	}

	public ArrayList<Place> getPlaceList() {
		return placeList;
	}

	public ArrayList<Transition> getTransitionList() {
		return transitionList;
	}

	public void transformToWorkflowNet(){
        healDanglingTransitions();
        unifySources();
        unifySinks();
    }

	public List<Place> getAllSources() {
		ArrayList<Place> targetPlaces = new ArrayList<Place>();
		Iterator<Arc> i = arcList.iterator();
		while (i.hasNext()) {
			Arc a = i.next();
			String elementID = a.getTarget();
			if (elementID.startsWith("p")) {
				// Place
				targetPlaces.add((Place)getPetrinetElementByID(elementID));
			}
		}
		
		ArrayList<Place> sources = new ArrayList<Place>();
		Iterator<Place> i2 = placeList.iterator();
		while (i2.hasNext()) {
			Place p = i2.next();
			boolean found = false;
			Iterator<Place> i3 = targetPlaces.iterator();
			while (i3.hasNext()) {
				Place p2 = i3.next();
				if (p.getID().equals(p2.getID())) {
					found = true;
				}
			}
			if (!found) {
				sources.add(p);
			}

		}
		return sources;
	}

	public String getPNML() {
		String PNML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<!--PLEASE DO NOT EDIT THIS FILE\n"
				+ "Created with Workflow PetriNet Designer Version 3.2.0 (woped.org)-->\n" + "<pnml>\n"
				+ "  <net type=\"http://www.informatik.hu-berlin.de/top/pntd/ptNetb\" id=\"noID\">";
		List<String> roleName = new ArrayList<String>();
		List<String> orgaUnit = new ArrayList<String>();
    
    

		Iterator<Place> i = placeList.iterator();
		while (i.hasNext()) {
			Place p = i.next();
			PNML = PNML + p.toString() + "\n";
		}

		Iterator<Transition> i2 = transitionList.iterator();
		while (i2.hasNext()) {
			Transition t = i2.next();
			String role = "";
			String orga = "";

			// Get RoleNames
			if (t.getRoleName() != null) {
				role = "";
				role = "<role Name='" + t.getRoleName() + "'/>";
				roleName.add(role);
			}

			// Get OrganisationalUnits
			if (!(t.getOrganizationalUnitName().contentEquals("default"))) {
				orga = "";
				orga = "<organizationUnit Name='" + t.getRoleName() + "'/>";
				orgaUnit.add(orga);

			}

			PNML = PNML + t.toString() + "\n";
		}
		
		//Delete Duplicates in RoleNames and OrganisationalUnits
		List<String> deleteDuplicates = new ArrayList<>(new HashSet<>(roleName));
		Iterator<String> it = deleteDuplicates.iterator();
		String rolenames="", unitnames="";
		while(it.hasNext()) {
			rolenames = rolenames + it.next().toString(); 
		}
		List<String> deleteDuplicates2 = new ArrayList<>(new HashSet<>(orgaUnit));
		it = deleteDuplicates2.iterator();
		while(it.hasNext()) {
			unitnames = unitnames + it.next().toString();
		}

		Iterator<Arc> i3 = arcList.iterator();
		while (i3.hasNext()) {
			Arc a = i3.next();
			PNML = PNML + a.toString() + "\n";
		}

		PNML = PNML + "<toolspecific tool=\"WoPeD\" version=\"1.0\">\n" 
				+ "      <bounds>\n"
				+ "        <position x=\"2\" y=\"25\"/>\n" 
				+ "        <dimension x=\"763\" y=\"574\"/>\n"
				+ "      </bounds>\n" 
				+ "      <scale>100</scale>\n" 
				+ "      <treeWidthRight>549</treeWidthRight>\n"
				+ "      <overviewPanelVisible>true</overviewPanelVisible>\n"
				+ "      <treeHeightOverview>100</treeHeightOverview>\n"
				+ "      <treePanelVisible>true</treePanelVisible>\n" 
				+ "      <verticalLayout>false</verticalLayout>\n"
				+ "      <resources>\n" 
				+ 			rolenames + "\n"
				+ 			unitnames + "\n" 
				+ "      </resources>\n" 
				+ "      <simulations/>\n"
				+ "      <partnerLinks/>\n" 
				+ "      <variables/>\n" 
				+ "    </toolspecific>" 
				+ "  </net>\n" 
				+ "</pnml>";
		return PNML;
	}

    public PetriNetElement getPetrinetElementByID(String ID){
        ArrayList<ArrayList<? extends PetriNetElement>> elementLists = new  ArrayList<ArrayList<? extends PetriNetElement>> ();
        elementLists.add(arcList);
        elementLists.add(placeList);
        elementLists.add(transitionList);
        Iterator<ArrayList<? extends PetriNetElement>> i = elementLists.iterator();
        while(i.hasNext()){
            ArrayList<? extends PetriNetElement> elementList = i.next();
            Iterator<? extends PetriNetElement> j = elementList.iterator();
            while(j.hasNext()){
                PetriNetElement element= j.next();
                if(element.getID().equals(ID)){
                    return element;
                }
            }
        }
        //if nothing is found
        return null;
    }

    public boolean removePetrinetElementByID(String ID){
        ArrayList<ArrayList<? extends PetriNetElement>> elementLists = new  ArrayList<ArrayList<? extends PetriNetElement>> ();
        elementLists.add(arcList);
        elementLists.add(placeList);
        elementLists.add(transitionList);
        Iterator<ArrayList<? extends PetriNetElement>> i = elementLists.iterator();
        while(i.hasNext()){
            ArrayList<? extends PetriNetElement> elementList = i.next();
            Iterator<? extends PetriNetElement> j = elementList.iterator();
            while(j.hasNext()){
                PetriNetElement element= j.next();
                if(element.getID().equals(ID)){
                    j.remove();
                    return true;
                }
            }
        }
        //if nothing is found
        return false;
    }

    public List<Arc> getAllReferencingArcsForElement(String ID, REFERENCE_DIRECTION directionRestriction ){
    PetriNetElement element = getPetrinetElementByID(ID);
    ArrayList<Arc> referencingArcs = new ArrayList<Arc>();
    if(element!=null){
        Iterator<Arc> i = arcList.iterator();
        while(i.hasNext()){
            Arc a= i.next();
            switch (directionRestriction) {
                case ingoing:
                    if(a.getTarget().equals(element.getID()))
                        referencingArcs.add(a);
                    break;
                case outgoing:
                    if(a.getSource().equals(element.getID()))
                        referencingArcs.add(a);
                    break;
                default:
                    if(a.getSource().equals(element.getID())||a.getTarget().equals(element.getID()))
                        referencingArcs.add(a);
            }
        }
    }
    return referencingArcs;
    }

    private List<Place> getAllIsolatedElements(ISOLATED_PLACE_TYPE placeType) {
        ArrayList<Place> isolatedPlaces = new ArrayList<Place>();
        Iterator<Place> i = placeList.iterator();
        while(i.hasNext()){
            Place p = i.next();

            switch (placeType) {
                case sink:
                    if(isSink(p))
                        isolatedPlaces.add(p);
                    break;
                case source:
                    if(isSource(p))
                        isolatedPlaces.add(p);
                    break;
                default: break;
            }
        }
        return isolatedPlaces;
    }

    private boolean isSource(Place p){
        return getAllReferencingArcsForElement(p.getID(),REFERENCE_DIRECTION.ingoing).size()==0;
    }

    private boolean isSink(Place p){
        return getAllReferencingArcsForElement(p.getID(),REFERENCE_DIRECTION.outgoing).size()==0;
    }

    private void unifySources(){
        List<Place> sources = getAllIsolatedElements(ISOLATED_PLACE_TYPE.source);
        if(sources.size()>1){
            Iterator<Place> i = sources.iterator();
            Transition t = elementBuilder.createTransition("startprocess",false,false,"");
            Place source = elementBuilder.createPlace(false,"");
            source.setText("start");
            source.setHasMarking(true);
            Arc a1= elementBuilder.createArc(source.getID(),t.getID(),"");
            while(i.hasNext()){
                Arc a = elementBuilder.createArc(t.getID(),i.next().getID(),"");
                arcList.add(a);
            }
            transitionList.add(t);
            placeList.add(source);
            arcList.add(a1);
        }else{
            sources.get(0).setText("start");
            sources.get(0).setHasMarking(true);
        }
    }

    private void unifySinks(){
        List<Place> sinks = getAllIsolatedElements(ISOLATED_PLACE_TYPE.sink);
        if(sinks.size()>1){
            Iterator<Place> i = sinks.iterator();
            Place sink = elementBuilder.createPlace(false,"");
            placeList.add(sink);
            sink.setText("end");
            XORJoin xj= new XORJoin(sinks.size(),"",elementBuilder);
            xj.addXORJoinToPetriNet(this,sinks,sink);
        }else{
            sinks.get(0).setText("end");
        }
    }

    private void healDanglingTransitions(){
        Iterator<Transition> i = transitionList.iterator();
        while(i.hasNext()){
            Transition t = i.next();
            boolean hasOutgoingArcs =getAllReferencingArcsForElement(t.getID(),REFERENCE_DIRECTION.outgoing).size()>0;
            boolean hasIngoingArcs =getAllReferencingArcsForElement(t.getID(),REFERENCE_DIRECTION.ingoing).size()>0;

            if( (!hasIngoingArcs) && (!hasOutgoingArcs)){
                //fully Isolated Transition
                removePetrinetElementByID(t.getID());

            }else if(!hasIngoingArcs){
                Place p = elementBuilder.createPlace(false,"");
                this.add(p);
                this.add(elementBuilder.createArc(p.getID(),t.getID(),""));

            }else if(!hasOutgoingArcs){
                Place p = elementBuilder.createPlace(false,"");
                this.add(p);
                this.add(elementBuilder.createArc(t.getID(),p.getID(),""));
            }

        }
    }
}
