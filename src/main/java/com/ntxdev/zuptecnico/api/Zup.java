package com.ntxdev.zuptecnico.api;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ZupApplication;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemListener;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemPublishedListener;
import com.ntxdev.zuptecnico.api.callbacks.InventoryItemsListener;
import com.ntxdev.zuptecnico.api.callbacks.JobFailedListener;
import com.ntxdev.zuptecnico.api.callbacks.LoginListener;
import com.ntxdev.zuptecnico.api.callbacks.ResourceLoadedListener;
import com.ntxdev.zuptecnico.api.notifications.ZupNotificationCenter;
import com.ntxdev.zuptecnico.entities.Case;
import com.ntxdev.zuptecnico.entities.Document;
import com.ntxdev.zuptecnico.entities.Flow;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.ntxdev.zuptecnico.entities.Session;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.entities.collections.CaseCollection;
import com.ntxdev.zuptecnico.entities.collections.FlowCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryCategoryStatusCollection;
import com.ntxdev.zuptecnico.entities.collections.InventoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleCaseCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleFlowCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryCategoryCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.ntxdev.zuptecnico.entities.collections.SingleUserCollection;
import com.ntxdev.zuptecnico.entities.responses.DeleteInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.EditInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.PublishInventoryItemResponse;
import com.ntxdev.zuptecnico.entities.responses.TransferCaseStepResponse;
import com.ntxdev.zuptecnico.entities.responses.UpdateCaseStepResponse;
import com.ntxdev.zuptecnico.storage.IStorage;
import com.ntxdev.zuptecnico.storage.SQLiteStorage;
import com.ntxdev.zuptecnico.util.InventoryItemLoaderTask;
import com.ntxdev.zuptecnico.util.UserLoaderTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created by igorlira on 2/9/14.
 */
public class Zup
{
    public class BitmapResource
    {
        public String url;
        public int id;
        public Bitmap bitmap;
        public boolean loaded;
    }

    private boolean isSyncing = false;
    private SimpleDateFormat dateFormat;
    private ZupClient client;
    private static Zup instance;
    //private Storage storage;
    private IStorage storage;
    private ArrayList<BitmapResource> bitmaps;
    private ZupNotificationCenter notificationCenter;
    private int lastJobId;

    private String sessionToken;
    private int sessionUserId;

    //private ArrayList<SyncAction> syncActions;
    private Hashtable<Integer, Integer> categoriesPinResources;

    private InventoryItemPublishedListener inventoryItemPublishedListener;
    private ResourceLoadedListener resourceLoadedListener;

    private Zup()
    {
        this.client = new ZupClient();
        //this.storage = new Storage();
        this.bitmaps = new ArrayList<BitmapResource>();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        this.notificationCenter = new ZupNotificationCenter();
        //this.syncActions = new ArrayList<SyncAction>();
        this.categoriesPinResources = new Hashtable<Integer, Integer>();
        this.lastJobId = 0;
    }

    public void clearStorage()
    {
        if(this.storage == null)
            return;

        this.storage.clear();
        this.sessionToken = null;
        this.sessionUserId = 0;
        //this.syncActions.clear();
    }

    public void initStorage(Context context)
    {
        if(this.storage != null)
            return;

        this.storage = new SQLiteStorage(context);
        this.storage.resetSyncActions();

        sessionToken = storage.getSessionToken();
        sessionUserId = storage.getSessionUserId();
        client.setSessionToken(sessionToken);
    }

    public void addFlowStep(Flow.Step step)
    {
        storage.addFlowStep(step);
    }

    public boolean hasSessionToken()
    {
        return this.sessionToken != null;
    }

    public void setInventoryItemPublishedListener(InventoryItemPublishedListener listener)
    {
        this.inventoryItemPublishedListener = listener;
    }

    public void setResourceLoadedListener(ResourceLoadedListener listener)
    {
        this.resourceLoadedListener = listener;
    }

    public int getSyncActionCount()
    {
        return this.storage.getSyncActionCount();
    }

    public Iterator<SyncAction> getSyncActions()
    {
        return this.storage.getSyncActionIterator();
    }

    public void removeSyncAction(int id) { this.storage.removeSyncAction(id); }

