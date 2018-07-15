package edu.pitt.medschool.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Query templates for UPMC
 *
 * @author TonyZ
 */
public class QueryTemplate {

    // SQL Template with variables like '%%X, %%Y')
    private String _sqlTemplate;
    private String _sqlDescription;

    private Map<String, String> _variables = new HashMap<>();

    /**
     * Add a new query template
     *
     * @param sql         SQL template (with placeholders)
     * @param description Brief info about it
     */
    public QueryTemplate(String sql, String description) {
        this.set_sqlTemplate(sql);
        this.set_sqlDescription(description);
    }

    public void updateSqlTemplate(String newSql) {
        this.set_sqlTemplate(newSql);
    }

    public void updateTemplateDescription(String newDesc) {
        this.set_sqlDescription(newDesc);
    }

    void analyzeVars(String sqlT) {

    }

    public String get_sqlTemplate() {
        return _sqlTemplate;
    }

    public void set_sqlTemplate(String _sqlTemplate) {
        this._sqlTemplate = _sqlTemplate;
    }

    public String get_sqlDescription() {
        return _sqlDescription;
    }

    public void set_sqlDescription(String _sqlDescription) {
        this._sqlDescription = _sqlDescription;
    }

    public Map<String, String> get_variables() {
        return _variables;
    }

    public void set_variables(Map<String, String> _variables) {
        this._variables = _variables;
    }

}
