package evaluationbasics.XML;

/**
 * Created by ilias on 29.08.16.
 */
public class TestData {
    public int id = -1;
    public String name = "";
    public String description = "";
    public int points = 0;
    public int reachedPoints = 0;
    public boolean passed = false;
    public boolean passedPartially = false;


    public TestData(int id, String name, String description, int points) {
        this.id = id;
        this.name = name;
        this.points = points;
        this.description = description;
    }
}
