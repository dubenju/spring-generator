package org.springframework.orm.jpa.generator.model;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.springframework.orm.jpa.generator.Generator;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//@Data
@Getter
@Setter
public class Column {

    private String name;
    private String type;
    private String size;
    private String fullName;
    private String primaryKey;
    private String required;
    private String description;

    private Table table;
    private Element element;
    public String gerColumnId() {
        return Generator.getIdFromName(this.name);
    }
    public String getJavaType() {
        if ("NUMERIC".equals(this.type.toUpperCase())) {
            if (this.size.indexOf(",") > 0) {
                return "BigDecimal";
            }
            int i = Integer.parseInt(this.size);
            if (i > 9) {
                return "Long";
            }
        }
        return Generator.getType(this.type);
    }
    public String getColumnVar() {
        return Generator.getVarFromName(this.name);
    }
    public List<String> getDescriptions() {
        List<String> list= new ArrayList<String>();
        if (this.description == null || this.description.length() <= 0) {
            return list;
        }
        String[] ary = this.description.split("\\\n");
        for (String str : ary) {
            list.add(str);
        }
        return list;
    }
}
