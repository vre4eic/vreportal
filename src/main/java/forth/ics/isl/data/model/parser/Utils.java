/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.data.model.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class Utils {

    public static JSONObject parse(String jsonModel) {
        JSONObject res = new JSONObject();
        JSONParser parser = new JSONParser();
        try {
            res = (JSONObject) parser.parse(jsonModel);
        } catch (ParseException ex) {
            Logger.getLogger(TargetModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public static List<String> getGraphsFromClause(String fromClause) {
        List<String> graphs = new ArrayList<>();
        Pattern regex = Pattern.compile("(?<=<)[^>]+(?=>)");
        Matcher regexMatcher = regex.matcher(fromClause);
        while (regexMatcher.find()) {
            graphs.add(regexMatcher.group());
        }
        return graphs;
    }

    public static ArrayList<LinkedHashMap> jsonArrayToList(JSONArray array) {
        ArrayList<LinkedHashMap> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject obj = (JSONObject) array.get(i);
            LinkedHashMap item = new LinkedHashMap();
            for (Object key : obj.keySet()) {
                item.put(key, obj.get(key));
            }
            result.add(item);
        }
        return result;
    }

}
