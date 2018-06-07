package com.interwoven.teamsite.nikon.dealerfinder.search;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mike
 */
public abstract class AbstractSearch
{

    private List<String> olGroup = new ArrayList<String>();

    /**
     * @return the olGroup
     */
    public List<String> getGroups()
    {
        return olGroup;
    }

    /**
     * @param olGroup the olGroup to set
     */
    public void setGroups(List<String> olGroup)
    {
        this.olGroup = olGroup;
    }

    public void addGroup(String sGroup)
    {
        olGroup.add(sGroup);
    }

    public String generateGroupsClause()
    {
        StringBuilder oGroupsClauseBuf = new StringBuilder();

        if (olGroup != null && olGroup.size() > 0)
        {
            for (String sGroup : olGroup)
            {
                if (oGroupsClauseBuf.toString().length() > 0)
                {
                    oGroupsClauseBuf.append(" OR ")
                            .append(GROUP_COL)
                            .append(" = ")
                            .append("\'")
                            .append(sGroup)
                            .append("\'");
                }
                else
                {
                    oGroupsClauseBuf.append(GROUP_COL)
                            .append(" = ")
                            .append("\'")
                            .append(sGroup)
                            .append("\'");
                }
            }

            oGroupsClauseBuf.insert(0, "(").append(")");
        }

        return oGroupsClauseBuf.toString();
    }
   
    public final String GROUP_COL = "user_group";
}

