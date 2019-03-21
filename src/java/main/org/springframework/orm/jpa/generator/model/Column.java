package org.springframework.orm.jpa.generator.model;

import org.jdom2.Element;

import lombok.Data;

@Data
public class Column {

    private String name;
    private String type;
    private String size;
    private String primaryKey;
    private String required;

    private Table table;
    private Element element;
}
