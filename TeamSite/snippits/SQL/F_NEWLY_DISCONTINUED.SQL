----------------------------------------------------
-- NAME     : NEWLY_DICONTINUED
-- PARAMS   : @DISCONTINUED     - Discontinued flag from DCR Extended Atts
--          : @PRODUCT_CATEGORY - Root category for Product/Accessory
-- RETURN   : VARCHAR           - '1' if newly Discontinued or '0' if not
-- FUNCTION : Takes the discontinued bit and ProductCategory and if not already in a root
--            category of discontinued then returns '1' for newly discontinued to allow the
--            V_PRODUCT view to set the categories properly
----------------------------------------------------

USE []
GO
---------- DROP THE FUNCTION IF IT EXISTS ----------
IF  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[NEWLY_DISCONTINUED]'))
DROP FUNCTION [DBO].[NEWLY_DISCONTINUED]
----------------------------------------------------
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[NEWLY_DISCONTINUED](@DISCONTINUED BIT,@PRODUCT_CATEGORY VARCHAR(2000))
RETURNS VARCHAR WITH SCHEMABINDING
AS
BEGIN
    BEGIN
        DECLARE @RET_VAL VARCHAR;

        IF ((CAST(@DISCONTINUED AS VARCHAR) = '1') AND  (@PRODUCT_CATEGORY = 'Discontinued'))
          SET @RET_VAL='0';
        ELSE IF ((CAST(@DISCONTINUED AS VARCHAR) = '1') AND  (@PRODUCT_CATEGORY != 'Discontinued'))
          SET @RET_VAL='1';
	    ELSE 
          SET @RET_VAL='0';
    END
    RETURN @RET_VAL
END