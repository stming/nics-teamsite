USE []
GO
/****** Object:  Table [dbo].[image_library]    Script Date: 06/09/2010 16:20:27 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[image_library](
	[id] [nvarchar](300) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[relates_id] [nvarchar](300) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[path] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[nikon_locale] [nvarchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[nav_name] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[nav_cat_1] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[nav_cat_2] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[nav_cat_3] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[state] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[keywords] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[prod_wwa_date] [datetime] NULL,
	[asset_category] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[asset_type] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[relates_to_product] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[sort_order] [nvarchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[local_asset] [bit] NULL,
	[local_asset_lang_country] [nvarchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[local_asset_opt_out] [bit] NULL,
	[local_asset_locale_opt_out] [bit] NULL,
 CONSTRAINT [PK_image_library] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
