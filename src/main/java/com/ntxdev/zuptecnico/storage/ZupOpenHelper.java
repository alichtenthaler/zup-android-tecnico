package com.ntxdev.zuptecnico.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.JsonWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.internal.cu;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.User;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by igorlira on 5/19/14.
 */
public class ZupOpenHelper extends SQLiteOpenHelper {
    static final String DB_NAME = "zuptecnico";
    static final int DB_VERSION = 1;

    public ZupOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY, name VARCHAR(120), email VARCHAR(120), phone VARCHAR(120), document VARCHAR(120), address VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE session (user_id INTEGER, token VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories (id INTEGER PRIMARY KEY, title VARCHAR(120), description VARCHAR(120), require_item_status INTEGER, created_at VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_sections (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, title VARCHAR(120), required INTEGER);");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_sections_fields (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, inventory_section_id INTEGER, title VARCHAR(120), kind VARCHAR(120), position INTEGER, label VARCHAR(120), size VARCHAR(120), required INTEGER, location INTEGER, available_values TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_statuses (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, title VARCHAR(120), color VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_pins (inventory_category_id INTEGER PRIMARY KEY, url_web TEXT, url_mobile TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_items (id INTEGER PRIMARY KEY, title VARCHAR(120), latitude FLOAT, longitude FLOAT, inventory_category_id INTEGER, inventory_status_id INTEGER, created_at VARCHAR(120), address VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_items_data (id INTEGER PRIMARY_KEY, inventory_item_id INTEGER, inventory_field_id INTEGER, content TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE flows (id INTEGER PRIMARY KEY, title VARCHAR(120), description VARCHAR(120), initial INTEGER, steps_id VARCHAR(200), created_by_id INTEGER, updated_by_id INTEGER, status VARCHAR(120), last_version INTEGER, last_version_id INTEGER, created_at VARCHAR(120), updated_at VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE flows_resolution_states (id INTEGER PRIMARY KEY, flow_id INTEGER, title VARCHAR(120), _default INTEGER, active INTEGER, created_at VARCHAR(120), updated_at VARCHAR(120), last_version INTEGER, last_version_id INTEGER);");
    }

    ContentValues createFlowResolutionStateContentValues(Flow.ResolutionState resolutionState)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", resolutionState.id);
        contentValues.put("flow_id", resolutionState.flow_id);
        contentValues.put("title", resolutionState.title);
        contentValues.put("_default", resolutionState._default ? 1 : 0);
        contentValues.put("active", resolutionState.active ? 1 : 0);
        contentValues.put("created_at", resolutionState.created_at);
        contentValues.put("updated_at", resolutionState.updated_at);
        contentValues.put("last_version", resolutionState.last_version);
        contentValues.put("last_version_id", resolutionState.last_version_id);

