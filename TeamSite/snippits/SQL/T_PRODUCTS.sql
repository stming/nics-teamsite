USE []
GO
/****** Object:  Table [dbo].[products]    Script Date: 06/09/2010 16:21:44 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[products](
	[id] [nvarchar](300) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[path] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NOT NULL,
	[nikon_locale] [nvarchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[nav_cat_1] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[nav_cat_2] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[nav_cat_3] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[state] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[keywords] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_id] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_category] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[prod_wwa_date] [datetime] NULL,
	[product_formal_name] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_short_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_short_code] [nvarchar](50) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_dev_code] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_type] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_description] [nvarchar](2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_marketing_related] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_accessory_of] [nvarchar](2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_included_in_bom] [nvarchar](2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_related] [nvarchar](2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_active] [bit] NULL,
	[product_material_code] [nvarchar](2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[product_locale_opt_out] [nvarchar](5) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[local_product] [bit] NULL,
	[local_product_lang_country] [nvarchar](20) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[local_short_name] [nvarchar](100) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[iskit] [bit] NULL,
	[upc] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[comment] [nvarchar](2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[specs] [nvarchar](2000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[relates_to_product] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[available_date] [datetime] NULL,
	[billboard] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[sort_order] [nvarchar](10) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[release_season] [nvarchar](500) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[compatible_products] [nvarchar](1000) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[uid] [nvarchar](255) COLLATE SQL_Latin1_General_CP1_CI_AS NULL,
	[end_of_new] [datetime] NULL,
	[discontinued] [bit] NULL,
	[migrated] [bit] NULL,
 CONSTRAINT [PK_products] PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]
