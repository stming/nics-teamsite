USE []
GO
/****** Object:  Table [dbo].[communityAssetOverrideCategories]    Script Date: 03/24/2010 12:24:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[communityAssetOverrideCategories](
	[vpath] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[category] [varchar](512) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF