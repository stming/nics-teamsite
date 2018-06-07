USE []
GO
/****** Object:  View [dbo].[V_PRODUCT]    Script Date: 06/09/2010 16:27:34 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[V_PRODUCT] WITH SCHEMABINDING AS
SELECT id  		  					AS ID
      ,path                               			AS PATH
      ,nikon_locale                        			AS NIKON_LOCALE
      ,state                              			AS STATE
      ,keywords                           			AS KEYWORDS
      ,product_formal_name					AS TITLE
      ,relates_to_product                 			AS RELATED_TO_PRODUCT
      ,REPLACE(product_marketing_related,' ', '') 		AS PRODUCT_MARKETING_RELATED	
      ,release_season                     			AS RELEASE_SEASON
      ,compatible_products                			AS COMPATIBLE_PRODUCTS
      ,product_id                         			AS PROD_ID              
      ,product_material_code              			AS PRODUCT_MATERIAL_CODE
      ,product_short_code                    			AS PROD_SHORT_CODE
      ,product_short_name                 			AS PRODUCT_SHORT_CODE
      ,product_formal_name                			AS PRODUCT_FORMAL_NAME
      ,product_dev_code                      			AS PROD_DEV_CODE
      ,product_type                       			AS PRODUCT_TYPE
      ,product_description                   			AS PROD_DESCRIPTION
      ,UID                                			AS UID
      ,REPLACE(product_accessory_of,' ', '')			AS PROD_ACCESSORY_OF  
      ,REPLACE(product_included_in_bom,' ', '')			AS PROD_INCLUDED_IN_BOM
      ,REPLACE(product_related,' ', '')				AS PROD_RELATED
      ,product_locale_opt_out                			AS PROD_LOCALE_OPT_OUT
      ,upc                                			AS UPC
      ,end_of_new                         			AS END_OF_NEW
      ,ISNULL(discontinued, 0)            			AS DISCONTINUED
      ,(CASE
         WHEN (discontinued = 1 and product_category != 'Discontinued')
           THEN 'Discontinued' 
         ELSE product_category
         END
       )      AS PRODUCT_CATEGORY
      ,(CASE
         WHEN (discontinued = 1 and product_category != 'Discontinued') 
           THEN product_category 
         ELSE nav_cat_1
         END
       )      AS NAV_CAT_1
      ,(CASE
         WHEN (discontinued = 1 and product_category != 'Discontinued')
           THEN nav_cat_1 
         ELSE nav_cat_2
         END
       )	  AS NAV_CAT_2
      ,(CASE
         WHEN (discontinued = 1 and product_category != 'Discontinued')
           THEN nav_cat_2
         ELSE nav_cat_3
         END
       )	  AS NAV_CAT_3
      ,product_short_name                 			AS PROD_SHORT_NAME
      ,prod_wwa_date                      			AS PROD_WWA_DATE
      ,ISNULL(iskit, 0)                   			AS IS_KIT
      ,sort_order			 			AS SORT_ORDER
      ,ISNULL(local_product, 0)           			AS LOCAL_PROD
      ,local_short_name                   			AS LOCAL_SHORT_NAME   
      ,local_product_lang_country            			AS LOCAL_PROD_LANG_COUNTRY
      ,ISNULL(migrated, 0)					AS MIGRATED
FROM  dbo.products
WHERE (product_id IS NOT NULL)
--
