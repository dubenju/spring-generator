package org.springframework.orm.jpa.generator.model;

import java.util.List;

import org.jdom2.Element;
import org.springframework.orm.jpa.generator.Generator;
import org.springframework.orm.jpa.generator.SerialVersionUID;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//@Data
@Getter
@Setter
public class Table {
    // Table Name
    private String name;
    private String fullName;
    private boolean dataType;
    private boolean bigDecimalType;
    private boolean dataTypePk;

    private Schema schema;
    private Element element;
    private List<Column> columns;
    private List<Column> primaryKeys;
    private List<TestCase> testCases;
    
    public String getTableId() {
        return Generator.getIdFromName(this.name);
    }
    public String getTableVar() {
        return Generator.getVarFromName(this.name);
    }
    public String getSerialVersionUID(String name) {
        return SerialVersionUID.getSerialVersionUID(name);
    }
}
