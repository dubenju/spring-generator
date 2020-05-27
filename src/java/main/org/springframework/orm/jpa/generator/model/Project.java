package org.springframework.orm.jpa.generator.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Project {
    private String name;
    private String packageName;
    
    private List<Gyomu> gyomus;
}
