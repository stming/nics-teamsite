USE []
GO
/****** Object:  View [dbo].[V_IMAGE_LIBRARY]    Script Date: 06/09/2010 16:23:58 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[V_IMAGE_LIBRARY] WITH SCHEMABINDING AS
SELECT 
	MD.relates_id				AS ID,
	MD.nikon_locale				AS NIKON_LOCALE,
	MD.prod_wwa_date			AS WWA_DATE,
	MD.path					AS PATH,
	MD.asset_category			AS CATEGORY,
	MD.nav_cat_1				AS NAV_CAT_1,
	MD.nav_cat_2				AS NAV_CAT_2,
	MD.nav_cat_3				AS NAV_CAT_3,
	MD.nav_name				AS NAV_NAME,
	ISNULL(MD.local_asset,0)		AS LOCAL_ASSET,
	MD.local_asset_lang_country		AS LOCAL_ASSET_LANG_COUNTRY,
	ISNULL(MD.local_asset_opt_out,0)	AS LOCALE_OPT_OUT,
	MD.local_asset_locale_opt_out		AS LOCALE_OPT_OUT_LANG_COUNTRY,
	MD.asset_type				AS ASST_TYPE,
	MD.relates_to_product			AS RELATED_PRODUCT,
	VP.LOCAL_SHORT_NAME			AS RELATED_PRODUCT_LOCALE_SHORT_NAME,
	VP.PROD_SHORT_NAME			AS RELATED_PRODUCT_SHORT_NAME
FROM    dbo.image_library MD INNER JOIN dbo.V_PRODUCT VP
	ON MD.relates_id = VP.ID
WHERE   (MD.asset_type = 'press_library')
AND relates_to_product IS NOT NULL;
--
