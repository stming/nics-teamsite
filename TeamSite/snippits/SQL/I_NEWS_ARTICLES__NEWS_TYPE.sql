USE []
GO
/****** Object:  Index [IDX__NEWS_ARTICLES__NEWS_TYPE]    Script Date: 06/09/2010 17:02:40 ******/
CREATE NONCLUSTERED INDEX [IDX__NEWS_ARTICLES__NEWS_TYPE] ON [dbo].[news_articles] 
(
	[news_type] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]