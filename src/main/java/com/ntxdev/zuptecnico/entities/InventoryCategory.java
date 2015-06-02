package com.ntxdev.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by igorlira on 3/3/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryCategory
{
    public int id;
    public String title;
    public String description;
    public Object plot_format;
    public Section[] sections;
    public String created_at;
    public Pins pin;
    public Pins marker;
    public boolean require_item_status;
    public String color;

    //@JsonIgnore(true)
    //public int defaultPinResourceId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Section
    {
        public int id;
        public String title;
        public Boolean required;
        public Field[] fields;
        public Integer position;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Field
        {
            public int id;
            public String title;
            public String kind;
            public Integer position;
            public String label;
            public String size;
            public Boolean required;
            public Boolean location;
            //public String[] available_values;
            public Option[] field_options;
            public Integer minimum;
            public Integer maximum;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Option
            {
                public int id;
                public String value;
                public boolean disabled;
            }

            public Option getOption(int id)
            {
                if(field_options == null)
                    return null;

                for(Option opt : field_options)
                {
                    if(opt.id == id)
                        return opt;
                }

                return null;
            }

            public Option getOptionWithValue(String value)
            {
                if(field_options == null)
                    return null;

                for(Option opt : field_options)
                {
                    if(opt.value.equals(value))
                        return opt;
                }

                return null;
            }
        }

        public boolean isLocationSection()
        {
            for(int i = 0; i < fields.length; i++)
            {
                if(fields[i].location != null && fields[i].location == true || fields[i].title.equals("latitude"))
                    return true;
            }

            return false;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pins
    {
        public static class Pin
        {
            public String web;
            public String mobile;
        }

        public Pin retina;
        @JsonProperty("default")
        public Pin _default;
    }

    public void updateInfo(InventoryCategory copyFrom)
    {
        if(copyFrom.title != null)
            this.title = copyFrom.title;

        if(copyFrom.description != null)
            this.description = copyFrom.description;

        if(copyFrom.plot_format != null)
            this.plot_format = copyFrom.plot_format;

        if(copyFrom.created_at != null)
            this.created_at = copyFrom.created_at;

        if(copyFrom.sections != null)
            this.sections = copyFrom.sections;

        if(copyFrom.pin != null)
            this.pin = copyFrom.pin;

        this.require_item_status = copyFrom.require_item_status;
    }

    public Section.Field getFirstFieldOfKind(String kind)
    {
        return getNthFieldOfKind(kind, 1);
    }

    public Section.Field getNthFieldOfKind(String kind, int number)
    {
        int foundNumber = 0;

        for(int i = 0; i < sections.length; i++)
        {
            for(int j = 0; j < sections[i].fields.length; j++)
            {
                if(sections[i].fields[j].kind != null && sections[i].fields[j].kind.equals(kind))
                {
                    foundNumber++;
                    if(foundNumber == number)
                        return sections[i].fields[j];
                }
            }
        }

        return null;
    }

    public Section.Field getField(int id)
    {
        for(int i = 0; i < sections.length; i++)
        {
            for(int j = 0; j < sections[i].fields.length; j++)
            {
                if(sections[i].fields[j].id == id)
                    return sections[i].fields[j];
            }
        }

        return null;
    }

    public Section.Field getField(String name)
    {
        for(int i = 0; i < sections.length; i++)
        {
            for(int j = 0; j < sections[i].fields.length; j++)
            {
                if(sections[i].fields[j].title.equals(name))
                    return sections[i].fields[j];
            }
        }

        return null;
    }

    public Section getFieldSection(int id)
    {
        for(int i = 0; i < sections.length; i++)
        {
            for(int j = 0; j < sections[i].fields.length; j++)
            {
                if(sections[i].fields[j].id == id)
                    return sections[i];
            }
        }

        return null;
    }
}
