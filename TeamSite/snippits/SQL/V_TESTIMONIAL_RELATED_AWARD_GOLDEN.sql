USE []
GO
/****** Object:  View [dbo].[V_TESTIMONIAL_RELATED_AWARD_GOLDEN]    Script Date: 06/09/2010 16:27:57 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[V_TESTIMONIAL_RELATED_AWARD_GOLDEN] WITH SCHEMABINDING AS
SELECT id														AS ID
      ,award_id                                                 AS AWARD_TEST_ID
      ,award_description_list                                   AS AWARD_AWARD_ID
      ,nikon_locale                                             AS NIKON_LOCALE
FROM  dbo.award_testimonials
WHERE award_type = 'award_testimonial'
AND   nikon_locale = 'en_EU'
AND   award_id IS NOT NULL
AND   award_description_list IS NOT NULL
--
