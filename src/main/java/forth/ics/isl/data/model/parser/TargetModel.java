/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.data.model.parser;

import org.json.simple.JSONObject;

/**
 *
 * @author rousakis
 */
public class TargetModel {

    private String name, uri, selectionList, varName, selectionPattern;

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
        return selectionList.replace("@#$%VAR%$#@", var);
    }

    public String getVarName() {
        return varName;
    }

    public String getSelectionPattern(String var) {
        if (var == null) {
            return selectionPattern;
        }
        return selectionPattern.replace("@#$%VAR%$#@", var);
    }

    @Override
    public String toString() {
        return "TargetModel{" + "name=" + name + ", uri=" + uri + '}';
    }
}