    public void addSyncAction(SyncAction action)
    {
        this.storage.addSyncAction(action);
    }

    public ZupNotificationCenter getNotificationCenter()
    {
        return notificationCenter;
    }

    public SimpleDateFormat getDateFormat()
    {
        return dateFormat;
    }

    public String formatIsoDate(String isoDate)
    {
        if(isoDate == null)
            return "";

        try {
            ISO8601DateFormat fmt = new ISO8601DateFormat();
            Date date = fmt.parse(isoDate);

            return getDateFormat().format(date);
        }
        catch (ParseException ex)
        {
            return isoDate;
        }
    }

    public Date getIsoDate(String isoDate)
    {
        try {
            ISO8601DateFormat fmt = new ISO8601DateFormat();
            Date date = fmt.parse(isoDate);

            return date;
        }
        catch (ParseException ex)
        {
            return null;
        }
    }

    public static String getIsoDate(Date date) {
        ISO8601DateFormat fmt = new ISO8601DateFormat();
        return fmt.format(date);
    }

    public static Zup getInstance()
    {
        if(instance == null)
        {
            instance = new Zup();
        }

        return instance;
    }

    public boolean hasSession()
    {
        return this.sessionToken != null;
    }

    public int getSessionUserId()
    {
        return sessionUserId;
    }

    public User getSessionUser()
    {
        if(storage == null)
            return null;

        return storage.getUser(sessionUserId);
    }

    public boolean hasCase(int id)
    {
        return storage.hasCase(id);
    }

    public Case getCase(int id)
    {
        return storage.getCase(id);
    }

    public void addCase(Case kase)
    {
        storage.addCase(kase);
    }

    public void updateCase(Case kase)
    {
        storage.updateCase(kase);
    }

    public void removeCase(int caseId) { storage.removeCase(caseId); }

    public Iterator<Case> getCasesIterator()
    {
        return storage.getCasesIterator();
    }

    public Iterator<Case> getCasesIterator(int flowId)
    {
        return storage.getCasesIterator(flowId);
    }

    private BitmapResource getResource(int id)
    {
        for(int i = 0; i < bitmaps.size(); i++)
        {
            if(bitmaps.get(i).id == id)
            {
                return bitmaps.get(i);
            }
        }

        return null;
    }

    private BitmapResource getResource(String url)
    {
        for(int i = 0; i < bitmaps.size(); i++)
        {
            if(bitmaps.get(i).url.equals(url))
            {
                return bitmaps.get(i);
            }
        }

        return null;
    }

    private int getFreeResourceId()
    {
        int index = 1;
        while(getResource(index) != null)
        {
            index++;
        }

        return index;
    }

    public boolean isResourceLoaded(int id)
    {
        BitmapResource resource = getResource(id);
        if(resource == null)
            return false;

        return resource.loaded;
    }

    public boolean isResourceLoaded(String url)
    {
        BitmapResource resource = getResource(url);
        if(resource == null)
            return false;

        return resource.loaded;
    }

