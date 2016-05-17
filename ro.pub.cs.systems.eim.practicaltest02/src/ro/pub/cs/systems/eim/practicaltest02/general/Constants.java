package ro.pub.cs.systems.eim.practicaltest02.general;

public interface Constants {

    final public static String TAG = "[PracticalTest02]";
    
    public final static String  SERVER_HOST  = "localhost";
    public final static int     SERVER_PORT  = 2016;

    public final static String  SERVER_START = "Start Server";
    public final static String  SERVER_STOP  = "Stop Server";

    final public static boolean DEBUG = true;

    final public static String WEB_SERVICE_ADDRESS = "http://services.aonaware.com/DictService/DictService.asmx/Define";

    final public static String TEMPERATURE = "temperature";
    final public static String WIND_SPEED = "wind_speed";
    final public static String CONDITION = "condition";
    final public static String PRESSURE = "pressure";
    final public static String HUMIDITY = "humidity";
    final public static String ALL = "all";

    final public static String EMPTY_STRING = "";

    final public static String QUERY_ATTRIBUTE = "word";

    final public static String SCRIPT_TAG = "script";
    final public static String SEARCH_KEY = "WordDefinition";

    final public static String CURRENT_OBSERVATION = "current_observation";

}
