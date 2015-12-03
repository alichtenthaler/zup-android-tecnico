package com.ntxdev.zuptecnico.config;

/**
 * Created by igorlira on 7/23/15.
 */
public class InternalConstants {
    public static final String INVENTORY_CATEGORY_LISTING_RETURN_FIELDS = "id,title,plot_format,pin,marker,require_item_status,color,sections.id,sections.title,sections.disabled,sections.required,sections.location,sections.inventory_category_id,sections.position,sections.fields.id,sections.fields.disabled,sections.fields.title,sections.fields.kind,sections.fields.size,sections.fields.inventory_section_id,sections.fields.available_values,sections.fields.field_options,sections.fields.position,sections.fields.label,sections.fields.maximum,sections.fields.minimum,sections.fields.required,sections.fields.location,statuses";
    public static final String INVENTORY_ITEM_LISTING_RETURN_FIELDS = "id,inventory_category_id,inventory_status_id,created_at,title";
    public static final String INVENTORY_ITEM_MAP_RETURN_FIELDS = "count,category_id,position,id,inventory_category_id,title";
    public static final String INVENTORY_ITEM_RETURN_FIELDS = "id,title,address,inventory_status_id,position,inventory_category_id,data.id,data.inventory_field_id,data.content,data.selected_options,created_at,updated_at";
    public static final String REPORT_ITEM_LISTING_RETURN_FIELDS = "id,protocol,address,number,district,postal_code,city,state,country,status_id,category_id,created_at";
    public static final String REPORT_ITEM_HISTORY_RETURN_FIELDS = "id,user.id,user.name,kind,action,changes,created_at";
    public static final String REPORT_ITEM_MAP_RETURN_FIELDS = "id,protocol,category_id,position";
    public static final String REPORT_ITEM_NOTIFICATION_RETURN_FIELDS = "id,notification_type.title,deadline_in_days,days_to_deadline,content,created_at,updated_at,overdue_at";
}
