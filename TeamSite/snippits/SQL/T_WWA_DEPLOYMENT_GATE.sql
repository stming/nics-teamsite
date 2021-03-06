USE []
GO
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
IF  EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[DBO].[WWA_DEPLOYMENT_GATE]'))
DROP TABLE [DBO].[WWA_DEPLOYMENT_GATE]
GO
CREATE TABLE [DBO].[WWA_DEPLOYMENT_GATE](
    [DEPLOYMENT_DATE] [DATETIME],
    [PROD_DEV_CODE] [NVARCHAR](2000),
    [LOCAL_PRODUCT] [BIT],
    [MIGRATED] [BIT],
    [DEPLOY] [BIT],
    [STATUS] [NVARCHAR](2000),
    [SPENT] [BIT]
)
GO
SET ANSI_PADDING OFF