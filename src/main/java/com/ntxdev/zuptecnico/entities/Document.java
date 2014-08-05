package com.ntxdev.zuptecnico.entities;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by igorlira on 2/9/14.
 */
public class Document {
    public enum State
    {
        WaitingForSync,
        Running,
        Finished,
        Pending
    }

    private int id;
    private Date creationDate;
    private Date lastModification;
    private State state;

    public Document(int id, Date creationDate, Date lastModification, State state)
    {
        this.id = id;
        this.creationDate = creationDate;
        this.lastModification = lastModification;
        this.state = state;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Date getCreationDate()
    {
        return creationDate;
    }

    public void setCreationDate(Date creationDate)
    {
        this.creationDate = creationDate;
    }

    public Date getLastModification()
    {
        return lastModification;
    }

    public void setLastModification(Date lastModification)
    {
        this.lastModification = lastModification;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }
}
