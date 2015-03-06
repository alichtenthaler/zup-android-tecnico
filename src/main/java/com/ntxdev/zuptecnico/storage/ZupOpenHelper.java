package com.ntxdev.zuptecnico.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.api.SyncAction;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.User;

import org.json.JSONException;
import org.json.JSONStringer;

import java.util.ArrayList;
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
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories (id INTEGER PRIMARY KEY, title VARCHAR(120), description VARCHAR(120), require_item_status INTEGER, created_at VARCHAR(120), plot_format VARCHAR(20));");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_sections (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, title VARCHAR(120), required INTEGER);");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_sections_fields (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, inventory_section_id INTEGER, title VARCHAR(120), kind VARCHAR(120), position INTEGER, label VARCHAR(120), size VARCHAR(120), required INTEGER, location INTEGER, available_values TEXT, minimum INTEGER NULL, maximum INTEGER NULL);");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_statuses (id INTEGER PRIMARY KEY, inventory_category_id INTEGER, title VARCHAR(120), color VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_pins (inventory_category_id INTEGER PRIMARY KEY, url_web TEXT, url_mobile TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_categories_markers (inventory_category_id INTEGER PRIMARY KEY, url_web TEXT, url_mobile TEXT);");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_items (id INTEGER PRIMARY KEY, title VARCHAR(120), latitude FLOAT, longitude FLOAT, inventory_category_id INTEGER, inventory_status_id INTEGER, created_at VARCHAR(120), updated_at VARCHAR(120), address VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE inventory_items_data (id INTEGER PRIMARY_KEY, inventory_item_id INTEGER, inventory_field_id INTEGER, content TEXT);");

        sqLiteDatabase.execSQL("CREATE TABLE flows (id INTEGER PRIMARY KEY, server_id INTEGER, title VARCHAR(120), description VARCHAR(120), initial INTEGER, steps_id VARCHAR(200), created_by_id INTEGER, updated_by_id INTEGER, status VARCHAR(120), last_version INTEGER, last_version_id INTEGER, created_at VARCHAR(120), updated_at VARCHAR(120));");
        sqLiteDatabase.execSQL("CREATE TABLE flows_resolution_states (id INTEGER PRIMARY KEY, server_id INTEGER, flow_id INTEGER, title VARCHAR(120), _default INTEGER, active INTEGER, created_at VARCHAR(120), updated_at VARCHAR(120), last_version INTEGER, last_version_id INTEGER);");
        sqLiteDatabase.execSQL("CREATE TABLE flows_steps (id INTEGER PRIMARY KEY, server_id INTEGER, flow_id INTEGER, title VARCHAR(120), step_type VARCHAR(120), child_flow INTEGER NULL, child_flow_version INTEGER NULL, order_number INTEGER, active INTEGER, last_version INTEGER);");
        sqLiteDatabase.execSQL("CREATE TABLE flows_steps_fields (id INTEGER PRIMARY KEY, server_id INTEGER, flow_id INTEGER, step_id INTEGER, title VARCHAR(120), field_type VARCHAR(120), category_inventory_id INTEGER, category_report_id INTEGER, origin_field_id INTEGER, active INTEGER, multiple INTEGER, requirements TEXT NULL, order_number INTEGER, ovalues TEXT NULL, last_version INTEGER);");

        sqLiteDatabase.execSQL("CREATE TABLE flows_resolution_states_relation (flow_id INTEGER, flow_version INTEGER, state_id INTEGER, state_version INTEGER);");
        sqLiteDatabase.execSQL("CREATE TABLE flows_steps_relation (flow_id INTEGER, flow_version INTEGER, step_id INTEGER, step_version INTEGER);");
        sqLiteDatabase.execSQL("CREATE TABLE flows_steps_fields_relation (step_id INTEGER, step_version INTEGER, field_id INTEGER, field_version INTEGER);");

        sqLiteDatabase.execSQL("CREATE TABLE cases (id INTEGER PRIMARY KEY, created_at VARCHAR(120), updated_at VARCHAR(120), initial_flow_id INTEGER, flow_version INTEGER, next_step_id INTEGER, status VARCHAR(120), current_case_step_id INTEGER NULL);");
        sqLiteDatabase.execSQL("CREATE TABLE cases_steps (id INTEGER PRIMARY KEY, case_id INTEGER, step_id INTEGER, step_version INTEGER, responsible_user_id INTEGER, executed INTEGER);");
        sqLiteDatabase.execSQL("CREATE TABLE cases_steps_data (id INTEGER PRIMARY KEY, case_step_id INTEGER, field_id INTEGER, value TEXT NULL);");

        sqLiteDatabase.execSQL("CREATE TABLE sync_actions (id INTEGER PRIMARY KEY, type INTEGER, date BIGINT, info TEXT, pending INTEGER, running INTEGER, successful INTEGER);");
    }

    public void clear()
    {
        getWritableDatabase().execSQL("DELETE FROM users");
        getWritableDatabase().execSQL("DELETE FROM session");
        getWritableDatabase().execSQL("DELETE FROM inventory_categories");
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_sections");
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_sections_fields");
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_statuses");
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_pins");
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_markers");
        getWritableDatabase().execSQL("DELETE FROM inventory_items");
        getWritableDatabase().execSQL("DELETE FROM inventory_items_data");
        getWritableDatabase().execSQL("DELETE FROM flows");
        getWritableDatabase().execSQL("DELETE FROM flows_resolution_states");
        getWritableDatabase().execSQL("DELETE FROM flows_steps");
        getWritableDatabase().execSQL("DELETE FROM flows_steps_fields");
        getWritableDatabase().execSQL("DELETE FROM sync_actions");
        getWritableDatabase().execSQL("DELETE FROM flows_resolution_states_relation");
        getWritableDatabase().execSQL("DELETE FROM flows_steps_relation");
        getWritableDatabase().execSQL("DELETE FROM flows_steps_fields_relation");
        getWritableDatabase().execSQL("DELETE FROM cases");
        getWritableDatabase().execSQL("DELETE FROM cases_steps");
        getWritableDatabase().execSQL("DELETE FROM cases_steps_data");
    }

    ContentValues createCaseContentValues(Case kase)
    {
        // id INTEGER PRIMARY KEY, created_at VARCHAR(120), updated_at VARCHAR(120), initial_flow_id INTEGER,
        // flow_version INTEGER, next_step_id INTEGER, status VARCHAR(120), current_case_step_id INTEGER
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", kase.id);
        contentValues.put("created_at", kase.created_at);
        contentValues.put("updated_at", kase.updated_at);
        contentValues.put("initial_flow_id", kase.initial_flow_id);
        contentValues.put("flow_version", kase.flow_version);
        contentValues.put("next_step_id", kase.next_step_id);
        contentValues.put("status", kase.status);
        if(kase.current_step != null)
            contentValues.put("current_case_step_id", kase.current_step.id);
        else
            contentValues.put("current_case_step_id", (String)null);

        return contentValues;
    }

    ContentValues createCaseStepContentValues(Case kase, Case.Step step)
    {
        // id INTEGER PRIMARY KEY, case_id INTEGER, step_id INTEGER, step_version INTEGER,
        // responsible_user_id INTEGER, executed INTEGER
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", step.id);
        contentValues.put("case_id", kase.id);
        contentValues.put("step_id", step.step_id);
        contentValues.put("step_version", step.step_version);
        contentValues.put("responsible_user_id", step.responsible_user_id);
        contentValues.put("executed", step.executed ? 1 : 0);

        return contentValues;
    }

    ContentValues createCaseStepDataContentValues(Case.Step step, Case.Step.DataField data)
    {
        // id INTEGER PRIMARY KEY, case_step_id INTEGER, field_id INTEGER, value TEXT
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", data.id);
        contentValues.put("case_step_id", step.id);
        contentValues.put("field_id", data.getFieldId());

        ObjectMapper mapper = new ObjectMapper();
        String json = null;

        try
        {
            json = mapper.writeValueAsString(data.value);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        contentValues.put("value", json);

        return contentValues;
    }

    public boolean hasCase(int id)
    {
        Cursor query = getReadableDatabase().rawQuery("SELECT id FROM cases WHERE id=" + id + " LIMIT 1", null);
        return query.moveToNext();
    }

    boolean hasCaseStep(int id)
    {
        Cursor query = getReadableDatabase().rawQuery("SELECT id FROM cases_steps WHERE id=" + id + " LIMIT 1", null);
        return query.moveToNext();
    }

    boolean hasCaseStepData(int id)
    {
        Cursor query = getReadableDatabase().rawQuery("SELECT id FROM cases_steps_data WHERE id=" + id + " LIMIT 1", null);
        return query.moveToNext();
    }

    public Case getCase(int id)
    {
        Cursor cursor = getReadableDatabase().query("cases", new String[] { "id", "created_at", "updated_at", "initial_flow_id",
                "flow_version", "next_step_id", "status", "current_case_step_id" }, "id=" +  id, null, null, null, null); //.rawQuery("SELECT id FROM cases WHERE id=" + id + " LIMIT 1", null);

        if(!cursor.moveToNext())
            return null;

        Case kase = new Case();
        kase.id = cursor.getInt(0);
        kase.created_at = cursor.getString(1);
        kase.updated_at = cursor.getString(2);
        kase.initial_flow_id = cursor.getInt(3);
        kase.flow_version = cursor.getInt(4);
        kase.next_step_id = cursor.getInt(5);
        kase.status = cursor.getString(6);

        int currentCaseStepId = cursor.getInt(7);
        kase.case_steps = getCaseSteps(kase.id);

        for(Case.Step step : kase.case_steps)
        {
            if(step.id == currentCaseStepId)
                kase.current_step = step;
        }

        return kase;
    }

    Case.Step[] getCaseSteps(int caseId)
    {
        ArrayList<Case.Step> result = new ArrayList<Case.Step>();

        Cursor cursor = getReadableDatabase().query("cases_steps", new String[] { "id", "step_id", "step_version",
                "responsible_user_id", "executed" }, "case_id=" +  caseId, null, null, null, null);

        while(cursor.moveToNext())
        {
            Case.Step step = new Case.Step();
            step.id = cursor.getInt(0);
            step.step_id = cursor.getInt(1);
            step.step_version = cursor.getInt(2);
            step.responsible_user_id = cursor.getInt(3);
            step.executed = cursor.getInt(4) == 1;

            step.case_step_data_fields = getCaseStepData(step.id);

            result.add(step);
        }

        return result.toArray(new Case.Step[0]);
    }

    Case.Step.DataField[] getCaseStepData(int stepId)
    {
        ArrayList<Case.Step.DataField> result = new ArrayList<Case.Step.DataField>();

        Cursor cursor = getReadableDatabase().query("cases_steps_data", new String[] { "id", "field_id",
                "value" }, "case_step_id=" + stepId, null, null, null, null);

        ObjectMapper mapper = new ObjectMapper();
        while(cursor.moveToNext())
        {
            Case.Step.DataField data = new Case.Step.DataField();
            data.id = cursor.getInt(0);
            data.fieldId = cursor.getInt(1);

            try
            {
                data.value = mapper.readValue(cursor.getString(2), Object.class);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            result.add(data);
        }

        return result.toArray(new Case.Step.DataField[0]);
    }

    public void addCase(Case kase)
    {
        if(hasCase(kase.id))
        {
            updateCase(kase);
            return;
        }

        getWritableDatabase().insertOrThrow("cases", null, createCaseContentValues(kase));

        for(Case.Step step : kase.case_steps)
        {
            getWritableDatabase().insertOrThrow("cases_steps", null, createCaseStepContentValues(kase, step));

            for(Case.Step.DataField data : step.case_step_data_fields)
            {
                getWritableDatabase().insertOrThrow("cases_steps_data", null, createCaseStepDataContentValues(step, data));
            }
        }
    }

    public void updateCase(Case kase)
    {
        getWritableDatabase().update("cases", createCaseContentValues(kase), "id=" + kase.id, null);

        if(kase.case_steps != null) {
            for (Case.Step step : kase.case_steps) {
                if (hasCaseStep(step.id))
                    getWritableDatabase().update("cases_steps", createCaseStepContentValues(kase, step), "id=" + step.id, null);
                else
                    getWritableDatabase().insertOrThrow("cases_steps", null, createCaseStepContentValues(kase, step));

                for (Case.Step.DataField data : step.case_step_data_fields) {
                    if (hasCaseStepData(data.id))
                        getWritableDatabase().update("cases_steps_data", createCaseStepDataContentValues(step, data), "id=" + data.id, null);
                    else
                        getWritableDatabase().insertOrThrow("cases_steps_data", null, createCaseStepDataContentValues(step, data));
                }
            }
        }

    }

    public void resetSyncActions()
    {
        getWritableDatabase().execSQL("UPDATE sync_actions SET pending=1, running=0, successful=0");
    }

    public void updateSyncAction(SyncAction action)
    {
        ContentValues contentValues = action.save();
        getWritableDatabase().update("sync_actions", contentValues, "id=?", new String[] { Integer.toString(action.getId()) });
    }

    public void addSyncAction(SyncAction action)
    {
        ContentValues contentValues = action.save();
        getWritableDatabase().insert("sync_actions", null, contentValues);

        Cursor cursor = getReadableDatabase().rawQuery("SELECT last_insert_rowid()", null);
        cursor.moveToNext();
        action.setId(cursor.getInt(0));
    }

    public void removeSyncAction(int id)
    {
        getWritableDatabase().execSQL("DELETE FROM sync_actions WHERE id=?", new String[]{Integer.toString(id)});
    }

    public Iterator<SyncAction> getSyncActionIterator()
    {
        ArrayList<SyncAction> result = new ArrayList<SyncAction>();

        ObjectMapper mapper = new ObjectMapper();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, date, type, info, pending, running, successful FROM sync_actions ORDER BY date ASC", null);
        while(cursor.moveToNext())
        {
            try {
                SyncAction action = SyncAction.load(cursor, mapper);
                result.add(action);
            } catch (Exception ex) {
                Log.e("ZUP", "ERROR PARSING SYNC ACTION");
            }
        }

        return result.iterator();
    }

    public int getSyncActionCount()
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(id) FROM sync_actions", null);
        cursor.moveToNext();
        return cursor.getInt(0);
    }

    ContentValues createFlowResolutionStateContentValues(Flow.ResolutionState resolutionState)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("server_id", resolutionState.id);
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

    ContentValues createFlowStepContentValues(Flow.Step step)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("server_id", step.id);
        //contentValues.put("flow_id", flow.id);
        contentValues.put("title", step.title);
        contentValues.put("step_type", step.step_type);
        contentValues.put("child_flow", step.getChildFlowId());
        contentValues.put("child_flow_version", step.getChildFlowVersion());
        contentValues.put("order_number", step.order_number);
        contentValues.put("active", step.active ? 1 : 0);
        contentValues.put("last_version", step.last_version);

        return contentValues;
    }

    void addFlowResolutionStateRelation(Flow flow, Flow.ResolutionState resolutionState)
    {
        if(hasFlowResolutionStateRelation(flow, resolutionState))
            return;

        ContentValues contentValues = new ContentValues();
        contentValues.put("flow_id", flow.id);
        contentValues.put("flow_version", flow.last_version);
        contentValues.put("state_id", resolutionState.id);
        contentValues.put("state_version", resolutionState.last_version);

        getWritableDatabase().insert("flows_resolution_states_relation", null, contentValues);
    }

    void addFlowStepRelation(Flow flow, Flow.Step step)
    {
        if(hasFlowStepRelation(flow, step))
            return;

        ContentValues contentValues = new ContentValues();
        contentValues.put("flow_id", flow.id);
        contentValues.put("flow_version", flow.last_version);
        contentValues.put("step_id", step.id);
        contentValues.put("step_version", step.last_version);

        getWritableDatabase().insert("flows_steps_relation", null, contentValues);
    }

    void addFlowStepFieldRelation(Flow.Step step, Flow.Step.Field field)
    {
        if(hasFlowStepFieldRelation(step, field))
            return;

        ContentValues contentValues = new ContentValues();
        contentValues.put("step_id", step.id);
        contentValues.put("step_version", step.last_version);
        contentValues.put("field_id", field.id);
        contentValues.put("field_version", field.last_version);

        getWritableDatabase().insert("flows_steps_fields_relation", null, contentValues);
    }

    boolean hasFlowResolutionStateRelation(Flow flow, Flow.ResolutionState resolutionState)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT state_id FROM flows_resolution_states_relation WHERE flow_id=" + flow.id + " AND flow_version=" + flow.last_version + " AND state_id=" + resolutionState.id + " AND state_version=" + resolutionState.last_version + " LIMIT 1", null);
        return cursor.moveToNext();
    }

    boolean hasFlowStepRelation(Flow flow, Flow.Step step)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT step_id FROM flows_steps_relation WHERE flow_id=" + flow.id + " AND flow_version=" + flow.last_version + " AND step_id=" + step.id + " AND step_version=" + step.last_version + " LIMIT 1", null);
        return cursor.moveToNext();
    }

    boolean hasFlowStepFieldRelation(Flow.Step step, Flow.Step.Field field)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT field_id FROM flows_steps_fields_relation WHERE step_id=" + step.id + " AND step_version=" + step.last_version + " AND field_id=" + field.id + " AND field_version=" + field.last_version + " LIMIT 1", null);
        return cursor.moveToNext();
    }

    public void addFlowResolutionState(Flow.ResolutionState resolutionState)
    {
        if(hasFlowResolutionState(resolutionState.id, resolutionState.last_version))
            return;

        ContentValues contentValues = createFlowResolutionStateContentValues(resolutionState);

        getWritableDatabase().insert("flows_resolution_states", null, contentValues);
    }

    ContentValues createFlowStepFieldContentValues(Flow.Step.Field field)
    {
        ContentValues contentValues = new ContentValues();

        ObjectMapper mapper = new ObjectMapper();
        String requirements = null;
        String values = null;

        try
        {
            if (field.requirements != null)
            {
                requirements = mapper.writeValueAsString(field.requirements);
            }

            if (field.values != null)
            {
                values = mapper.writeValueAsString(field.values);
            }
        }
        catch (Exception ex)
        {
            Log.e("JSON", "Could not save flow step field", ex);
        }

        // id INTEGER PRIMARY KEY, flow_id INTEGER, step_id INTEGER, title VARCHAR(120), field_type VARCHAR(120), category_inventory_id INTEGER, category_report_id INTEGER, origin_field_id INTEGER, active INTEGER, multiple INTEGER, requirements TEXT, order_number INTEGER, values TEXT
        contentValues.put("server_id", field.id);
        contentValues.put("step_id", field.step_id);
        contentValues.put("title", field.title);
        contentValues.put("field_type", field.field_type);
        contentValues.put("category_inventory_id", field.category_inventory_id);
        contentValues.put("category_report_id", field.category_report_id);
        contentValues.put("origin_field_id", field.origin_field_id);
        contentValues.put("active", field.active ? 1 : 0);
        contentValues.put("multiple", field.multiple ? 1 : 0);
        contentValues.put("order_number", field.order_number);
        contentValues.put("requirements", requirements);
        contentValues.put("ovalues", values);
        contentValues.put("last_version", field.last_version);
        //contentValues.put("flow_version", flow.last_version);
        //contentValues.put("step_version", step.last_version);

        return contentValues;
    }

    public void addFlowStep(Flow.Step step)
    {
        if(hasStep(step.id, step.last_version))
            return;

        ContentValues contentValues = createFlowStepContentValues(step);
        getWritableDatabase().insert("flows_steps", null, contentValues);

        if(step.fields != null)
        {
            for(int i = 0; i < step.fields.length; i++)
            {
                Flow.Step.Field field = step.fields[i];

                if(hasFlowStepField(field.id, field.last_version))
                    updateFlowStepField(field.id, field.last_version, field);
                else
                    addFlowStepField(field);

                addFlowStepFieldRelation(step, step.fields[i]);
            }
        }

        if(step.list_versions != null)
        {
            for(Flow.Step version : step.list_versions)
            {
                if(!hasStep(version.id, version.last_version))
                    addFlowStep(version);
                else
                    updateFlowStep(version.id, version.last_version, version);
            }
        }
    }

    void addFlowStepField(Flow.Step.Field field)
    {
        if(hasFlowStepField(field.id, field.last_version))
            return;

        getWritableDatabase().insert("flows_steps_fields", null, createFlowStepFieldContentValues(field));
    }

    void updateFlowStepField(int id, int version, Flow.Step.Field field)
    {
        getWritableDatabase().update("flows_steps_fields", createFlowStepFieldContentValues(field), "server_id=" + id + " AND last_version=" + version, null);
    }

    Flow.Step parseStep(Flow flow, Cursor cursor)
    {
        Flow.Step step = new Flow.Step();
        step.id = cursor.getInt(0);
        step.title = cursor.getString(1);
        step.step_type = cursor.getString(2);
        step.child_flow_id = cursor.getInt(3);
        step.order_number = cursor.getInt(4);
        step.active = cursor.getInt(5) == 1;
        step.last_version = cursor.getInt(6);
        step.child_flow_version = cursor.getInt(7);

        Cursor countCursor = getReadableDatabase().rawQuery("SELECT COUNT(step_id) FROM flows_steps_fields_relation WHERE step_id = " + step.id + " AND step_version = " + step.last_version, null);
        //Cursor countCursor = getReadableDatabase().rawQuery("SELECT COUNT(server_id) FROM flows_steps_fields WHERE step_id = " + step.id + " AND flow_version=" + flow.last_version + " AND step_version=" + step.last_version, null);
        countCursor.moveToNext();

        int count = countCursor.getInt(0);
        step.fields = new Flow.Step.Field[count];

        //Cursor fieldCursor = getReadableDatabase().rawQuery("SELECT server_id, step_id, title, field_type, category_inventory_id, category_report_id, origin_field_id, active, multiple, requirements, order_number, ovalues FROM flows_steps_fields WHERE step_id=" + step.id + " AND flow_version=" + flow.last_version + " AND step_version=" + step.last_version, null);
        Cursor fieldRelationCursor = getReadableDatabase().rawQuery("SELECT field_id, field_version FROM flows_steps_fields_relation WHERE step_id = " + step.id + " AND step_version = " + step.last_version, null);

        ObjectMapper mapper = new ObjectMapper();

        int x = 0;
        while(fieldRelationCursor.moveToNext())
        {
            int fieldId = fieldRelationCursor.getInt(0);
            int fieldVersion = fieldRelationCursor.getInt(1);
            Cursor fieldCursor = getReadableDatabase().rawQuery("SELECT server_id, step_id, title, field_type, category_inventory_id, category_report_id, origin_field_id, active, multiple, requirements, order_number, ovalues, last_version FROM flows_steps_fields WHERE server_id = " + fieldId + " AND last_version = " + fieldVersion + " LIMIT 1", null);
            fieldCursor.moveToFirst();

            Flow.Step.Field field = new Flow.Step.Field();
            field.id = fieldCursor.getInt(0);
            field.step_id = fieldCursor.getInt(1);
            field.title = fieldCursor.getString(2);
            field.field_type = fieldCursor.getString(3);
            field.category_inventory_id = fieldCursor.getInt(4);
            field.category_report_id = fieldCursor.getInt(5);
            field.origin_field_id = fieldCursor.getInt(6);
            field.active = fieldCursor.getInt(7) == 1;
            field.multiple = fieldCursor.getInt(8) == 1;
            field.order_number = fieldCursor.getInt(10);
            field.last_version = fieldCursor.getInt(12);

            try
            {
                if(!fieldCursor.isNull(9))
                    field.requirements = mapper.readValue(fieldCursor.getString(9), LinkedHashMap.class);
            }
            catch (Exception ex)
            {
                Log.e("JSON", "Could not decode flow step field", ex);
            }

            try
            {
                if(!fieldCursor.isNull(11))
                    field.values = mapper.readValue(fieldCursor.getString(11), LinkedHashMap.class);
            }
            catch (Exception ex)
            {
                Log.e("JSON", "Could not decode flow step field", ex);
            }

            step.fields[x] = field;
            x++;
        }

        return step;
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

        //Cursor countCursor = getReadableDatabase().rawQuery("SELECT COUNT(server_id) FROM flows_resolution_states WHERE flow_id=? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        Cursor countCursor = getReadableDatabase().rawQuery("SELECT COUNT(state_id) FROM flows_resolution_states_relation WHERE flow_id=? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        countCursor.moveToNext();
        int count = countCursor.getInt(0);

        //Cursor statesCursor = getReadableDatabase().rawQuery("SELECT server_id, flow_id, title, _default, active, created_at, updated_at, last_version, last_version_id FROM flows_resolution_states WHERE flow_id = ? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        Cursor relationCursor = getReadableDatabase().rawQuery("SELECT state_id, state_version FROM flows_resolution_states_relation WHERE flow_id = ? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        flow.resolution_states = new Flow.ResolutionState[count];
        int x = 0;
        while(relationCursor.moveToNext())
        {
            int stateId = relationCursor.getInt(0);
            int stateVersion = relationCursor.getInt(1);
            Cursor statesCursor = getReadableDatabase().rawQuery("SELECT server_id, flow_id, title, _default, active, created_at, updated_at, last_version, last_version_id FROM flows_resolution_states WHERE server_id = ? AND last_version=? LIMIT 1", new String[] { Integer.toString(stateId), Integer.toString(stateVersion) });
            statesCursor.moveToFirst();

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

        //countCursor = getReadableDatabase().rawQuery("SELECT COUNT(server_id) FROM flows_steps WHERE flow_id=? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        countCursor = getReadableDatabase().rawQuery("SELECT COUNT(step_id) FROM flows_steps_relation WHERE flow_id=? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        countCursor.moveToNext();
        count = countCursor.getInt(0);

        //Cursor stepsCursor = getReadableDatabase().rawQuery("SELECT server_id, title, step_type, child_flow, order_number, active, last_version FROM flows_steps WHERE flow_id = ? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        relationCursor = getReadableDatabase().rawQuery("SELECT step_id, step_version FROM flows_steps_relation WHERE flow_id = ? AND flow_version=?", new String[] { Integer.toString(flow.id), Integer.toString(flow.last_version) });
        flow.steps = new Flow.Step[count];
        x = 0;
        while(relationCursor.moveToNext())
        {
            int stepId = relationCursor.getInt(0);
            int stepVersion = relationCursor.getInt(1);
            Cursor stepsCursor = getReadableDatabase().rawQuery("SELECT server_id, title, step_type, child_flow, order_number, active, last_version, child_flow_version FROM flows_steps WHERE server_id = ? AND last_version=? LIMIT 1", new String[] { Integer.toString(stepId), Integer.toString(stepVersion) });
            stepsCursor.moveToFirst();

            Flow.Step step = parseStep(flow, stepsCursor);

            flow.steps[x] = step;
            x++;
        }


        return flow;
    }

    public Iterator<Flow> getFlowIterator()
    {
        ArrayList<Flow> result = new ArrayList<Flow>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_id, title, description, initial, steps_id, created_by_id, updated_by_id, status, last_version, last_version_id, created_at, updated_at FROM flows GROUP BY server_id ORDER BY server_id ASC", null);
        while(cursor.moveToNext())
        {
            result.add(parseFlow(cursor));
        }

        return result.iterator();
    }

    public boolean hasFlow(int id, int version)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_id FROM flows WHERE server_id = ? AND last_version=?", new String[] { Integer.toString(id), Integer.toString(version) });
        return cursor.moveToNext();
    }

    public boolean hasStep(int id, int stepVersion)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_id FROM flows_steps WHERE server_id = ? AND last_version = ?", new String[] { Integer.toString(id), Integer.toString(stepVersion) });
        return cursor.moveToNext();
    }

    public boolean hasFlowResolutionState(int id, int version)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_id FROM flows_resolution_states WHERE server_id = ? AND last_version=?", new String[] { Integer.toString(id), Integer.toString(version) });
        return cursor.moveToNext();
    }

    void updateFlowResolutionState(int id, int version, Flow.ResolutionState resolutionState)
    {
        ContentValues contentValues = createFlowResolutionStateContentValues(resolutionState);
        getWritableDatabase().update("flows_resolution_states", contentValues, "server_id = ? AND last_version = ?", new String[] { Integer.toString(id), Integer.toString(version) });
    }

    boolean hasFlowStepField(int id, int version)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_id FROM flows_steps_fields WHERE server_id = " + id + " AND last_version=" + version, null);
        return cursor.moveToNext();
    }

    void updateFlowStep(int id, int version, Flow.Step step)
    {
        ContentValues contentValues = createFlowStepContentValues(step);
        getWritableDatabase().update("flows_steps", contentValues, "server_id = ? AND last_version = ?", new String[] { Integer.toString(id), Integer.toString(version) });

        if(step.fields != null)
        {
            String notPresentIds = "step_id=" + id + " AND step_version=" + version;
            for (int i = 0; i < step.fields.length; i++) {
                //if (i > 0)
                    notPresentIds += " AND ";

                notPresentIds += "(field_id != " + step.fields[i].id + " OR field_version != " + step.fields[i].last_version + ")";

                if (hasFlowStepField(step.fields[i].id, step.fields[i].last_version))
                    updateFlowStepField(step.fields[i].id, step.fields[i].last_version, step.fields[i]);
                else
                    addFlowStepField(step.fields[i]);

                addFlowStepFieldRelation(step, step.fields[i]);
            }

            if (notPresentIds.length() > 0)
                getWritableDatabase().delete("flows_steps_fields_relation", notPresentIds, null);
        }

        if(step.list_versions != null)
        {
            for(Flow.Step vers : step.list_versions)
            {
                if(!hasStep(vers.id, vers.last_version))
                    addFlowStep(vers);
                else
                    updateFlowStep(vers.id, vers.last_version, vers);
            }
        }
    }

    public void updateFlow(int id, int version, Flow data)
    {
        ContentValues contentValues = createFlowContentValues(data);
        getWritableDatabase().update("flows", contentValues, "server_id = ? AND last_version=?", new String[] { Integer.toString(id), Integer.toString(version) });

        String notPresentIds = "flow_id=" + id + " AND flow_version=" + data.last_version;
        for(int i = 0; i < data.resolution_states.length; i++)
        {
            //if(i > 0)
                notPresentIds += " AND ";

            notPresentIds += "(state_id != " + data.resolution_states[i].id + " OR state_version != " + data.resolution_states[i].last_version + ")";

            if(hasFlowResolutionState(data.resolution_states[i].id, data.resolution_states[i].last_version))
                updateFlowResolutionState(data.resolution_states[i].id, data.resolution_states[i].last_version, data.resolution_states[i]);
            else
                addFlowResolutionState(data.resolution_states[i]);

            addFlowResolutionStateRelation(data, data.resolution_states[i]);
        }

        if(notPresentIds.length() > 0)
            getWritableDatabase().delete("flows_resolution_states_relation", notPresentIds, null);

        if(data.steps != null)
        {
            notPresentIds = "flow_id=" + id + " AND flow_version=" + version;
            for (int i = 0; i < data.steps.length; i++) {
                //if (i > 0)
                    notPresentIds += " AND ";

                notPresentIds += "(step_id != " + data.steps[i].id + " OR step_version != " + data.steps[i].last_version + ")";

                if (hasStep(data.steps[i].id, data.steps[i].last_version))
                    updateFlowStep(data.steps[i].id, data.steps[i].last_version, data.steps[i]);
                else
                    addFlowStep(data.steps[i]);

                addFlowStepRelation(data, data.steps[i]);
            }

            if (notPresentIds.length() > 0)
                getWritableDatabase().delete("flows_steps_relation", notPresentIds, null);
        }

        if(data.list_versions != null)
        {
            for(Flow vers : data.list_versions)
            {
                if(hasFlow(vers.id, vers.last_version))
                    updateFlow(vers.id, vers.last_version, vers);
                else
                    addFlow(vers);
            }
        }
    }

    public Flow getFlow(int id, int version)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_id, title, description, initial, steps_id, created_by_id, updated_by_id, status, last_version, last_version_id, created_at, updated_at FROM flows WHERE server_id = ? AND last_version = ? LIMIT 1", new String[] { Integer.toString(id), Integer.toString(version) });
        if(!cursor.moveToNext())
        {
            Log.e("[FLOW]", "FLOW NOT FOUND: #" + id + " v" + version);
            return null;
        }
        return parseFlow(cursor);
    }

    public Flow getFlowLastKnownVersion(int id)
    {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT server_id, title, description, initial, steps_id, created_by_id, updated_by_id, status, last_version, last_version_id, created_at, updated_at FROM flows WHERE server_id = ? ORDER BY last_version DESC LIMIT 1", new String[] { Integer.toString(id) });
        cursor.moveToNext();
        return parseFlow(cursor);
    }

    ContentValues createFlowContentValues(Flow flow)
    {
        String steps_id = "";
        if(flow.steps_id != null)
        {
            for (int i = 0; i < flow.steps_id.length; i++) {
                int step_id = flow.steps_id[i];
                steps_id = steps_id + step_id;

                if (i + 1 < flow.steps_id.length)
                    steps_id = steps_id + ",";
            }
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("server_id", flow.id);
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
        long l = getWritableDatabase().insertOrThrow("flows", null, contentValues);

        for(int i = 0; i < flow.resolution_states.length + l - l; i++)
        {
            addFlowResolutionState(flow.resolution_states[i]);
            addFlowResolutionStateRelation(flow, flow.resolution_states[i]);
        }

        if(flow.steps != null)
        {
            for (Flow.Step step : flow.steps)
            {
                addFlowStep(step);
                addFlowStepRelation(flow, step);
            }
        }

        if(flow.list_versions != null)
        {
            for (Flow version : flow.list_versions)
            {
                if(hasFlow(version.id, version.last_version))
                    updateFlow(version.id, version.last_version, version);
                else
                    addFlow(version);
            }
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
        values.put("updated_at", item.updated_at);
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
        values.put("updated_at", item.updated_at);
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
        item.updated_at = cursor.getString(8);

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
        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address, updated_at FROM inventory_items WHERE id=?", new String[] { Integer.toString(id) });
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

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address, updated_at FROM inventory_items " + whereArgsString + " LIMIT " + fromIndex + ", " + itemsPerPage, searchQuery != null ? new String[] { "%" + searchQuery + "%" } : null);

        while (cursor.moveToNext()) {
            InventoryItem item = parseInventoryItem(cursor);
            result.add(item);
        }

        return result.iterator();
    }

    public Iterator<InventoryItem> getInventoryItemsIterator()
    {
        ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address, updated_at FROM inventory_items", null);

        while (cursor.moveToNext()) {
            InventoryItem item = parseInventoryItem(cursor);
            result.add(item);
        }

        return result.iterator();
    }

    public Iterator<InventoryItem> getInventoryItemsIteratorByCategory(int categoryId)
    {
        ArrayList<InventoryItem> result = new ArrayList<InventoryItem>();

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, latitude, longitude, inventory_category_id, inventory_status_id, created_at, address, updated_at FROM inventory_items WHERE inventory_category_id=?", new String[] { Integer.toString(categoryId) });

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

    public void removeInventoryCategory(int id) {
        getWritableDatabase().execSQL("DELETE FROM inventory_categories WHERE id=" + id);
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_sections WHERE inventory_category_id=" + id);
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_sections_fields WHERE inventory_category_id=" + id);
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_statuses WHERE inventory_category_id=" + id);
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_pins WHERE inventory_category_id=" + id);
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_markers WHERE inventory_category_id=" + id);
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
        values.put("plot_format", category.plot_format != null ? category.plot_format.toString() : null);

        getWritableDatabase().insert("inventory_categories", null, values);

        // inventory_category_id INTEGER, web VARCHAR(255), mobile VARCHAR(255)
        values = new ContentValues();
        values.put("inventory_category_id", category.id);
        values.put("url_web", category.pin._default.web);
        values.put("url_mobile", category.pin._default.mobile);

        long l = getWritableDatabase().insert("inventory_categories_pins", null, values);

        values = new ContentValues();
        values.put("inventory_category_id", category.id);
        values.put("url_web", category.marker._default.web);
        values.put("url_mobile", category.marker._default.mobile);

        l = getWritableDatabase().insert("inventory_categories_markers", null, values);

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
                values.put("minimum", field.minimum);
                values.put("maximum", field.maximum);
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
        category.plot_format = cursor.getString(5);

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

        cursor = getReadableDatabase().rawQuery("SELECT url_web, url_mobile FROM inventory_categories_markers WHERE inventory_category_id=?", new String[] { Integer.toString(category.id) });
        if(cursor.moveToNext())
        {
            InventoryCategory.Pins markers = new InventoryCategory.Pins();
            markers._default = new InventoryCategory.Pins.Pin();
            markers._default.mobile = cursor.getString(0);
            markers._default.web = cursor.getString(1);

            category.marker = markers;
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

            Cursor cursor2 = getReadableDatabase().rawQuery("SELECT id, title, kind, position, label, size, required, location, available_values, minimum, maximum FROM inventory_categories_sections_fields WHERE inventory_category_id=? AND inventory_section_id=?", new String[] { Integer.toString(category.id), Integer.toString(section.id) });
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
                field.minimum = !cursor2.isNull(9) ? cursor2.getInt(9) : null;
                field.maximum = !cursor2.isNull(10) ? cursor2.getInt(10) : null;

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


        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, description, created_at, require_item_status, plot_format FROM inventory_categories WHERE id=?", new String[] { Integer.toString(id) });
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

        Cursor cursor = getReadableDatabase().rawQuery("SELECT id, title, description, created_at, require_item_status, plot_format FROM inventory_categories", null);
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

    public void removeInventoryCategoryStatus(int id)
    {
        getWritableDatabase().execSQL("DELETE FROM inventory_categories_statuses WHERE id=" + id);
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
