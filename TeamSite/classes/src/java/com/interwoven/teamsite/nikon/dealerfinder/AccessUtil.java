package com.interwoven.teamsite.nikon.dealerfinder;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.access.CSGroup;
import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.common.CSRemoteException;

/**
 * 
 * @author Michael Stewart, Realise
 *
 */
public class AccessUtil
{

    static final Logger oLogger = LoggerFactory.getLogger(AccessUtil.class);
    
    private static final String GROUP_NAME_PREFIX = "dealers_";
    
    public static List<String> getCurrentUserGroups(CSClient client)
    {
        List<String> groups = new ArrayList<String>();
        CSUser currentUser = client.getCurrentUser();
        String currentUserName = currentUser.getName();
        
        CSIterator allTSGroupsIterator;
        
        try
        {
            allTSGroupsIterator = client.getPrincipalManager().getAllTSGroups();
            while (allTSGroupsIterator.hasNext())
            {
                /* first check whether user is a member of the current group */
                CSGroup group = (CSGroup) allTSGroupsIterator.next();
                if (! group.getName().startsWith(GROUP_NAME_PREFIX)) {
                    
					oLogger.info("TS Group: " + group.getName());
					
					continue;
                
				} else {
				
					oLogger.info("Checking whether user is a member of the group " + group.getName());
					
					if (isMemberOfGroup(group, currentUserName))
					{
						oLogger.info("Matched Group: " + group.getName());
					
						groups.add(group.getName());
					}
				}                
            }
        }
        catch (CSException e)
        {
            oLogger.error("CSException occurred when attempting to retrieve all TeamSite groups");
        }
        
        return groups;
    }
    
    public static boolean isMemberOfGroup(CSGroup group, String userName)
    {

        CSIterator groupUsers;
        String groupName = group.getName();
        try
        {
            groupUsers = group.getUsers(true);
			
			while (groupUsers.hasNext())
            {
                CSUser groupUser = (CSUser) groupUsers.next();
                
				if (groupUser.getName().equalsIgnoreCase(userName))
                {
                    return true;
                }
            }
        }
        catch (CSAuthorizationException e)
        {
            oLogger.error(String.format("CSAuthorizationException occurred when attempting to check whether the current user is a member of group '%s'", groupName));
        }
        catch (CSRemoteException e)
        {
            oLogger.error(String.format("CSRemoteException occurred when attempting to check whether the current user is a member of group '%s'", groupName));
        }
        catch (CSExpiredSessionException e)
        {
            oLogger.error(String.format("CSExpiredSessionException occurred when attempting to check whether the current user is a member of group '%s'", groupName));
        }
        catch (CSException e)
        {
            oLogger.error(String.format("CSException occurred when attempting to check whether the current user is a member of group '%s'", groupName));
        }

        return false;
    }
    
}

