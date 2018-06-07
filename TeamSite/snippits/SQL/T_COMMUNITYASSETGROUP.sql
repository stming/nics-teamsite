USE []
GO
/****** Object:  Table [dbo].[communityAssetGroup]    Script Date: 03/30/2010 14:44:47 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[communityAssetGroup](
	[vpath] [varchar](255) NOT NULL,
	[id] [varchar](1024) NOT NULL,
	[replicantOrder] [int] NOT NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF