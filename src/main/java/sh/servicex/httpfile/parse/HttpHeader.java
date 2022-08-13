package sh.servicex.httpfile.parse;


public class HttpHeader implements Comparable<HttpHeader> {
    private String name;
    private String value;
    private boolean variablesIncluded;

    public HttpHeader() {
    }

    public HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
        this.variablesIncluded = value.contains("{{");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.variablesIncluded = value.contains("{{");
    }

    public boolean isVariablesIncluded() {
        return variablesIncluded;
    }

    public void setVariablesIncluded(boolean variablesIncluded) {
        this.variablesIncluded = variablesIncluded;
    }

    @Override
    public int compareTo(HttpHeader o) {
        return this.name.compareTo(o.name);
    }
}

