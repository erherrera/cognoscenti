package org.socialbiz.cog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.socialbiz.cog.spring.Constant;

public class RoleRequestRecord extends DOMFace
{

    public RoleRequestRecord(Document doc, Element upEle, DOMFace p)
    {
        super(doc,upEle, p);
    }

    public String getRequestId()
    {
        return getAttribute("id");
    }
    public void setRequestId(String id)
    {
        setAttribute("id",id);
    }

     public String getState()
     {
         return getAttribute("state");
     }
     public void setState(String state)
     {
         setAttribute("state",state);
     }

     public long getCreatedDate()
     {
         String timeAttrib = getAttribute("createdDate");
         return safeConvertLong(timeAttrib);
     }


     public String getRequestedBy()
     {
         return getScalar("requestedBy");
     }
     public void setRequestedBy(String requestedBy)
     {
         setScalar("requestedBy",requestedBy);
     }
     public String getRoleName()
     {
         return getScalar("roleName");
     }
     public void setRoleName(String roleName)
     {
         setScalar("roleName",roleName);
     }

     public long getModifiedDate()
     {
         String timeAttrib = getAttribute("modifiedDate");
         return safeConvertLong(timeAttrib);
     }
     public void setModifiedDate(String modifiedDate)
     {
         setAttribute("modifiedDate",modifiedDate);
     }

     public String getModifiedBy()
     {
         return  getAttribute("modifiedBy");
     }
     public void setModifiedBy(String modifiedBy)
     {
         setAttribute("modifiedBy",modifiedBy);
     }

     public boolean isCompleted()
     {
         return  Boolean.parseBoolean(getAttribute("isCompleted"));
     }
     public void setCompleted(boolean isCompleted) {
         setAttribute("isCompleted",String.valueOf(isCompleted));
    }

     public String getRequestDescription()
     {
         return getScalar("requestDescription");
     }
     public void setRequestDescription(String requestDescription)
     {
         setScalar("requestDescription",requestDescription);
     }

     public String getResponseDescription()
     {
         return getScalar("responseDescription");
     }
     public void setResponseDescription(String responseDescription)
     {
         setScalar("responseDescription",responseDescription);
     }

     public boolean showRecord() throws Exception{
         boolean showRecord = false;
         long max_days =Constant.HISTORY_MAX_DAYS;
         long days_diff = UtilityMethods.getDurationInDays(System.currentTimeMillis(),getModifiedDate());
         String max_days_interval = ConfigFile.getProperty(Constant.HISTORY_MAX_DAYS_PROPERTY) ;
         if(max_days_interval != null && !"".equals(max_days_interval.trim())){
                max_days = safeConvertLong(max_days_interval);
         }
         if(days_diff <= max_days){
             showRecord = true;
         }
         return showRecord;
     }
}