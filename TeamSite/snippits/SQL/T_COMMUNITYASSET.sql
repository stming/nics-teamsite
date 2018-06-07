USE []
GO
/****** Object:  Table [dbo].[communityAsset]    Script Date: 03/31/2010 18:03:46 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[communityAsset](
	[vpath] [varchar](255) NOT NULL,
	[id] [varchar](1024) NOT NULL,
	[title] [varchar](1024) NOT NULL,
	[modifyDate] [varchar](1024) NOT NULL,
	[takenDate] [varchar](1024) NOT NULL,
	[description] [varchar](4096) NOT NULL,
	[thumb] [varchar](1024) NOT NULL,
	[asset] [varchar](1024) NOT NULL,
	[hidden] [varchar](10) NULL,
 CONSTRAINT [PK_communityAssetTable] PRIMARY KEY CLUSTERED 
(
	[vpath] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF