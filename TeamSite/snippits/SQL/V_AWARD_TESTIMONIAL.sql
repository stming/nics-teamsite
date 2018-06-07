USE []
GO
/****** Object:  View [dbo].[V_AWARD_TESTIMONIAL]    Script Date: 06/09/2010 16:23:33 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[V_AWARD_TESTIMONIAL] WITH SCHEMABINDING AS
SELECT id				AS ID
       ,award_id                        AS AWARD_TEST_ID
       ,award_type          		AS AWARD_TYPE
       ,award_testimonial_date          AS TESTIMONIAL_DATE
       ,award_product_related   	AS PROD_RELATED
       ,award_description_list          AS AWARD_AWARD_ID
       ,nikon_locale			AS NIKON_LOCALE
       ,path                            AS PATH
FROM  dbo.award_testimonials
WHERE award_type = 'award_testimonial'
AND   award_id IS NOT NULL
AND   award_product_related IS NOT NULL
AND   award_description_list IS NOT NULL
AND   nikon_locale IS NOT NULL
