USE []
GO
/****** Object:  View [dbo].[V_PROD_LOCALE_OPT_OUT]    Script Date: 06/09/2010 16:26:39 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
/****** Object:  View [dbo].[V_PROD_LOCALE_OPT_OUT]    Script Date: 02/01/2009 20:01:42 ******/
CREATE VIEW [dbo].[V_PROD_LOCALE_OPT_OUT]
AS
SELECT	 VP1.id				AS ID
	,VP1.prod_id			AS PROD_ID
	,VP1.prod_dev_code		AS PROD_DEV_CODE
	,SBQ1.prod_locale_opt_out  AS LOCALE_OPT_OUT 
FROM dbo.V_PRODUCT AS VP1 
INNER JOIN (SELECT VP.ID, VP.prod_id, VP.Nikon_Locale, VP.prod_dev_code, SUBSTRING(VP.prod_locale_opt_out, Numbers.n, CHARINDEX(',', 
                                                   VP.prod_locale_opt_out + ',', Numbers.n) - Numbers.n) AS prod_locale_opt_out, 
                                                   Numbers.n + 1 - LEN(REPLACE(LEFT(VP.prod_locale_opt_out, Numbers.n), ',', '') )   AS INDX
                            FROM          dbo.V_PRODUCT AS VP INNER JOIN
                                                       (SELECT     number
                                                         FROM          master.dbo.spt_values
                                                         WHERE      (type = 'P') AND (number BETWEEN 0 AND 1000000)) AS Numbers(n) ON LEN(VP.prod_locale_opt_out) + 1 > Numbers.n
                            WHERE      (SUBSTRING(',' + VP.prod_locale_opt_out, Numbers.n, 1) = ',')) AS SBQ1 ON VP1.ID = SBQ1.ID
