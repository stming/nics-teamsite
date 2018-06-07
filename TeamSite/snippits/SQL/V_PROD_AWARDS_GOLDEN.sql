USE []
GO
/****** Object:  View [dbo].[V_PROD_AWARDS_GOLDEN]    Script Date: 06/09/2010 16:25:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  View [dbo].[V_PROD_AWARDS_GOLDEN]    Script Date: 02/01/2009 20:01:42 ******/
CREATE VIEW [dbo].[V_PROD_AWARDS_GOLDEN]
AS
SELECT     VP1.ID                                                                             AS ID
          ,VP1.AWARD_TEST_ID                                                                  AS AWARD_TEST_ID
          ,VP1.NIKON_LOCALE                                                                   AS NIKON_LOCALE
          ,SBQ1.PROD_RELATED                                                                  AS PROD_PROD_ID
          ,(SELECT PROD_DEV_CODE
            FROM   dbo.V_PRODUCT AS VP2
	        WHERE  (PROD_ID = SBQ1.PROD_RELATED) AND (NIKON_LOCALE = VP1.NIKON_LOCALE)) AS PROD_PROD_DEV_CODE
FROM        dbo.V_AWARD_TESTIMONIAL AS VP1 INNER JOIN
            (SELECT VP.ID, SUBSTRING(VP.PROD_RELATED, Numbers.n, CHARINDEX(',', VP.PROD_RELATED + ',', Numbers.n) - Numbers.n) AS PROD_RELATED, Numbers.n + 1 - LEN(REPLACE(LEFT(VP.PROD_RELATED, Numbers.n), ',', '')) AS INDX
             FROM   dbo.V_AWARD_TESTIMONIAL AS VP INNER JOIN
            	(SELECT number
			 	 FROM master.dbo.spt_values
		     	 WHERE (type = 'P') AND (number BETWEEN 1 AND 1000000)) AS Numbers(n) ON LEN(VP.PROD_RELATED) + 1 > Numbers.n
             WHERE (SUBSTRING(',' + VP.PROD_RELATED, Numbers.n, 1) = ',')) AS SBQ1 ON VP1.ID = SBQ1.ID
WHERE     (VP1.NIKON_LOCALE = 'en_EU')
