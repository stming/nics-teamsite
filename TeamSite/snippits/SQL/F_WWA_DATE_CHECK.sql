----------------------------------------------------
-- NAME     : WWA_DATE_CHECK
-- PARAMS   : @PROD_DEV_CODES    - Varchar comma delimited list of prod_dev_codes
--          : @DEPLOYMENT_LOCALE - language_country code of deployment e.g. en_GB, en_EU etc.
-- RETURN   : DATETIME
-- FUNCTION : Takes a comma seperated list of prod_dev_codes and a language_country code
--            e.g. 'Q320--,Q440,Q5555','en_GB' and returns the highest WWA_DATE for that 
--            list of products. To be used in deployment workflow and TAG UI
----------------------------------------------------

USE []
GO
---------- DROP THE FUNCTION IF IT EXISTS ----------
IF  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[WWA_DATE_CHECK]'))
DROP FUNCTION [DBO].[WWA_DATE_CHECK]
----------------------------------------------------
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[WWA_DATE_CHECK](@PROD_DEV_CODES VARCHAR(2500),@DEPLOYMENT_LOCALE VARCHAR(5))
RETURNS DATETIME
AS
BEGIN
    BEGIN
        DECLARE @HIGHEST_DATE DATETIME;

        SET @HIGHEST_DATE = (SELECT MAX(PROD_WWA_DATE)
        FROM V_PRODUCT
        WHERE PROD_DEV_CODE IN (SELECT * FROM [dbo].STRING_TOKENIZER(@PROD_DEV_CODES,','))
        --If global product then from en_EU golden
        AND ((NIKON_LOCALE = 'en_EU') 
        OR 
        --Check for local golden versions based on deployment locale
       ((LOCAL_PROD = 1) 
        AND (LOCAL_PROD_LANG_COUNTRY = REVERSE(NIKON_LOCALE))
        --Below we need to be able to cator for same country code
        AND (SUBSTRING(NIKON_LOCALE,4,2) = SUBSTRING(@DEPLOYMENT_LOCALE,4,2)))
       ))
    END
    RETURN @HIGHEST_DATE
END