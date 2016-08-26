package liveevaluation;

/**
 * Created by ilias on 22.08.16.
 */
public class ERROR_CODE {
    public static int UNKNWON = 666;

    // xml request parsing errors
    public static int METHOD_AND_CLASSES_MIXED = 1;
    public static int TEACHER_NOT_FOUND = 2;
    public static int TEACHER_PARAMGROUP_NOT_FOUND = 3;
    public static int TEACHER_PARAMS_NOT_FOUND = 4;
    public static int PARAMETER_DO_NOT_MATCH_FUNCTION_SIGNATURE = 5;
    public static int ACTION_NOT_FOUND = 6;
    public static int CODE_NOT_FOUND = 7;
    public static int ACTION_NOT_KNOWN = 8;
    public static int QUESTION_TYPE_NOT_KNOWN = 9;
    public static int CODE_IS_EMPTY = 10;

    // errors when handling functions
    public static int TOO_MANY_METHODS = 51;
    public static int SECURITY_VIOLATION_DETECTED = 52;
    public static int FOUND_VOID_METHOD = 53;

    // crticial errors
    public static int UNKOWN_ERROR = 100;
    public static int XML_PARSING_ERROR = 101;
    public static int COULD_NOT_CREATE_RESPONSEXML = 102;
    public static int INPUTSTREAM_IO_ERROR = 103;
    public static int CLIENT_ADRESS_NOT_ALLOWED = 104;
    public static int TIMEOUT = 105;
}
