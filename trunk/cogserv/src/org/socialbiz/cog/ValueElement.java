package org.socialbiz.cog;

public final class ValueElement {

    public String name;
    public String value;

    public ValueElement() {
    }

    public ValueElement(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String toString()
    {
        final java.lang.StringBuffer _ret = new java.lang.StringBuffer(
                "org.socialbiz.cog.ValueElement {");
        _ret.append("String name=");
        _ret.append(name);
        _ret.append("\n");
        _ret.append("String value=");
        _ret.append(value);
        _ret.append("}");
        return _ret.toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof ValueElement)) {
            return false;
        }

        ValueElement ve = (ValueElement) o;
        if (name.equals(ve.name) == false || value.equals(ve.value) == false) {
            return false;
        }
        return true;
    }

}