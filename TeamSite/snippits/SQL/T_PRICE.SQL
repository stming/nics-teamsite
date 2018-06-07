 USE []
GO
/****** Object:  Table [dbo].[price]    Script Date: 02/01/2009 18:22:35 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
IF  EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[dbo].[price]'))
DROP TABLE [dbo].[price]
GO
CREATE TABLE [dbo].[price](
	[PROD_DEV_CODE] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[UPC] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PROD_MATERIAL_CODE] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PROD_SHORT_NAME] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[COUNTRY_CODE] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[LANGUAGE_CODE] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[PRICE_INC_VAT] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[CURRENCY_CODE] [nvarchar](100)    COLLATE SQL_Latin1_General_CP1_CI_AS NULL)
GO
SET ANSI_PADDING OFF