    public void tryLogin(final String email, final String password, final LoginListener listener)
    {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                final ApiHttpResult<Session> result = client.authenticate(email, password);
                if(result.statusCode == 200 || result.statusCode == 201)
                {
                    sessionToken = result.result.token;
                    sessionUserId = result.result.user.id;
                    client.setSessionToken(sessionToken);

                    storage.addUser(result.result.user);
                    storage.setSession(sessionUserId, sessionToken);
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onLoginSuccess();
                        }
                    });
                }
                else
                {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if(result.result != null && result.result.error != null)
                                listener.onLoginError(result.statusCode, result.result.error);
                            else
                                listener.onLoginError(result.statusCode, "Não foi possível conectar ao servidor");
                        }
                    });
                }
            }
        });
        worker.start();
    }

    public void updateInventoryItemInfo(int id, InventoryItem copyFrom)
    {
        this.storage.updateInventoryItemInfo(id, copyFrom);
    }

    public static void runOnMainThread(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public InventoryItemImage getInventoryItemFirstImage(InventoryItem item)
    {
        //InventoryItem item = getInventoryItem(itemId);
        //if(item == null)
        //    return null;

        InventoryCategory category = getInventoryCategory(item.inventory_category_id);
        if(category == null)
            return null;

        int index = 1;
        ArrayList images = null;
        while(images == null)
        {
            InventoryCategory.Section.Field imagesField = category.getNthFieldOfKind("images", index);
            // Are there any more "images" field?
            if(imagesField == null)
                break;

            images = (ArrayList)item.getFieldValue(imagesField.id);
            index++;

            if(images == null || images.size() < 1)
                images = null;
        }

        if(images != null && images.size() > 0)
        {
            InventoryItemImage image;
            if(images.get(0) instanceof InventoryItemImage)
                image = (InventoryItemImage)images.get(0);
            else {
                LinkedHashMap map = (LinkedHashMap) images.get(0);
                ObjectMapper mapper = new ObjectMapper();
                image = mapper.convertValue(map, InventoryItemImage.class);
            }
            return image;
        }
        else
        {
            return null;
        }
    }

    public Bitmap getInventoryCategoryPinBitmap(int categoryId)
    {
        int resourceId = getInventoryCategoryPinResourceId(categoryId);
        if(resourceId == 0 || !Zup.getInstance().isResourceLoaded(resourceId))
            return null;

        Bitmap markerIcon = Zup.getInstance().getBitmap(resourceId);
        return markerIcon;
    }

    public int getInventoryCategoryPinResourceId(int categoryId)
    {
        if(!categoriesPinResources.containsKey(categoryId))
            requestInventoryCategoryPin(categoryId);

        return categoriesPinResources.get(categoryId);
    }

    public void requestInventoryCategoryPin(int categoryId)
    {
        final InventoryCategory category = getInventoryCategory(categoryId);
        if(category == null || categoriesPinResources.containsKey(categoryId) || category.pin == null)
            return;

        final InventoryCategory.Pins pin;
        if(category.plot_format != null && category.plot_format.equals("marker"))
            pin = category.marker;
        else
            pin = category.pin;


        final BitmapResource resource = new BitmapResource();
        resource.url = pin._default.mobile;
        resource.id = getFreeResourceId();
        bitmaps.add(resource);

        categoriesPinResources.put(categoryId, resource.id);
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                client.loadResource(pin._default.mobile, resource); // STAHP
                if(resource.loaded) {
                    Zup.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (resourceLoadedListener != null)
                                resourceLoadedListener.onResourceLoaded(category.pin._default.mobile, resource.id);
                        }
                    });
                }
            }
        });
        worker.start();
    }

    public int requestImage(final String imageUrl)
    {
        return requestImage(imageUrl, true);
    }

    public int requestImage(final String imageUrl, boolean async)
    {
        final BitmapResource resource = new BitmapResource();
        resource.id = getFreeResourceId();
        resource.url = imageUrl;
        bitmaps.add(resource);

        if(async) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    client.loadResource(imageUrl, resource); // STAHP
                }
            });
            worker.start();
        }
        else
        {
            client.loadResource(imageUrl, resource);
        }

        return resource.id;
    }

    public Bitmap getResourceBitmap(int resourceId)
    {
        if(this.getResource(resourceId) == null)
            return null;

        return this.getResource(resourceId).bitmap;
    }

    public int requestInventoryCategoryStatuses(final int categoryId)
    {
        final int jobId = this.generateJobId();

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
            ApiHttpResult<InventoryCategoryStatusCollection> result = client.retrieveInventoryCategoryStatuses(categoryId);
            if(result.statusCode == 200)
                inventoryCategoryStatusesReceived(result.result);
            }
        });
        worker.start();

        return jobId;
    }

    private void inventoryCategoryStatusesReceived(InventoryCategoryStatusCollection result)
    {
        for(int i = 0; i < result.statuses.length; i++)
        {
            InventoryCategoryStatus status = result.statuses[i];

            this.storage.removeInventoryCategoryStatus(status.id);
            this.storage.addInventoryCategoryStatus(status);
        }
    }

    public Bitmap getBitmap(int id)
    {
        BitmapResource resource = getResource(id);
        if(resource == null || !resource.loaded)
            return null;

        return resource.bitmap;
    }

    public Bitmap getBitmap(String url)
    {
        BitmapResource resource = getResource(url);
        if(resource == null || !resource.loaded)
            return null;

        return resource.bitmap;
    }

    public String getInventoryItemTitle(InventoryItem item)
    {
        String extra = "";
        if(hasSyncActionRelatedToInventoryItem(item.id))
            extra = "* ";

        if(item.title != null)
            return extra + item.title;

        InventoryCategory category = getInventoryCategory(item.inventory_category_id);
        if(!hasSyncActionRelatedToInventoryItem(item.id))
            return category.title + " " + item.id;
        else
            return category.title + " *";
    }

    public void refreshInventoryItemCategories()
    {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                InventoryCategoryCollection collection = client.retrieveInventoryCategories().result;
                inventoryItemCategoriesReceived(collection);
            }
        });
        worker.start();
    }

    public void refreshInventoryItemCategoryInfo(final int categoryId)
    {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                SingleInventoryCategoryCollection category = client.retrieveInventoryCategoryInfo(categoryId).result;
                inventoryItemCategoryInfoReceived(category.category);
            }
        });
        worker.start();
    }

    public void refreshInventoryItemCategoryForm(final int categoryId)
    {
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                ApiHttpResult<InventoryCategory> result = client.retrieveInventoryCategoryForm(categoryId);
                InventoryCategory category = result.result;
                storage.updateInventoryCategoryInfo(categoryId, category);
            }
        });
        worker.start();
    }

    private void inventoryItemCategoryInfoReceived(InventoryCategory category)
    {
        if(storage.hasInventoryCategory(category.id))
        {
            storage.updateInventoryCategoryInfo(category.id, category);
            requestInventoryCategoryStatuses(category.id);
        }
        else
        {
            storage.addInventoryCategory(category);

            // TODO Isso não deveria estar aqui
            requestInventoryCategoryPin(category.id);
            requestInventoryCategoryStatuses(category.id);
        }
    }

    private void inventoryItemCategoriesReceived(InventoryCategoryCollection collection)
    {
        if(collection == null)
        {
            return;
        }

        ArrayList<Integer> idsToRemove = new ArrayList<Integer>();
        Iterator<InventoryCategory> categories = this.storage.getInventoryCategoriesIterator();
        while(categories.hasNext())
        {
            InventoryCategory cat = categories.next();
            idsToRemove.add(cat.id);
        }

        for(int i = 0; i < collection.categories.length; i++)
        {
            InventoryCategory category = collection.categories[i];
            inventoryItemCategoryInfoReceived(category);

            idsToRemove.remove((Object)new Integer(category.id));

            // TODO isso também não deveria estar aqui
            //refreshInventoryItemCategoryInfo(category.id);
            //refreshInventoryItemCategoryForm(category.id);
        }

        for(Integer id : idsToRemove)
        {
            this.storage.removeInventoryCategory(id);
        }
    }

    public InventoryItem createInventoryItem()
    {
        InventoryItem item = new InventoryItem();
        item.isLocal = true;
        item.created_at = getIsoDate(Calendar.getInstance().getTime());
        item.id = (storage.getLocalInventoryItemCount() + 1) | InventoryItem.LOCAL_MASK;
        storage.addInventoryItem(item);

        return item;
    }

    public boolean hasSyncActionRelatedToInventoryItem(int id)
    {
        Iterator<SyncAction> actions = storage.getSyncActionIterator();
        while(actions.hasNext())
        {
            SyncAction action = actions.next();

            if(action instanceof PublishInventoryItemSyncAction && ((PublishInventoryItemSyncAction)action).item.id == id)
                return true;
            else if(action instanceof EditInventoryItemSyncAction && ((EditInventoryItemSyncAction)action).item.id == id)
                return true;
            else if(action instanceof DeleteInventoryItemSyncAction && ((DeleteInventoryItemSyncAction)action).itemId == id)
                return true;
        }

        return false;
    }

    public void performSyncAction(final SyncAction action)
    {
        if(this.isSyncing)
            return;

        broadcastAction(SyncAction.ACTION_SYNC_BEGIN);

        this.isSyncing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(action.perform()) {
                    storage.removeSyncAction(action.getId());

                    if(action instanceof PublishInventoryItemSyncAction && Zup.this.inventoryItemPublishedListener != null)
                    {
                        PublishInventoryItemSyncAction publishAction = (PublishInventoryItemSyncAction)action;
                        Zup.this.inventoryItemPublishedListener.onInventoryItemPublished(publishAction.item.id, publishAction.item);
                    }
                }

                isSyncing = false;
                broadcastAction(SyncAction.ACTION_SYNC_END);
            }
        }).start();
    }

    public void sync()
    {
        if(this.isSyncing)
            return;

        broadcastAction(SyncAction.ACTION_SYNC_BEGIN);

        this.isSyncing = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<SyncAction> actionsToRemove = new ArrayList<SyncAction>();

                Iterator<SyncAction> actions = storage.getSyncActionIterator();
                while(actions.hasNext())
                {
                    SyncAction action = actions.next();

                    if(action.perform()) {
                        actionsToRemove.add(action);
                        if(action instanceof PublishInventoryItemSyncAction && Zup.this.inventoryItemPublishedListener != null)
                        {
                            PublishInventoryItemSyncAction publishAction = (PublishInventoryItemSyncAction)action;
                            Zup.this.inventoryItemPublishedListener.onInventoryItemPublished(publishAction.item.id, publishAction.item);
                        }
                    }
                }

                for(SyncAction action : actionsToRemove)
                {
                    Zup.this.storage.removeSyncAction(action.id);
                }

                isSyncing = false;
                broadcastAction(SyncAction.ACTION_SYNC_END);
            }
        }).start();
    }

    public boolean isSyncing()
    {
        return isSyncing;
    }

    public void updateSyncAction(SyncAction action)
    {
        storage.updateSyncAction(action);
    }

    void broadcastAction(String action)
    {
        if(ZupApplication.getContext() == null)
            return;

        Intent intent = new Intent(action);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
        manager.sendBroadcast(intent);
    }

    public ApiHttpResult<PublishInventoryItemResponse> publishInventoryItem(final InventoryItem item)
    {
        //Thread worker = new Thread(new Runnable() {
            //@Override
            //public void run() {
                ApiHttpResult<PublishInventoryItemResponse> result = client.publishInventoryItem(item);
                item.syncError = !(result.statusCode == 200 || result.statusCode == 201);

                if(result.statusCode == 200 || result.statusCode == 201)
                {
                    int oldId = item.id;
                    item.updateInfo(result.result.item);
                    updateInventoryItemInfo(oldId, item);
                    item.isLocal = false;
                    //return result.result;
                }
                //else
                //    return null;
                return result;
            //}
        //});
        //worker.start();
    }

    public ApiHttpResult<EditInventoryItemResponse> editInventoryItem(final InventoryItem item)
    {
        /*Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {*/
                ApiHttpResult<EditInventoryItemResponse> result = client.editInventoryItem(item);
                item.syncError = !(result.statusCode == 200 || result.statusCode == 201);

                if(result.statusCode == 200 || result.statusCode == 201)
                {
                    //item.updateInfo(result.result.item);
                    item.isLocal = false;
                    //return true;
                }
                else
                {
                    //return false;
                }
                return result;
        /*    }
        });
        worker.start();*/
    }

    public int requestInventoryItemInfo(final int categoryId, final int itemId, final InventoryItemListener listener, final JobFailedListener failedListener)
    {
        final int job_id = generateJobId();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                final ApiHttpResult<SingleInventoryItemCollection> result = client.retrieveInventoryItemInfo(categoryId, itemId);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result.statusCode == 200)
                            listener.onInventoryItemReceived(result.result.item, categoryId, itemId, job_id);
                        else if(failedListener != null)
                            failedListener.onJobFailed(job_id);
                    }
                });
            }
        });
        worker.start();

        return job_id;
    }

    public ApiHttpResult<SingleInventoryItemCollection> retrieveInventoryItemInfo(int categoryId, int itemId)
    {
        ApiHttpResult<SingleInventoryItemCollection> result = client.retrieveInventoryItemInfo(categoryId, itemId);
        return result;
    }

    public InventoryItem retrieveSingleInventoryItemInfo(int categoryId, int itemId)
    {
        ApiHttpResult<SingleInventoryItemCollection> result = client.retrieveInventoryItemInfo(categoryId, itemId);
        if(result == null || result.result == null)
            return null;

        return result.result.item;
    }

    public int searchInventoryItems(final int page, final int per_page, final int[] inventory_category_ids, final Integer[] inventory_statuses_ids, final String query, final InventoryItemsListener listener, final JobFailedListener failedListener)
    {
        final int job_id = generateJobId();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                final ApiHttpResult<InventoryItemCollection> result = client.searchInventoryItems(page, per_page, inventory_category_ids, inventory_statuses_ids, query);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result.statusCode == 200)
                            listener.onInventoryItemsReceived(result.result.items, page, per_page, inventory_category_ids, null, query, null, null, null, null, null, null, job_id);
                        else if(failedListener != null)
                            failedListener.onJobFailed(job_id);
                    }
                });
            }
        });
        worker.start();

        return job_id;
    }

    public int searchInventoryItems(final int page, final int per_page, final int[] inventory_category_ids, final Integer[] inventory_statuses_ids, final String address, final String title, final Calendar creation_from, final Calendar creation_to, final Calendar modification_from, final Calendar modification_to, final Float latitude, final Float longitude, final InventoryItemsListener listener, final JobFailedListener failedListener)
    {
        final int job_id = generateJobId();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                final ApiHttpResult<InventoryItemCollection> result = client.searchInventoryItems(page, per_page, inventory_category_ids, inventory_statuses_ids, address, title, creation_from, creation_to, modification_from, modification_to, latitude, longitude);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result.statusCode == 200)
                            listener.onInventoryItemsReceived(result.result.items, page, per_page, inventory_category_ids, address, title, creation_from, creation_to, modification_from, modification_to, latitude, longitude, job_id);
                        else if(failedListener != null)
                            failedListener.onJobFailed(job_id);
                    }
                });
            }
        });
        worker.start();

        return job_id;
    }

    public int requestInventoryItems(final int categoryId, final int page, final String sort, final String order, final InventoryItemsListener listener, final JobFailedListener failedListener)
    {
        final int job_id = generateJobId();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                final ApiHttpResult<InventoryItemCollection> result = client.retrieveInventoryItems(categoryId, page, sort, order);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result.statusCode == 200)
                            listener.onInventoryItemsReceived(result.result.items, categoryId, page, job_id);
                        else if(failedListener != null)
                            failedListener.onJobFailed(job_id);
                    }
                });
            }
        });
        worker.start();

        return job_id;
    }

    public int requestInventoryItems(final int categoryId, final int page, final InventoryItemsListener listener, final JobFailedListener failedListener)
    {
        final int job_id = generateJobId();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                final ApiHttpResult<InventoryItemCollection> result = client.retrieveInventoryItems(categoryId, page);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result.statusCode == 200)
                            listener.onInventoryItemsReceived(result.result.items, categoryId, page, job_id);
                        else if(failedListener != null)
                            failedListener.onJobFailed(job_id);
                    }
                });
            }
        });
        worker.start();

        return job_id;
    }

    public int requestInventoryItems(final int categoryId, final double latitude, final double longitude, final double radius, final double zoom, final InventoryItemsListener listener, final JobFailedListener failedListener)
    {
        final int job_id = generateJobId();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                final ApiHttpResult<InventoryItemCollection> result = client.retrieveInventoryItems(categoryId, latitude, longitude, radius, zoom);
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result.statusCode == 200)
                            listener.onInventoryItemsReceived(result.result.items, latitude, longitude, radius, zoom, job_id);
                        else if (failedListener != null)
                            failedListener.onJobFailed(job_id);
                    }
                });
            }
        });
        worker.start();

        return job_id;
    }

    public FlowCollection retrieveFlows()
    {
        ApiHttpResult<FlowCollection> result = client.retrieveFlows();
        return result.result;
    }

    public Flow.StepCollection retrieveFlowSteps(int flowId)
    {
        ApiHttpResult<Flow.StepCollection> result = client.retrieveFlowSteps(flowId);
        return result.result;
    }

    public SingleFlowCollection retrieveFlowVersion(int flowId, int version)
    {
        ApiHttpResult<SingleFlowCollection> result = client.retrieveFlowVersion(flowId, version);
        return result.result;
    }

    public boolean hasFlow(int id, int version)
    {
        return storage.hasFlow(id, version);
    }

    public void updateFlow(int id, int version, Flow data)
    {
        storage.updateFlow(id, version, data);
    }

    public void addFlow(Flow flow)
    {
        storage.addFlow(flow);
    }

    public CaseCollection retrieveCases(int page)
    {
        ApiHttpResult<CaseCollection> result = client.retrieveCases(page);
        return result.result;
    }

    public CaseCollection retrieveCases(int initial, int page)
    {
        ApiHttpResult<CaseCollection> result = client.retrieveCases(initial, page);
        return result.result;
    }

    public Case retrieveCase(int id)
    {
        ApiHttpResult<SingleCaseCollection> result = client.retrieveCase(id);
        if(result == null || result.result == null)
            return null;

        return result.result._case;
    }

    public ApiHttpResult<DeleteInventoryItemResponse> deleteInventoryItem(int categoryId, int itemId)
    {
        ApiHttpResult<DeleteInventoryItemResponse> result = client.deleteInventoryItem(categoryId, itemId);
        /*if(result.statusCode == 200)
            return result.result;
        else
            return null;*/
        return result;
    }

    public boolean transferCaseStep(int caseId, int stepId, int responsible_user_id)
    {
        ApiHttpResult<TransferCaseStepResponse> result = client.transferCaseStep(caseId, stepId, responsible_user_id);
        return result.statusCode == 200;
    }

    public Case updateCaseStep(int caseId, int stepId, int stepVersion, Hashtable<Integer, Object> fields)
    {
        ApiHttpResult<UpdateCaseStepResponse> result = client.updateCaseStep(caseId, stepId, stepVersion, fields);
        if(result.statusCode == 200)
            return result.result._case;
        else
            return null;
    }

    public ApiHttpResult<UpdateCaseStepResponse> updateCaseStepFull(int caseId, int stepId, int stepVersion, Hashtable<Integer, Object> fields)
    {
        ApiHttpResult<UpdateCaseStepResponse> result = client.updateCaseStep(caseId, stepId, stepVersion, fields);
        return result;
    }

    public User retrieveUserInfo(int userId)
    {
        ApiHttpResult<SingleUserCollection> result = client.retrieveUser(userId);
        if(result.statusCode == 200)
            return result.result.user;
        else
            return null;
    }

    public void showUsernameInto(TextView textView, String prefix, int userid)
    {
        UserLoaderTask task = new UserLoaderTask(textView, prefix);
        task.execute(userid);
    }

    public void showInventoryItemTitleInto(TextView textView, String prefix, int categoryId, int id)
    {
        if(hasInventoryItem(id))
            textView.setText(prefix + getInventoryItem(id).title);
        else if(ZupCache.hasInventoryItem(id))
            textView.setText(prefix + ZupCache.getInventoryItem(id).title);
        else
        {
            InventoryItemLoaderTask task = new InventoryItemLoaderTask(textView, prefix);
            task.execute(categoryId, id);
        }
    }

    public int generateJobId()
    {
        int result = this.lastJobId++;

        return result;
    }

    public Iterator<InventoryItem> getInventoryItems()
    {
        return this.storage.getInventoryItemsIterator();
    }

    public Iterator<InventoryItem> getInventoryItemsIterator(Integer categoryId, Integer stateId, String searchQuery, int page)
    {
        return this.storage.getInventoryItemsIterator(categoryId, stateId, searchQuery, page);
    }

    public boolean hasInventoryItem(int id)
    {
        return this.storage.hasInventoryItem(id);
    }

    public void addInventoryItem(InventoryItem item)
    {
        storage.addInventoryItem(item);
    }

    public void removeInventoryItem(int itemId)
    {
        storage.removeInventoryItem(itemId);
    }

    public InventoryItem getInventoryItem(int id)
    {
        return this.storage.getInventoryItem(id);
    }

    public InventoryCategory getInventoryCategory(int id)
    {
        return this.storage.getInventoryCategory(id);
    }

    public InventoryCategoryStatus getInventoryCategoryStatus(int categoryId, int statusId)
    {
        return this.storage.getInventoryCategoryStatus(statusId);
    }

    public Iterator<InventoryCategoryStatus> getInventoryCategoryStatusIterator(int categoryId)
    {
        return this.storage.getInventoryCategoryStatusIterator(categoryId);
    }

    public Iterator<InventoryItem> getInventoryItemsByCategory(int category)
    {
        return this.storage.getInventoryItemsIteratorByCategory(category);
    }

    public Iterator<InventoryCategory> getInventoryCategories()
    {
        return this.storage.getInventoryCategoriesIterator();
    }

    public Iterator<Flow> getFlows()
    {
        return storage.getFlowIterator();
    }

    public Flow getFlow(int id, int version)
    {
        return storage.getFlow(id, version);
    }

    /*public Flow getFlowLastKnownVersion(int id)
    {
        return storage.getFlowLastKnownVersion(id);
    }*/

    public Iterator<Document> getDocuments(Document.State state)
    {
        return new ArrayList<Document>().iterator();
        //return this.storage.getDocumentsIterator(state);
    }

    public Iterator<Document> getDocuments()
    {
        return new ArrayList<Document>().iterator();
        //return this.storage.getDocumentsIterator();
    }

    public Document getDocument(int id)
    {
        return null;
        //return this.storage.getDocument(id);
    }

    public int getCaseStatusDrawable(String status)
    {
        if(status.equals("pending"))
            return R.drawable.documentos_lista_status_icon_pendente;
        else if(status.equals("active"))
            return R.drawable.documentos_lista_status_icon_andamento;
        else if(status.equals("finished"))
            return R.drawable.documentos_lista_status_icon_concluido;
        else
            return R.drawable.documentos_lista_status_icon_sync;
    }

    public int getCaseStatusBigDrawable(String status)
    {
        if(status.equals("pending"))
            return R.drawable.documento_detalhes_status_icon_pendente;
        else if(status.equals("active"))
            return R.drawable.documento_detalhes_status_icon_andamento;
        else if(status.equals("finished"))
            return R.drawable.documento_detalhes_status_icon_concluido;
        else
            return R.drawable.documento_detalhes_status_icon_sync;
    }

    public int getCaseStatusColor(String status)
    {
        if(status.equals("pending"))
            return 0xffff6049;
        else if(status.equals("active"))
            return 0xffffac2d;
        else if(status.equals("finished"))
            return 0xff78c953;
        else
            return 0xff999999;
    }

    public String getCaseStatusString(String status)
    {
        if(status.equals("pending"))
            return "Pendente";
        else if(status.equals("active"))
            return "Em andamento";
        else if(status.equals("finished"))
            return "Concluído";
        else
            return "Aguardando sincronização";
    }

    public int getDocumentStateDrawable(Document.State state)
    {
        switch (state)
        {
            case WaitingForSync:
                return R.drawable.documentos_lista_status_icon_sync;

            case Pending:
                return R.drawable.documentos_lista_status_icon_pendente;

            case Running:
                return R.drawable.documentos_lista_status_icon_andamento;

            case Finished:
                return R.drawable.documentos_lista_status_icon_concluido;

            default:
                return R.drawable.documentos_lista_status_icon_sync;
        }
    }

    public int getDocumentStateBigDrawable(Document.State state)
    {
        switch (state)
        {
            case WaitingForSync:
                return R.drawable.documento_detalhes_status_icon_sync;

            case Pending:
                return R.drawable.documento_detalhes_status_icon_pendente;

            case Running:
                return R.drawable.documento_detalhes_status_icon_andamento;

            case Finished:
                return R.drawable.documento_detalhes_status_icon_concluido;

            default:
                return R.drawable.documento_detalhes_status_icon_sync;
        }
    }

    public int getDocumentStateColor(Document.State state)
    {
        switch (state)
        {
            case WaitingForSync:
                return 0xff999999;

            case Pending:
                return 0xffff6049;

            case Running:
                return 0xffffac2d;

            case Finished:
                return 0xff78c953;

            default:
                return 0xffff0000;
        }
    }
}
