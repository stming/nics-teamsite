USE []
GO
/****** Object:  Table [dbo].[communityAssetSuppTable]    Script Date: 03/24/2010 12:26:55 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[communityAssetSuppTable](
	[vpath] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[supplementalKey] [varchar](512) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[supplementalValue] [varchar](2048) COLLATE SQL_Latin1_General_CP1_CI_AS NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF