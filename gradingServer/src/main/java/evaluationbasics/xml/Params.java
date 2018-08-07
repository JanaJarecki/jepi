package evaluationbasics.xml;

/**
 * Created by ilias on 22.08.16.
 */
public class Params {
    public int id;
    public Object[] values;
    public Object zReturn;
    public boolean equals = false;
    public String error = "";
    public String consoleOutput = null;

    public Params( int id, Object[] values) {
        this.id = id;
        this.values = values;
    }

    public Params( Params other) {
        this.id = other.id;
        this.values = other.values;
        this.zReturn = other.zReturn;
        this.equals = other.equals;
        this.error = other.error;
        this.consoleOutput = other.consoleOutput;
    }
}
