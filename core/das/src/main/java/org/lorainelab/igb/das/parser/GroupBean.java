package org.lorainelab.igb.das.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sgblanch
 * @version $Id: GroupBean.java 5343 2010-02-25 01:39:42Z sgblanch $
 */
class GroupBean {

    private String id, label, type;
    private List<String> notes = new ArrayList<>(2);
    private List<LinkBean> links = new ArrayList<>(2);
    private List<TargetBean> targets = new ArrayList<>(2);

    GroupBean() {
        this.clear();
    }

    void setID(String id) {
        this.id = id.intern();
    }

    String getID() {
        return this.id;
    }

    void setLabel(String label) {
        this.label = label.intern();
    }

    String getLabel() {
        return this.label;
    }

    void setType(String type) {
        this.type = type.intern();
    }

    String getType() {
        return this.type;
    }

    void addNote(String note) {
        this.notes.add(note.intern());
    }

    List<String> getNotes() {
        return Collections.unmodifiableList(this.notes);
    }

    void addLink(LinkBean link) {
        this.links.add(link);
    }

    List<LinkBean> getLinks() {
        return Collections.unmodifiableList(this.links);
    }

    void addTarget(TargetBean target) {
        this.targets.add(target);
    }

    List<TargetBean> getTargets() {
        return Collections.unmodifiableList(this.targets);
    }

    void clear() {
        id = "";
        label = "";
        type = "";
        notes.clear();
        links.clear();
        targets.clear();
    }
}
