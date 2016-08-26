package liveevaluation;

import evaluationbasics.EmptyCodeException;
import org.jdom2.Attribute;
import org.jdom2.Element;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ilias on 26.08.16.
 */
public class XMLParser {

    public static String getCode( Element top ) throws EmptyCodeException {
        Element codeNode = top.getChild("code");
        String code = codeNode.getValue();
        if ( code.isEmpty() ) throw new EmptyCodeException("Empty code at node: "+top.getName());
        return code;
    }

    public static List<ParamGroup> parseParameterGroups(Element base) throws org.jdom2.DataConversionException {
        LinkedList<ParamGroup> ll = new LinkedList<ParamGroup>();
        for (Element group : base.getChildren("paramgroup")) {
            ll.add(parseParameters(group));
        }
        return ll;
    }

    public static ParamGroup parseParameters(Element group) throws org.jdom2.DataConversionException {
        ParamGroup grp = new ParamGroup();

        // only parameter groups from teacher have associated points
        Attribute points = group.getAttribute("points");
        if ( points!=null) {
            grp.points = points.getIntValue();
        }

        for (Element params : group.getChildren("params")) {
            int id = params.getAttribute("id").getIntValue();
            Object[] values = paramsToArray(params);
            grp.add(new Params(id, values));
        }
        return grp;
    }

    public static Object[] paramsToArray(Element params) {
        List<Element> param = params.getChildren("param");
        Object[] zReturn = new Object[param.size()];
        for (int j = 0; j < param.size(); j++)
            zReturn[j] = param.get(j).getValue();
        return zReturn;
    }
}
