/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nhk.ls.runtime.common;

/**
 *
 * @author wxiaoxi
 */
public class StringTrimUtils {
    
     public static String retrieveSubstring(String s, int length, String elide) throws Exception
     {
         if (s == null || s.length()<=0) {
            return "";
         }
         
         byte[] bytes = s.getBytes("Unicode");
         int n = 0;
         int i = 2;
         for (; i < bytes.length && n < length; i++)
         {
           if (i % 2 == 1)
           {
              n++;
           }
           else
           {
                if (bytes[i] != 0)
                {
                   n++;
                }
           }
         }//end of for
         if (i % 2 == 1)
         {
             if (bytes[i - 1] != 0)  
             {
                 i = i - 1;
             }
             else {
                 i = i + 1;
             }
         }
         String returnString = new String(bytes, 0, i, "Unicode");
         
         if (!returnString.equals(s)) returnString = returnString + elide;
         return returnString;
     }
}


