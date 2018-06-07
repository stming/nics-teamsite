USE []
GO
/****** Object:  View [dbo].[V_PRESS_LIBRARY]    Script Date: 06/09/2010 16:25:04 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[V_PRESS_LIBRARY] WITH SCHEMABINDING AS
SELECT id								AS ID
      ,nikon_locale						AS NIKON_LOCALE
      ,prod_wwa_date					AS WWA_DATE
      ,path								AS PATH
      ,ISNULL(local_asset,0)			AS LOCAL_ASSET
      ,local_asset_lang_country			AS LOCAL_ASSET_LANG_COUNTRY
      ,ISNULL(local_asset_opt_out,0)	AS LOCALE_OPT_OUT
      ,local_asset_locale_opt_out		AS LOCALE_OPT_OUT_LANG_COUNTRY
      ,asset_category					AS CATEGORY
      ,nav_cat_1						AS NAV_CAT_1
      ,nav_cat_2						AS NAV_CAT_2
      ,nav_cat_3						AS NAV_CAT_3
      ,nav_name							AS NAV_NAME
FROM  dbo.image_library
WHERE (asset_type = 'press_library');
--
