USE []
GO
/****** Object:  View [dbo].[V_PROD_INCLUDED_IN_BOM_GOLDEN]    Script Date: 06/09/2010 16:26:05 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  View [dbo].[V_PROD_INCLUDED_BOM_GOLDEN]    Script Date: 02/01/2009 20:01:42 ******/
CREATE VIEW [dbo].[V_PROD_INCLUDED_IN_BOM_GOLDEN]
AS
SELECT     VP1.ID                      AS ID
          ,VP1.NIKON_LOCALE            AS NIKON_LOCALE
          ,VP1.PROD_ID                                                                               AS PROD_PROD_ID        -- Parent PROD_ID
          ,VP1.PROD_DEV_CODE                                                                         AS PROD_PROD_DEV_CODE  -- Parent PROD_DEV_CODE                                     
          ,VP1.PROD_WWA_DATE                                                                         AS PROD_PROD_WWA_DATE  -- Parent WWA_DATE
          ,VP1.LOCAL_PROD                                           AS LOCAL_PROD              -- If is a local product
          ,VP1.LOCAL_PROD_LANG_COUNTRY                              AS LOCAL_PROD_LANG_COUNTRY -- Lang country local productcreated in
          ,(SELECT PROD_ID
            FROM   dbo.V_PRODUCT AS VP2
            WHERE (PROD_DEV_CODE = SBQ1.PROD_INCLUDED_IN_BOM) AND ((NIKON_LOCALE = 'en_EU') OR ((VP2.LOCAL_PROD = 1) AND (REVERSE(VP2.LOCAL_PROD_LANG_COUNTRY) = VP2.NIKON_LOCALE)))) AS PROD_ID             -- Child  PROD_ID
          ,SBQ1.PROD_INCLUDED_IN_BOM                                                                 AS PROD_DEV_CODE       -- Child  PROD_DEV_CODE
          ,(SELECT PROD_WWA_DATE
            FROM dbo.V_PRODUCT AS VP3
            WHERE (PROD_DEV_CODE = SBQ1.PROD_INCLUDED_IN_BOM) AND ((NIKON_LOCALE = 'en_EU') OR ((VP3.LOCAL_PROD = 1) AND (REVERSE(VP3.LOCAL_PROD_LANG_COUNTRY) = VP3.NIKON_LOCALE)))) AS PROD_WWA_DATE       -- Child WWA_DATE
FROM  dbo.V_PRODUCT AS VP1 INNER JOIN
          (SELECT     VP.ID, VP.PROD_ID, VP.NIKON_LOCALE, VP.PROD_DEV_CODE, SUBSTRING(VP.PROD_INCLUDED_IN_BOM, Numbers.n, CHARINDEX(',', 
                                                   VP.PROD_INCLUDED_IN_BOM + ',', Numbers.n) - Numbers.n) AS PROD_INCLUDED_IN_BOM, 
                                                   Numbers.n + 1 - LEN(REPLACE(LEFT(VP.PROD_INCLUDED_IN_BOM, Numbers.n), ',', '')) AS INDX
                            FROM          dbo.V_PRODUCT AS VP INNER JOIN
                                                       (SELECT     number
                                                         FROM          master.dbo.spt_values
                                                         WHERE      (type = 'P') AND (number BETWEEN 1 AND 1000000)) AS Numbers(n) ON LEN(VP.PROD_INCLUDED_IN_BOM) 
                                                   + 1 > Numbers.n
                            WHERE      (SUBSTRING(',' + VP.PROD_INCLUDED_IN_BOM, Numbers.n, 1) = ',')) AS SBQ1 ON VP1.ID = SBQ1.ID
WHERE     ((VP1.NIKON_LOCALE = 'en_EU') OR ((VP1.LOCAL_PROD = 1) AND (REVERSE(VP1.LOCAL_PROD_LANG_COUNTRY) = VP1.NIKON_LOCALE)))
