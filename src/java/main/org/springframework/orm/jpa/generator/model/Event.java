package org.springframework.orm.jpa.generator.model;

import org.jdom2.Element;
import org.springframework.orm.jpa.generator.GeneratorView;
import org.springframework.orm.jpa.generator.SerialVersionUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {
    private String name;
    private String value;
    private String method;
    private String index;
    
    private View view;
    private SubView subView;
    private Element element;
    
    public String getVarname() {
        return GeneratorView.getVarFromName(this.name);
    }
    public String getSerialVersionUID(String name) {
        return SerialVersionUID.getSerialVersionUID(name);
    }
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        buf.append("name=" + this.name);
        buf.append(",value=" + this.value);
        buf.append(",method=" + this.method);
        buf.append(",index=" + this.index);
        buf.append(",VarName=" + this.getVarname());
        buf.append("]");
        return buf.toString();
    }
}
