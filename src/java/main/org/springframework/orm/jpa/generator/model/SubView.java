package org.springframework.orm.jpa.generator.model;

import java.util.List;

import org.jdom2.Element;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubView {
    private String control;
    private View view;
    private Element element;
    private List<Event> events;
}
