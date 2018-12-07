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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author rousakis
 */
public class TargetModel {

    private String name, uri, selectionList, varName, selectionPattern;
    private String targetEntitySearchText;
    private String keywordSearchPattern;

    public TargetModel(JSONObject jsonModel) {
        init(jsonModel);
    }

    public TargetModel(String jsonModelString) {
        JSONObject jsonModel = Utils.parse(jsonModelString);
        init(jsonModel);
    }

    private void init(JSONObject jsonModel) {
        if (jsonModel == null) {
            return;
        }
        this.name = (String) ((JSONObject) jsonModel.get("selectedTargetEntity")).get("name");
        this.varName = (String) ((JSONObject) jsonModel.get("selectedTargetEntity")).get("var_name");
        this.selectionPattern = (String) ((JSONObject) jsonModel.get("selectedTargetEntity")).get("selection_pattern");
        this.uri = (String) ((JSONObject) jsonModel.get("selectedTargetEntity")).get("uri");
        this.selectionList = (String) ((JSONObject) jsonModel.get("selectedTargetEntity")).get("selection_list");
        ///
        StringBuilder sb = new StringBuilder();
        JSONArray searchChips = (JSONArray) jsonModel.get("targetChips");
        sb.append(Utils.getChipsFilter(searchChips));
        sb.append((String) jsonModel.get("searchTargetKeywords"));
        this.targetEntitySearchText = sb.toString().trim();
        this.keywordSearchPattern = "";
        if (!this.targetEntitySearchText.equals("")) {
            this.keywordSearchPattern = (String) ((JSONObject) jsonModel.get("selectedTargetEntity")).get("keyword_search");
            this.keywordSearchPattern = this.keywordSearchPattern.replace("@#$%TERM%$#@", this.targetEntitySearchText);
        }
    }

    public String getKeywordSearchPattern(String var) {
        return keywordSearchPattern.replaceAll("@#\\$%VAR%\\$#@", var);
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getSelectionList(String var) {
        if (var == null) {
            return selectionList;
        }
        return selectionList.replaceAll("@#\\$%VAR%\\$#@", var);
    }

    public String getVarName() {
        return varName;
    }

    public String getSelectionPattern(String var) {
        if (var == null) {
            return selectionPattern;
        }
        return selectionPattern.replaceAll("@#\\$%VAR%\\$#@", var);
    }

    @Override
    public String toString() {
        return "TargetModel{" + "name=" + name + ", uri=" + uri + '}';
    }
}
