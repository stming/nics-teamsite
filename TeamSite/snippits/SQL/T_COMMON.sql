USE []
GO
/****** Object:  Table [dbo].[common]    Script Date: 06/09/2010 16:19:29 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[common](
	[id] [nvarchar](300) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[path] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[nikon_locale] [nvarchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[state] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[keywords] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[prod_wwa_date] [datetime] NULL,
	[relates_to_product] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[sort_order] [nvarchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[migrated] [bit] NULL,
 CONSTRAINT [PK_common] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
