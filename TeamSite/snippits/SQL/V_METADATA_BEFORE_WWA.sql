USE []
GO
/****** Object:  View [dbo].[V_METADATA_BEFORE_WWA]    Script Date: 06/09/2010 16:24:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[V_METADATA_BEFORE_WWA]
AS
SELECT *
    FROM   dbo.metadata
WHERE  (prod_wwa_date <= GETDATE())
