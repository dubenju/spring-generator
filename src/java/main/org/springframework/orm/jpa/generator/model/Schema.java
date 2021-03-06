package org.springframework.orm.jpa.generator.model;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

//@Data
@Getter
@Setter
public class Schema {
    // build.properties project.name値
    private String name;
    // build.properties project.packageName値
    private String packageName;
    
    private List<Table> tables;
}
