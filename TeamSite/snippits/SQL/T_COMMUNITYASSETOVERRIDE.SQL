USE []
GO
/****** Object:  Table [dbo].[communityAssetOverride]    Script Date: 03/24/2010 12:21:56 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[communityAssetOverride](
	[vpath] [varchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[id] [varchar](1024) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[title] [varchar](1024) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[modifyDate] [varchar](1024) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[takenDate] [varchar](1024) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[description] [varchar](2048) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[thumb] [varchar](1024) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[asset] [varchar](1024) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[hidden] [varchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
 CONSTRAINT [PK_communityAssetOverride] PRIMARY KEY CLUSTERED 
(
	[vpath] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF