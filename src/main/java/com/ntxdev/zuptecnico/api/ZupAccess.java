package com.ntxdev.zuptecnico.api;

import com.ntxdev.zuptecnico.entities.Group;
import com.ntxdev.zuptecnico.entities.User;
import com.ntxdev.zuptecnico.util.Utilities;

/**
 * Created by igorlira on 8/23/15.
 */
public class ZupAccess {
    private Group[] groups;

    public ZupAccess(User user) {
        if (user == null) {
            this.groups = new Group[0];
        }
        else {
            this.groups = user.groups;
        }
    }

    public boolean canCreateReportItem() {
        for(Group group : groups) {
            if(group.canCreateReportItem()) {
                return true;
            }
        }

        return false;
    }

    public boolean canCreateReportItem(int categoryId) {
        for(Group group : groups) {
            if(group.canCreateReportItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canEditReportItem(int categoryId) {
        for(Group group : groups) {
            if(group.canEditReportItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canDeleteReportItem(int categoryId) {
        for(Group group : groups) {
            if(group.canDeleteReportItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canCreateCommentOnReportItem(int categoryId) {
        for(Group group : groups) {
            if(group.canCreateCommentOnReportItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canCreateInternalCommentOnReportItem(int categoryId) {
        for(Group group : groups) {
            if(group.canCreateInternalCommentOnReportItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canEditInventoryCategory(int categoryId) {
        for(Group group : groups) {
            if(group.canEditInventoryCategory(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canViewInventoryCategory(int categoryId) {
        for(Group group : groups) {
            if(group.canViewInventoryCategory(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canViewInventoryItem(int categoryId) {
        for(Group group : groups) {
            if(group.canViewInventoryItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canCreateInventoryItem(int categoryId) {
        for(Group group : groups) {
            if(group.canCreateInventoryItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canCreateInventoryItem() {
        for(Group group : groups) {
            if(group.canCreateInventoryItem()) {
                return true;
            }
        }

        return false;
    }

    public boolean canEditInventoryItem(int categoryId) {
        for(Group group : groups) {
            if(group.canEditInventoryItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canDeleteInventoryItem(int categoryId) {
        for(Group group : groups) {
            if(group.canDeleteInventoryItem(categoryId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canViewInventoryField(int categoryId, int fieldId) {
        for(Group group : groups) {
            if(group.canViewInventoryField(categoryId, fieldId)) {
                return true;
            }
        }

        return false;
    }

    public boolean canViewInventorySection(int categoryId, int sectionId) {
        for(Group group : groups) {
            if(group.canViewInventorySection(categoryId, sectionId)) {
                return true;
            }
        }

        return false;
    }
}
