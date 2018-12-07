/*******************************************************************************
 * Copyright (c) 2018 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

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

    public static StringBuilder getChipsFilter(JSONArray searchChips) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < searchChips.size(); i++) {
//            String[] chipArr = ((String) ((JSONObject) searchChips.get(i)).get("name")).split(" ");
//            StringBuilder chipStr = new StringBuilder();
//            for (int j = 0; j < chipArr.length; j++) {
//                String chipWord = removePuncuations(chipArr[j]);
//                chipStr.append(chipWord);
//                if (j < chipArr.length - 1) {
////                    chipStr.append(" and ");
//                }
//            }
//            sb.append("'"+chipName+"'");

            String chipName = removePuncuations((String) ((JSONObject) searchChips.get(i)).get("name"));
            sb.append("'" + chipName + "'");
            if (i < searchChips.size() - 1) {
                sb.append(" or ");
            }
        }
        return sb;
    }

    public static String removePuncuations(String s) {
        int i;
        for (i = s.length() - 1; i >= 0; i--) {
            if (Character.isLetterOrDigit(s.charAt(i))) {
                break;
            }
        }
        return s.substring(0, i + 1);
    }
}
