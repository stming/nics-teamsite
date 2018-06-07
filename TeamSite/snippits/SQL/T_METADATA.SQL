USE []
GO
/****** Object:  Table [dbo].[metadata]    Script Date: 04/24/2009 09:13:46 ******/
/* $Rev */
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
IF  EXISTS (SELECT * FROM sys.tables WHERE object_id = OBJECT_ID(N'[dbo].[metadata]'))
DROP TABLE [dbo].[metadata]
GO
CREATE TABLE [dbo].[metadata](
    [DateMade] [datetime] NULL,
    [Keywords] [nvarchar](2000) NULL,
    [NavCat1] [nvarchar](2000) NULL,
    [NavCat2] [nvarchar](2000) NULL,
    [NavCat3] [nvarchar](2000) NULL,
    [NavName] [nvarchar](2000) NULL,
    [NikonLocale] [nvarchar](2000) NULL,
    [ProductCategory] [nvarchar](2000) NULL,
    [UID] [nvarchar](2000) NULL,
    [available_date] [datetime] NULL,
    [award_description_list] [nvarchar](2000) NULL,
    [award_id] [nvarchar](2000) NULL,
    [award_prod_related] [nvarchar](2000) NULL,
    [award_testimonial_date] [datetime] NULL,
    [award_title] [nvarchar](2000) NULL,
    [award_type] [nvarchar](2000) NULL,
    [billboard] [nvarchar](2000) NULL,
    [comment] [nvarchar](2000) NULL,
    [compatible_products] [nvarchar](2000) NULL,
    [description] [nvarchar](2000) NULL,
    [discontinued] [bit] NULL,
    [end_of_new] [datetime] NULL,
    [iskit] [bit] NULL,
    [isnew] [bit] NULL,
    [isrelated] [bit] NULL,
    [keep_on_page] [bit] NULL,
    [local_prod_lang_country] [nvarchar](2000) NULL,
    [local_product] [bit] NULL,
    [local_short_name] [nvarchar](2000) NULL,
    [news_type] [nvarchar](2000) NULL,
    [path] [nvarchar](255) NOT NULL,
    [prod_description] [nvarchar](2000) NULL,
    [prod_dev_code] [nvarchar](2000) NULL,
    [prod_local_opt_out] [bit] NULL,
    [prod_locale_opt_out] [nvarchar](2000) NULL,
    [prod_related] [nvarchar](2000) NULL,
    [prod_short_code] [nvarchar](2000) NULL,
    [prod_wwa_date] [datetime] NULL,
    [product_accessory_of] [nvarchar](2000) NULL,
    [product_active] [bit] NULL,
    [product_formal_name] [nvarchar](2000) NULL,
    [product_id] [nvarchar](2000) NULL,
    [product_included_in_bom] [nvarchar](2000) NULL,
    [product_material_code] [nvarchar](2000) NULL,
    [product_short_name] [nvarchar](2000) NULL,
    [product_status] [varchar](2000) NULL,
    [product_type] [nvarchar](2000) NULL,
    [relates_to_product] [nvarchar](2000) NULL,
    [product_marketing_related] [nvarchar](2000) NULL,
    [release_season] [nvarchar](2000) NULL,
    [show_on_page] [bit] NULL,
    [specs] [nvarchar](2000) NULL,
    [state] [nvarchar](2000) NULL,
    [title] [nvarchar](2000) NULL,
    [upc] [nvarchar](2000) NULL,
    [AssetCategory] [nvarchar](2000) NULL,
    [local_asst_lang_country] [nvarchar](2000) NULL,
    [local_asst] [bit] NULL,
    [asst_local_opt_out] [bit] NULL,
    [asst_locale_opt_out] [nvarchar](2000) NULL,
    [asst_type] [nvarchar](2000) NULL,
    [sort_order] [nvarchar](2000) NULL,
	[Migrated] [bit] NULL,
    
    
 CONSTRAINT [metadata_key] PRIMARY KEY CLUSTERED 
(
    [path] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF