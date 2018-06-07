/*********************************************************************************
 * 
 * PLEASE READ THIS NOTE BEFORE AMENDING THIS SQL SCRIPT
 * 
 * IF YOU MAKE A CHANGE PLEASE ENSURE YOU FORMAT THE STATEMENT AS BELOW
 * 
 * THIS MEANS EACH COLUMN OF A SEPERATE LINE WITH ITS ALIAS, i.e. ,X AS X
 * 
 * IF YOU TEST THINGS IN SQL SERVER MANAGEMENT STUDIO THEN PASTE BACK INTO HERE
 * 
 * THE FORMATTING IS RUINED. THIS IS BECAUSE THE ENGINEERS AT MICROSOFT ARE ABLE
 * 
 * TO READ MANGLED TEXT OR PUT WAY TO MUCH FAITH IN THEIR PRODUCTS
 * 
 * THE FORMATTING IS THERE TO MAKE THINGS EASIER TO READ FOR US MERE MORTALS
 * 
 * IT IS WAISTING TIME TO HAVE TO PUT THIS BACK IN. 
 *
 **********************************************************************************/

USE []
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
/****** Object:  View [dbo].[V_ACCESSORY]    Script Date: 01/29/2009 15:36:23 ******/
IF  EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[dbo].[V_PRICE]'))
DROP VIEW [dbo].[V_PRICE]
GO
CREATE VIEW [dbo].[V_PRICE] WITH SCHEMABINDING AS
SELECT LANGUAGE_CODE + '_' + COUNTRY_CODE + '_' + PROD_DEV_CODE AS ID
    ,PROD_DEV_CODE                                              AS PROD_DEV_CODE
	,LANGUAGE_CODE                                              AS LANGUAGE_CODE
	,COUNTRY_CODE                                               AS COUNTRY_CODE
	,LANGUAGE_CODE + '_' + COUNTRY_CODE                         AS NIKON_LOCALE
	,CURRENCY_CODE                                              AS CURRENCY_CODE
	,PRICE_INC_VAT                                              AS PRICE_INC_VAT
FROM  dbo.price
--
GO

BEGIN

/*	
-- Create NonClustered Indexes
CREATE UNIQUE CLUSTERED INDEX V_PRICE_PK ON
V_PRICE(ID)

CREATE INDEX V_PRICE_NIKON_LOCALE ON
V_PRICE(NIKON_LOCALE)

*/
EXEC SP_SPACEUSED 'V_PRICE'
END
