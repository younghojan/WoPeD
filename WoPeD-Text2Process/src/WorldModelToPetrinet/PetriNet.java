package WorldModelToPetrinet;

import transform.DummyAction;
import worldModel.Action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PetriNet {

    private ArrayList<Arc> arcList= new ArrayList<Arc>();
    private ArrayList<Place> placeList= new ArrayList<Place>();
    private ArrayList<Transition> transitionList= new ArrayList<Transition>();

    public PetriNet(){
        Transition.resetStaticContext();
        Arc.resetStaticContext();
        Place.resetStaticContext();
        DummyAction.resetStaticContext();
    }

    public void add(Place p){
        placeList.add(p);

    }

    public void add(Arc a){
        arcList.add(a);
    }

    public void add(Transition t){
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

    private Place getPlaceById(String id){

        Iterator<Place> i = placeList.iterator();
        while (i.hasNext()){
            Place p = i.next();
            if(p.getPlaceID().equals(id)){
                return p;
            }
        }
        //not found
        return null;
    }

    public List<Place> getAllSinks(){
        ArrayList<Place> sourcePlaces = new ArrayList<Place>();
        Iterator<Arc> i = arcList.iterator();
        while(i.hasNext()){
            Arc a = i.next();
            String elementID= a.getSource();
            if(elementID.startsWith("p")){
                //Place
                sourcePlaces.add(getPlaceById(elementID));
            }
        }
        ArrayList<Place> sinks = new ArrayList<Place>();
        Iterator<Place> i2 = placeList.iterator();
        while(i2.hasNext()){
            Place p = i2.next();
            boolean found=false;
            Iterator<Place> i3 = sourcePlaces.iterator();
            while(i3.hasNext()){
                Place p2 = i3.next();
                if(p.getPlaceID().equals(p2.getPlaceID())){
                    found=true;
                }
            }
            if(!found){
                sinks.add(p);
            }

        }
        return sinks;
    }

    public List<Place> getAllSources(){
        ArrayList<Place> targetPlaces = new ArrayList<Place>();
        Iterator<Arc> i = arcList.iterator();
        while(i.hasNext()){
            Arc a = i.next();
            String elementID= a.getTarget();
            if(elementID.startsWith("p")){
                //Place
                targetPlaces.add(getPlaceById(elementID));
            }
        }
        ArrayList<Place> sources = new ArrayList<Place>();
        Iterator<Place> i2 = placeList.iterator();
        while(i2.hasNext()){
            Place p = i2.next();
            boolean found=false;
            Iterator<Place> i3 = targetPlaces.iterator();
            while(i3.hasNext()){
                Place p2 = i3.next();
                if(p.getPlaceID().equals(p2.getPlaceID())){
                    found=true;
                }
            }
            if(!found){
                sources.add(p);
            }

        }
        return sources;
    }


    public String getPNML(){
        String PNML= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!--PLEASE DO NOT EDIT THIS FILE\n" +
                "Created with Workflow PetriNet Designer Version 3.2.0 (woped.org)-->\n" +
                "<pnml>\n" +
                "  <net type=\"http://www.informatik.hu-berlin.de/top/pntd/ptNetb\" id=\"noID\">";

        Iterator<Place> i = placeList.iterator();
        while(i.hasNext()){
            Place p = i.next();
            PNML=PNML+p.toString()+"\n";
        }

        Iterator<Transition> i2 = transitionList.iterator();
        while(i2.hasNext()){
            Transition t = i2.next();
            PNML=PNML+t.toString()+"\n";
        }

        Iterator<Arc> i3 = arcList.iterator();
        while(i3.hasNext()){
            Arc a = i3.next();
            PNML=PNML+a.toString()+"\n";
        }

        PNML=PNML+
                "  </net>\n" +
                "</pnml>";
        return PNML;
    }

    public void unifySources(List<Place> sources){
        Iterator<Place> i = sources.iterator();
        Transition t = new Transition("startprocess",false,false,"");
        Place source = new Place(false,"");
        Arc a1= new Arc(source.getPlaceID(),t.getTransID(),"");
        while(i.hasNext()){
            Place p = i.next();
            Arc a = new Arc(t.getTransID(),p.getPlaceID(),"");
            placeList.add(p);
            arcList.add(a);
        }
        transitionList.add(t);
        placeList.add(source);
        arcList.add(a1);
    }

    public void unifySinks(List<Place> sinks){
        Iterator<Place> i = sinks.iterator();
        Place sink = new Place(false,"");
        placeList.add(sink);
        XORJoin xj= new XORJoin(sinks.size(),"");
        xj.addXORJoinToPetriNet(this,sinks,sink);
    }

}