package org.hibernate.bugs;

import javax.persistence.Embeddable;

/**
 * Created by lecogiteur on 28/10/17.
 */
@Embeddable
public class Team {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
}
