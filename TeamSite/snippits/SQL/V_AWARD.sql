USE []
GO
/****** Object:  View [dbo].[V_AWARD]    Script Date: 06/09/2010 16:23:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[V_AWARD] WITH SCHEMABINDING AS
SELECT   id				AS ID
	,award_id			AS AWARD_ID
	,award_type			AS AWARD_TYPE
	,nikon_locale			AS NIKON_LOCALE
	,path				AS PATH
FROM  dbo.awards
WHERE award_type = 'award_description'
AND award_id is not null
AND nikon_locale is not null
