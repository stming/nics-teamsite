----------------------------------------------------
-- NAME     : STRING_TOKENIZER
-- PARAMS   : @STRING    - String to tokenize
--          : @DELIMETER - Char to tokenize on e.g. ,
-- RETURN   : TEMPORARY TABLE OF VARCHAR
-- FUNCTION : Takes a Varchar of token @DELIMETER seperated values
--            and creates a temporary table of the values
----------------------------------------------------

USE []
GO
---------- DROP THE FUNCTION IF IT EXISTS ----------
IF  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[STRING_TOKENIZER]'))
DROP FUNCTION [DBO].[STRING_TOKENIZER]
----------------------------------------------------
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
   CREATE FUNCTION [dbo].[STRING_TOKENIZER](@STRING VARCHAR(8000), @DELIMETER CHAR(1))       
   RETURNS @TEMPTABLE TABLE (ITEMS VARCHAR(8000))       
   AS       
   BEGIN       
    DECLARE @IDX INT       
    DECLARE @SLICE VARCHAR(8000)       
          
        SELECT @IDX = 1       
            IF LEN(@STRING)<1 OR @STRING IS NULL  RETURN       
         
       WHILE @IDX!= 0       
       BEGIN       
           SET @IDX = CHARINDEX(@DELIMETER,@STRING)       
           IF @IDX!=0       
               SET @SLICE = LEFT(@STRING,@IDX - 1)       
           ELSE       
               SET @SLICE = @STRING       
             
           IF(LEN(@SLICE)>0)  
               INSERT INTO @TEMPTABLE(ITEMS) VALUES(@SLICE)       
     
           SET @STRING = RIGHT(@STRING,LEN(@STRING) - @IDX)       
           SET @STRING = LTRIM(@STRING)       
           SET @STRING = RTRIM(@STRING)       
           IF LEN(@STRING) = 0 BREAK       
       END   
   RETURN       
  END  