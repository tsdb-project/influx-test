package app;

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
        this._sqlTemplate = sql;
        this._sqlDescription = description;
    }

    public void updateSqlTemplate(String newSql) {
        this._sqlTemplate = newSql;
    }

    public void updateTemplateDescription(String newDesc) {
        this._sqlDescription = newDesc;
    }

    private void analyzeVars(String sqlT) {

    }

}
