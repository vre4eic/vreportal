/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.data.model.parser;

/**
 *
 * @author rousakis
 */
public enum FilterExp {
    AND, OR;

    public static FilterExp fromString(String expr) {
        if (expr != null) {
            if (expr.equals("AND")) {
                return FilterExp.AND;
            } else if (expr.equals("OR")) {
                return FilterExp.OR;
            }
        }
        return null;
    }
}
