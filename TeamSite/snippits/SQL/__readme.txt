This file describes the views which sit on top of the metadata table

/Teamsite-Nikon-Customizations/snippits/SQL/V_PRODUCT.sql

This is the main view and should be applied first. It contains information about all of 
the Products and Accessories.

/Teamsite-Nikon-Customizations/snippits/SQL/V_ACC_OF_GOLDEN.sql

This view gives the realationship between Accessories and Products denoted by the
coma sepearted values field prod_accessory_of field from the meta data. Only the
en_EU data is presented as this is seen as the Golden Set of relationship rules.

/Teamsite-Nikon-Customizations/snippits/SQL/V_PROD_INCLUDED_IN_BOM_GOLDEN.sql

This view gives all of the included in the box or bill of materials for a Product/Accessory
again the en_EU set is seen as the Golden Set.

/Teamsite-Nikon-Customizations/snippits/SQL/V_PROD_RELATED_GOLDEN.sql

This view gives all of the prod_related information, say where a camera body has several
kits.

/Teamsite-Nikon-Customizations/snippits/SQL/V_PROD_LOCALE_OPT_OUT.sql

This is still design in progress and is so that if a product or accessory is not available in 
a country it is excluded from resultsets when searching for products and how they are related to
other products and accessories.

/Teamsite-Nikon-Customizations/snippits/SQL/V_NEWS_ARTICLE.sql

This is a view of news articles on top of the metadata table. It needs to come from the migrated data
and also the dcrs (going forwards).