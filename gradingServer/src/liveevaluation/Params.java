package liveevaluation;

/**
 * Created by ilias on 22.08.16.
 */
public class Params {
    public int id;
    public Object[] values;
    public Object zReturn;
    public boolean equals = false;
    public String error = "";

    public Params( int id, Object[] values) {
        this.id = id;
        this.values = values;
    }
}