        return contentValues;
    }

    public void addFlowResolutionState(Flow.ResolutionState resolutionState)
    {
        ContentValues contentValues = createFlowResolutionStateContentValues(resolutionState);

        getWritableDatabase().insert("flows_resolution_states", null, contentValues);
    }

    Flow parseFlow(Cursor cursor)
    {
        String steps_id_str = cursor.getString(4);
        String[] steps_id_str_e = steps_id_str.split(",");

        Integer[] steps_id = new Integer[steps_id_str.length() == 0 ? 0 : steps_id_str_e.length];
        if(steps_id_str.length() > 0)
        {
            for(int i = 0; i < steps_id_str_e.length; i++)
            {
                steps_id[i] = Integer.parseInt(steps_id_str_e[i]);
            }
        }

        Flow flow = new Flow();
        flow.id = cursor.getInt(0);
        flow.title = cursor.getString(1);
        flow.description = cursor.getString(2);
        flow.initial = cursor.getInt(3) == 1;
        flow.steps_id = steps_id;
        flow.created_by_id = cursor.getInt(5);
        flow.updated_by_id = cursor.getInt(6);
        flow.status = cursor.getString(7);
        flow.last_version = cursor.getInt(8);
        flow.last_version_id = cursor.getInt(9);
        flow.created_at = cursor.getString(10);
        flow.updated_at = cursor.getString(11);

        Cursor countCursor = getReadableDatabase().rawQuery("SELECT COUNT(id) FROM flows_resolution_states WHERE flow_id=?", new String[] { Integer.toString(flow.id) });
        countCursor.moveToNext();
        int count = countCursor.getInt(0);

        Cursor statesCursor = getReadableDatabase().rawQuery("SELECT id, flow_id, title, _default, active, created_at, updated_at, last_version, last_version_id FROM flows_resolution_states WHERE flow_id = ?", new String[] { Integer.toString(flow.id) });
        flow.resolution_states = new Flow.ResolutionState[count];
        int x = 0;
        while(statesCursor.moveToNext())
        {
            Flow.ResolutionState state = new Flow.ResolutionState();
            state.id = statesCursor.getInt(0);
            state.flow_id = statesCursor.getInt(1);
            state.title = statesCursor.getString(2);
            state._default = statesCursor.getInt(3) == 1;
            state.active = statesCursor.getInt(4) == 1;
            state.created_at = statesCursor.getString(5);
            state.updated_at = statesCursor.getString(6);
            state.last_version = statesCursor.getInt(7);
            state.last_version_id = statesCursor.getInt(8);

            flow.resolution_states[x] = state;
            x++;
        }


        return flow;
    }

    public Iterator<Flow> getFlowIterator()
    {
        ArrayList<Flow> result = new ArrayList<Flow>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, description, initial, steps_id, created_by_id, updated_by_id, status, last_version, last_version_id, created_at, updated_at FROM flows ORDER BY id ASC", null);
        while(cursor.moveToNext())
        {
            result.add(parseFlow(cursor));
        }

        return result.iterator();
    }

    public boolean hasFlow(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id FROM flows WHERE id = ?", new String[] { Integer.toString(id) });
        return cursor.moveToNext();
    }

    public boolean hasFlowResolutionState(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id FROM flows_resolution_states WHERE id = ?", new String[] { Integer.toString(id) });
        return cursor.moveToNext();
    }

    void updateFlowResolutionState(int id, Flow.ResolutionState resolutionState)
    {
        ContentValues contentValues = createFlowResolutionStateContentValues(resolutionState);
        getWritableDatabase().update("flows_resolution_states", contentValues, "id = ?", new String[] { Integer.toString(id) });
    }

    public void updateFlow(int id, Flow data)
    {
        ContentValues contentValues = createFlowContentValues(data);
        getWritableDatabase().update("flows", contentValues, "id = ?", new String[] { Integer.toString(id) });

        String notPresentIds = "";
        for(int i = 0; i < data.resolution_states.length; i++)
        {
            if(i > 0)
                notPresentIds += " AND ";

            notPresentIds += "id != " + data.resolution_states[i].id;

            if(hasFlowResolutionState(data.resolution_states[i].id))
                updateFlowResolutionState(data.resolution_states[i].id, data.resolution_states[i]);
            else
                addFlowResolutionState(data.resolution_states[i]);
        }

        if(notPresentIds.length() > 0)
            getWritableDatabase().delete("flows_resolution_states", notPresentIds, null);
    }

    public Flow getFlow(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, description, initial, steps_id, created_by_id, updated_by_id, status, last_version, last_version_id, created_at, updated_at FROM flows WHERE id = ?", new String[] { Integer.toString(id) });
        cursor.moveToNext();
        return parseFlow(cursor);
    }

    ContentValues createFlowContentValues(Flow flow)
    {
        String steps_id = "";
        for(int i = 0; i < flow.steps_id.length; i++)
        {
            int step_id = flow.steps_id[i];
            steps_id = steps_id + step_id;

            if(i + 1 < flow.steps_id.length)
                steps_id = steps_id + ",";
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("id", flow.id);
        contentValues.put("title", flow.title);
        contentValues.put("description", flow.description);
        contentValues.put("initial", flow.initial ? 1 : 0);
        contentValues.put("steps_id", steps_id);
        contentValues.put("created_by_id", flow.created_by_id);
        contentValues.put("updated_by_id", flow.updated_by_id);
        contentValues.put("status", flow.status);
        contentValues.put("last_version", flow.last_version);
        contentValues.put("last_version_id", flow.last_version_id);
        contentValues.put("created_at", flow.created_at);
        contentValues.put("updated_at", flow.updated_at);

        return contentValues;
    }

    public void addFlow(Flow flow)
    {
        ContentValues contentValues = createFlowContentValues(flow);
        getWritableDatabase().insert("flows", null, contentValues);

        for(int i = 0; i < flow.resolution_states.length; i++)
        {
            addFlowResolutionState(flow.resolution_states[i]);
        }
    }

    public void addInventoryItem(InventoryItem item)
    {
        ContentValues values = new ContentValues();
        values.put("id", item.id);
        values.put("title", item.title);
        if(item.position != null) {
            values.put("latitude", item.position.latitude);
            values.put("longitude", item.position.longitude);
        }
        else
        {
            values.put("latitude", 0f);
            values.put("longitude", 0f);
        }
        values.put("inventory_category_id", item.inventory_category_id);
        values.put("inventory_status_id", item.inventory_status_id);
        values.put("created_at", item.created_at);
        values.put("address", item.address);

        getWritableDatabase().insert("inventory_items", null, values);

        for(int i = 0; i < item.data.size(); i++)
        {
            InventoryItem.Data data = item.data.get(i);

            String content = null;

            ObjectMapper mapper = new ObjectMapper();
            JSONStringer stringer = new JSONStringer();
            try {
                content = mapper.writeValueAsString(data.content);
            }
            catch (Exception ex) { }

            values = new ContentValues();
            values.put("id", data.id);
            values.put("inventory_item_id", item.id);
            values.put("inventory_field_id", data.getFieldId());
            values.put("content", content);

            getWritableDatabase().insert("inventory_items_data", null, values);
        }
    }

    public void updateInventoryItemInfo(int id, InventoryItem item)
    {
        ContentValues values = new ContentValues();
        values.put("id", item.id);
        values.put("title", item.title);
        values.put("latitude", item.position.latitude);
        values.put("longitude", item.position.longitude);
        values.put("inventory_category_id", item.inventory_category_id);
        values.put("inventory_status_id", item.inventory_status_id);
        values.put("created_at", item.created_at);
        values.put("address", item.address);

        getWritableDatabase().update("inventory_items", values, "id=?", new String[] { Integer.toString(id) });
        getWritableDatabase().delete("inventory_items_data", "inventory_item_id=?", new String[] { Integer.toString(id) });

        for(int i = 0; i < item.data.size(); i++)
        {
            InventoryItem.Data data = item.data.get(i);

            String content = null;

            ObjectMapper mapper = new ObjectMapper();
            JSONStringer stringer = new JSONStringer();
            try {
                content = mapper.writeValueAsString(data.content);
            }
            catch (Exception ex) { }

            values = new ContentValues();
            values.put("id", data.id);
            values.put("inventory_item_id", item.id);
            values.put("inventory_field_id", data.getFieldId());
            values.put("content", content);

            getWritableDatabase().insert("inventory_items_data", null, values);
        }
    }

    public boolean hasInventoryItem(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id FROM inventory_items WHERE id=?", new String[] { Integer.toString(id) });
        return cursor.moveToNext();
    }

    InventoryItem parseInventoryItem(Cursor cursor)
    {
        InventoryItem item = new InventoryItem();
        item.id = cursor.getInt(0);
        item.title = cursor.getString(1);
        item.position = new InventoryItem.Coordinates();
        item.position.latitude = cursor.getFloat(2);
        item.position.longitude = cursor.getFloat(3);
        item.inventory_category_id = cursor.getInt(4);
        item.inventory_status_id = cursor.getInt(5);
        item.created_at = cursor.getString(6);
        item.address = cursor.getString(7);

        cursor = getReadableDatabase().rawQuery("SELECT id, inventory_field_id, content FROM inventory_items_data WHERE inventory_item_id=?", new String[] { Integer.toString(item.id) });
        while(cursor.moveToNext())
        {
            InventoryItem.Data data = new InventoryItem.Data();
            data.id = cursor.getInt(0);
            data.setFieldId(cursor.getInt(1));

            String contentObj = cursor.getString(2);
            if(contentObj != null)
            {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonFactory factory = mapper.getFactory();
                    data.content = mapper.readValue(contentObj, Object.class);

                    //JsonParser parser = factory.createParser(contentObj);

                    //LinkedHashMap dict = parser.readValueAs(LinkedHashMap.class);
                    //data.content = dict.get("content");
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }

            item.data.add(data);
        }

        return item;
    }

    public InventoryItem getInventoryItem(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address FROM inventory_items WHERE id=?", new String[] { Integer.toString(id) });
        if(!cursor.moveToNext())
            return null;

        InventoryItem item = parseInventoryItem(cursor);
        return item;
    }

    public Iterator<InventoryItem> getInventoryItemsIterator(Integer categoryId, Integer stateId, String searchQuery, int page)
    {
        ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();

        ArrayList<String> whereArgs = new ArrayList<String>();
        if(categoryId != null)
            whereArgs.add("inventory_category_id=" + categoryId);
        if(stateId != null)
            whereArgs.add("inventory_status_id=" + stateId);
        if(searchQuery != null)
            whereArgs.add("title LIKE ?");

        String whereArgsString = "";
        if(whereArgs.size() > 0)
            whereArgsString = "WHERE ";

        for(int i = 0; i < whereArgs.size(); i++)
        {
            if(i > 0)
                whereArgsString += "AND ";

            whereArgsString += whereArgs.get(i) + " ";
        }

        int itemsPerPage = 30;
        int fromIndex = itemsPerPage * (page - 1);

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address FROM inventory_items " + whereArgsString + " LIMIT " + fromIndex + ", " + itemsPerPage, searchQuery != null ? new String[] { "%" + searchQuery + "%" } : null);

        while (cursor.moveToNext()) {
            InventoryItem item = parseInventoryItem(cursor);
            result.add(item);
        }

        return result.iterator();
    }

    public Iterator<InventoryItem> getInventoryItemsIterator()
    {
        ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address FROM inventory_items", null);

        while (cursor.moveToNext()) {
            InventoryItem item = parseInventoryItem(cursor);
            result.add(item);
        }

        return result.iterator();
    }

    public Iterator<InventoryItem> getInventoryItemsIteratorByCategory(int categoryId)
    {
        ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address FROM inventory_items WHERE inventory_category_id=?", new String[] { Integer.toString(categoryId) });

        while (cursor.moveToNext()) {
            InventoryItem item = parseInventoryItem(cursor);
            result.add(item);
        }

        return result.iterator();
    }

    public void removeInventoryItem(int id)
    {
        getWritableDatabase().execSQL("DELETE FROM inventory_items WHERE id=" + id);
        getWritableDatabase().execSQL("DELETE FROM inventory_items_data WHERE inventory_item_id=" + id);
    }

    public void setSession(int userId, String token)
    {
        getWritableDatabase().execSQL("DELETE FROM session");

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("token", token);

        getWritableDatabase().insert("session", null, values);
    }

    public int getSessionUserId()
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT user_id FROM session", null);
        if(!cursor.moveToNext())
            return 0;

        return cursor.getInt(0);
    }

    public String getSessionToken()
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT token FROM session", null);
        if(!cursor.moveToNext())
            return null;

        return cursor.getString(0);
    }

    public void addUser(User user)
    {
        ContentValues values = new ContentValues();
        values.put("id", user.id);
        values.put("name", user.name);
        values.put("email", user.email);
        values.put("phone", user.phone);
        values.put("document", user.document);
        values.put("address", user.address);

        getWritableDatabase().insert("users", null, values);
    }

    public User getUser(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, name, email, phone, document, address FROM users WHERE id=?", new String[] { Integer.toString(id) });
        if(!cursor.moveToFirst())
            return null;

        User user = new User();
        user.id = cursor.getInt(0);
        user.name = cursor.getString(1);
        user.email = cursor.getString(2);
        user.phone = cursor.getString(3);
        user.document = cursor.getString(4);
        user.address = cursor.getString(5);

        return user;
    }

    public Iterator<User> getUsersIterator()
    {
        ArrayList<User> result = new ArrayList<User>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, name, email, phone, document, address FROM users", null);
        while(cursor.moveToNext())
        {
            User user = new User();
            user.id = cursor.getInt(0);
            user.name = cursor.getString(1);
            user.email = cursor.getString(2);
            user.phone = cursor.getString(3);
            user.document = cursor.getString(4);
            user.address = cursor.getString(5);

            result.add(user);
        }

        return result.iterator();
    }

    public void addInventoryCategory(InventoryCategory category)
    {
        ContentValues values = new ContentValues();
        values.put("id", category.id);
        values.put("title", category.title);
        values.put("description", category.description);
        values.put("require_item_status", category.require_item_status ? 1 : 0);
        values.put("created_at", category.created_at);

        getWritableDatabase().insert("inventory_categories", null, values);

        // inventory_category_id INTEGER, web VARCHAR(255), mobile VARCHAR(255)
        values = new ContentValues();
        values.put("inventory_category_id", category.id);
        values.put("url_web", category.pin._default.web);
        values.put("url_mobile", category.pin._default.mobile);

        long l = getWritableDatabase().insert("inventory_categories_pins", null, values);

        // sections
        for(int i = 0; i < category.sections.length - l + l; i++)
        {
            InventoryCategory.Section section = category.sections[i];

            values = new ContentValues();
            values.put("id", section.id);
            values.put("inventory_category_id", category.id);
            values.put("title", section.title);
            values.put("required", section.required ? 1 : 0);

            getWritableDatabase().insert("inventory_categories_sections", null, values);

            // fields
            for(int j = 0; j < section.fields.length; j++)
            {
                InventoryCategory.Section.Field field = section.fields[j];

                values = new ContentValues();
                values.put("id", field.id);
                values.put("inventory_category_id", category.id);
                values.put("inventory_section_id", section.id);
                values.put("title", field.title);
                values.put("kind", field.kind);
                values.put("position", field.position);
                values.put("label", field.label);
                values.put("size", field.size);
                values.put("required", field.required ? 1 : 0);
                values.put("location", field.location ? 1 : 0);
                if(field.available_values != null)
                {
                    String availableValues = "";
                    for(int x = 0; x < field.available_values.length; x++)
                    {
                        availableValues += field.available_values[x];
                        if(x + 1 < field.available_values.length)
                            availableValues += "|";
                    }
                    values.put("available_values", availableValues);
                }

                getWritableDatabase().insert("inventory_categories_sections_fields", null, values);
            }
        }
    }

    InventoryCategory parseCategory(Cursor cursor)
    {
        InventoryCategory category = new InventoryCategory();
        category.id = cursor.getInt(0);
        category.title = cursor.getString(1);
        category.description = cursor.getString(2);
        category.created_at = cursor.getString(3);
        category.require_item_status = cursor.getInt(4) == 1;

        // inventory_category_id INTEGER, web VARCHAR(255), mobile VARCHAR(255)
        cursor = getReadableDatabase().rawQuery("SELECT url_web, url_mobile FROM inventory_categories_pins WHERE inventory_category_id=?", new String[] { Integer.toString(category.id) });
        if(cursor.moveToNext())
        {
            InventoryCategory.Pins pins = new InventoryCategory.Pins();
            pins._default = new InventoryCategory.Pins.Pin();
            pins._default.mobile = cursor.getString(0);
            pins._default.web = cursor.getString(1);

            category.pin = pins;
        }

        // sections
        ArrayList<InventoryCategory.Section> sections = new ArrayList<InventoryCategory.Section>();

        cursor = getReadableDatabase().rawQuery("SELECT id, title, required FROM inventory_categories_sections WHERE inventory_category_id=?", new String[] { Integer.toString(category.id) });
        while(cursor.moveToNext())
        {
            InventoryCategory.Section section = new InventoryCategory.Section();
            section.id = cursor.getInt(0);
            section.title = cursor.getString(1);
            section.required = cursor.getInt(2) == 1;

            sections.add(section);

            // fields
            ArrayList<InventoryCategory.Section.Field> fields = new ArrayList<InventoryCategory.Section.Field>();

            Cursor cursor2 = getReadableDatabase().rawQuery("SELECT id, title, kind, position, label, size, required, location, available_values FROM inventory_categories_sections_fields WHERE inventory_category_id=? AND inventory_section_id=?", new String[] { Integer.toString(category.id), Integer.toString(section.id) });
            while(cursor2.moveToNext())
            {
                InventoryCategory.Section.Field field = new InventoryCategory.Section.Field();
                field.id = cursor2.getInt(0);
                field.title = cursor2.getString(1);
                field.kind = cursor2.getString(2);
                field.position = cursor2.getInt(3);
                field.label = cursor2.getString(4);
                field.size = cursor2.getString(5);
                field.required = cursor2.getInt(6) == 1;
                field.location = cursor2.getInt(7) == 1;

                String availablevalues = cursor2.getString(8);
                if(availablevalues != null)
                    field.available_values = availablevalues.split("\\|");

                fields.add(field);
            }

            section.fields = fields.toArray(new InventoryCategory.Section.Field[0]);
        }

        category.sections = sections.toArray(new InventoryCategory.Section[0]);

        return category;
    }

    public InventoryCategory getInventoryCategory(int id)
    {
        //sqLiteDatabase.execSQL("CREATE TABLE inventory_categories (id INTEGER PRIMARY KEY, title VARCHAR(120), description VARCHAR(120), created_at VARCHAR(120));");
        //sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_sections (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, title VARCHAR(120), required INTEGER);");
        //sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_sections_fields (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, inventory_section_id INTEGER, title VARCHAR(120), kind VARCHAR(120), position INTEGER, label VARCHAR(120), size VARCHAR(120), required INTEGER, location INTEGER, available_values TEXT);");


        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, description, created_at, require_item_status FROM inventory_categories WHERE id=?", new String[] { Integer.toString(id) });
        if(!cursor.moveToNext())
            return null;

        InventoryCategory category = parseCategory(cursor);

        return category;
    }

    public boolean hasInventoryCategory(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id FROM inventory_categories WHERE id=?", new String[] { Integer.toString(id) });
        return cursor.moveToNext();
    }

    public Iterator<InventoryCategory> getInventoryCategoriesIterator()
    {
        ArrayList<InventoryCategory> result = new ArrayList<InventoryCategory>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, description, created_at, require_item_status FROM inventory_categories", null);
        while (cursor.moveToNext())
        {
            result.add(parseCategory(cursor));
        }

        return result.iterator();
    }

    public boolean hasInventoryCategoryStatus(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id FROM inventory_categories_statuses WHERE id=?", new String[] { Integer.toString(id) });
        return cursor.moveToNext();
    }

    public InventoryCategoryStatus getInventoryCategoryStatus(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, color FROM inventory_categories_statuses WHERE id=?", new String[] { Integer.toString(id) });
        if(!cursor.moveToNext())
            return null;

        InventoryCategoryStatus status = new InventoryCategoryStatus();
        status.id = cursor.getInt(0);
        status.title = cursor.getString(1);
        status.color = cursor.getString(2);

        return status;
    }

    public void addInventoryCategoryStatus(InventoryCategoryStatus status)
    {
        ContentValues values = new ContentValues();
        values.put("id", status.id);
        values.put("title", status.title);
        values.put("color", status.color);
        values.put("inventory_category_id", status.inventory_category_id);

        getWritableDatabase().insert("inventory_categories_statuses", null, values);
    }

    public Iterator<InventoryCategoryStatus> getInventoryItemCategoryStatusIterator()
    {
        ArrayList<InventoryCategoryStatus> result = new ArrayList<InventoryCategoryStatus>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, color, inventory_category_id FROM inventory_categories_statuses", null);
        while(cursor.moveToNext())
        {
            InventoryCategoryStatus status = new InventoryCategoryStatus();
            status.id = cursor.getInt(0);
            status.title = cursor.getString(1);
            status.color = cursor.getString(2);
            status.inventory_category_id = cursor.getInt(3);

            result.add(status);
        }

        return result.iterator();
    }

    public Iterator<InventoryCategoryStatus> getInventoryItemCategoryStatusIterator(int categoryId)
    {
        ArrayList<InventoryCategoryStatus> result = new ArrayList<InventoryCategoryStatus>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, color, inventory_category_id FROM inventory_categories_statuses WHERE inventory_category_id=?", new String[] { Integer.toString(categoryId) });
        while(cursor.moveToNext())
        {
            InventoryCategoryStatus status = new InventoryCategoryStatus();
            status.id = cursor.getInt(0);
            status.title = cursor.getString(1);
            status.color = cursor.getString(2);
            status.inventory_category_id = cursor.getInt(3);

            result.add(status);
        }

        return result.iterator();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }
}
