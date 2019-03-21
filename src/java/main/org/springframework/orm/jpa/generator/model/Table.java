package org.springframework.orm.jpa.generator.model;

import java.util.List;

import org.jdom2.Element;

import lombok.Data;

@Data
public class Table {
    // Table Name
    private String name;

    private Schema schema;
    private Element element;
    private List<Column> columns;
    private List<Column> primaryKeys;
}
