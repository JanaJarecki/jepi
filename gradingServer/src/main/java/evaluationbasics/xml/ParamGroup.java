package evaluationbasics.xml;

import java.util.LinkedList;

/**
 * Created by ilias on 22.08.16.
 */
public class ParamGroup {
    public int id;
    public int points = -1;
    public int reachedPoints = -1;
    public LinkedList<Params> params = new LinkedList<Params>();
    public boolean equals = false;
    public String error = "";
    public String name = "";

    public void add( Params p ) { params.add(p); }

    public ParamGroup() {

    }

    public ParamGroup(ParamGroup g) {
        this.id = g.id;
        this.points = g.points;
        this.reachedPoints = g.reachedPoints;
        this.params = new LinkedList();
        for ( Params p : g.params) {
            this.params.add(new Params(p));
        }
        this.equals = g.equals;
        this.error = g.error;
        this.name = g.name;
    }
}
